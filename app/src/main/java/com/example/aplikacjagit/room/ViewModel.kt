package com.example.aplikacjagit.room

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class DaneViewModel(application: Application) : AndroidViewModel(application) {

    // expose'owane LiveData
    val wszystkieProdukty: LiveData<MutableList<Produkt>>
    val nazwyProduktow: LiveData<MutableList<String>>
    val wyswietlDodane: LiveData<MutableList<Dodane>>
    val zczytajDodane: LiveData<MutableList<ProduktyDodaneWynik>>

    // repozytorium
    private val repozytorium: Repozytorium

    // ---- Wyszukiwanie (query -> wyniki) ----
    private val _query = MutableLiveData<String>("")
    val query: LiveData<String> get() = _query

    // LiveData z wynikami wyszukiwania (przełącza źródło w zależności od query)
    val szukajProdukty: LiveData<MutableList<Produkt>>

    init {
        val dao = BazaDanych.getInstance(application).DAO()
        repozytorium = Repozytorium(dao)
        wszystkieProdukty = repozytorium.wszystkieProdukty
        nazwyProduktow = repozytorium.nazwyProduktow
        wyswietlDodane = repozytorium.wyswietlDodane
        zczytajDodane = repozytorium.zczytajDodane

        szukajProdukty = _query.switchMap { q ->
            val text = q ?: ""
            if (text.isBlank()) {
                // show all
                wszystkieProdukty
            } else {
                repozytorium.szukajProdukty("%$text%")
            }
        }
    }

    fun setQuery(q: String) {
        _query.value = q
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
