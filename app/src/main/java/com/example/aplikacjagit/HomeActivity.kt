package com.example.aplikacjagit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import androidx.room.withTransaction
import com.example.aplikacjagit.room.BazaDanych
import com.example.aplikacjagit.room.DAO
import com.example.aplikacjagit.room.DaneGlobalne
import com.example.aplikacjagit.room.DaneViewModel
import com.example.aplikacjagit.room.Produkt
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
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

        // czytanie preferencji do zmiennych globalnych bo przy odpaleniu aplikacji odpala sie home
        app.celBialek = danePreferencje.getInt("celBialek", 0)
        app.celWeglowodanow = danePreferencje.getInt("celWeglowodanow", 0)
        app.celTluszczy = danePreferencje.getInt("celTluszczy", 0)
        app.celKalorii = danePreferencje.getInt("celKalorii", 0)
        app.waga = danePreferencje.getFloat("waga", 0.0F)
        app.wiek = danePreferencje.getInt("wiek", 0)
        app.wzrost = danePreferencje.getFloat("wzrost", 0.0F)
        app.cel = danePreferencje.getInt("cel", 0)
        app.aktywnosc = danePreferencje.getInt("aktywnosc", 0)
        app.plec = danePreferencje.getBoolean("plec", true)

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

        var sumakalorii = 0.0
        var sumabialek = 0.0
        var sumaweglowodanow = 0.0
        var sumatluszczy = 0.0

        sumaKaloriiText.text = "Kalorie\n${sumakalorii} / ${app.celKalorii}"
        sumaBialekText.text = "B\n${sumabialek} / ${app.celBialek}"
        sumaWeglowodanowText.text = "W\n${sumaweglowodanow} / ${app.celWeglowodanow}"
        sumaTluszczyText.text = "T\n${sumatluszczy} / ${app.celTluszczy}"

        daneViewModel.wyswietlDodane.observe(this) { lista ->
            sumakalorii = 0.0
            sumabialek = 0.0
            sumaweglowodanow = 0.0
            sumatluszczy = 0.0
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

        lifecycleScope.launch {
            try {
                // pobierz instancję DB (jeśli masz singleton)
                val db = BazaDanych.getInstance(applicationContext)

                // wykonaj synchronizację w tle (funkcja jest suspend)
                aktualizacjaDanychZPlikuOptymalnie(applicationContext, db)

                // po powrocie do UI (jesteśmy nadal w coroutine): powiadom usera
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HomeActivity, "Synchronizacja danych zakończona", Toast.LENGTH_SHORT).show()
                    Log.d("HomeActivity", "Sync: wykonano pomyślnie")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HomeActivity, "Błąd synchronizacji: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    Log.e("HomeActivity", "Błąd synchronizacji", e)
                }
            }
        }

        ProfilButton.setOnClickListener { przenies(ProfilActivity::class.java)}
        HomeButton.setOnClickListener { przenies(HomeActivity::class.java)}
        LodowkaButton.setOnClickListener { przenies(LodowkaActivity::class.java)}
        TreningButton.setOnClickListener { przenies(TreningActivity::class.java)}
        DietaButton.setOnClickListener { przenies(DietaActivity::class.java)}

    }

    suspend fun aktualizacjaDanychZPlikuOptymalnie(context: Context, db: BazaDanych){
        val TAG = "SyncFromFileOpt"
        val PREFS = "preferencje"
        val PREF_DB_VER = "db_version"
        val assetName = "output.jsonl"

        val sharedPref: SharedPreferences =
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val currentVersion = sharedPref.getInt(PREF_DB_VER, 0)

        withContext(Dispatchers.IO) {
            try {
                context.assets.open(assetName).use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        // 1) pierwsza linia = numer wersji (int)
                        val firstLine = reader.readLine()
                        if (firstLine == null) {
                            Log.w(TAG, "Plik $assetName jest pusty.")
                            return@withContext
                        }

                        val fileVersion = firstLine.trim().toIntOrNull()
                        if (fileVersion == null) {
                            Log.e(TAG, "Nieprawidłowy numer wersji w pierwszej linii: '$firstLine'")
                            return@withContext
                        }

                        if (fileVersion <= currentVersion) {
                            Log.i(TAG, "Brak aktualizacji — wersja pliku ($fileVersion) nie jest nowsza niż lokalna ($currentVersion).")
                            return@withContext
                        }

                        // 2) parsuj linie i synchronizuj ATOMOWO w transakcji
                        val dao = db.DAO()
                        var inserted = 0
                        var updated = 0
                        var skipped = 0
                        var lineNo = 1

                        // wykonujemy całą synchronizację w jednej transakcji Room
                        db.withTransaction {
                            var line: String?
                            while (reader.readLine().also { line = it } != null) {
                                lineNo++
                                val raw = line!!.trim()
                                if (raw.isEmpty()) continue

                                try {
                                    // zakładamy że żadne pole nie jest null — parsujemy bez sprawdzeń
                                    val obj = JSONObject(raw)

                                    val nazwa = obj.getString("nazwa")
                                    // kalorycznosc w pliku może być double, a w entity Int?
                                    val kalorycznosc = obj.getDouble("kalorycznosc").toInt()
                                    val bialka = obj.getDouble("bialka")      // entity: Double?
                                    val tluszcze = obj.getDouble("tluszcze")  // entity: Double?
                                    val weglowodany = obj.getDouble("weglowodany") // entity: Double?
                                    val kodKreskowy = obj.getString("kodKreskowy")

                                    // mapowanie literówki: JSON "kodKreskowy" -> entity.kodKrekowy
                                    val kodDlaBazy = kodKreskowy

                                    // sprawdź istnienie po kodzie
                                    val existing = dao.getProduktByBarcode(kodDlaBazy)

                                    if (existing != null) {
                                        // update — zachowaj id
                                        val updatedProd = Produkt(
                                            nazwa = nazwa,
                                            kalorycznosc = kalorycznosc,
                                            bialka = bialka,
                                            tluszcze = tluszcze,
                                            weglowodany = weglowodany,
                                            kodKreskowy = kodDlaBazy
                                        ).also { it.id = existing.id }
                                        dao.updateProdukt(updatedProd)
                                        updated++
                                    } else {
                                        val newProd = Produkt(
                                            nazwa = nazwa,
                                            kalorycznosc = kalorycznosc,
                                            bialka = bialka,
                                            tluszcze = tluszcze,
                                            weglowodany = weglowodany,
                                            kodKreskowy = kodDlaBazy
                                        )
                                        dao.insertProdukt(newProd)
                                        inserted++
                                    }
                                } catch (e: Exception) {
                                    // w try/catch tylko zapisujemy info i kontynuujemy, ale nie sprawdzamy nulli
                                    Log.e(TAG, "Linia $lineNo - błąd parsowania/insertu (pomijam): ${e.localizedMessage}")
                                    skipped++
                                }
                            } // koniec pętli linii
                        } // koniec transakcji

                        // 3) zapisz nową wersję
                        sharedPref.edit().putInt(PREF_DB_VER, fileVersion).apply()

                        Log.i(TAG, "Synchronizacja zakończona. Insert: $inserted, Update: $updated, Skipped: $skipped, Nowa wersja: $fileVersion")
                    } // reader.use
                } // inputStream.use
            } catch (e: Exception) {
                Log.e(TAG, "Błąd przetwarzania pliku $assetName", e)
            }
        } // withContext
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

    fun przenies(Cel : Class<out Activity>){
        val intent = Intent(this@HomeActivity, Cel)
        startActivity(intent)
    }
}
