package com.example.aplikacjagit.room

import androidx.lifecycle.LiveData

class Repozytorium(private val DAO: DAO){
    val wszystkieProdukty : LiveData<MutableList<Produkt>> = DAO.wyswietlProdukty()
    val nazwyProduktow : LiveData<MutableList<String>> = DAO.nazwyProduktow()
    val wyswietlDodane : LiveData<MutableList<Dodane>> = DAO.wyswietlDodane()
    val zczytajDodane : LiveData<MutableList<ProduktyDodaneWynik>> = DAO.zczytajDodane()

    suspend fun insertProdukt(produkt: Produkt){
        DAO.insertProdukt(produkt)
    }

    suspend fun deleteProdukt(produkt: Produkt){
        DAO.deleteProdukt(produkt)
    }

    suspend fun updateProdukt(produkt : Produkt){
        DAO.updateProdukt(produkt)
    }

    suspend fun insertDodane(dodane: Dodane){
        DAO.insertDodane(dodane)
    }

    suspend fun deleteDodane(dodane: Dodane){
        DAO.deleteDodane(dodane)
    }

    suspend fun updateDodane(dodane: Dodane){
        DAO.updateDodane(dodane)
    }

    fun szukajProdukty(query: String): LiveData<MutableList<Produkt>> {
        return DAO.szukajProdukty(query)
    }
}
