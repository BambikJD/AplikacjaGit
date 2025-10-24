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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profil)

        PowrotButton =  findViewById(R.id.PowrotButton)

        PowrotButton.setOnClickListener {
            val intent = Intent(this@ProfilActivity, HomeActivity::class.java)
            startActivity(intent)
            // Toast.makeText(this@HomeActivity, "nacisnieto", Toast.LENGTH_SHORT).show()
        }

    }

}

