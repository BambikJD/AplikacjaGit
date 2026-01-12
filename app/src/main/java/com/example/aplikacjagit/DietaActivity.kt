package com.example.aplikacjagit

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikacjagit.adaptery.DodaneAdapter
import com.example.aplikacjagit.adaptery.ProduktAdapter
import com.example.aplikacjagit.room.*
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale // DODANO

class DietaActivity : ComponentActivity() {

    private lateinit var daneViewModel: DaneViewModel
    private var selectedLocalDate: LocalDate = LocalDate.now()
    private var obecnaPora = 1

    // Layouty
    private lateinit var widokDodane: View
    private lateinit var widokProdukty: RecyclerView
    private lateinit var widokDodawanieProduktuLayout: View
    private lateinit var DataLayout: View
    private lateinit var WyszukiwanieLayout: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dieta)

        daneViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application))[DaneViewModel::class.java]
        val app = application as DaneGlobalne

        // Inicjalizacja widoków
        widokDodane = findViewById(R.id.widokDodane)
        widokProdukty = findViewById(R.id.widokProdukty)
        widokDodawanieProduktuLayout = findViewById(R.id.widokDodawanieProduktuLayout)
        DataLayout = findViewById(R.id.DataLayout)
        WyszukiwanieLayout = findViewById(R.id.WyszukiwanieLayout)

        val sumaKcalText = findViewById<TextView>(R.id.sumaKaloriiText)
        val sumaBText = findViewById<TextView>(R.id.sumaBialekText)
        val sumaWText = findViewById<TextView>(R.id.sumaWeglowodanowText)
        val sumaTText = findViewById<TextView>(R.id.sumaTluszczyText)

        // Adaptery (onDeleteClick przekazuje obiekt do usunięcia)
        val adapterSniadanie = DodaneAdapter { d -> daneViewModel.deleteDodane(d) }
        val adapterObiad = DodaneAdapter { d -> daneViewModel.deleteDodane(d) }
        val adapterKolacja = DodaneAdapter { d -> daneViewModel.deleteDodane(d) }

        findViewById<RecyclerView>(R.id.sniadanie).apply { adapter = adapterSniadanie; layoutManager = LinearLayoutManager(this@DietaActivity) }
        findViewById<RecyclerView>(R.id.obiad).apply { adapter = adapterObiad; layoutManager = LinearLayoutManager(this@DietaActivity) }
        findViewById<RecyclerView>(R.id.kolacja).apply { adapter = adapterKolacja; layoutManager = LinearLayoutManager(this@DietaActivity) }

        val produktAdapter = ProduktAdapter { produkt ->
            pokazDialogGramy(produkt)
        }
        widokProdukty.apply { adapter = produktAdapter; layoutManager = LinearLayoutManager(this@DietaActivity) }

        // --- OBSERWATORY ---
        daneViewModel.wyswietlDodane.observe(this) { lista ->
            // Filtrowanie i aktualizacja list (naprawiony mismatch typów przez .toMutableList())
            adapterSniadanie.stworzDodane(lista.filter { it.poraDnia == 1 }.toMutableList())
            adapterObiad.stworzDodane(lista.filter { it.poraDnia == 2 }.toMutableList())
            adapterKolacja.stworzDodane(lista.filter { it.poraDnia == 3 }.toMutableList())

            // Obliczanie sum
            val sk = lista.sumOf { it.sumaKalorii ?: 0 }
            val sb = lista.sumOf { it.sumaBialek ?: 0.0 }
            val sw = lista.sumOf { it.sumaWeglowodanow ?: 0.0 }
            val st = lista.sumOf { it.sumaTluszczy ?: 0.0 }


            // Formuła zaokrąglająca do 1 miejsca po przecinku (np. B: 40.5 / 150)
            sumaKcalText.text = "Kalorie\n$sk / ${app.celKalorii}"
            sumaBText.text = String.format(Locale.US, "B\n%.1f / %d", sb, app.celBialek)
            sumaWText.text = String.format(Locale.US, "W\n%.1f / %d", sw, app.celWeglowodanow)
            sumaTText.text = String.format(Locale.US, "T\n%.1f / %d", st, app.celTluszczy)

            // Obsługa nowych pasków postępu
            findViewById<ProgressBar>(R.id.pbSumaKcal).apply {
                max = app.celKalorii
                progress = sk
            }
            findViewById<ProgressBar>(R.id.pbSumaBialka).apply {
                max = app.celBialek
                progress = sb.toInt()
            }
            findViewById<ProgressBar>(R.id.pbSumaWegle).apply {
                max = app.celWeglowodanow
                progress = sw.toInt()
            }
            findViewById<ProgressBar>(R.id.pbSumaTluszcze).apply {
                max = app.celTluszczy
                progress = st.toInt()
            }
        }

        daneViewModel.szukajProdukty.observe(this) { produktAdapter.stworzProdukt(it) }

        // --- LISTENERY POSIŁKÓW ---
        findViewById<ImageButton>(R.id.sniadanieDodaj).setOnClickListener { przełączNaSzukanie(1) }
        findViewById<ImageButton>(R.id.obiadDodaj).setOnClickListener { przełączNaSzukanie(2) }
        findViewById<ImageButton>(R.id.kolacjaDodaj).setOnClickListener { przełączNaSzukanie(3) }
        findViewById<ImageButton>(R.id.PowrotButton).setOnClickListener { przełączNaDziennik() }

        // Dodawanie Produktu do bazy (formularz)
        findViewById<ImageButton>(R.id.otworzDodawanieProduktu).setOnClickListener {
            widokProdukty.visibility = View.GONE
            widokDodawanieProduktuLayout.visibility = View.VISIBLE
        }

        findViewById<Button>(R.id.zapiszProduktButton).setOnClickListener {
            val nazwa = findViewById<EditText>(R.id.nowaNazwaProduktu).text.toString()
            val kcal = findViewById<EditText>(R.id.noweKcalProduktu).text.toString().toIntOrNull() ?: 0
            val b = findViewById<EditText>(R.id.noweBialko).text.toString().toDoubleOrNull() ?: 0.0
            val t = findViewById<EditText>(R.id.noweTluszcze).text.toString().toDoubleOrNull() ?: 0.0
            val w = findViewById<EditText>(R.id.noweWegle).text.toString().toDoubleOrNull() ?: 0.0

            if(nazwa.isNotEmpty()){
                daneViewModel.insertProdukt(Produkt(nazwa, kcal, b, t, w, ""))
                findViewById<EditText>(R.id.nowaNazwaProduktu).setText("")
                findViewById<EditText>(R.id.noweKcalProduktu).setText("")
                przełączNaSzukanie(obecnaPora)
                Toast.makeText(this, "Dodano produkt do bazy!", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<EditText>(R.id.Wyszukiwanie).addTextChangedListener { daneViewModel.setQuery(it.toString()) }

        // Przyciski daty
        findViewById<ImageButton>(R.id.DataWLewo).setOnClickListener { updateSelectedDate(selectedLocalDate.minusDays(1)) }
        findViewById<ImageButton>(R.id.DataWPrawo).setOnClickListener { updateSelectedDate(selectedLocalDate.plusDays(1)) }

        updateSelectedDate(selectedLocalDate)
        setupNawigacja()
    }

    private fun pokazDialogGramy(produkt: Produkt) {
        val builder = AlertDialog.Builder(this)
        val input = EditText(this)
        input.hint = "Ile gram?"
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        builder.setTitle(produkt.nazwa).setView(input)
        builder.setPositiveButton("Dodaj") { _, _ ->
            val gramy = input.text.toString().toIntOrNull() ?: 0
            if(gramy > 0) {
                val ratio = gramy / 100.0
                val dodane = Dodane(
                    idProduktu = produkt.id,
                    nazwa = produkt.nazwa,
                    ilosc = gramy,
                    sumaKalorii = ((produkt.kalorycznosc ?: 0) * ratio).toInt(),
                    sumaBialek = (produkt.bialka ?: 0.0) * ratio,
                    sumaTluszczy = (produkt.tluszcze ?: 0.0) * ratio,
                    sumaWeglowodanow = (produkt.weglowodany ?: 0.0) * ratio,
                    data = Date.from(selectedLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant()),
                    poraDnia = obecnaPora
                )
                daneViewModel.insertDodane(dodane)
                przełączNaDziennik()
            }
        }
        builder.setNegativeButton("Anuluj", null)
        builder.show()
    }

    private fun przełączNaSzukanie(pora: Int) {
        obecnaPora = pora
        widokDodane.visibility = View.GONE
        widokProdukty.visibility = View.VISIBLE
        widokDodawanieProduktuLayout.visibility = View.GONE
        DataLayout.visibility = View.GONE
        WyszukiwanieLayout.visibility = View.VISIBLE
    }

    private fun przełączNaDziennik() {
        widokDodane.visibility = View.VISIBLE
        widokProdukty.visibility = View.GONE
        widokDodawanieProduktuLayout.visibility = View.GONE
        DataLayout.visibility = View.VISIBLE
        WyszukiwanieLayout.visibility = View.GONE
    }

    private fun updateSelectedDate(newDate: LocalDate) {
        selectedLocalDate = newDate
        findViewById<TextView>(R.id.DataDnia).text = selectedLocalDate.toString()
        daneViewModel.setDateQuery(Date.from(selectedLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant()))
    }

    private fun setupNawigacja() {
        findViewById<ImageButton>(R.id.ProfilButton).setOnClickListener { przenies(ProfilActivity::class.java) }
        findViewById<ImageButton>(R.id.HomeButton).setOnClickListener { przenies(HomeActivity::class.java) }
        findViewById<ImageButton>(R.id.LodowkaButton).setOnClickListener { przenies(LodowkaActivity::class.java) }
        findViewById<ImageButton>(R.id.TreningButton).setOnClickListener { przenies(TreningActivity::class.java) }
    }

    fun przenies(Cel: Class<out Activity>) { startActivity(Intent(this, Cel)) }
}