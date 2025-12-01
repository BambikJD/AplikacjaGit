package com.example.aplikacjagit.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import java.util.Date

@Dao
interface DAO{

    // Produkty
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProdukt(produkt: Produkt)

    @Delete
    suspend fun deleteProdukt(produkt: Produkt)

    @Update
    suspend fun updateProdukt(produkt: Produkt)

    @Query("SELECT * from ListaProduktow")
    fun wyswietlProdukty() : LiveData<MutableList<Produkt>>

    @Query("SELECT nazwa from ListaProduktow")
    fun nazwyProduktow() : LiveData<MutableList<String>>

    @Query("SELECT * FROM ListaProduktow WHERE nazwa LIKE :query")
    fun szukajProdukty(query: String): LiveData<MutableList<Produkt>>

    @Query("SELECT * FROM ListaProduktow WHERE kodKreskowy = :kod LIMIT 1")
    suspend fun getProduktByBarcode(kod: String): Produkt?

    @Query("SELECT * FROM ListaProduktow WHERE id IN (:ids)")
    suspend fun getProduktyByIds(ids: List<Int>): List<Produkt>

    // Dodane
    @Insert
    suspend fun insertDodane(dodane: Dodane)

    @Delete
    suspend fun deleteDodane(dodane: Dodane)

    @Update
    suspend fun updateDodane(dodane: Dodane)

    @Query("SELECT  ListaProduktow.nazwa, ListaProduktow.kalorycznosc, ListaProduktow.bialka, ListaProduktow.weglowodany, ListaProduktow.tluszcze, ProduktyDodane.ilosc, ProduktyDodane.data from ListaProduktow, ProduktyDodane where ListaProduktow.id == ProduktyDodane.id")
    fun zczytajDodane() : LiveData<MutableList<ProduktyDodaneWynik>>

    @Query("SELECT  * from ProduktyDodane where data == :data")
    fun wyswietlDodane(data: Date) : LiveData<MutableList<Dodane>>

    @Query("SELECT * FROM ProduktyLodowka")
    fun getProduktyWLodowce(): LiveData<MutableList<Lodowka>>

    @Insert
    suspend fun insertPrzepis(przepis: Przepis)

    @Insert
    suspend fun insertPrzepisProdukt(przepis: PrzepisProdukt)

    @Query("SELECT id from Przepisy ORDER BY id DESC LIMIT 1 ")
    fun getOstatniPrzepisId() : LiveData<Int>

    @Query("""SELECT p.id AS id,p.nazwa AS nazwa,p.opis AS opis,p.kalorycznosc AS kalorycznosc,p.bialka AS bialka,p.weglowodany AS weglowodany,p.tluszcze AS tluszcze,GROUP_CONCAT(pp.produktId) AS produktIdsCsv,GROUP_CONCAT(COALESCE(pp.iloscPotrzebna, 0)) AS ilosciCsv FROM Przepisy p LEFT JOIN PrzepisProdukt pp ON p.id = pp.przepisId GROUP BY p.id""")
    fun getPrzepisyWynikRaw(): LiveData<MutableList<PrzepisWynikRaw>>
}