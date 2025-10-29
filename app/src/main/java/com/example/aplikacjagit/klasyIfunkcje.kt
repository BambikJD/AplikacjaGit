package com.example.aplikacjagit

import android.app.Application

data class Uzytkownik(
    val login: String?,
    val haslo: String?,
)

class DaneGlobalne : Application() {
    var aktualnyUzytkownik: Uzytkownik? = null

    fun wyczysc(){
        aktualnyUzytkownik = null
    }
}
