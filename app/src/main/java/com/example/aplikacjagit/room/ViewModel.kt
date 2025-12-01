package com.example.aplikacjagit.room

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DaneViewModel(application: Application) : AndroidViewModel(application) {

    val wszystkieProdukty: LiveData<MutableList<Produkt>>
    val nazwyProduktow: LiveData<MutableList<String>>
    val zczytajDodane: LiveData<MutableList<ProduktyDodaneWynik>>
    val wyswietlPrzepisy: LiveData<MutableList<PrzepisWynik>>  // finalne, uzupełnione o nazwy
    val getOstatniPrzepisId: LiveData<Int>

    private val repozytorium: Repozytorium

    private val _szukajProduktyQuery = MutableLiveData<String>("")
    val ProduktyQuery: LiveData<String> get() = _szukajProduktyQuery
    val szukajProdukty: LiveData<MutableList<Produkt>>

    private val _dataQuery = MutableLiveData<java.util.Date?>(null)
    val dataQuery: LiveData<java.util.Date?> get() = _dataQuery
    val wyswietlDodane: LiveData<MutableList<Dodane>>

    // we will expose final LiveData via MediatorLiveData
    private val _wyswietlPrzepisy = MediatorLiveData<MutableList<PrzepisWynik>>()
    override fun onCleared() {
        super.onCleared()
        // nothing extra to cleanup here
    }

    init {
        val dao = BazaDanych.getInstance(application).DAO()
        repozytorium = Repozytorium(dao)

        wszystkieProdukty = repozytorium.wszystkieProdukty
        nazwyProduktow = repozytorium.nazwyProduktow
        zczytajDodane = repozytorium.zczytajDodane
        getOstatniPrzepisId = repozytorium.getOstatniePrzepisID

        szukajProdukty = _szukajProduktyQuery.switchMap { q ->
            val text = q ?: ""
            if (text.isBlank()) {
                wszystkieProdukty
            } else {
                repozytorium.szukajProdukty("%$text%")
            }
        }

        wyswietlDodane = _dataQuery.switchMap { date ->
            if (date == null) {
                MutableLiveData(mutableListOf())
            } else {
                repozytorium.wyswietlDodane(date)
            }
        }

        // === OBSERWUJ surowe LiveData (z CSV) i wypełnij nazwy produktów asynchronicznie ===
        val rawLive = repozytorium.getPrzepisyWynikRaw()
        wyswietlPrzepisy = _wyswietlPrzepisy

        _wyswietlPrzepisy.value = mutableListOf() // initial

        // mediator obserwuje surowe LiveData
        _wyswietlPrzepisy.addSource(rawLive) { rawList ->
            // rawList: MutableList<PrzepisWynikRaw>
            viewModelScope.launch {
                // mapowanie i wzbogacanie nazw
                val enriched = mutableListOf<PrzepisWynik>()
                // przetwarzaj po kolei, ale pobieraj nazwy w jednym zapytaniu dla każdego przepisu
                for (raw in rawList) {
                    val base = repozytorium.mapRawToPrzepisWynik(raw)
                    if (base.listaProduktow.isEmpty()) {
                        enriched.add(base)
                        continue
                    }

                    // pobierz produkty odpowiadające id (suspend)
                    val produkty = withContext(Dispatchers.IO) {
                        repozytorium.fetchProduktyByIds(base.listaProduktow)
                    }

                    // zbuduj listę ProduktPotrzebny w tej samej kolejności co listaProduktow
                    val produktyPotrzebne = base.listaProduktow.mapIndexed { idx, pid ->
                        val nazwa = produkty.firstOrNull { it.id == pid }?.nazwa ?: "Produkt#$pid"
                        val ilosc = base.listaIlosci.getOrNull(idx) ?: 0
                        ProduktPotrzebny(id = pid, nazwa = nazwa ?: "Brak nazwy", ilosc = ilosc)
                    }

                    // dodaj kopię obiektu z wypełnionym polem produktyPotrzebne
                    val enrichedItem = base.copy(produktyPotrzebne = produktyPotrzebne)
                    enriched.add(enrichedItem)
                }

                _wyswietlPrzepisy.postValue(enriched)
            }
        }
    }

    // ... reszta metod (setQuery, insert/delete/update) - zostaw bez zmian ...
    fun setQuery(q: String) { _szukajProduktyQuery.value = q }
    fun setDateQuery(date: java.util.Date?) { _dataQuery.value = date }

    fun deleteProdukt(produkt: Produkt) = viewModelScope.launch { repozytorium.deleteProdukt(produkt) }
    fun updateProdukt(produkt: Produkt) = viewModelScope.launch { repozytorium.updateProdukt(produkt) }
    fun insertProdukt(produkt: Produkt) = viewModelScope.launch { repozytorium.insertProdukt(produkt) }

    fun insertPrzepis(przepis: Przepis) = viewModelScope.launch { repozytorium.insertPrzepis(przepis) }
    fun insertPrzepisProdukt(przepisProdukt: PrzepisProdukt) = viewModelScope.launch { repozytorium.insertPrzepisProdukt(przepisProdukt) }

    fun deleteDodane(dodane: Dodane) = viewModelScope.launch { repozytorium.deleteDodane(dodane) }
    fun updateDodane(dodane: Dodane) = viewModelScope.launch { repozytorium.updateDodane(dodane) }
    fun insertDodane(dodane: Dodane) = viewModelScope.launch { repozytorium.insertDodane(dodane) }
}
