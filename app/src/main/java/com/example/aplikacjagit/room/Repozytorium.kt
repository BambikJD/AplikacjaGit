package com.example.aplikacjagit.room

import androidx.lifecycle.LiveData
import java.util.Date

class Repozytorium(private val DAO: DAO){
    val wszystkieProdukty : LiveData<MutableList<Produkt>> = DAO.wyswietlProdukty()
    val nazwyProduktow : LiveData<MutableList<String>> = DAO.nazwyProduktow()
    val zczytajDodane : LiveData<MutableList<ProduktyDodaneWynik>> = DAO.zczytajDodane()
    val getOstatniePrzepisID: LiveData<Int> = DAO.getOstatniPrzepisId()
    val wyswietlLodowka: LiveData<MutableList<Lodowka>> = DAO.wyswietlLodowka()

    // zwraca surowe LiveData z DAO (CSV itd.)
    fun getPrzepisyWynikRaw(): LiveData<MutableList<PrzepisWynikRaw>> =
        DAO.getPrzepisyWynikRaw()

    fun getPrzepisyZLodowkiRaw(): LiveData<MutableList<PrzepisWynikRaw>> =
        DAO.getPrzepisyZLodowkiRaw()

    // suspend helper - pobiera listę Produkt dla podanych id
    suspend fun fetchProduktyByIds(ids: List<Int>): List<Produkt> {
        if (ids.isEmpty()) return emptyList()
        return DAO.getProduktyByIds(ids)
    }

    // pozostałe metody: insert/update/delete (jak wcześniej)
    suspend fun insertProdukt(produkt: Produkt){ DAO.insertProdukt(produkt) }
    suspend fun deleteProdukt(produkt: Produkt){ DAO.deleteProdukt(produkt) }
    suspend fun updateProdukt(produkt : Produkt){ DAO.updateProdukt(produkt) }
    suspend fun insertDodane(dodane: Dodane){ DAO.insertDodane(dodane) }
    suspend fun deleteDodane(dodane: Dodane){ DAO.deleteDodane(dodane) }
    suspend fun updateDodane(dodane: Dodane){ DAO.updateDodane(dodane) }
    suspend fun insertLodowka(Lodowka: Lodowka){ DAO.insertLodowka(Lodowka) }
    suspend fun deleteLodowka(Lodowka: Lodowka){ DAO.deleteLodowka(Lodowka) }
    suspend fun updateLodowka(Lodowka : Lodowka){ DAO.updateLodowka(Lodowka) }
    suspend fun insertPrzepis(przepis: Przepis){ DAO.insertPrzepis(przepis) }
    suspend fun insertPrzepisProdukt(przepisProdukt: PrzepisProdukt){ DAO.insertPrzepisProdukt(przepisProdukt) }


    fun szukajProdukty(query: String): LiveData<MutableList<Produkt>> = DAO.szukajProdukty(query)
    fun wyswietlDodane(data: Date): LiveData<MutableList<Dodane>> = DAO.wyswietlDodane(data)

    // pomocnik parsujący CSV -> List<Int>
    fun csvToIntList(csv: String?): List<Int> =
        csv?.split(",")?.mapNotNull { it.trim().toIntOrNull() } ?: emptyList()

    // mapowanie pojedynczego PrzepisWynikRaw -> PrzepisWynik (bez nazw produktów)
    fun mapRawToPrzepisWynik(raw: PrzepisWynikRaw): PrzepisWynik {
        val produktIds = csvToIntList(raw.produktIdsCsv)
        val ilosci = csvToIntList(raw.ilosciCsv).let { list ->
            if (list.size < produktIds.size) list + List(produktIds.size - list.size) { 0 } else list
        }
        return PrzepisWynik(
            id = raw.id,
            listaProduktow = produktIds,
            listaIlosci = ilosci,
            nazwa = raw.nazwa,
            opis = raw.opis,
            kalorycznosc = raw.kalorycznosc,
            bialka = raw.bialka,
            weglowodany = raw.weglowodany,
            tluszcze = raw.tluszcze,
            produktyPotrzebne = emptyList()
        )
    }
}
