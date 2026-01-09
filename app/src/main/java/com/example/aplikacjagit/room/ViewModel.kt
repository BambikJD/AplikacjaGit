package com.example.aplikacjagit.room

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DaneViewModel(application: Application) : AndroidViewModel(application) {

    private val repozytorium: Repozytorium

    // --- Produkty i Lodówka ---
    val wszystkieProdukty: LiveData<MutableList<Produkt>>
    val nazwyProduktow: LiveData<MutableList<String>>
    val zczytajDodane: LiveData<MutableList<ProduktyDodaneWynik>>
    val wyswietlLodowka: LiveData<MutableList<Lodowka>>

    private val _szukajProduktyQuery = MutableLiveData<String>("")
    val szukajProdukty: LiveData<MutableList<Produkt>>

    // --- Przepisy (z wzbogacaniem nazw) ---
    private val _wyswietlPrzepisy = MediatorLiveData<MutableList<PrzepisWynik>>()
    val wyswietlPrzepisy: LiveData<MutableList<PrzepisWynik>> = _wyswietlPrzepisy

    private val _wyswietlPrzepisyZLodowki = MediatorLiveData<MutableList<PrzepisWynik>>()
    val wyswietlPrzepisyZLodowki: LiveData<MutableList<PrzepisWynik>> = _wyswietlPrzepisyZLodowki

    // --- Treningi i Ćwiczenia ---
    private val _szukajCwiczeniaQuery = MutableLiveData<String>("")
    val wyswietlCwiczenia: LiveData<MutableList<Cwiczenie>>
    val wyswietlWykonane: LiveData<MutableList<Wykonane>>

    // --- Plany (z wzbogacaniem nazw) ---
    private val _wyswietlPlany = MediatorLiveData<MutableList<PlanWynik>>()
    val wyswietlPlany: LiveData<MutableList<PlanWynik>> = _wyswietlPlany

    // --- Zapytania o ID i Datę ---
    val getOstatniPrzepisId: LiveData<Int>
    val getOstatniPlanId: LiveData<Int>

    private val _dataQuery = MutableLiveData<java.util.Date?>(null)
    val dataQuery: LiveData<java.util.Date?> get() = _dataQuery
    val wyswietlDodane: LiveData<MutableList<Dodane>>

    init {
        val dao = BazaDanych.getInstance(application).DAO()
        repozytorium = Repozytorium(dao)

        // Inicjalizacja podstawowych list
        wszystkieProdukty = repozytorium.wszystkieProdukty
        nazwyProduktow = repozytorium.nazwyProduktow
        zczytajDodane = repozytorium.zczytajDodane
        wyswietlLodowka = repozytorium.wyswietlLodowka
        getOstatniPrzepisId = repozytorium.getOstatniePrzepisID
        getOstatniPlanId = repozytorium.getOstatniePlanID

        // Mechanizm wyszukiwania produktów
        szukajProdukty = _szukajProduktyQuery.switchMap { q ->
            if (q.isNullOrBlank()) wszystkieProdukty
            else repozytorium.szukajProdukty("%$q%")
        }

        // Mechanizm wyszukiwania ćwiczeń
        wyswietlCwiczenia = _szukajCwiczeniaQuery.switchMap { q ->
            if (q.isNullOrBlank()) repozytorium.wszystkieCwiczenia
            else repozytorium.szukajCwiczenia("%$q%")
        }

        // Pobieranie wykonanych rzeczy na dany dzień
        wyswietlDodane = _dataQuery.switchMap { date ->
            if (date == null) MutableLiveData(mutableListOf())
            else repozytorium.wyswietlDodane(date)
        }

        wyswietlWykonane = _dataQuery.switchMap { date ->
            if (date == null) MutableLiveData(mutableListOf())
            else repozytorium.wyswietlWykonane(date)
        }

        // --- WZBOGACANIE PLANÓW (ID -> Nazwy) ---
        val rawPlany = repozytorium.getPlanyRaw()
        _wyswietlPlany.addSource(rawPlany) { rawList ->
            viewModelScope.launch {
                val enriched = mutableListOf<PlanWynik>()
                for (raw in rawList) {
                    val base = repozytorium.mapRawToPlanWynik(raw)
                    if (base.listaCwiczen.isEmpty()) {
                        enriched.add(base)
                        continue
                    }
                    val cwiczeniaZBase = withContext(Dispatchers.IO) {
                        repozytorium.fetchCwiczeniaByIds(base.listaCwiczen)
                    }
                    val nazwy = base.listaCwiczen.map { id ->
                        cwiczeniaZBase.find { it.id == id }?.nazwa ?: "Nieznane ćwiczenie"
                    }
                    enriched.add(base.copy(nazwyCwiczen = nazwy))
                }
                _wyswietlPlany.postValue(enriched)
            }
        }

        // --- WZBOGACANIE PRZEPISÓW (ID -> Nazwy) ---
        val rawPrzepisy = repozytorium.getPrzepisyWynikRaw()
        _wyswietlPrzepisy.addSource(rawPrzepisy) { rawList ->
            viewModelScope.launch {
                val enriched = wzbogacPrzepisy(rawList)
                _wyswietlPrzepisy.postValue(enriched)
            }
        }

        val rawPrzepisyLodowka = repozytorium.getPrzepisyZLodowkiRaw()
        _wyswietlPrzepisyZLodowki.addSource(rawPrzepisyLodowka) { rawList ->
            viewModelScope.launch {
                val enriched = wzbogacPrzepisy(rawList)
                _wyswietlPrzepisyZLodowki.postValue(enriched)
            }
        }
    }

    // Funkcja pomocnicza do wzbogacania przepisów (żeby nie powtarzać kodu)
    private suspend fun wzbogacPrzepisy(rawList: List<PrzepisWynikRaw>): MutableList<PrzepisWynik> {
        val enriched = mutableListOf<PrzepisWynik>()
        for (raw in rawList) {
            val base = repozytorium.mapRawToPrzepisWynik(raw)
            if (base.listaProduktow.isEmpty()) {
                enriched.add(base)
                continue
            }
            val produkty = withContext(Dispatchers.IO) {
                repozytorium.fetchProduktyByIds(base.listaProduktow)
            }
            val produktyPotrzebne = base.listaProduktow.mapIndexed { idx, pid ->
                val nazwa = produkty.firstOrNull { it.id == pid }?.nazwa ?: "Produkt#$pid"
                val ilosc = base.listaIlosci.getOrNull(idx) ?: 0
                ProduktPotrzebny(id = pid, nazwa = nazwa, ilosc = ilosc)
            }
            enriched.add(base.copy(produktyPotrzebne = produktyPotrzebne))
        }
        return enriched
    }

    // --- METODY OBSŁUGI ---
    fun setQuery(q: String) { _szukajProduktyQuery.value = q }
    fun setCwiczenieQuery(q: String) { _szukajCwiczeniaQuery.value = q }
    fun setDateQuery(date: java.util.Date?) { _dataQuery.value = date }

    // Insert / Delete / Update
    fun deleteProdukt(p: Produkt) = viewModelScope.launch { repozytorium.deleteProdukt(p) }
    fun insertProdukt(p: Produkt) = viewModelScope.launch { repozytorium.insertProdukt(p) }

    fun insertPrzepis(p: Przepis) = viewModelScope.launch { repozytorium.insertPrzepis(p) }
    fun insertPrzepisProdukt(pp: PrzepisProdukt) = viewModelScope.launch { repozytorium.insertPrzepisProdukt(pp) }

    fun insertDodane(d: Dodane) = viewModelScope.launch { repozytorium.insertDodane(d) }
    fun deleteDodane(d: Dodane) = viewModelScope.launch { repozytorium.deleteDodane(d) }

    fun insertLodowka(l: Lodowka) = viewModelScope.launch { repozytorium.insertLodowka(l) }
    fun deleteLodowka(l: Lodowka) = viewModelScope.launch { repozytorium.deleteLodowka(l) }

    fun insertCwiczenie(c: Cwiczenie) = viewModelScope.launch { repozytorium.insertCwiczenie(c) }
    fun insertWykonane(w: Wykonane) = viewModelScope.launch { repozytorium.insertWykonane(w) }
    fun deleteWykonane(w: Wykonane) = viewModelScope.launch { repozytorium.deleteWykonane(w) }

    fun insertPlan(p: Plan) = viewModelScope.launch { repozytorium.insertPlan(p) }
    fun insertPlanCwiczenie(pc: PlanCwiczenie) = viewModelScope.launch { repozytorium.insertPlanCwiczenie(pc) }
}