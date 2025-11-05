package com.example.aplikacjagit

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity

class LodowkaActivity : ComponentActivity() {
    private lateinit var ProfilButton: ImageButton
    private lateinit var HomeButton: ImageButton
    private lateinit var LodowkaButton: ImageButton
    private lateinit var TreningButton: ImageButton
    private lateinit var DietaButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lodowka)

        ProfilButton =  findViewById(R.id.ProfilButton)
        HomeButton =  findViewById(R.id.HomeButton)
        LodowkaButton =  findViewById(R.id.LodowkaButton)
        TreningButton =  findViewById(R.id.TreningButton)
        DietaButton =  findViewById(R.id.DietaButton)

        ProfilButton.setOnClickListener {
            val intent = Intent(this@LodowkaActivity, ProfilActivity::class.java)
            startActivity(intent)
            // Toast.makeText(this@HomeActivity, "nacisnieto", Toast.LENGTH_SHORT).show()
        }

        HomeButton.setOnClickListener {
            val intent = Intent(this@LodowkaActivity, HomeActivity::class.java)
            startActivity(intent)
            // Toast.makeText(this@HomeActivity, "nacisnieto", Toast.LENGTH_SHORT).show()
        }

        LodowkaButton.setOnClickListener {
            val intent = Intent(this@LodowkaActivity, LodowkaActivity::class.java)
            startActivity(intent)
            // Toast.makeText(this@HomeActivity, "nacisnieto", Toast.LENGTH_SHORT).show()
        }

        TreningButton.setOnClickListener {
            val intent = Intent(this@LodowkaActivity, TreningActivity::class.java)
            startActivity(intent)
            // Toast.makeText(this@HomeActivity, "nacisnieto", Toast.LENGTH_SHORT).show()
        }

        DietaButton.setOnClickListener {
            val intent = Intent(this@LodowkaActivity, DietaActivity::class.java)
            startActivity(intent)
            // Toast.makeText(this@HomeActivity, "nacisnieto", Toast.LENGTH_SHORT).show()
        }

    }

}

