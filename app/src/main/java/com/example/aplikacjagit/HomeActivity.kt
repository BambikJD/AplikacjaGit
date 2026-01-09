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
    private lateinit var proprozycjeAdapter: PrzepisyAdapter
    private var sumakcal = 0
    private var celKcal = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        val app = application as DaneGlobalne
        daneViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application))[DaneViewModel::class.java]

        loadPreferences(app)
        celKcal = app.celKalorii

        // Inicjalizacja Adaptera dla propozycji przepisów
        proprozycjeAdapter = PrzepisyAdapter { przepis ->
            // Możesz tu dodać przejście do szczegółów przepisu
            Toast.makeText(this, "Polecane: ${przepis.nazwa}", Toast.LENGTH_SHORT).show()
        }
        findViewById<RecyclerView>(R.id.rvPropozycje).apply {
            adapter = proprozycjeAdapter
            layoutManager = LinearLayoutManager(this@HomeActivity)
        }

        setupObservers(app)
        setupNavigation()

        // Wywołaj synchronizację
        syncDatabase()
    }

    private fun setupObservers(app: DaneGlobalne) {
        // 1. Obserwator Diety (Podsumowanie + Kalkulator "Zostało")
        daneViewModel.wyswietlDodane.observe(this) { lista ->
            sumakcal = lista.sumOf { it.sumaKalorii ?: 0 }
            val sb = lista.sumOf { it.sumaBialek ?: 0.0 }
            val sw = lista.sumOf { it.sumaWeglowodanow ?: 0.0 }
            val st = lista.sumOf { it.sumaTluszczy ?: 0.0 }

            // Główny licznik na środku
            val pozostalo = celKcal - sumakcal
            val textPozostalo = findViewById<TextView>(R.id.pozostaloKcal)
            textPozostalo.text = "$pozostalo kcal"
            if(pozostalo < 0) textPozostalo.setTextColor(resources.getColor(R.color.red_400, null))

            // Dolny pasek (zaokrąglony)
            findViewById<TextView>(R.id.sumaKaloriiText).text = "Kalorie\n$sumakcal / $celKcal"
            findViewById<TextView>(R.id.sumaBialekText).text = String.format(Locale.US, "B\n%.1f / %d", sb, app.celBialek)
            findViewById<TextView>(R.id.sumaWeglowodanowText).text = String.format(Locale.US, "W\n%.1f / %d", sw, app.celWeglowodanow)
            findViewById<TextView>(R.id.sumaTluszczyText).text = String.format(Locale.US, "T\n%.1f / %d", st, app.celTluszczy)
        }

        // 2. Obserwator Treningu (Status)
        daneViewModel.wyswietlWykonane.observe(this) { lista ->
            val statusTekst = findViewById<TextView>(R.id.treningStatusTekst)
            val ikona = findViewById<ImageView>(R.id.treningStatusIkona)

            if (lista.isNotEmpty()) {
                statusTekst.text = "Brawo! Wykonałeś dzisiaj trening."
                statusTekst.setTextColor(resources.getColor(R.color.green_400, null))
                ikona.setColorFilter(resources.getColor(R.color.green_400, null))
            } else {
                statusTekst.text = "Nie zapomnij o dzisiejszym treningu!"
                statusTekst.setTextColor(resources.getColor(R.color.white, null))
                ikona.setColorFilter(resources.getColor(R.color.white_50, null))
            }
        }

        // 3. INTELIGENTNE PROPOZYCJE (Filtrowanie przepisów)
        daneViewModel.wyswietlPrzepisy.observe(this) { listaPrzepisow ->
            val limit = celKcal - sumakcal
            // Filtrujemy przepisy, które mają mniej kalorii niż nam zostało (minimum 100 kcal)
            val propozycje = listaPrzepisow.filter {
                val kcal = it.kalorycznosc ?: 0
                kcal in 100..limit
            }.shuffled().take(3) // Bierzemy 3 losowe pasujące przepisy

            proprozycjeAdapter.submitList(propozycje)
        }
    }

    private fun loadPreferences(app: DaneGlobalne) {
        val pref = getSharedPreferences("preferencje", Context.MODE_PRIVATE)
        app.celBialek = pref.getInt("celBialek", 0)
        app.celWeglowodanow = pref.getInt("celWeglowodanow", 0)
        app.celTluszczy = pref.getInt("celTluszczy", 0)
        app.celKalorii = pref.getInt("celKalorii", 0)
        app.waga = pref.getFloat("waga", 0.0F)
        app.wiek = pref.getInt("wiek", 0)
        app.wzrost = pref.getFloat("wzrost", 0.0F)

        // Kolor tła
        // Zdefiniowana przez "K"
        val zapisanyKolor = pref.getInt("wybranyKolor", -1)

// Użyta przez "K"
        if (zapisanyKolor != -1) findViewById<View>(R.id.mainLayout).setBackgroundColor(zapisanyKolor)
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