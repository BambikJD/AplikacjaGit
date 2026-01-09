package com.example.aplikacjagit

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikacjagit.adaptery.CwiczeniaAdapter
import com.example.aplikacjagit.adaptery.WykonaneAdapter
import com.example.aplikacjagit.adaptery.PlanyAdapter
import com.example.aplikacjagit.room.*
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale // DODANO dla zaokrąglania

class TreningActivity : ComponentActivity() {
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

    // Elementy zakładek
    private lateinit var widokDziennik: ConstraintLayout
    private lateinit var widokPlany: ConstraintLayout
    private lateinit var widokBazaCwiczen: ConstraintLayout
    private lateinit var tekstOpcji: TextView
    private lateinit var OpcjaWLewo: ImageButton
    private lateinit var OpcjaWPrawo: ImageButton

    // Elementy dodawania (Cwiczenie i Plan)
    private lateinit var widokDodawanieCwiczeniaLayout: ConstraintLayout
    private lateinit var widokDodawaniePlanuLayout: ConstraintLayout
    private lateinit var gornynapisDodajCwiczenie: LinearLayout
    private lateinit var gornynapisDodajPlan: LinearLayout
    private lateinit var gornynapisStandard: LinearLayout

    private lateinit var rvWykonane: RecyclerView
    private lateinit var rvBazaCwiczen: RecyclerView
    private lateinit var rvPlany: RecyclerView
    private lateinit var rvDodaneDoPlanu: RecyclerView
    private lateinit var rvBazaDoPlanu: RecyclerView

    private lateinit var szukajCwiczenia: EditText
    private lateinit var szukajDoPlanu: EditText

    private var numerOpcji = 0 // 0: Dziennik, 1: Plany, 2: Baza
    private var selectedLocalDate: LocalDate = LocalDate.now()
    private var ID_PLANU = 0

    private lateinit var planyAdapter: PlanyAdapter
    private lateinit var budowaniePlanuAdapter: WykonaneAdapter

    private var listaDodanychDoPlanu = mutableListOf<Wykonane>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.trening)

        val danePreferencje = getSharedPreferences("preferencje", Context.MODE_PRIVATE)
        val mainLayout = findViewById<ConstraintLayout>(R.id.mainLayout)
        val zapisanyKolor = danePreferencje.getInt("wybranyWybranyKolor", -1)
        if (zapisanyKolor != -1) {
            mainLayout.setBackgroundColor(zapisanyKolor)
        }

        val app = application as DaneGlobalne

        // --- INICJALIZACJA WIDOKÓW ---
        ProfilButton = findViewById(R.id.ProfilButton)
        HomeButton = findViewById(R.id.HomeButton)
        LodowkaButton = findViewById(R.id.LodowkaButton)
        TreningButton = findViewById(R.id.TreningButton)
        DietaButton = findViewById(R.id.DietaButton)

        sumaKaloriiText = findViewById(R.id.sumaKaloriiText)
        sumaBialekText = findViewById(R.id.sumaBialekText)
        sumaWeglowodanowText = findViewById(R.id.sumaWeglowodanowText)
        sumaTluszczyText = findViewById(R.id.sumaTluszczyText)

        widokDziennik = findViewById(R.id.widokDziennik)
        widokPlany = findViewById(R.id.widokPlany)
        widokBazaCwiczen = findViewById(R.id.widokBazaCwiczen)
        widokDodawanieCwiczeniaLayout = findViewById(R.id.widokDodawanieCwiczeniaLayout)
        widokDodawaniePlanuLayout = findViewById(R.id.widokDodawaniePlanuLayout)

        tekstOpcji = findViewById(R.id.tekstOpcji)
        OpcjaWLewo = findViewById(R.id.OpcjaWLewo)
        OpcjaWPrawo = findViewById(R.id.OpcjaWPrawo)

        gornynapisDodajCwiczenie = findViewById(R.id.gornynapisDodajCwiczenie)
        gornynapisDodajPlan = findViewById(R.id.gornynapisDodajPlan)
        gornynapisStandard = findViewById(R.id.zmienOpcje)

        rvWykonane = findViewById(R.id.rvWykonane)
        rvBazaCwiczen = findViewById(R.id.rvBazaCwiczen)
        rvPlany = findViewById(R.id.rvPlany)
        rvDodaneDoPlanu = findViewById(R.id.rvDodaneDoPlanu)
        rvBazaDoPlanu = findViewById(R.id.rvBazaDoPlanu)

        szukajCwiczenia = findViewById(R.id.szukajCwiczenia)
        szukajDoPlanu = findViewById(R.id.szukajDoPlanu)

        daneViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application))[DaneViewModel::class.java]

        // --- KONFIGURACJA ADAPTERÓW ---

        val wykonaneAdapter = WykonaneAdapter { item -> daneViewModel.deleteWykonane(item) }
        rvWykonane.adapter = wykonaneAdapter
        rvWykonane.layoutManager = LinearLayoutManager(this)

        val cwiczeniaAdapter = CwiczeniaAdapter { cw -> pokazDialogDodawania(cw, false) }
        rvBazaCwiczen.adapter = cwiczeniaAdapter
        rvBazaCwiczen.layoutManager = LinearLayoutManager(this)

        daneViewModel.getOstatniPlanId.observe(this) { id ->
            if(id != null) ID_PLANU = id
        }

        planyAdapter = PlanyAdapter { plan ->
            plan.listaCwiczen.forEachIndexed { index, cwId ->
                val dataTeraz = Date.from(selectedLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                val zaplanowaneSerie = plan.serie.getOrNull(index) ?: 0
                val zaplanowanePowt = plan.powtorzenia.getOrNull(index) ?: 0

                val noweWykonane = Wykonane(
                    idCwiczenia = cwId,
                    nazwa = plan.nazwyCwiczen.getOrNull(index) ?: "Ćwiczenie z planu",
                    serie = zaplanowaneSerie,
                    powtorzenia = zaplanowanePowt,
                    ciezar = 0.0,
                    data = dataTeraz
                )
                daneViewModel.insertWykonane(noweWykonane)
            }
            Toast.makeText(this, "Plan ${plan.nazwa} wczytany!", Toast.LENGTH_SHORT).show()
            numerOpcji = 0
            aktualizujWidok()
        }

        rvPlany.adapter = planyAdapter
        rvPlany.layoutManager = LinearLayoutManager(this)

        budowaniePlanuAdapter = WykonaneAdapter { item ->
            listaDodanychDoPlanu.remove(item)
            budowaniePlanuAdapter.stworzWykonane(listaDodanychDoPlanu.toList())
        }
        rvDodaneDoPlanu.adapter = budowaniePlanuAdapter
        rvDodaneDoPlanu.layoutManager = LinearLayoutManager(this)

        val bazaDoPlanuAdapter = CwiczeniaAdapter { cw -> pokazDialogDodawania(cw, true) }
        rvBazaDoPlanu.adapter = bazaDoPlanuAdapter
        rvBazaDoPlanu.layoutManager = LinearLayoutManager(this)

        // --- OBSERWATORY I LISTENERY ---

        daneViewModel.wyswietlPlany.observe(this) { planyAdapter.stworzPlany(it) }

        szukajCwiczenia.addTextChangedListener { daneViewModel.setCwiczenieQuery(it?.toString() ?: "") }
        szukajDoPlanu.addTextChangedListener { daneViewModel.setCwiczenieQuery(it?.toString() ?: "") }

        // POPRAWIONA LOGIKA PODLICZANIA MAKRO (ZAOKRĄGLANIE)
        daneViewModel.wyswietlDodane.observe(this) { lista ->
            var sumak = 0
            var sumab = 0.0
            var sumaw = 0.0
            var sumat = 0.0
            for(p in lista){
                sumak += p.sumaKalorii ?: 0
                sumab += p.sumaBialek ?: 0.0
                sumat += p.sumaTluszczy ?: 0.0
                sumaw += p.sumaWeglowodanow ?: 0.0
            }

            // Formatowanie do 1 miejsca po przecinku (identycznie jak w Dieta/Home)
            sumaKaloriiText.text = "Kalorie\n$sumak / ${app.celKalorii}"
            sumaBialekText.text = String.format(Locale.US, "B\n%.1f / %d", sumab, app.celBialek)
            sumaWeglowodanowText.text = String.format(Locale.US, "W\n%.1f / %d", sumaw, app.celWeglowodanow)
            sumaTluszczyText.text = String.format(Locale.US, "T\n%.1f / %d", sumat, app.celTluszczy)
        }

        daneViewModel.wyswietlWykonane.observe(this) { wykonaneAdapter.stworzWykonane(it) }
        daneViewModel.wyswietlCwiczenia.observe(this) {
            cwiczeniaAdapter.stworzCwiczenie(it)
            bazaDoPlanuAdapter.stworzCwiczenie(it)
        }

        OpcjaWLewo.setOnClickListener { numerOpcji = if (numerOpcji == 0) 2 else numerOpcji - 1; aktualizujWidok() }
        OpcjaWPrawo.setOnClickListener { numerOpcji = if (numerOpcji == 2) 0 else numerOpcji + 1; aktualizujWidok() }

        // Logika formularzy (bez zmian)
        findViewById<ImageButton>(R.id.otworzDodawanieCwiczenia).setOnClickListener {
            widokBazaCwiczen.visibility = View.GONE; gornynapisStandard.visibility = View.GONE
            widokDodawanieCwiczeniaLayout.visibility = View.VISIBLE; gornynapisDodajCwiczenie.visibility = View.VISIBLE
        }

        findViewById<ImageButton>(R.id.PowrotCwiczenieButton).setOnClickListener {
            widokDodawanieCwiczeniaLayout.visibility = View.GONE; gornynapisDodajCwiczenie.visibility = View.GONE
            widokBazaCwiczen.visibility = View.VISIBLE; gornynapisStandard.visibility = View.VISIBLE
        }

        findViewById<ImageButton>(R.id.zapiszCwiczenieButton).setOnClickListener {
            val n = findViewById<EditText>(R.id.nowaNazwaCwiczenia)
            val p = findViewById<EditText>(R.id.nowaPartiaCwiczenia)
            val o = findViewById<EditText>(R.id.nowyOpisCwiczenia)
            if (n.text.isNotEmpty()) {
                daneViewModel.insertCwiczenie(Cwiczenie(n.text.toString(), p.text.toString(), o.text.toString()))
                n.setText(""); p.setText(""); o.setText("")
                findViewById<ImageButton>(R.id.PowrotCwiczenieButton).performClick()
            }
        }

        findViewById<ImageButton>(R.id.dodajPlanPrzycisk).setOnClickListener {
            widokPlany.visibility = View.GONE; gornynapisStandard.visibility = View.GONE
            widokDodawaniePlanuLayout.visibility = View.VISIBLE; gornynapisDodajPlan.visibility = View.VISIBLE
        }

        findViewById<ImageButton>(R.id.PowrotPlanButton).setOnClickListener {
            widokDodawaniePlanuLayout.visibility = View.GONE; gornynapisDodajPlan.visibility = View.GONE
            widokPlany.visibility = View.VISIBLE; gornynapisStandard.visibility = View.VISIBLE
        }

        findViewById<ImageButton>(R.id.zapiszPlanButton).setOnClickListener {
            val nPlan = findViewById<EditText>(R.id.nowaNazwaPlanu)
            val oPlan = findViewById<EditText>(R.id.nowyOpisPlanu)
            if (nPlan.text.isNotEmpty() && listaDodanychDoPlanu.isNotEmpty()) {
                daneViewModel.insertPlan(Plan(nPlan.text.toString(), oPlan.text.toString()))
                listaDodanychDoPlanu.forEach {
                    daneViewModel.insertPlanCwiczenie(PlanCwiczenie(ID_PLANU + 1, it.idCwiczenia ?: 0, it.serie, it.powtorzenia))
                }
                listaDodanychDoPlanu.clear()
                budowaniePlanuAdapter.stworzWykonane(emptyList())
                nPlan.setText(""); oPlan.setText("")
                findViewById<ImageButton>(R.id.PowrotPlanButton).performClick()
            }
        }

        // Nawigacja (bez zmian)
        ProfilButton.setOnClickListener { przenies(ProfilActivity::class.java)}
        HomeButton.setOnClickListener { przenies(HomeActivity::class.java)}
        LodowkaButton.setOnClickListener { przenies(LodowkaActivity::class.java)}
        TreningButton.setOnClickListener { przenies(TreningActivity::class.java)}
        DietaButton.setOnClickListener { przenies(DietaActivity::class.java)}

        updateSelectedDate(selectedLocalDate)
    }

    private fun aktualizujWidok() {
        widokDziennik.visibility = View.GONE; widokPlany.visibility = View.GONE; widokBazaCwiczen.visibility = View.GONE
        when(numerOpcji) {
            0 -> { tekstOpcji.text = "D Z I E N N I K"; widokDziennik.visibility = View.VISIBLE }
            1 -> { tekstOpcji.text = "P L A N Y"; widokPlany.visibility = View.VISIBLE }
            2 -> { tekstOpcji.text = "B A Z A  Ć W I C Z E Ń"; widokBazaCwiczen.visibility = View.VISIBLE }
        }
    }

    private fun pokazDialogDodawania(cw: Cwiczenie, doPlanu: Boolean) {
        val builder = AlertDialog.Builder(this)
        val dialogLayout = layoutInflater.inflate(R.layout.dialog_dodaj_cwiczenie, null)
        val editSerie = dialogLayout.findViewById<EditText>(R.id.dialogSerie)
        val editPowt = dialogLayout.findViewById<EditText>(R.id.dialogPowt)
        val editCiezar = dialogLayout.findViewById<EditText>(R.id.dialogCiezar)

        builder.setView(dialogLayout)
        builder.setPositiveButton("Dodaj") { _, _ ->
            val s = editSerie.text.toString().toIntOrNull() ?: 0
            val p = editPowt.text.toString().toIntOrNull() ?: 0
            val c = editCiezar.text.toString().replace(",", ".").toDoubleOrNull() ?: 0.0

            if(doPlanu) {
                listaDodanychDoPlanu.add(Wykonane(cw.id, cw.nazwa, s, p, c, null))
                budowaniePlanuAdapter.stworzWykonane(listaDodanychDoPlanu.toList())
                Toast.makeText(this, "Dodano do planu", Toast.LENGTH_SHORT).show()
            } else {
                val date = Date.from(selectedLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                daneViewModel.insertWykonane(Wykonane(cw.id, cw.nazwa, s, p, c, date))
            }
        }
        builder.setNegativeButton("Anuluj", null).show()
    }

    private fun updateSelectedDate(newDate: LocalDate) {
        selectedLocalDate = newDate
        daneViewModel.setDateQuery(Date.from(selectedLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant()))
    }

    fun przenies(Cel : Class<out Activity>){
        startActivity(Intent(this, Cel))
    }
}