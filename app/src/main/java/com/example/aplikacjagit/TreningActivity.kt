package com.example.aplikacjagit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.aplikacjagit.room.DaneGlobalne
import com.example.aplikacjagit.room.DaneViewModel
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

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

    private var selectedLocalDate: LocalDate = LocalDate.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.trening)

        val danePreferencje = getSharedPreferences("preferencje", Context.MODE_PRIVATE)

        val app = application as DaneGlobalne
        var aktualnyUzytkownik = app.aktualnyUzytkownik

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

        ProfilButton.setOnClickListener { przenies(ProfilActivity::class.java)}
        HomeButton.setOnClickListener { przenies(HomeActivity::class.java)}
        LodowkaButton.setOnClickListener { przenies(LodowkaActivity::class.java)}
        TreningButton.setOnClickListener { przenies(TreningActivity::class.java)}
        DietaButton.setOnClickListener { przenies(DietaActivity::class.java)}

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
        val intent = Intent(this@TreningActivity, Cel)
        startActivity(intent)
    }
}
