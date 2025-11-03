package com.example.aplikacjagit.room

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ProduktyViewModel(application: Application) : AndroidViewModel(application){
    val wszystkieProdukty: LiveData<MutableList<Produkt>>
    val nazwyProduktow: LiveData<MutableList<String>>
    val wyswietlDodane: LiveData<MutableList<ProduktyDodaneWynik>>
    var repozytorium: Repozytorium

    init {
        val dao = BazaDanych.getInstance(application).DAO()
        repozytorium = Repozytorium(dao)
        wszystkieProdukty = repozytorium.wszystkieProdukty
        nazwyProduktow = repozytorium.nazwyProduktow
        wyswietlDodane = repozytorium.wyswietlDodane
    }

    fun deleteProdukt(produkt: Produkt) = viewModelScope.launch{
        repozytorium.deleteProdukt(produkt)
    }

    fun updateProdukt(produkt: Produkt) = viewModelScope.launch{
        repozytorium.updateProdukt(produkt)
    }

    fun insertProdukt(produkt: Produkt) = viewModelScope.launch{
        repozytorium.insertProdukt(produkt)
    }

    fun deleteDodane(dodane: Dodane) = viewModelScope.launch{
        repozytorium.deleteDodane(dodane)
    }

    fun updateDodane(dodane: Dodane) = viewModelScope.launch{
        repozytorium.updateDodane(dodane)
    }

    fun insertDodane(dodane: Dodane) = viewModelScope.launch{
        repozytorium.insertDodane(dodane)
    }
}