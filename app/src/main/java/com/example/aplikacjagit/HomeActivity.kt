package com.example.aplikacjagit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.withTransaction
import com.example.aplikacjagit.adaptery.PrzepisyAdapter
import com.example.aplikacjagit.room.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

class HomeActivity : ComponentActivity() {
    private lateinit var daneViewModel: DaneViewModel
    private lateinit var propozycjeAdapter: PrzepisyAdapter
    private var sumakcal = 0
    private var celKcal = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        val app = application as DaneGlobalne
        celKcal = app.celKalorii
        daneViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application))[DaneViewModel::class.java]

        // Konfiguracja list propozycji
        propozycjeAdapter = PrzepisyAdapter { przepis ->
            Toast.makeText(this, "Polecane: ${przepis.nazwa}", Toast.LENGTH_SHORT).show()
        }
        findViewById<RecyclerView>(R.id.rvPropozycje).apply {
            adapter = propozycjeAdapter
            layoutManager = LinearLayoutManager(this@HomeActivity)
        }

        setupObservers(app)
        setupNavigation()
        syncDatabase() // Twoja logika synchronizacji
    }

    private fun setupObservers(app: DaneGlobalne) {
        // 1. DZISIEJSZA DIETA I KALORIE
        daneViewModel.wyswietlDodane.observe(this) { lista ->
            sumakcal = lista.sumOf { it.sumaKalorii ?: 0 }
            val pozostalo = celKcal - sumakcal

            // UI Główne
            val tvPozostalo = findViewById<TextView>(R.id.pozostaloKcal)
            tvPozostalo.text = "$pozostalo kcal"
            if (pozostalo < 0) tvPozostalo.setTextColor(getColor(R.color.red_400))

            val pasek = findViewById<ProgressBar>(R.id.pasekPostepuKcal)
            pasek.max = celKcal
            pasek.progress = sumakcal

            // Dolny pasek podsumowania (zaokrąglony)
            findViewById<TextView>(R.id.sumaKaloriiText).text = "Kcal: $sumakcal / $celKcal"
            findViewById<TextView>(R.id.sumaBialekText).text = String.format(Locale.US, "B: %.1f", lista.sumOf { it.sumaBialek ?: 0.0 })
            findViewById<TextView>(R.id.sumaWeglowodanowText).text = String.format(Locale.US, "W: %.1f", lista.sumOf { it.sumaWeglowodanow ?: 0.0 })
            findViewById<TextView>(R.id.sumaTluszczyText).text = String.format(Locale.US, "T: %.1f", lista.sumOf { it.sumaTluszczy ?: 0.0 })

            // AKTUALIZUJ PROPOZYCJE po zmianie kalorii
            odswiezPropozycje(pozostalo)
        }

        // 2. STATUS TRENINGU
        daneViewModel.wyswietlWykonane.observe(this) { lista ->
            val tvStatus = findViewById<TextView>(R.id.statusTreninguTekst)
            val ivStatus = findViewById<ImageView>(R.id.statusTreninguIkona)
            if (lista.isNotEmpty()) {
                tvStatus.text = "Trening wykonany! Dobra robota!"
                tvStatus.setTextColor(getColor(R.color.green_400))
                ivStatus.setColorFilter(getColor(R.color.green_400))
            }
        }
    }

    private fun odswiezPropozycje(pozostalo: Int) {
        daneViewModel.wyswietlPrzepisy.observe(this) { przepisy ->
            // Tolerancja 5% – pokaże przepisy o 5% większe niż limit, żeby nie być zbyt surowym
            val maxDopuszczalne = pozostalo * 1.05

            val propozycje = przepisy.filter {
                val k = it.kalorycznosc ?: 0
                k in 150..(maxDopuszczalne.toInt())
            }.shuffled().take(2) // Losuj 2 pasujące przepisy

            propozycjeAdapter.submitList(propozycje)
        }
    }

    private fun syncDatabase() {
        lifecycleScope.launch {
            try {
                val db = BazaDanych.getInstance(applicationContext)
                aktualizacjaDanychZPlikuOptymalnie(applicationContext, db)
            } catch (e: Exception) {
                Log.e("HomeActivity", "Błąd synchronizacji", e)
            }
        }
    }

    // Funkcja synchronizacji (Twoja oryginalna)
    suspend fun aktualizacjaDanychZPlikuOptymalnie(context: Context, db: BazaDanych){

        val TAG = "SyncFromFileOpt"

        val PREFS = "preferencje"

        val PREF_DB_VER = "db_version"

        val assetName = "output.jsonl"



        val sharedPref: SharedPreferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        val currentVersion = sharedPref.getInt(PREF_DB_VER, 0)



        withContext(Dispatchers.IO) {

            try {

                context.assets.open(assetName).use { inputStream ->

                    BufferedReader(InputStreamReader(inputStream)).use { reader ->

                        val firstLine = reader.readLine() ?: return@withContext

                        val fileVersion = firstLine.trim().toIntOrNull() ?: return@withContext



                        if (fileVersion <= currentVersion) return@withContext



                        val dao = db.DAO()

                        db.withTransaction {

                            var line: String?

                            while (reader.readLine().also { line = it } != null) {

                                try {

                                    val obj = JSONObject(line!!)

                                    val nazwa = obj.getString("nazwa")

                                    val kalorycznosc = obj.getDouble("kalorycznosc").toInt()

                                    val bialka = obj.getDouble("bialka")

                                    val tluszcze = obj.getDouble("tluszcze")

                                    val weglowodany = obj.getDouble("weglowodany")

                                    val kodKreskowy = obj.getString("kodKreskowy")



                                    val existing = dao.getProduktByBarcode(kodKreskowy)

                                    val prod = Produkt(nazwa, kalorycznosc, bialka, tluszcze, weglowodany, kodKreskowy)



                                    if (existing != null) {

                                        prod.id = existing.id

                                        dao.updateProdukt(prod)

                                    } else {

                                        dao.insertProdukt(prod)

                                    }

                                } catch (e: Exception) {

                                    Log.e(TAG, "Błąd linii: ${e.localizedMessage}")

                                }

                            }

                        }

                        sharedPref.edit().putInt(PREF_DB_VER, fileVersion).apply()

                    }

                }

            } catch (e: Exception) {

                Log.e(TAG, "Błąd pliku", e)

            }

        }

    }

    private fun setupNavigation() {
        findViewById<ImageButton>(R.id.ProfilButton).setOnClickListener { przenies(ProfilActivity::class.java) }
        findViewById<ImageButton>(R.id.HomeButton).setOnClickListener { przenies(HomeActivity::class.java) }
        findViewById<ImageButton>(R.id.LodowkaButton).setOnClickListener { przenies(LodowkaActivity::class.java) }
        findViewById<ImageButton>(R.id.TreningButton).setOnClickListener { przenies(TreningActivity::class.java) }
        findViewById<ImageButton>(R.id.DietaButton).setOnClickListener { przenies(DietaActivity::class.java) }
    }

    private fun updateSelectedDate(newDate: LocalDate) {
        val dateForRoom: Date = Date.from(newDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
        daneViewModel.setDateQuery(dateForRoom)
    }

    fun przenies(Cel: Class<out Activity>) { startActivity(Intent(this, Cel)) }
}