package com.example.aplikacjagit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikacjagit.adaptery.DodaneAdapter
import com.example.aplikacjagit.adaptery.ProduktAdapter
import com.example.aplikacjagit.room.DaneGlobalne
import com.example.aplikacjagit.room.DaneViewModel
import com.example.aplikacjagit.room.Dodane
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import kotlin.math.round

class DietaActivity : ComponentActivity() {

    private lateinit var ProfilButton: ImageButton
    private lateinit var HomeButton: ImageButton
    private lateinit var LodowkaButton: ImageButton
    private lateinit var TreningButton: ImageButton
    private lateinit var DietaButton: ImageButton

    private lateinit var DataDnia: TextView
    private lateinit var Wyszukaj: EditText
    private lateinit var DataLayout: LinearLayout
    private lateinit var WyszukiwanieLayout: LinearLayout
    private lateinit var DataWLewo: ImageButton
    private lateinit var DataWPrawo: ImageButton

    private lateinit var sumaKaloriiText: TextView
    private lateinit var sumaBialekText: TextView
    private lateinit var sumaWeglowodanowText: TextView
    private lateinit var sumaTluszczyText: TextView

    private lateinit var WstawButton: Button
    private lateinit var WstawioneButton: Button

    private lateinit var widokProdukty: RecyclerView
    private lateinit var widokDodane: LinearLayout
    private lateinit var sniadanie : RecyclerView
    private lateinit var obiad : RecyclerView
    private lateinit var kolacja : RecyclerView
    private lateinit var sniadanieDodaj : ImageButton
    private lateinit var obiadDodaj :  ImageButton
    private lateinit var kolacjaDodaj :  ImageButton

    private lateinit var produktAdapter: ProduktAdapter
    private lateinit var dodaneAdapter: DodaneAdapter

    private lateinit var daneViewModel: DaneViewModel

    // pole przechowujące aktualnie wybraną datę (LocalDate dla wygody liczenia dni)
    private var selectedLocalDate: LocalDate = LocalDate.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dieta)

        val danePreferencje = getSharedPreferences("preferencje", Context.MODE_PRIVATE)

        val app = application as DaneGlobalne
        var aktualnyUzytkownik = app.aktualnyUzytkownik

        daneViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application))[DaneViewModel::class.java]

        ProfilButton = findViewById(R.id.ProfilButton)
        HomeButton = findViewById(R.id.HomeButton)
        LodowkaButton = findViewById(R.id.LodowkaButton)
        TreningButton = findViewById(R.id.TreningButton)
        DietaButton = findViewById(R.id.DietaButton)
        DataDnia = findViewById(R.id.DataDnia)
        Wyszukaj = findViewById(R.id.Wyszukiwanie)
        WyszukiwanieLayout = findViewById(R.id.WyszukiwanieLayout)
        DataLayout = findViewById(R.id.DataLayout)
        WstawButton = findViewById(R.id.WstawButton)
        DataWLewo = findViewById(R.id.DataWLewo)
        DataWPrawo = findViewById(R.id.DataWPrawo)
        WstawioneButton = findViewById(R.id.WstawioneButton)

        sumaKaloriiText = findViewById(R.id.sumaKaloriiText)
        sumaBialekText = findViewById(R.id.sumaBialekText)
        sumaWeglowodanowText = findViewById(R.id.sumaWeglowodanowText)
        sumaTluszczyText = findViewById(R.id.sumaTluszczyText)

        widokProdukty = findViewById(R.id.widokProdukty)
        widokDodane = findViewById(R.id.widokDodane)
        sniadanie = findViewById(R.id.sniadanie)
        obiad = findViewById(R.id.obiad)
        kolacja = findViewById(R.id.kolacja)
        sniadanieDodaj = findViewById(R.id.sniadanieDodaj)
        obiadDodaj = findViewById(R.id.obiadDodaj)
        kolacjaDodaj = findViewById(R.id.kolacjaDodaj)

        val adapterSniadanie = DodaneAdapter { dodane -> daneViewModel.deleteDodane(dodane) }
        val adapterObiad = DodaneAdapter { dodane -> daneViewModel.deleteDodane(dodane) }
        val adapterKolacja = DodaneAdapter { dodane -> daneViewModel.deleteDodane(dodane) }

        var obecnaPora = 1
        updateSelectedDate(selectedLocalDate)

        produktAdapter = ProduktAdapter { produkt, gramy ->
            val produktId = produkt.id
            if (produktId == null) {
                android.widget.Toast.makeText(this, "Produkt nie ma id, najpierw zapisz produkt", android.widget.Toast.LENGTH_SHORT).show()
                return@ProduktAdapter
            }

            val sumaKalorii = kotlin.math.round((produkt.kalorycznosc ?: 0).toDouble() * gramy / 100.0).toInt()
            val sumaBialek = kotlin.math.round((produkt.bialka ?: 0).toDouble() * gramy / 100.0)
            val sumaTluszczy = kotlin.math.round((produkt.tluszcze ?: 0).toDouble() * gramy / 100.0)
            val sumaWeglowodanow = kotlin.math.round((produkt.weglowodany ?: 0).toDouble() * gramy / 100.0)

            // używamy selectedLocalDate (to jest data wybrana przez użytkownika)
            val dateForRoom: Date = Date.from(
                selectedLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
            )

            val dodane = Dodane(
                idProduktu = produktId,
                nazwa = produkt.nazwa,
                ilosc = gramy,
                sumaKalorii = sumaKalorii,
                sumaBialek = sumaBialek,
                sumaTluszczy = sumaTluszczy,
                sumaWeglowodanow = sumaWeglowodanow,
                data = dateForRoom,
                poraDnia = obecnaPora
            )

            daneViewModel.insertDodane(dodane)
            android.widget.Toast.makeText(this, "Dodano ${produkt.nazwa} — ${gramy}g (${sumaKalorii} kcal)", android.widget.Toast.LENGTH_SHORT).show()
        }
        // adapter który dodaje każdemu polu listener do usuwania
        dodaneAdapter = DodaneAdapter { produkt ->
            daneViewModel.deleteDodane(produkt)
        }

        // Adaptery do wyświetlania listy produktów
        widokProdukty.adapter = produktAdapter
        widokProdukty.layoutManager = LinearLayoutManager(this)

        sniadanie.adapter = adapterSniadanie
        sniadanie.layoutManager = LinearLayoutManager(this)

        obiad.adapter = adapterObiad
        obiad.layoutManager = LinearLayoutManager(this)

        kolacja.adapter = adapterKolacja
        kolacja.layoutManager = LinearLayoutManager(this)

        daneViewModel.szukajProdukty.observe(this) { lista ->
            produktAdapter.stworzProdukt(lista)
        }

        // TEN KODZIK WYSZUKUJE DODANE PRODUKTY I LICZY KALORIE
        var sumakalorii = 0.0
        var sumabialek = 0.0
        var sumaweglowodanow = 0.0
        var sumatluszczy = 0.0
        sumaKaloriiText.text = "Kalorie\n${sumakalorii} / ${app.celKalorii}"
        sumaBialekText.text = "B\n${sumabialek} / ${app.celBialek}"
        sumaWeglowodanowText.text = "W\n${sumaweglowodanow} / ${app.celWeglowodanow}"
        sumaTluszczyText.text = "T\n${sumatluszczy} / ${app.celTluszczy}"

        daneViewModel.wyswietlDodane.observe(this) { lista ->
            val sniadanieList = lista.filter { it.poraDnia == 1 }.toMutableList()
            val obiadList = lista.filter { it.poraDnia == 2 }.toMutableList()
            val kolacjaList = lista.filter { it.poraDnia == 3 }.toMutableList()

            sumakalorii = 0.0
            sumabialek = 0.0
            sumaweglowodanow = 0.0
            sumatluszczy = 0.0
            sumaKaloriiText.text = "Kalorie\n${sumakalorii} / ${app.celKalorii}"
            sumaBialekText.text = "B\n${sumabialek} / ${app.celBialek}"
            sumaWeglowodanowText.text = "W\n${sumaweglowodanow} / ${app.celWeglowodanow}"
            sumaTluszczyText.text = "T\n${sumatluszczy} / ${app.celTluszczy}"
            adapterSniadanie.stworzDodane(sniadanieList)
            adapterObiad.stworzDodane(obiadList)
            adapterKolacja.stworzDodane(kolacjaList)

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

        DataDnia.text = selectedLocalDate.toString()

        WstawButton.setOnClickListener {
            widokDodane.visibility = View.GONE
            widokProdukty.visibility = View.VISIBLE
            WyszukiwanieLayout.visibility = View.VISIBLE
            DataLayout.visibility = View.GONE
            WstawButton.isEnabled = false
            WstawioneButton.isEnabled = true
        }
        WstawioneButton.setOnClickListener {
            updateSelectedDate(selectedLocalDate)
            widokDodane.visibility = View.VISIBLE
            DataLayout.visibility = View.VISIBLE
            WyszukiwanieLayout.visibility = View.GONE
            widokProdukty.visibility = View.GONE
            WstawButton.isEnabled = true
            WstawioneButton.isEnabled = false
        }

        sniadanieDodaj.setOnClickListener {
            obecnaPora = 1
            widokDodane.visibility = View.GONE
            widokProdukty.visibility = View.VISIBLE
            WyszukiwanieLayout.visibility = View.VISIBLE
            DataLayout.visibility = View.GONE
            WstawButton.isEnabled = false
            WstawioneButton.isEnabled = true
        }

        obiadDodaj.setOnClickListener {
            obecnaPora = 2
            widokDodane.visibility = View.GONE
            widokProdukty.visibility = View.VISIBLE
            WyszukiwanieLayout.visibility = View.VISIBLE
            DataLayout.visibility = View.GONE
            WstawButton.isEnabled = false
            WstawioneButton.isEnabled = true
        }

        kolacjaDodaj.setOnClickListener {
            obecnaPora = 3
            widokDodane.visibility = View.GONE
            widokProdukty.visibility = View.VISIBLE
            WyszukiwanieLayout.visibility = View.VISIBLE
            DataLayout.visibility = View.GONE
            WstawButton.isEnabled = false
            WstawioneButton.isEnabled = true
        }

        // przyciski przesuwające datę
        DataWLewo.setOnClickListener {
            updateSelectedDate(selectedLocalDate.minusDays(1))
        }
        DataWPrawo.setOnClickListener {
            updateSelectedDate(selectedLocalDate.plusDays(1))
        }

        Wyszukaj.addTextChangedListener{ text ->
            daneViewModel.setQuery(text?.toString() ?: "")
        }

        ProfilButton.setOnClickListener { przenies(ProfilActivity::class.java)}
        HomeButton.setOnClickListener { przenies(HomeActivity::class.java)}
        LodowkaButton.setOnClickListener { przenies(LodowkaActivity::class.java)}
        TreningButton.setOnClickListener { przenies(TreningActivity::class.java)}
        DietaButton.setOnClickListener { przenies(DietaActivity::class.java)}
    }

    // pomocnicza funkcja aktualizująca widok i ViewModel po zmianie daty
    private fun updateSelectedDate(newDate: LocalDate) {
        selectedLocalDate = newDate
        // aktualizujemy tekst
        DataDnia.text = selectedLocalDate.toString()
        // konwertujemy LocalDate -> Date (start of day)
        val dateForRoom: Date = Date.from(
            selectedLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
        )
        // ustawiamy query w ViewModelie
        daneViewModel.setDateQuery(dateForRoom)
    }

    fun przenies(Cel : Class<out Activity>){
        val intent = Intent(this@DietaActivity, Cel)
        startActivity(intent)
    }
}
