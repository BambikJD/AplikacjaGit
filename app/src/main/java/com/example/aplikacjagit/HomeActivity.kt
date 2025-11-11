package com.example.aplikacjagit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.aplikacjagit.room.BazaDanych
import com.example.aplikacjagit.room.DaneGlobalne
import com.example.aplikacjagit.room.DaneViewModel
import com.example.aplikacjagit.room.Produkt
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class HomeActivity : ComponentActivity() {
    private lateinit var ProfilButton: ImageButton
    private lateinit var HomeButton: ImageButton
    private lateinit var LodowkaButton: ImageButton
    private lateinit var TreningButton: ImageButton
    private lateinit var DietaButton: ImageButton

    private lateinit var sumaKaloriiText: TextView
    private lateinit var sumaBialekText: TextView
    private lateinit var sumaWeglowodanowText: TextView
    private lateinit var sumaTluszczyText: TextView

    private lateinit var daneViewModel: DaneViewModel

    private var selectedLocalDate: LocalDate = LocalDate.now()

    private val dbOnline: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        val danePreferencje = getSharedPreferences("preferencje", Context.MODE_PRIVATE)

        val app = application as DaneGlobalne
        var aktualnyUzytkownik = app.aktualnyUzytkownik

        app.celBialek = danePreferencje.getInt("celBialek", 0)
        app.celWeglowodanow = danePreferencje.getInt("celWeglowodanow", 0)
        app.celTluszczy = danePreferencje.getInt("celTluszczy", 0)
        app.celKalorii = danePreferencje.getInt("celKalorii", 0)

        ProfilButton =  findViewById(R.id.ProfilButton)
        HomeButton =  findViewById(R.id.HomeButton)
        LodowkaButton =  findViewById(R.id.LodowkaButton)
        TreningButton =  findViewById(R.id.TreningButton)
        DietaButton =  findViewById(R.id.DietaButton)

        sumaKaloriiText = findViewById(R.id.sumaKaloriiText)
        sumaBialekText = findViewById(R.id.sumaBialekText)
        sumaWeglowodanowText = findViewById(R.id.sumaWeglowodanowText)
        sumaTluszczyText = findViewById(R.id.sumaTluszczyText)

        daneViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application))[DaneViewModel::class.java]

        updateSelectedDate(selectedLocalDate)

        var sumakalorii = 0
        var sumabialek = 0
        var sumaweglowodanow = 0
        var sumatluszczy = 0

        sumaKaloriiText.text = "Kalorie\n${sumakalorii} / ${app.celKalorii}"
        sumaBialekText.text = "B\n${sumabialek} / ${app.celBialek}"
        sumaWeglowodanowText.text = "W\n${sumaweglowodanow} / ${app.celWeglowodanow}"
        sumaTluszczyText.text = "T\n${sumatluszczy} / ${app.celTluszczy}"


        daneViewModel.wyswietlDodane.observe(this) { lista ->
            sumakalorii = 0
            sumabialek = 0
            sumaweglowodanow = 0
            sumatluszczy = 0
            for(produkt in lista){
                if(produkt.sumaKalorii != null && produkt.sumaBialek != null  && produkt.sumaTluszczy != null && produkt.sumaWeglowodanow != null) {
                    sumakalorii += produkt.sumaKalorii
                    sumabialek += produkt.sumaBialek
                    sumatluszczy += produkt.sumaTluszczy
                    sumaweglowodanow += produkt.sumaWeglowodanow

                    sumaKaloriiText.text = "Kalorie\n${sumakalorii} / ${app.celKalorii}"
                    sumaBialekText.text = "B\n${sumabialek} / ${app.celBialek}"
                    sumaWeglowodanowText.text = "W\n${sumaweglowodanow} / ${app.celWeglowodanow}"
                    sumaTluszczyText.text = "T\n${sumatluszczy} / ${app.celTluszczy}"

                }
            }
        }

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

    private fun updateSelectedDate(newDate: LocalDate) {
        selectedLocalDate = newDate
        // konwertujemy LocalDate -> Date (start of day)
        val dateForRoom: Date = Date.from(
            selectedLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
        )
        // ustawiamy query w ViewModelie
        daneViewModel.setDateQuery(dateForRoom)
    }
}

