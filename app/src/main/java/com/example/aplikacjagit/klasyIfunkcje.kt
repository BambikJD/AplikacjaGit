package com.example.aplikacjagit

import android.app.Application

data class Uzytkownik(
    val login: String?,
    val haslo: String?,
    val email: String?,
    val telefon: String?,
    val adres: String?,
)

class DaneGlobalne : Application() {
    var aktualnyUzytkownik: Uzytkownik? = null

    fun wyczysc(){
        aktualnyUzytkownik = null
    }
}
