package com.example.aplikacjagit

import android.R.attr.text
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
import com.example.aplikacjagit.adaptery.LodowkaAdapter
import com.example.aplikacjagit.adaptery.ProduktAdapter
import com.example.aplikacjagit.adaptery.PrzepisyAdapter
import com.example.aplikacjagit.room.DAO
import com.example.aplikacjagit.room.DaneGlobalne
import com.example.aplikacjagit.room.DaneViewModel
import com.example.aplikacjagit.room.Dodane
import com.example.aplikacjagit.room.Lodowka
import com.example.aplikacjagit.room.Przepis
import com.example.aplikacjagit.room.PrzepisProdukt
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import kotlin.Int
import kotlin.String

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
    private lateinit var widokProduktyLayout: ConstraintLayout
    private lateinit var gornyNapisProdukty: LinearLayout
    private lateinit var widokProduktyLodowka: RecyclerView
    private lateinit var dodajProduktButton: ImageButton
    private lateinit var widokDodawanieProduktowLayout : ConstraintLayout
    private lateinit var widokProduktyDoLodowki: RecyclerView
    private lateinit var WyszukiwanieProduktowDoLodowki: EditText
    private lateinit var PowrotProduktButton: ImageButton
    private lateinit var gornynapisDodajProdukt: LinearLayout

    private lateinit var nazwa: EditText
    private lateinit var opis: EditText

    private lateinit var Wyszukaj: EditText

    private lateinit var daneViewModel: DaneViewModel

    private lateinit var produktAdapter: ProduktAdapter
    private lateinit var produktLodowkaAdapter : ProduktAdapter

    private var selectedLocalDate: LocalDate = LocalDate.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lodowka)

        var listaDodanychDoPrzepisu: MutableList<Dodane>  = mutableListOf<Dodane>()

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
        dodajProduktButton = findViewById(R.id.dodajProdukt)
        widokDodawanieProduktowLayout = findViewById(R.id.widokDodawanieProduktowLayout)
        widokProduktyDoLodowki = findViewById(R.id.widokProduktyDoLodowki)
        WyszukiwanieProduktowDoLodowki = findViewById(R.id.WyszukiwanieProduktowDoLodowki)
        PowrotProduktButton = findViewById(R.id.PowrotProduktButton)
        gornynapisDodajProdukt = findViewById(R.id.gornynapisDodajProdukt)
        widokProduktyLodowka = findViewById(R.id.widokProduktyLodowka)
        gornyNapisProdukty = findViewById(R.id.gornynapisProdukty)
        widokProduktyLayout = findViewById(R.id.widokProduktyLayout)

        nazwa = findViewById(R.id.nazwa)
        opis = findViewById(R.id.opis)

        daneViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application))[DaneViewModel::class.java]


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

        val LodowkaAdapter = LodowkaAdapter { produkt ->
            daneViewModel.deleteLodowka(produkt)
        }

        produktLodowkaAdapter = ProduktAdapter { produkt, gramy ->
            val produktId = produkt.id
            val lodowka = Lodowka(
                idProduktu = produktId,
                nazwa = produkt.nazwa,
                ilosc = gramy,
            )
            daneViewModel.insertLodowka(lodowka)
        }

        widokPrzepisy.adapter = przepisyAdapter
        widokPrzepisy.layoutManager = LinearLayoutManager(this)

        widokProduktyDoLodowki.adapter = produktLodowkaAdapter
        widokProduktyDoLodowki.layoutManager = LinearLayoutManager(this)

        widokProdukty.adapter = produktAdapter
        widokProdukty.layoutManager = LinearLayoutManager(this)

        widokDodane.adapter = adapterDodane
        widokDodane.layoutManager = LinearLayoutManager(this)

        widokProduktyLodowka.adapter = LodowkaAdapter
        widokProduktyLodowka.layoutManager = LinearLayoutManager(this)

        updateSelectedDate(selectedLocalDate)

        var sumakalorii = 0.0
        var sumabialek = 0.0
        var sumaweglowodanow = 0.0
        var sumatluszczy = 0.0

        sumaKaloriiText.text = "Kalorie\n${sumakalorii} / ${app.celKalorii}"
        sumaBialekText.text = "B\n${sumabialek} / ${app.celBialek}"
        sumaWeglowodanowText.text = "W\n${sumaweglowodanow} / ${app.celWeglowodanow}"
        sumaTluszczyText.text = "T\n${sumatluszczy} / ${app.celTluszczy}"

        daneViewModel.wyswietlPrzepisyZLodowki.observe(this){ lista ->
            przepisyAdapter.submitList(lista.toList())
        }

        daneViewModel.szukajProdukty.observe(this) { lista ->
            produktAdapter.stworzProdukt(lista)
            produktLodowkaAdapter.stworzProdukt(lista)
        }

        daneViewModel.wyswietlLodowka.observe(this){ lista ->
            LodowkaAdapter.stworzLodowka(lista)
        }

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

        WyszukiwanieProduktowDoLodowki.addTextChangedListener { text ->
            daneViewModel.setQuery(text?.toString() ?: "")
        }

        wrocPrzepisyButton.setOnClickListener {
            widokDodawanieLayout.visibility = View.GONE
            gornyNapisDodawanie.visibility = View.GONE

            gornyNapisProdukty.visibility = View.VISIBLE
            gornyNapisPrzepisy.visibility = View.VISIBLE
            widokProduktyLayout.visibility = View.VISIBLE
            widokPrzepisyLayout.visibility = View.VISIBLE
        }

        dodajPrzepisOknoButton.setOnClickListener {
            gornyNapisPrzepisy.visibility = View.GONE
            widokProduktyLayout.visibility = View.GONE
            gornyNapisProdukty.visibility = View.GONE
            widokPrzepisyLayout.visibility = View.GONE

            widokDodawanieLayout.visibility = View.VISIBLE
            gornyNapisDodawanie.visibility = View.VISIBLE
        }

        PowrotProduktButton.setOnClickListener {
            widokDodawanieProduktowLayout.visibility = View.GONE
            gornynapisDodajProdukt.visibility = View.GONE

            widokPrzepisyLayout.visibility = View.VISIBLE
            gornyNapisPrzepisy.visibility = View.VISIBLE
            widokProduktyLayout.visibility = View.VISIBLE
            gornyNapisProdukty.visibility = View.VISIBLE
        }

        dodajProduktButton.setOnClickListener {
            widokPrzepisyLayout.visibility = View.GONE
            gornyNapisPrzepisy.visibility = View.GONE
            gornyNapisProdukty.visibility = View.GONE
            widokProduktyLayout.visibility = View.GONE

            widokDodawanieProduktowLayout.visibility = View.VISIBLE
            gornynapisDodajProdukt.visibility = View.VISIBLE
        }

        var ID = 0
        daneViewModel.getOstatniPrzepisId.observe(this){ id ->
            if(id != null) {
                ID = id
            }
        }

        dodajPrzepisButton.setOnClickListener {
            var sumakcal = 0
            var sumab = 0.0
            var sumaw = 0.0
            var sumat = 0.0
            var waga = 0
            for(element in listaDodanychDoPrzepisu){
                if(element.sumaBialek!= null && element.sumaKalorii!= null && element.sumaTluszczy!= null && element.sumaWeglowodanow != null && element.ilosc != null) {
                    sumakcal += element.sumaKalorii
                    sumab += element.sumaBialek
                    sumaw += element.sumaWeglowodanow
                    sumat += element.sumaTluszczy
                    waga += element.ilosc
                }
                var przepisProdukt = PrzepisProdukt(0, 0, 0)
                if(waga != 0) {
                    if (element.idProduktu != null) {
                        przepisProdukt = PrzepisProdukt(
                            przepisId = ID + 1,
                            produktId = element.idProduktu,
                            iloscPotrzebna = element.ilosc
                        )
                    }
                }
                    daneViewModel.insertPrzepisProdukt(przepisProdukt)
                    val przepis = Przepis(
                        nazwa = nazwa.text.toString(),
                        opis = opis.text.toString(),
                        kalorycznosc = sumakcal / waga * 100,
                        bialka = sumab / waga * 100,
                        weglowodany = sumaw / waga * 100,
                        tluszcze = sumat / waga * 100
                    )

                    daneViewModel.insertPrzepis(przepis)

            }


            listaDodanychDoPrzepisu = mutableListOf<Dodane>()
            adapterDodane.stworzDodane(listaDodanychDoPrzepisu)
            opis.setText("")
            nazwa.setText("")
            widokDodawanieLayout.visibility = View.GONE
            gornyNapisDodawanie.visibility = View.GONE

            gornyNapisProdukty.visibility = View.VISIBLE
            gornyNapisPrzepisy.visibility = View.VISIBLE
            widokProduktyLayout.visibility = View.VISIBLE
            widokPrzepisyLayout.visibility = View.VISIBLE
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

