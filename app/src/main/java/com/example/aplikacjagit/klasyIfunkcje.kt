package com.example.aplikacjagit

import android.app.Application

data class Uzytkownik(
    val login: String?,
    val haslo: String?,
    var email: String?,
    var telefon: String?,
    var adres: String?,
    var imie: String?,
    var nazwisko: String?,
)

class DaneGlobalne : Application() {
    var aktualnyUzytkownik: Uzytkownik? = null

    fun wyczysc(){
        aktualnyUzytkownik = null
    }
}
