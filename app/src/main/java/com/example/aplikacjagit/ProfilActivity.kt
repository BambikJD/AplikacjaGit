package com.example.aplikacjagit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity

class ProfilActivity : ComponentActivity() {
    private lateinit var PowrotButton: Button
    private lateinit var DaneLogowania: TextView
    private lateinit var WylogujButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.profil)

        val KEY_ADRES = getString(R.string.KEY_ADRES_STRING)
        val KEY_EMAIL = getString(R.string.KEY_EMAIL_STRING)
        val KEY_TELEFON = getString(R.string.KEY_TELEFON_STRING)
        val KEY_LOGIN = getString(R.string.KEY_LOGIN_STRING) // klucze do danych chwilowych lokalnych z klasiIFunkcje
        val KEY_HASLO = getString(R.string.KEY_HASLO_STRING)
        val KEY_IS_LOGGED = getString(R.string.KEY_IS_LOGGED_STRING)

        PowrotButton =  findViewById(R.id.PowrotButton)
        DaneLogowania = findViewById(R.id.DaneLogowania)
        WylogujButton = findViewById(R.id.WylogujButton)

        val app = application as DaneGlobalne
        val aktualnyUzytkownik = app.aktualnyUzytkownik

        if(aktualnyUzytkownik != null) {
            DaneLogowania.text = "Zalogowano jako: ${aktualnyUzytkownik.login}"
        }

        PowrotButton.setOnClickListener {
            val intent = Intent(this@ProfilActivity, HomeActivity::class.java)
            startActivity(intent)
            // Toast.makeText(this@HomeActivity, "nacisnieto", Toast.LENGTH_SHORT).show()
        }

        WylogujButton.setOnClickListener {
            val danePreferencje = getSharedPreferences("preferencje", Context.MODE_PRIVATE)
            val edycjaPreferencji = danePreferencje.edit()
            edycjaPreferencji.apply {
                putInt(KEY_IS_LOGGED, 0)
                putString(KEY_LOGIN, null)
                putString(KEY_HASLO, null)
            }.apply()
            app.wyczysc()
            val intent = Intent(this@ProfilActivity, LoginActivity::class.java)
            startActivity(intent)
            // Toast.makeText(this@HomeActivity, "nacisnieto", Toast.LENGTH_SHORT).show()
        }

    }

}

