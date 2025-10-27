package com.example.aplikacjagit

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
            app.wyczysc()
            val intent = Intent(this@ProfilActivity, LoginActivity::class.java)
            startActivity(intent)
            // Toast.makeText(this@HomeActivity, "nacisnieto", Toast.LENGTH_SHORT).show()
        }

    }

}

