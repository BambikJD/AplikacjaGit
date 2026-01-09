package com.example.aplikacjagit

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikacjagit.adaptery.*
import com.example.aplikacjagit.room.*
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale

class LodowkaActivity : ComponentActivity() {
    private lateinit var daneViewModel: DaneViewModel
    private var selectedLocalDate: LocalDate = LocalDate.now()
    private var listaDodanychDoPrzepisu = mutableListOf<Dodane>()
    private var ID_PRZEPISU = 0
    private var numerOpcji = 0 // 0: Lodówka, 1: Wszystkie przepisy

    // Elementy widoku
    private lateinit var widokPrzepisyLayout: ConstraintLayout
    private lateinit var widokDodawanieLayout: ConstraintLayout
    private lateinit var widokProduktyLayout: ConstraintLayout
    private lateinit var widokDodawanieProduktowLayout: ConstraintLayout
    private lateinit var widokWszystkiePrzepisyLayout: ConstraintLayout
    private lateinit var tekstOpcji: TextView
    private lateinit var OpcjaWLewo: ImageButton
    private lateinit var OpcjaWPrawo: ImageButton

    private lateinit var adapterDodane: DodaneAdapter
    private lateinit var lodowkaAdapter: LodowkaAdapter
    private lateinit var produktAdapter: ProduktAdapter
    private lateinit var produktLodowkaAdapter: ProduktAdapter
    private lateinit var przepisyAdapter: PrzepisyAdapter
    private lateinit var wszystkiePrzepisyAdapter: PrzepisyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lodowka)

        val danePreferencje = getSharedPreferences("preferencje", Context.MODE_PRIVATE)
        val mainLayout = findViewById<ConstraintLayout>(R.id.mainLayout)
        val zapisanyKolor = danePreferencje.getInt("wybranyKolor", -1)
        if (zapisanyKolor != -1) mainLayout.setBackgroundColor(zapisanyKolor)

        val app = application as DaneGlobalne
        daneViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application))[DaneViewModel::class.java]

        initViews()
        setupAdapters()
        setupObservers(app)
        setupClickListeners()
        setupNavigation()

        // Ustawienie widoku startowego
        aktualizujWidokGłówny()
    }

    private fun setupAdapters() {
        adapterDodane = DodaneAdapter { item ->
            listaDodanychDoPrzepisu.remove(item)
            adapterDodane.stworzDodane(listaDodanychDoPrzepisu.toList())
        }
        findViewById<RecyclerView>(R.id.widokDodane).apply {
            adapter = adapterDodane
            layoutManager = LinearLayoutManager(this@LodowkaActivity)
        }

        produktAdapter = ProduktAdapter { produkt -> pokazDialogGramy(produkt, "PRZEPIS") }
        findViewById<RecyclerView>(R.id.widokProdukty).apply {
            adapter = produktAdapter
            layoutManager = LinearLayoutManager(this@LodowkaActivity)
        }

        produktLodowkaAdapter = ProduktAdapter { produkt -> pokazDialogGramy(produkt, "LODOWKA") }
        findViewById<RecyclerView>(R.id.widokProduktyDoLodowki).apply {
            adapter = produktLodowkaAdapter
            layoutManager = LinearLayoutManager(this@LodowkaActivity)
        }

        lodowkaAdapter = LodowkaAdapter { item -> daneViewModel.deleteLodowka(item) }
        findViewById<RecyclerView>(R.id.widokProduktyLodowka).apply {
            adapter = lodowkaAdapter
            layoutManager = LinearLayoutManager(this@LodowkaActivity)
        }

        przepisyAdapter = PrzepisyAdapter { /* szczegóły */ }
        findViewById<RecyclerView>(R.id.widokPrzepisy).apply {
            adapter = przepisyAdapter
            layoutManager = LinearLayoutManager(this@LodowkaActivity)
        }

        wszystkiePrzepisyAdapter = PrzepisyAdapter { /* szczegóły */ }
        findViewById<RecyclerView>(R.id.widokWszystkiePrzepisy).apply {
            adapter = wszystkiePrzepisyAdapter
            layoutManager = LinearLayoutManager(this@LodowkaActivity)
        }
    }

    private fun setupObservers(app: DaneGlobalne) {
        daneViewModel.wyswietlPrzepisyZLodowki.observe(this) { przepisyAdapter.submitList(it) }
        daneViewModel.wyswietlPrzepisy.observe(this) { wszystkiePrzepisyAdapter.submitList(it) }
        daneViewModel.wyswietlLodowka.observe(this) { lodowkaAdapter.stworzLodowka(it) }
        daneViewModel.szukajProdukty.observe(this) {
            produktAdapter.stworzProdukt(it)
            produktLodowkaAdapter.stworzProdukt(it)
        }
        daneViewModel.getOstatniPrzepisId.observe(this) { id -> if (id != null) ID_PRZEPISU = id }

        daneViewModel.wyswietlDodane.observe(this) { lista ->
            val sk = lista.sumOf { it.sumaKalorii ?: 0 }
            val sb = lista.sumOf { it.sumaBialek ?: 0.0 }
            val sw = lista.sumOf { it.sumaWeglowodanow ?: 0.0 }
            val st = lista.sumOf { it.sumaTluszczy ?: 0.0 }

            findViewById<TextView>(R.id.sumaKaloriiText).text = "Kalorie\n$sk / ${app.celKalorii}"
            findViewById<TextView>(R.id.sumaBialekText).text = String.format(Locale.US, "B\n%.1f / %d", sb, app.celBialek)
            findViewById<TextView>(R.id.sumaWeglowodanowText).text = String.format(Locale.US, "W\n%.1f / %d", sw, app.celWeglowodanow)
            findViewById<TextView>(R.id.sumaTluszczyText).text = String.format(Locale.US, "T\n%.1f / %d", st, app.celTluszczy)
        }
    }

    private fun setupClickListeners() {
        // Logika cyklicznych strzałek
        OpcjaWLewo.setOnClickListener {
            numerOpcji = if (numerOpcji == 0) 1 else 0
            aktualizujWidokGłówny()
        }
        OpcjaWPrawo.setOnClickListener {
            numerOpcji = if (numerOpcji == 1) 0 else 1
            aktualizujWidokGłówny()
        }

        // Reszta listenerów dla przycisków dodawania (wyłączają widoki główne)
        findViewById<ImageButton>(R.id.dodajPrzepisOtworzOkno).setOnClickListener { switchLayouts(recipeAdd = true) }
        findViewById<ImageButton>(R.id.PowrotPrzepisButton).setOnClickListener { switchLayouts(recipeAdd = false) }
        findViewById<ImageButton>(R.id.dodajProdukt).setOnClickListener { switchLayouts(productAdd = true) }
        findViewById<ImageButton>(R.id.PowrotProduktButton).setOnClickListener { switchLayouts(productAdd = false) }

        // Zapisywanie przepisu
        findViewById<ImageButton>(R.id.dodajPrzepisButton).setOnClickListener {
            val nazwaET = findViewById<EditText>(R.id.etNazwaPrzepisu)
            val opisET = findViewById<EditText>(R.id.etOpisPrzepisu)
            if (nazwaET.text.isNotEmpty() && listaDodanychDoPrzepisu.isNotEmpty()) {
                val wagaTotal = listaDodanychDoPrzepisu.sumOf { it.ilosc ?: 0 }
                val kcalTotal = listaDodanychDoPrzepisu.sumOf { it.sumaKalorii ?: 0 }
                val bTotal = listaDodanychDoPrzepisu.sumOf { it.sumaBialek ?: 0.0 }
                val wTotal = listaDodanychDoPrzepisu.sumOf { it.sumaWeglowodanow ?: 0.0 }
                val tTotal = listaDodanychDoPrzepisu.sumOf { it.sumaTluszczy ?: 0.0 }

                val przepis = Przepis(
                    nazwaET.text.toString(), opisET.text.toString(),
                    (kcalTotal.toDouble() / wagaTotal * 100).toInt(),
                    (bTotal / wagaTotal * 100), (wTotal / wagaTotal * 100), (tTotal / wagaTotal * 100)
                )
                daneViewModel.insertPrzepis(przepis)

                listaDodanychDoPrzepisu.forEach {
                    daneViewModel.insertPrzepisProdukt(PrzepisProdukt(ID_PRZEPISU + 1, it.idProduktu ?: 0, it.ilosc))
                }

                listaDodanychDoPrzepisu.clear()
                adapterDodane.stworzDodane(emptyList())
                nazwaET.setText(""); opisET.setText("")
                switchLayouts(recipeAdd = false)
                Toast.makeText(this, "Przepis zapisany!", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<EditText>(R.id.Wyszukiwanie).addTextChangedListener { daneViewModel.setQuery(it?.toString() ?: "") }
        findViewById<EditText>(R.id.WyszukiwanieProduktowDoLodowki).addTextChangedListener { daneViewModel.setQuery(it?.toString() ?: "") }
    }

    private fun aktualizujWidokGłówny() {
        // Reset widoczności formularzy
        widokDodawanieLayout.visibility = View.GONE
        widokDodawanieProduktowLayout.visibility = View.GONE
        findViewById<View>(R.id.gornynapisDodajPrzepis).visibility = View.GONE
        findViewById<View>(R.id.gornynapisDodajProdukt).visibility = View.GONE

        when (numerOpcji) {
            0 -> {
                tekstOpcji.text = "T W O J A   L O D Ó W K A"
                widokPrzepisyLayout.visibility = View.VISIBLE
                widokProduktyLayout.visibility = View.VISIBLE
                widokWszystkiePrzepisyLayout.visibility = View.GONE
            }
            1 -> {
                tekstOpcji.text = "W S Z Y S T K I E   P R Z E P I S Y"
                widokPrzepisyLayout.visibility = View.GONE
                widokProduktyLayout.visibility = View.GONE
                widokWszystkiePrzepisyLayout.visibility = View.VISIBLE
            }
        }
    }

    private fun switchLayouts(recipeAdd: Boolean = false, productAdd: Boolean = false) {
        // Jeśli zamykamy formularz, wracamy do widoku zależnego od numerOpcji
        if (!recipeAdd && !productAdd) {
            aktualizujWidokGłówny()
            return
        }

        // Ukrywamy widoki główne przed pokazaniem formularza
        widokPrzepisyLayout.visibility = View.GONE
        widokProduktyLayout.visibility = View.GONE
        widokWszystkiePrzepisyLayout.visibility = View.GONE

        if (recipeAdd) {
            widokDodawanieLayout.visibility = View.VISIBLE
            findViewById<View>(R.id.gornynapisDodajPrzepis).visibility = View.VISIBLE
        } else if (productAdd) {
            widokDodawanieProduktowLayout.visibility = View.VISIBLE
            findViewById<View>(R.id.gornynapisDodajProdukt).visibility = View.VISIBLE
        }
    }

    private fun initViews() {
        widokPrzepisyLayout = findViewById(R.id.widokPrzepisyLayout)
        widokDodawanieLayout = findViewById(R.id.widokDodawaniePrzepisowLayout)
        widokProduktyLayout = findViewById(R.id.widokProduktyLayout)
        widokDodawanieProduktowLayout = findViewById(R.id.widokDodawanieProduktowLayout)
        widokWszystkiePrzepisyLayout = findViewById(R.id.widokWszystkiePrzepisyLayout)
        tekstOpcji = findViewById(R.id.tekstOpcji)
        OpcjaWLewo = findViewById(R.id.OpcjaWLewo)
        OpcjaWPrawo = findViewById(R.id.OpcjaWPrawo)
    }

    private fun pokazDialogGramy(produkt: Produkt, target: String) {
        val builder = AlertDialog.Builder(this)
        val input = EditText(this).apply {
            hint = "Ilość (g/szt)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        builder.setTitle(produkt.nazwa).setView(input)
        builder.setPositiveButton("Dodaj") { _, _ ->
            val gramy = input.text.toString().toIntOrNull() ?: 0
            if (gramy > 0) {
                if (target == "PRZEPIS") {
                    val ratio = gramy / 100.0
                    listaDodanychDoPrzepisu.add(Dodane(
                        produkt.id, produkt.nazwa, gramy, null, 0,
                        ((produkt.kalorycznosc ?: 0) * ratio).toInt(),
                        (produkt.bialka ?: 0.0) * ratio,
                        (produkt.weglowodany ?: 0.0) * ratio,
                        (produkt.tluszcze ?: 0.0) * ratio
                    ))
                    adapterDodane.stworzDodane(listaDodanychDoPrzepisu.toList())
                } else {
                    daneViewModel.insertLodowka(Lodowka(produkt.id, gramy, produkt.nazwa))
                    switchLayouts(productAdd = false)
                }
            }
        }
        builder.show()
    }

    private fun setupNavigation() {
        findViewById<ImageButton>(R.id.ProfilButton).setOnClickListener { przenies(ProfilActivity::class.java) }
        findViewById<ImageButton>(R.id.HomeButton).setOnClickListener { przenies(HomeActivity::class.java) }
        findViewById<ImageButton>(R.id.TreningButton).setOnClickListener { przenies(TreningActivity::class.java) }
        findViewById<ImageButton>(R.id.DietaButton).setOnClickListener { przenies(DietaActivity::class.java) }
    }

    fun przenies(Cel: Class<out Activity>) { startActivity(Intent(this, Cel)) }
}