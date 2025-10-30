package com.example.aplikacjagit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : ComponentActivity() {


    private lateinit var ZalogujButton: Button
    private lateinit var WprowadzHaslo: EditText
    private lateinit var WprowadzLogin: EditText
    private lateinit var WiadomoscLogowania: TextView
    private lateinit var RejestracjaButton: Button
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        val KEY_LOGIN = getString(R.string.KEY_LOGIN_STRING) // klucze do danych chwilowych lokalnych z klasiIFunkcje
        val KEY_HASLO = getString(R.string.KEY_HASLO_STRING)
        val KEY_IS_LOGGED = getString(R.string.KEY_IS_LOGGED_STRING)
        val KEY_ADRES = getString(R.string.KEY_ADRES_STRING)
        val KEY_EMAIL = getString(R.string.KEY_EMAIL_STRING)
        val KEY_TELEFON = getString(R.string.KEY_TELEFON_STRING)
        val danePreferencje = getSharedPreferences("preferencje", Context.MODE_PRIVATE) // nazwa preferencji, gdzie są zapisane dane preferencji

        if (danePreferencje.getInt(KEY_IS_LOGGED, 0) == 1){
            val app = application as DaneGlobalne
            val login = danePreferencje.getString(KEY_LOGIN, null) // klucze pod którymi zapisane są dane w preferencji
            val haslo = danePreferencje.getString(KEY_HASLO, null)
            val email = danePreferencje.getString(KEY_EMAIL, null)
            val adres = danePreferencje.getString(KEY_ADRES, null)
            val telefon = danePreferencje.getString(KEY_TELEFON, null)
            val uzytkownik = Uzytkownik(login = login, haslo = haslo, email = email, adres = adres, telefon = telefon)
            app.aktualnyUzytkownik = uzytkownik
            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
            startActivity(intent)
        }

        ZalogujButton = findViewById(R.id.ZalogujButton) // czytanie elementów UI
        WprowadzLogin = findViewById(R.id.WprowadzLogin)
        WprowadzHaslo = findViewById(R.id.WprowadzHaslo)
        WiadomoscLogowania = findViewById(R.id.WiadomoscLogowania)
        RejestracjaButton = findViewById(R.id.RejestracjaButton)

        ZalogujButton.setOnClickListener { // listenery elementów
           Logowanie()
        }
        RejestracjaButton.setOnClickListener {
            val intent = Intent(this@LoginActivity, RejestracjaActivity::class.java)
            startActivity(intent)
        }

    }

    private fun Logowanie(){ // funkcja logowania
        val KEY_LOGIN = getString(R.string.KEY_LOGIN_STRING) // klucze do danych chwilowych lokalnych z klasiIFunkcje
        val KEY_HASLO = getString(R.string.KEY_HASLO_STRING)
        val KEY_ADRES = getString(R.string.KEY_ADRES_STRING)
        val KEY_EMAIL = getString(R.string.KEY_EMAIL_STRING)
        val KEY_TELEFON = getString(R.string.KEY_TELEFON_STRING)
        val KEY_IS_LOGGED = getString(R.string.KEY_IS_LOGGED_STRING)
        val danePreferencje = getSharedPreferences("preferencje", Context.MODE_PRIVATE)
        val edycjaPreferencji = danePreferencje.edit()
        db.collection("Loginy").get().addOnSuccessListener { result ->
            val login = WprowadzLogin.text.toString()
            val haslo = WprowadzHaslo.text.toString()
            var czyZalogowano = 0
            for (document in result) {
                if (login == document.getString(KEY_LOGIN) && haslo == document.getString(KEY_HASLO)) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Zalogowano",
                        Toast.LENGTH_LONG
                    ).show()
                    val email = document.getString(KEY_EMAIL)
                    val adres = document.getString(KEY_ADRES)
                    val telefon = document.getString(KEY_TELEFON)
                    czyZalogowano = 1
                    edycjaPreferencji.apply {
                        putInt(KEY_IS_LOGGED, 1)
                        putString(KEY_LOGIN, login)
                        putString(KEY_HASLO, haslo)
                        putString(KEY_EMAIL, email)
                        putString(KEY_ADRES, adres)
                        putString(KEY_TELEFON, telefon)
                    }.apply()
                    val app = application as DaneGlobalne
                    val uzytkownik = Uzytkownik(login = login, haslo = haslo, email = email, adres = adres, telefon = telefon)
                    app.aktualnyUzytkownik = uzytkownik
                    val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                    startActivity(intent)
                }
            }
            if (czyZalogowano == 0) {
                Toast.makeText(
                    this@LoginActivity,
                    "Wprowadź prawidłowe dane ;)",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

