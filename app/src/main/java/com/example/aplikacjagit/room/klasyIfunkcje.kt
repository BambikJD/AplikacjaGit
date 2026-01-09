package com.example.aplikacjagit.room

import android.R
import android.app.Application
import androidx.core.app.ComponentActivity
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.TypeConverter
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time
}

data class Uzytkownik(
    val login: String?,
    val haslo: String?,
    var email: String?,
    var telefon: String?,
    var adres: String?,
    var imie: String?,
    var nazwisko: String?,
)

data class ProduktyDodaneWynik(
    val nazwa: String?,
    val kalorycznosc: Int?,
    val bialka: Int?,
    val weglowodany: Int?,
    val tluszcze: Int?,
    val ilosc: Int?,
    val data: Date?,
)

@Entity(tableName = "ListaProduktow")
data class Produkt(
    val nazwa: String?,
    val kalorycznosc: Int?,
    val bialka: Double?,
    val tluszcze: Double?,
    val weglowodany: Double?,
    val kodKreskowy: String?,
) {
    @PrimaryKey(autoGenerate = true)
    var id = 0
}

@Entity(tableName = "ProduktyDodane")
data class Dodane(
    val idProduktu: Int?,
    val nazwa: String?,
    val ilosc: Int?,
    val data: Date?,
    val poraDnia: Int?,
    val sumaKalorii: Int?,
    val sumaBialek: Double?,
    val sumaWeglowodanow: Double?,
    val sumaTluszczy: Double?,
) {
    @PrimaryKey(autoGenerate = true)
    var id = 0
}

@Entity(tableName = "ProduktyLodowka")
data class Lodowka(
    val idProduktu: Int?,
    val ilosc:  Int?,
    val nazwa: String?,

    ) {
    @PrimaryKey(autoGenerate = true)
    var id = 0
}

@Entity(tableName = "Przepisy")
data class Przepis(
    val nazwa: String?,
    val opis: String?,
    val kalorycznosc: Int?,
    val bialka: Double?,
    val weglowodany: Double?,
    val tluszcze: Double?,
) {
    @PrimaryKey(autoGenerate = true)
    var id = 0
}

data class ProduktPotrzebny(
    val id: Int,
    val nazwa: String,
    val ilosc: Int
)

@Entity(tableName = "PrzepisProdukt")
data class PrzepisProdukt(
    val przepisId: Int,
    val produktId: Int,
    val iloscPotrzebna: Int? = null
){
    @PrimaryKey(autoGenerate = true)
    var id = 0
}

data class PrzepisWynikRaw(
    val id: Int,
    val nazwa: String?,
    val opis: String?,
    val kalorycznosc: Int?,
    val bialka: Double?,
    val weglowodany: Double?,
    val tluszcze: Double?,
    val produktIdsCsv: String?,   // np. "3,7,12"
    val ilosciCsv: String?        // np. "2,1,1"
)

data class PrzepisWynik(
    val id: Int,
    val listaProduktow: List<Int>,
    val listaIlosci: List<Int>,
    val nazwa: String?,
    val opis: String?,
    val kalorycznosc: Int?,
    val bialka: Double?,
    val weglowodany: Double?,
    val tluszcze: Double?,
    // nowe pole: lista z nazwami i ilościami; ViewModel wypełni to pole
    val produktyPotrzebne: List<ProduktPotrzebny> = emptyList()
)

@Entity(tableName = "ListaCwiczen")
data class Cwiczenie(
    val nazwa: String?,
    val partiaMiesniowa: String?,
    val opis: String?
) {
    @PrimaryKey(autoGenerate = true)
    var id = 0
}

@Entity(tableName = "WykonaneCwiczenia")
data class Wykonane(
    val idCwiczenia: Int?,
    val nazwa: String?,
    val serie: Int?,
    val powtorzenia: Int?,
    val ciezar: Double?,
    val data: Date?
) {
    @PrimaryKey(autoGenerate = true)
    var id = 0
}

@Entity(tableName = "PlanyTreningowe")
data class Plan(
    val nazwa: String?,
    val opis: String?
) {
    @PrimaryKey(autoGenerate = true)
    var id = 0
}

@Entity(tableName = "PlanCwiczenie")
data class PlanCwiczenie(
    val planId: Int,
    val cwiczenieId: Int,
    val serie: Int?,
    val powtorzenia: Int?
) {
    @PrimaryKey(autoGenerate = true)
    var id = 0
}

// Model do przesyłania danych planu (analogicznie do PrzepisWynikRaw)
data class PlanWynikRaw(
    val id: Int,
    val nazwa: String?,
    val opis: String?,
    val cwiczenieIdsCsv: String?,
    val serieCsv: String?,      // DODANE
    val powtorzeniaCsv: String? // DODANE
)

data class PlanWynik(
    val id: Int,
    val nazwa: String?,
    val opis: String?,
    val listaCwiczen: List<Int>,
    val nazwyCwiczen: List<String> = emptyList(),
    val serie: List<Int> = emptyList(),      // DODANE
    val powtorzenia: List<Int> = emptyList() // DODANE
)

class DaneGlobalne : Application() {
    var aktualnyUzytkownik: Uzytkownik? = null

    var data: Date? = null
    var sumaKalorii: Double = 0.0
    var sumaBialek: Double = 0.0
    var sumaTluszczy: Double = 0.0
    var sumaWeglowodanow: Double = 0.0
    var celBialek: Int = 0
    var celWeglowodanow: Int = 0
    var celTluszczy: Int = 0
    var celKalorii: Int = 0
    var waga: Float = 0.0F
    var wzrost: Float = 0.0F
    var wiek: Int = 0
    var cel: Int = 2
    var aktywnosc: Int = 2
    var plec: Boolean = true

    fun wyczysc(){
        aktualnyUzytkownik = null
    }
}