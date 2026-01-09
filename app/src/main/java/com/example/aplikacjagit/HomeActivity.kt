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
    private var selectedLocalDate: LocalDate = LocalDate.now() // DODANO: Inicjalizacja daty
    private var sumakcal = 0
    private var celKcal = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        val app = application as DaneGlobalne

        // --- KLUCZOWA POPRAWKA: Najpierw tworzymy ViewModel ---
        daneViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application))[DaneViewModel::class.java]

        // --- TERAZ ustawiamy datę (kiedy ViewModel już istnieje) ---
        updateSelectedDate(selectedLocalDate)

        celKcal = app.celKalorii

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
        syncDatabase()
    }

    // Dodano onResume, aby dane odświeżały się po powrocie z dodawania posiłku
    override fun onResume() {
        super.onResume()
        updateSelectedDate(selectedLocalDate)
    }

    private fun setupObservers(app: DaneGlobalne) {
        daneViewModel.wyswietlDodane.observe(this) { lista ->
            // Obliczamy sumy
            sumakcal = lista.sumOf { it.sumaKalorii ?: 0 }
            val sb = lista.sumOf { it.sumaBialek ?: 0.0 }
            val sw = lista.sumOf { it.sumaWeglowodanow ?: 0.0 }
            val st = lista.sumOf { it.sumaTluszczy ?: 0.0 }

            // Pobieramy świeży cel (na wypadek zmiany w profilu)
            val aktualnyCelKcal = app.celKalorii

            // 1. UI Główne (Karta na środku)
            val pozostalo = aktualnyCelKcal - sumakcal
            val tvPozostalo = findViewById<TextView>(R.id.pozostaloKcal)
            tvPozostalo.text = "$pozostalo kcal"

            if (pozostalo < 0) {
                tvPozostalo.setTextColor(getColor(R.color.red_400))
            } else {
                tvPozostalo.setTextColor(getColor(R.color.green_400))
            }

            val pasek = findViewById<ProgressBar>(R.id.pasekPostepuKcal)
            pasek.max = if (aktualnyCelKcal > 0) aktualnyCelKcal else 2000
            pasek.progress = sumakcal

            // 2. DOLNY PASEK
            findViewById<TextView>(R.id.sumaKaloriiText).text = "Kalorie\n$sumakcal / $aktualnyCelKcal"

            findViewById<TextView>(R.id.sumaBialekText).text =
                String.format(Locale.US, "B\n%.1f / %d", sb, app.celBialek)

            findViewById<TextView>(R.id.sumaWeglowodanowText).text =
                String.format(Locale.US, "W\n%.1f / %d", sw, app.celWeglowodanow)

            findViewById<TextView>(R.id.sumaTluszczyText).text =
                String.format(Locale.US, "T\n%.1f / %d", st, app.celTluszczy)

            odswiezPropozycje(pozostalo)
        }

        daneViewModel.wyswietlWykonane.observe(this) { lista ->
            val tvStatus = findViewById<TextView>(R.id.statusTreninguTekst)
            val ivStatus = findViewById<ImageView>(R.id.statusTreninguIkona)
            if (lista.isNotEmpty()) {
                tvStatus.text = "Trening wykonany! Dobra robota!"
                tvStatus.setTextColor(getColor(R.color.green_400))
                ivStatus.setColorFilter(getColor(R.color.green_400))
            } else {
                tvStatus.text = "Dzisiaj nie było jeszcze treningu"
                tvStatus.setTextColor(getColor(R.color.white))
                ivStatus.setColorFilter(getColor(R.color.white_50))
            }
        }
    }

    // Reszta funkcji bez zmian (odswiezPropozycje, syncDatabase, aktualizacjaDanychZPlikuOptymalnie, setupNavigation, przenies)

    private fun odswiezPropozycje(pozostalo: Int) {
        daneViewModel.wyswietlPrzepisy.observe(this) { przepisy ->
            val maxDopuszczalne = pozostalo * 1.05
            val propozycje = przepisy.filter {
                val k = it.kalorycznosc ?: 0
                k in 150..(maxDopuszczalne.toInt())
            }.shuffled().take(2)
            propozycjeAdapter.submitList(propozycje)
        }
    }

    private fun updateSelectedDate(newDate: LocalDate) {
        selectedLocalDate = newDate
        // Normalizujemy datę do 00:00:00 (początek dnia) - to jest kluczowe dla Room!
        val dateForRoom: Date = Date.from(selectedLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
        daneViewModel.setDateQuery(dateForRoom)
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

    suspend fun aktualizacjaDanychZPlikuOptymalnie(context: Context, db: BazaDanych) {
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
                                    val prod = Produkt(
                                        obj.getString("nazwa"),
                                        obj.getDouble("kalorycznosc").toInt(),
                                        obj.getDouble("bialka"),
                                        obj.getDouble("tluszcze"),
                                        obj.getDouble("weglowodany"),
                                        obj.getString("kodKreskowy")
                                    )
                                    val existing = dao.getProduktByBarcode(prod.kodKreskowy!!)
                                    if (existing != null) {
                                        prod.id = existing.id
                                        dao.updateProdukt(prod)
                                    } else {
                                        dao.insertProdukt(prod)
                                    }
                                } catch (e: Exception) { }
                            }
                        }
                        sharedPref.edit().putInt(PREF_DB_VER, fileVersion).apply()
                    }
                }
            } catch (e: Exception) { }
        }
    }

    private fun setupNavigation() {
        findViewById<ImageButton>(R.id.ProfilButton).setOnClickListener { przenies(ProfilActivity::class.java) }
        findViewById<ImageButton>(R.id.HomeButton).setOnClickListener { przenies(HomeActivity::class.java) }
        findViewById<ImageButton>(R.id.LodowkaButton).setOnClickListener { przenies(LodowkaActivity::class.java) }
        findViewById<ImageButton>(R.id.TreningButton).setOnClickListener { przenies(TreningActivity::class.java) }
        findViewById<ImageButton>(R.id.DietaButton).setOnClickListener { przenies(DietaActivity::class.java) }
    }

    fun przenies(Cel: Class<out Activity>) {
        startActivity(Intent(this, Cel))
    }
}