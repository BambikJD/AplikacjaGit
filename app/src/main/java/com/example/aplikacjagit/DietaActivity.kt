package com.example.aplikacjagit

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
import com.example.aplikacjagit.room.DaneViewModel
import com.example.aplikacjagit.room.Dodane
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import kotlin.math.round

class DietaActivity : ComponentActivity() {

    private lateinit var ProfilButton: Button
    private lateinit var HomeButton: Button
    private lateinit var LodowkaButton: Button
    private lateinit var TreningButton: Button
    private lateinit var DietaButton: Button
    private lateinit var DataDnia: TextView
    private lateinit var Wyszukaj: EditText
    private lateinit var DataLayout: LinearLayout
    private lateinit var WyszukiwanieLayout: LinearLayout
    private lateinit var DataWLewo: ImageButton
    private lateinit var DataWPrawo: ImageButton

    private lateinit var WstawButton: Button
    private lateinit var WstawioneButton: Button

    private lateinit var widokProdukty: RecyclerView
    private lateinit var widokDodane: RecyclerView
    private lateinit var produktAdapter: ProduktAdapter
    private lateinit var dodaneAdapter: DodaneAdapter

    private lateinit var daneViewModel: DaneViewModel

    // pole przechowujące aktualnie wybraną datę (LocalDate dla wygody liczenia dni)
    private var selectedLocalDate: LocalDate = LocalDate.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dieta)

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

        widokProdukty = findViewById(R.id.widokProdukty)
        widokDodane = findViewById(R.id.widokDodane)

        // ustawiamy początkową datę i powiadamiamy ViewModel (konwersja w updateSelectedDate)
        updateSelectedDate(selectedLocalDate)

        produktAdapter = ProduktAdapter { produkt, gramy ->
            val produktId = produkt.id
            if (produktId == null) {
                android.widget.Toast.makeText(this, "Produkt nie ma id, najpierw zapisz produkt", android.widget.Toast.LENGTH_SHORT).show()
                return@ProduktAdapter
            }

            val sumaKalorii = kotlin.math.round((produkt.kalorycznosc ?: 0).toDouble() * gramy / 100.0).toInt()
            val sumaBialek = kotlin.math.round((produkt.bialka ?: 0).toDouble() * gramy / 100.0).toInt()
            val sumaTluszczy = kotlin.math.round((produkt.tluszcze ?: 0).toDouble() * gramy / 100.0).toInt()
            val sumaWeglowodanow = kotlin.math.round((produkt.weglowodany ?: 0).toDouble() * gramy / 100.0).toInt()

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
                data = dateForRoom
            )

            daneViewModel.insertDodane(dodane)
            android.widget.Toast.makeText(this, "Dodano ${produkt.nazwa} — ${gramy}g (${sumaKalorii} kcal)", android.widget.Toast.LENGTH_SHORT).show()
        }

        dodaneAdapter = DodaneAdapter { produkt ->
            daneViewModel.deleteDodane(produkt)
        }

        widokProdukty.adapter = produktAdapter
        widokProdukty.layoutManager = LinearLayoutManager(this)

        widokDodane.adapter = dodaneAdapter
        widokDodane.layoutManager = LinearLayoutManager(this)

        daneViewModel.szukajProdukty.observe(this) { lista ->
            produktAdapter.stworzProdukt(lista)
        }

        daneViewModel.wyswietlDodane.observe(this) { lista ->
            dodaneAdapter.stworzDodane(lista)
        }

        // pokaż aktualną datę (uaktualniane też w updateSelectedDate)
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
            // kiedy przechodzimy do "wstawione", upewnij się, że ViewModel ma aktualną datę
            updateSelectedDate(selectedLocalDate)
            widokDodane.visibility = View.VISIBLE
            DataLayout.visibility = View.VISIBLE
            WyszukiwanieLayout.visibility = View.GONE
            widokProdukty.visibility = View.GONE
            WstawButton.isEnabled = true
            WstawioneButton.isEnabled = false
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

        ProfilButton.setOnClickListener {
            startActivity(Intent(this@DietaActivity, ProfilActivity::class.java))
        }
        HomeButton.setOnClickListener {
            startActivity(Intent(this@DietaActivity, HomeActivity::class.java))
        }
        LodowkaButton.setOnClickListener {
            startActivity(Intent(this@DietaActivity, LodowkaActivity::class.java))
        }
        TreningButton.setOnClickListener {
            startActivity(Intent(this@DietaActivity, TreningActivity::class.java))
        }
        DietaButton.setOnClickListener {
            startActivity(Intent(this@DietaActivity, DietaActivity::class.java))
        }
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
}
