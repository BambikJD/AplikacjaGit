package com.example.aplikacjagit.room

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.liveData

class DaneViewModel(application: Application) : AndroidViewModel(application) {

    val wszystkieProdukty: LiveData<MutableList<Produkt>>
    val nazwyProduktow: LiveData<MutableList<String>>
    val zczytajDodane: LiveData<MutableList<ProduktyDodaneWynik>>

    private val repozytorium: Repozytorium

    private val _query = MutableLiveData<String>("")
    val query: LiveData<String> get() = _query
    val szukajProdukty: LiveData<MutableList<Produkt>>

    private val _dateQuery = MutableLiveData<java.util.Date?>(null)
    val dateQuery: LiveData<java.util.Date?> get() = _dateQuery
    val wyswietlDodane: LiveData<MutableList<Dodane>>

    init {
        val dao = BazaDanych.getInstance(application).DAO()
        repozytorium = Repozytorium(dao)

        wszystkieProdukty = repozytorium.wszystkieProdukty
        nazwyProduktow = repozytorium.nazwyProduktow
        zczytajDodane = repozytorium.zczytajDodane

        szukajProdukty = _query.switchMap { q ->
            val text = q ?: ""
            if (text.isBlank()) {
                wszystkieProdukty
            } else {
                repozytorium.szukajProdukty("%$text%")
            }
        }

        // switchMap dla listy "Dodane" według daty
        wyswietlDodane = _dateQuery.switchMap { date ->
            if (date == null) {
                // domyślnie pusta lista (żeby nie obserwować niepotrzebnie DB)
                MutableLiveData(mutableListOf())
            } else {
                // oczekujemy, że repozytorium ma funkcję wyswietlDodane(Date)
                repozytorium.wyswietlDodane(date)
            }
        }
    }

    // ustaw query tekstowe
    fun setQuery(q: String) {
        _query.value = q
    }

    // ustaw query daty (przekaż startOfDay Date)
    fun setDateQuery(date: java.util.Date?) {
        _dateQuery.value = date
    }

    // operacje DB (przez repozytorium)
    fun deleteProdukt(produkt: Produkt) = viewModelScope.launch {
        repozytorium.deleteProdukt(produkt)
    }

    fun updateProdukt(produkt: Produkt) = viewModelScope.launch {
        repozytorium.updateProdukt(produkt)
    }

    fun insertProdukt(produkt: Produkt) = viewModelScope.launch {
        repozytorium.insertProdukt(produkt)
    }

    fun deleteDodane(dodane: Dodane) = viewModelScope.launch {
        repozytorium.deleteDodane(dodane)
    }

    fun updateDodane(dodane: Dodane) = viewModelScope.launch {
        repozytorium.updateDodane(dodane)
    }

    fun insertDodane(dodane: Dodane) = viewModelScope.launch {
        repozytorium.insertDodane(dodane)
    }
}
