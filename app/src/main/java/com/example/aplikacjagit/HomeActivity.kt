package com.example.aplikacjagit

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : ComponentActivity() {
    private lateinit var ProfilButton: Button
    private lateinit var StatystykiButton: Button
    private lateinit var LodowkaButton: Button
    private lateinit var TreningButton: Button
    private lateinit var DietaButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        ProfilButton =  findViewById(R.id.ProfilButton)
        StatystykiButton =  findViewById(R.id.StatystykiButton)
        LodowkaButton =  findViewById(R.id.LodowkaButton)
        TreningButton =  findViewById(R.id.TreningButton)
        DietaButton =  findViewById(R.id.DietaButton)

        ProfilButton.setOnClickListener {
            val intent = Intent(this@HomeActivity, ProfilActivity::class.java)
            startActivity(intent)
            // Toast.makeText(this@HomeActivity, "nacisnieto", Toast.LENGTH_SHORT).show()
        }

        StatystykiButton.setOnClickListener {
            val intent = Intent(this@HomeActivity, SpolecznoscActivity::class.java)
            startActivity(intent)
            // Toast.makeText(this@HomeActivity, "nacisnieto", Toast.LENGTH_SHORT).show()
        }

        LodowkaButton.setOnClickListener {
            val intent = Intent(this@HomeActivity, LodowkaActivity::class.java)
            startActivity(intent)
            // Toast.makeText(this@HomeActivity, "nacisnieto", Toast.LENGTH_SHORT).show()
        }

        TreningButton.setOnClickListener {
            val intent = Intent(this@HomeActivity, TreningActivity::class.java)
            startActivity(intent)
            // Toast.makeText(this@HomeActivity, "nacisnieto", Toast.LENGTH_SHORT).show()
        }

        DietaButton.setOnClickListener {
            val intent = Intent(this@HomeActivity, DietaActivity::class.java)
            startActivity(intent)
            // Toast.makeText(this@HomeActivity, "nacisnieto", Toast.LENGTH_SHORT).show()
        }

    }

}

