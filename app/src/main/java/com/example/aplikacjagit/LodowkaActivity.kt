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
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikacjagit.adaptery.DodaneAdapter
import com.example.aplikacjagit.adaptery.ProduktAdapter
import com.example.aplikacjagit.adaptery.PrzepisyAdapter
import com.example.aplikacjagit.room.DaneGlobalne
import com.example.aplikacjagit.room.DaneViewModel
import com.example.aplikacjagit.room.Dodane
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class LodowkaActivity : ComponentActivity() {
    private lateinit var ProfilButton: ImageButton
    private lateinit var HomeButton: ImageButton
    private lateinit var LodowkaButton: ImageButton
    private lateinit var TreningButton: ImageButton
    private lateinit var DietaButton: ImageButton

    private lateinit var sumaKaloriiText: TextView
    private lateinit var sumaBialekText: TextView
    private lateinit var sumaWeglowodanowText: TextView
    private lateinit var sumaTluszczyText: TextView

    private lateinit var widokPrzepisy: RecyclerView
    private lateinit var widokProdukty: RecyclerView
    private lateinit var widokDodane: RecyclerView
    private lateinit var widokPrzepisyLayout : ConstraintLayout
    private lateinit var widokDodawanieLayout : ConstraintLayout
    private lateinit var dodajPrzepisOknoButton : ImageButton
    private lateinit var dodajPrzepisButton : ImageButton
    private lateinit var wrocPrzepisyButton : ImageButton
    private lateinit var gornyNapisPrzepisy: LinearLayout
    private lateinit var gornyNapisDodawanie: LinearLayout

    private lateinit var Wyszukaj: EditText

    private lateinit var daneViewModel: DaneViewModel

    private lateinit var produktAdapter: ProduktAdapter

    private var selectedLocalDate: LocalDate = LocalDate.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lodowka)

        val listaDodanychDoPrzepisu: MutableList<Dodane>  = emptyList<Dodane>().toMutableList()

        val danePreferencje = getSharedPreferences("preferencje", Context.MODE_PRIVATE)

        val app = application as DaneGlobalne
        var aktualnyUzytkownik = app.aktualnyUzytkownik

        val przepisyAdapter = PrzepisyAdapter()

        ProfilButton =  findViewById(R.id.ProfilButton)
        HomeButton =  findViewById(R.id.HomeButton)
        LodowkaButton =  findViewById(R.id.LodowkaButton)
        TreningButton =  findViewById(R.id.TreningButton)
        DietaButton =  findViewById(R.id.DietaButton)

        sumaKaloriiText = findViewById(R.id.sumaKaloriiText)
        sumaBialekText = findViewById(R.id.sumaBialekText)
        sumaWeglowodanowText = findViewById(R.id.sumaWeglowodanowText)
        sumaTluszczyText = findViewById(R.id.sumaTluszczyText)

        widokPrzepisy = findViewById(R.id.widokPrzepisy)

        Wyszukaj = findViewById(R.id.Wyszukiwanie)
        widokProdukty = findViewById(R.id.widokProdukty)
        widokDodane = findViewById(R.id.widokDodane)
        widokPrzepisyLayout = findViewById(R.id.widokPrzepisyLayout)
        widokDodawanieLayout = findViewById(R.id.widokDodawaniePrzepisowLayout)
        dodajPrzepisButton = findViewById(R.id.dodajPrzepisButton)
        dodajPrzepisOknoButton = findViewById(R.id.dodajPrzepisOtworzOkno)
        wrocPrzepisyButton = findViewById(R.id.PowrotPrzepisButton)
        gornyNapisPrzepisy = findViewById(R.id.gornynapisPrzepisy)
        gornyNapisDodawanie = findViewById(R.id.gornynapisDodajPrzepis)

        daneViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application))[DaneViewModel::class.java]

        widokPrzepisy.adapter = przepisyAdapter
        widokPrzepisy.setHasFixedSize(true)

        daneViewModel.wyswietlPrzepisy.observe(this){ lista ->
            przepisyAdapter.submitList(lista.toList())
        }

        daneViewModel.szukajProdukty.observe(this) { lista ->
            produktAdapter.stworzProdukt(lista)
        }

        var adapterDodane = DodaneAdapter()
        adapterDodane = DodaneAdapter { dodane ->
            listaDodanychDoPrzepisu.remove(dodane)
            adapterDodane.stworzDodane(listaDodanychDoPrzepisu)
        }

        produktAdapter = ProduktAdapter { produkt, gramy ->
            val produktId = produkt.id
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
                poraDnia = 1
            )
            listaDodanychDoPrzepisu.add(dodane)
            android.widget.Toast.makeText(this, "Dodano ${produkt.nazwa} — ${gramy}g (${sumaKalorii} kcal)", android.widget.Toast.LENGTH_SHORT).show()
            adapterDodane.stworzDodane(listaDodanychDoPrzepisu)

        }

        adapterDodane.stworzDodane(listaDodanychDoPrzepisu)

        widokProdukty.adapter = produktAdapter
        widokProdukty.layoutManager = LinearLayoutManager(this)

        widokDodane.adapter = adapterDodane
        widokDodane.layoutManager = LinearLayoutManager(this)

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

        ProfilButton.setOnClickListener { przenies(ProfilActivity::class.java)}
        HomeButton.setOnClickListener { przenies(HomeActivity::class.java)}
        LodowkaButton.setOnClickListener { przenies(LodowkaActivity::class.java)}
        TreningButton.setOnClickListener { przenies(TreningActivity::class.java)}
        DietaButton.setOnClickListener { przenies(DietaActivity::class.java)}

        Wyszukaj.addTextChangedListener{ text ->
            daneViewModel.setQuery(text?.toString() ?: "")
        }

        wrocPrzepisyButton.setOnClickListener {
            widokPrzepisyLayout.visibility = View.VISIBLE
            widokDodawanieLayout.visibility = View.GONE
            gornyNapisPrzepisy.visibility = View.VISIBLE
            gornyNapisDodawanie.visibility = View.GONE
        }

        dodajPrzepisOknoButton.setOnClickListener {
            widokPrzepisyLayout.visibility = View.GONE
            widokDodawanieLayout.visibility = View.VISIBLE
            gornyNapisPrzepisy.visibility = View.GONE
            gornyNapisDodawanie.visibility = View.VISIBLE
        }

        dodajPrzepisButton.setOnClickListener {

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

    fun przenies(Cel : Class<out Activity>){
        val intent = Intent(this@LodowkaActivity, Cel)
        startActivity(intent)
    }
}

