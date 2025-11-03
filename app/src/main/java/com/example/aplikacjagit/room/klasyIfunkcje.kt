package com.example.aplikacjagit.room

import android.app.Application
import androidx.room.Entity
import androidx.room.PrimaryKey
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
    val bialka: Int?,
    val tluszcze: Int?,
    val weglowodany: Int?,
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
    val sumaKalorii: Int?,
    val sumaBialek: Int?,
    val sumaWeglowodanow: Int?,
    val sumaTluszczy: Int?,
) {
    @PrimaryKey(autoGenerate = true)
    var id = 0
}

class DaneGlobalne : Application() {
    var aktualnyUzytkownik: Uzytkownik? = null

    fun wyczysc(){
        aktualnyUzytkownik = null
    }
}
