package com.example.aplikacjagit.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface DAO{

    // Produkty
    @Insert
    suspend fun insertProdukt(produkt: Produkt)

    @Delete
    suspend fun deleteProdukt(produkt: Produkt)

    @Update
    suspend fun updateProdukt(produkt: Produkt)

    @Query("SELECT * from ListaProduktow")
    fun wyswietlProdukty() : LiveData<MutableList<Produkt>>

    @Query("SELECT nazwa from ListaProduktow")
    fun nazwyProduktow() : LiveData<MutableList<String>>


    // Dodane
    @Insert
    suspend fun insertDodane(dodane: Dodane)

    @Delete
    suspend fun deleteDodane(dodane: Dodane)

    @Update
    suspend fun updateDodane(dodane: Dodane)

    @Query("SELECT  ListaProduktow.nazwa, ListaProduktow.kalorycznosc, ListaProduktow.bialka, ListaProduktow.weglowodany, ListaProduktow.tluszcze, ProduktyDodane.ilosc, ProduktyDodane.data from ListaProduktow, ProduktyDodane where ListaProduktow.id == ProduktyDodane.id")
    fun wyswietlDodane() : LiveData<MutableList<ProduktyDodaneWynik>>

}