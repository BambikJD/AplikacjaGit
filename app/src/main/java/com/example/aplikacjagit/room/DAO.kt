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

    // --- PRODUKTY ---
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

    // --- DODANE (KALORIE) ---
    @Insert
    suspend fun insertDodane(dodane: Dodane)

    @Delete
    suspend fun deleteDodane(dodane: Dodane)

    @Update
    suspend fun updateDodane(dodane: Dodane)

    @Query("SELECT ListaProduktow.nazwa, ListaProduktow.kalorycznosc, ListaProduktow.bialka, ListaProduktow.weglowodany, ListaProduktow.tluszcze, ProduktyDodane.ilosc, ProduktyDodane.data from ListaProduktow, ProduktyDodane where ListaProduktow.id == ProduktyDodane.id")
    fun zczytajDodane() : LiveData<MutableList<ProduktyDodaneWynik>>

    @Query("SELECT * from ProduktyDodane where data == :data")
    fun wyswietlDodane(data: Date) : LiveData<MutableList<Dodane>>

    // --- LODÓWKA ---
    @Query("SELECT * FROM ProduktyLodowka")
    fun getProduktyWLodowce(): LiveData<MutableList<Lodowka>>

    @Query("SELECT * from produktylodowka")
    fun wyswietlLodowka(): LiveData<MutableList<Lodowka>>

    @Insert
    suspend fun insertLodowka(lodowka: Lodowka)

    @Delete
    suspend fun deleteLodowka(lodowka: Lodowka)

    @Update
    suspend fun updateLodowka(lodowka: Lodowka)

    // --- PRZEPISY ---
    @Insert
    suspend fun insertPrzepis(przepis: Przepis)

    @Insert
    suspend fun insertPrzepisProdukt(przepis: PrzepisProdukt)

    @Query("SELECT id from Przepisy ORDER BY id DESC LIMIT 1")
    fun getOstatniPrzepisId() : LiveData<Int>

    @Query("""SELECT p.id AS id,p.nazwa AS nazwa,p.opis AS opis,p.kalorycznosc AS kalorycznosc,p.bialka AS bialka,p.weglowodany AS weglowodany,p.tluszcze AS tluszcze,GROUP_CONCAT(pp.produktId) AS produktIdsCsv,GROUP_CONCAT(COALESCE(pp.iloscPotrzebna, 0)) AS ilosciCsv FROM Przepisy p LEFT JOIN PrzepisProdukt pp ON p.id = pp.przepisId GROUP BY p.id""")
    fun getPrzepisyWynikRaw(): LiveData<MutableList<PrzepisWynikRaw>>

    @Query("""
        SELECT 
            p.id AS id,
            p.nazwa AS nazwa,
            p.opis AS opis,
            p.kalorycznosc AS kalorycznosc,
            p.bialka AS bialka,
            p.weglowodany AS weglowodany,
            p.tluszcze AS tluszcze,
            GROUP_CONCAT(pp.produktId) AS produktIdsCsv,
            GROUP_CONCAT(COALESCE(pp.iloscPotrzebna, 0)) AS ilosciCsv 
        FROM Przepisy p 
        LEFT JOIN PrzepisProdukt pp ON p.id = pp.przepisId 
        WHERE NOT EXISTS (
            SELECT 1 
            FROM PrzepisProdukt pp_check
            LEFT JOIN ProduktyLodowka pl ON pp_check.produktId = pl.idProduktu
            WHERE pp_check.przepisId = p.id
            AND pl.idProduktu IS NULL
        )
        GROUP BY p.id
    """)
    fun getPrzepisyZLodowkiRaw(): LiveData<MutableList<PrzepisWynikRaw>>

    // --- TRENINGI (ĆWICZENIA) ---
    @Insert
    suspend fun insertCwiczenie(cwiczenie: Cwiczenie)

    @Delete // DODANE: Możliwość usunięcia ćwiczenia z bazy
    suspend fun deleteCwiczenie(cwiczenie: Cwiczenie)

    @Update // DODANE: Możliwość edycji ćwiczenia
    suspend fun updateCwiczenie(cwiczenie: Cwiczenie)

    @Query("SELECT * FROM ListaCwiczen")
    fun wyswietlCwiczenia(): LiveData<MutableList<Cwiczenie>>

    @Query("SELECT * FROM ListaCwiczen WHERE nazwa LIKE :query")
    fun szukajCwiczenia(query: String): LiveData<MutableList<Cwiczenie>>

    @Query("SELECT * FROM ListaCwiczen WHERE id IN (:ids)")
    suspend fun getCwiczeniaByIds(ids: List<Int>): List<Cwiczenie>

    // --- WYKONANE TRENINGI (DZIENNIK) ---
    @Insert
    suspend fun insertWykonane(wykonane: Wykonane)

    @Delete
    suspend fun deleteWykonane(wykonane: Wykonane)

    @Update // DODANE: Możliwość poprawienia wpisu w dzienniku (np. ciężaru)
    suspend fun updateWykonane(wykonane: Wykonane)

    @Query("SELECT * FROM WykonaneCwiczenia WHERE data == :data")
    fun wyswietlWykonane(data: Date): LiveData<MutableList<Wykonane>>

    // --- PLANY TRENINGOWE ---
    @Insert
    suspend fun insertPlan(plan: Plan)

    @Delete // DODANE: Usuwanie planu
    suspend fun deletePlan(plan: Plan)

    @Insert
    suspend fun insertPlanCwiczenie(planCwiczenie: PlanCwiczenie)

    @Query("DELETE FROM PlanCwiczenie WHERE planId = :planId") // DODANE: Czyści powiązania przy usuwaniu planu
    suspend fun usunCwiczeniaZPlanu(planId: Int)

    @Query("""
    SELECT p.id, p.nazwa, p.opis, 
    GROUP_CONCAT(pc.cwiczenieId) AS cwiczenieIdsCsv,
    GROUP_CONCAT(pc.serie) AS serieCsv,
    GROUP_CONCAT(pc.powtorzenia) AS powtorzeniaCsv
    FROM PlanyTreningowe p 
    LEFT JOIN PlanCwiczenie pc ON p.id = pc.planId 
    GROUP BY p.id
""")
    fun getPlanyRaw(): LiveData<MutableList<PlanWynikRaw>>

    @Query("SELECT id from PlanyTreningowe ORDER BY id DESC LIMIT 1")
    fun getOstatniPlanId() : LiveData<Int>
}