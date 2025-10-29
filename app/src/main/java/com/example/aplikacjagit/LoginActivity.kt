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
    private val KEY_LOGIN = "login" // klucze do danych chwilowych lokalnych z klasiIFunkcje
    private val KEY_HASLO = "haslo"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)
        val danePreferencje = getSharedPreferences("preferencje", Context.MODE_PRIVATE) // nazwa preferencji, gdzie są zapisane dane preferencji
        if (danePreferencje.getInt("ZALOGOWANY_KEY", 0) == 1){
            val app = application as DaneGlobalne
            val login = danePreferencje.getString("LOGIN_KEY", null) // klucze pod którymi zapisane są dane w preferencji
            val haslo = danePreferencje.getString("HASLO_KEY", null)
            val uzytkownik = Uzytkownik(login = login, haslo = haslo)
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
                    czyZalogowano = 1
                    edycjaPreferencji.apply {
                        putInt("ZALOGOWANY_KEY", 1)
                        putString("LOGIN_KEY", login)
                        putString("HASLO_KEY", haslo)
                    }.apply()
                    val app = application as DaneGlobalne
                    val uzytkownik = Uzytkownik(login = login, haslo = haslo)
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

