package com.example.aplikacjagit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.aplikacjagit.room.DaneGlobalne
import com.example.aplikacjagit.room.Uzytkownik
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : ComponentActivity() {

    private lateinit var ZalogujButton: Button
    private lateinit var WprowadzHaslo: EditText
    private lateinit var WprowadzLogin: EditText
    private lateinit var WiadomoscLogowania: TextView
    private lateinit var RejestracjaButton: TextView // Zmienione z Button na TextView
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        // Klucze SharedPreferences
        val KEY_LOGIN = getString(R.string.KEY_LOGIN_STRING)
        val KEY_HASLO = getString(R.string.KEY_HASLO_STRING)
        val KEY_IS_LOGGED = getString(R.string.KEY_IS_LOGGED_STRING)
        val KEY_ADRES = getString(R.string.KEY_ADRES_STRING)
        val KEY_EMAIL = getString(R.string.KEY_EMAIL_STRING)
        val KEY_TELEFON = getString(R.string.KEY_TELEFON_STRING)
        val KEY_IMIE = getString(R.string.KEY_IMIE_STRING)
        val KEY_NAZWISKO = getString(R.string.KEY_NAZWISKO_STRING)

        val danePreferencje = getSharedPreferences("preferencje", Context.MODE_PRIVATE)

        // Automatyczne logowanie, jeśli użytkownik jest już zapisany
        if (danePreferencje.getInt(KEY_IS_LOGGED, 0) == 1) {
            val app = application as DaneGlobalne

            // Wczytujemy dane uzytkownika (to co już miałeś)
            val uzytkownik = Uzytkownik(
                login = danePreferencje.getString(KEY_LOGIN, null),
                haslo = danePreferencje.getString(KEY_HASLO, null),
                email = danePreferencje.getString(KEY_EMAIL, null),
                adres = danePreferencje.getString(KEY_ADRES, null),
                telefon = danePreferencje.getString(KEY_TELEFON, null),
                imie = danePreferencje.getString(KEY_IMIE, null),
                nazwisko = danePreferencje.getString(KEY_NAZWISKO, null)
            )
            app.aktualnyUzytkownik = uzytkownik

            // --- KLUCZOWA POPRAWKA: Wczytujemy cele do obiektu globalnego ---
            app.celKalorii = danePreferencje.getInt("celKalorii", 0)
            app.celBialek = danePreferencje.getInt("celBialek", 0)
            app.celWeglowodanow = danePreferencje.getInt("celWeglowodanow", 0)
            app.celTluszczy = danePreferencje.getInt("celTluszczy", 0)
            app.waga = danePreferencje.getFloat("waga", 0.0F)
            app.wzrost = danePreferencje.getFloat("wzrost", 0.0F)
            app.wiek = danePreferencje.getInt("wiek", 0)
            app.plec = danePreferencje.getBoolean("plec", true)
            app.aktywnosc = danePreferencje.getInt("aktywnosc", 0)
            app.cel = danePreferencje.getInt("cel", 0)

            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
            startActivity(intent)
            finish() // Ważne, żeby zamknąć LoginActivity
        }

        // Inicjalizacja widoków
        ZalogujButton = findViewById(R.id.ZalogujButton)
        WprowadzLogin = findViewById(R.id.WprowadzLogin)
        WprowadzHaslo = findViewById(R.id.WprowadzHaslo)
        WiadomoscLogowania = findViewById(R.id.WiadomoscLogowania)
        RejestracjaButton = findViewById(R.id.RejestracjaButton)

        ZalogujButton.setOnClickListener { Logowanie() }

        RejestracjaButton.setOnClickListener {
            startActivity(Intent(this, RejestracjaActivity::class.java))
        }
    }

    private fun Logowanie() {
        val KEY_LOGIN = getString(R.string.KEY_LOGIN_STRING)
        val KEY_HASLO = getString(R.string.KEY_HASLO_STRING)
        val KEY_ADRES = getString(R.string.KEY_ADRES_STRING)
        val KEY_EMAIL = getString(R.string.KEY_EMAIL_STRING)
        val KEY_TELEFON = getString(R.string.KEY_TELEFON_STRING)
        val KEY_IMIE = getString(R.string.KEY_IMIE_STRING)
        val KEY_NAZWISKO = getString(R.string.KEY_NAZWISKO_STRING)
        val KEY_IS_LOGGED = getString(R.string.KEY_IS_LOGGED_STRING)

        val danePreferencje = getSharedPreferences("preferencje", Context.MODE_PRIVATE)
        val login = WprowadzLogin.text.toString()
        val haslo = WprowadzHaslo.text.toString()

        if (login.isEmpty() || haslo.isEmpty()) {
            Toast.makeText(this, "Wypełnij wszystkie pola!", Toast.LENGTH_SHORT).show()
            return
        }

        // Loader lub zmiana tekstu przycisku (opcjonalnie)
        ZalogujButton.text = "LOGOWANIE..."
        ZalogujButton.isEnabled = false

        db.collection("Loginy").get().addOnSuccessListener { result ->
            var czyZalogowano = false

            if (haslo == "admin" && login == "admin") {
                startActivity(Intent(this, AdminActivity::class.java))
            } else {
                for (document in result) {
                    if (login == document.getString(KEY_LOGIN) && haslo == document.getString(KEY_HASLO)) {
                        czyZalogowano = true

                        val uzytkownik = Uzytkownik(
                            login = login,
                            haslo = haslo,
                            email = document.getString(KEY_EMAIL),
                            adres = document.getString(KEY_ADRES),
                            telefon = document.getString(KEY_TELEFON),
                            imie = document.getString(KEY_IMIE),
                            nazwisko = document.getString(KEY_NAZWISKO)
                        )

                        // Zapis do SharedPreferences
                        danePreferencje.edit().apply {
                            putInt(KEY_IS_LOGGED, 1)
                            putString(KEY_LOGIN, login)
                            putString(KEY_HASLO, haslo)
                            putString(KEY_EMAIL, uzytkownik.email)
                            putString(KEY_ADRES, uzytkownik.adres)
                            putString(KEY_TELEFON, uzytkownik.telefon)
                            putString(KEY_IMIE, uzytkownik.imie)
                            putString(KEY_NAZWISKO, uzytkownik.nazwisko)
                        }.apply()

                        (application as DaneGlobalne).aktualnyUzytkownik = uzytkownik

                        Toast.makeText(this, "Witaj ponownie!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                        break
                    }
                }
                if (!czyZalogowano) {
                    ZalogujButton.text = "ZALOGUJ SIĘ"
                    ZalogujButton.isEnabled = true
                    WiadomoscLogowania.visibility = View.VISIBLE
                    WiadomoscLogowania.text = "Błędny login lub hasło!"
                }
            }
        }.addOnFailureListener {
            ZalogujButton.text = "ZALOGUJ SIĘ"
            ZalogujButton.isEnabled = true
            Toast.makeText(this, "Błąd połączenia z bazą danych", Toast.LENGTH_SHORT).show()
        }
    }
}