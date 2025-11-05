package com.example.aplikacjagit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.aplikacjagit.room.BazaDanych
import com.example.aplikacjagit.room.Produkt
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeActivity : ComponentActivity() {
    private lateinit var ProfilButton: ImageButton
    private lateinit var HomeButton: ImageButton
    private lateinit var LodowkaButton: ImageButton
    private lateinit var TreningButton: ImageButton
    private lateinit var DietaButton: ImageButton
    private val dbOnline: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        ProfilButton =  findViewById(R.id.ProfilButton)
        HomeButton =  findViewById(R.id.HomeButton)
        LodowkaButton =  findViewById(R.id.LodowkaButton)
        TreningButton =  findViewById(R.id.TreningButton)
        DietaButton =  findViewById(R.id.DietaButton)

        aktualizacjaDanych()

        ProfilButton.setOnClickListener {
            val intent = Intent(this@HomeActivity, ProfilActivity::class.java)
            startActivity(intent)
            // Toast.makeText(this@HomeActivity, "nacisnieto", Toast.LENGTH_SHORT).show()
        }

        HomeButton.setOnClickListener {
            val intent = Intent(this@HomeActivity, HomeActivity::class.java)
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
    fun aktualizacjaDanych(){
        val KEY_NAZWA = "nazwa"
        val KEY_KALORYCZNOSC = "kalorycznosc"
        val KEY_BIALKA = "bialka"
        val KEY_TLUSZCZE = "tluszcze"
        val KEY_WEGLOWODANY = "weglowodany"

        val dbLocal = Room.databaseBuilder(
            applicationContext,
            BazaDanych::class.java, "baza_danych"
        ).build()

        val ListaProduktowDAO = dbLocal.DAO()
        val produkty = ListaProduktowDAO.nazwyProduktow()
        produkty.observe(this){ produkty ->
            dbOnline.collection("Produkty").get().addOnSuccessListener { result ->
                for (document in result) {
                    val nazwa = document.getString(KEY_NAZWA)
                    if (!produkty.contains(nazwa)) {
                        val kalorycznosc = document.getDouble(KEY_KALORYCZNOSC)?.toInt()
                        val bialka = document.getDouble(KEY_BIALKA)?.toInt()
                        val tluszcze = document.getDouble(KEY_TLUSZCZE)?.toInt()
                        val weglowodany = document.getDouble(KEY_WEGLOWODANY)?.toInt()

                        val nowyProdukt = Produkt(
                            nazwa = nazwa,
                            kalorycznosc = kalorycznosc,
                            bialka = bialka,
                            tluszcze = tluszcze,
                            weglowodany = weglowodany
                        )

                        lifecycleScope.launch(Dispatchers.IO) {
                            try {
                                ListaProduktowDAO.insertProdukt(nowyProdukt)
                                Log.d("HomeActivity", "Wstawiono produkt: $nazwa")
                            } catch (e: Exception) {
                                Log.e(
                                    "HomeActivity",
                                    "Błąd podczas insert: ${e.localizedMessage}",
                                    e
                                )
                            }
                        }

                    } else {
                        Log.d("HomeActivity", "Produkt już istnieje lokalnie: $nazwa")
                    }
                }
            }.addOnFailureListener { e ->
                Log.e("HomeActivity", "Błąd pobierania z Firestore: ${e.localizedMessage}", e)
            }
        }

    }
}

