package com.example.aplikacjagit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.aplikacjagit.room.DaneGlobalne
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible

class ProfilActivity : ComponentActivity() {
    // Baza danych
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // tworzenie zmiennych elementów na stronie
    private lateinit var DaneLogowania: TextView
    private lateinit var WylogujButton: Button
    private lateinit var ZatwierdzButton: Button
    private lateinit var EdytujButton: Button
    private lateinit var EdytujImie: EditText
    private lateinit var EdytujNazwisko: EditText
    private lateinit var EdytujEmail: EditText
    private lateinit var EdytujTelefon: EditText
    private lateinit var EdytujAdres: EditText

    private lateinit var ProfilButton: ImageButton
    private lateinit var HomeButton: ImageButton
    private lateinit var LodowkaButton: ImageButton
    private lateinit var TreningButton: ImageButton
    private lateinit var DietaButton: ImageButton

    // Zmienne do obsługi zakładek i tła
    private lateinit var mainLayout: ConstraintLayout
    private lateinit var dane: LinearLayout
    private lateinit var preferencje: LinearLayout
    private lateinit var tlo: LinearLayout
    private lateinit var opcjaWybrana: TextView
    private var numerOpcji = 0 // 0: DANE, 1: PREFERENCJE, 2: TŁO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profil)

        val KEY_LOGIN = getString(R.string.KEY_LOGIN_STRING)
        val KEY_HASLO = getString(R.string.KEY_HASLO_STRING)
        val KEY_IS_LOGGED = getString(R.string.KEY_IS_LOGGED_STRING)

        // Inicjalizacja przycisków nawigacji
        ProfilButton = findViewById(R.id.ProfilButton)
        HomeButton = findViewById(R.id.HomeButton)
        LodowkaButton = findViewById(R.id.LodowkaButton)
        TreningButton = findViewById(R.id.TreningButton)
        DietaButton = findViewById(R.id.DietaButton)

        // Inicjalizacja elementów profilu
        DaneLogowania = findViewById(R.id.DaneLogowania)
        WylogujButton = findViewById(R.id.WylogujButton)
        EdytujButton = findViewById(R.id.EdytujButton)
        ZatwierdzButton = findViewById(R.id.ZatwierdzButton)

        EdytujImie = findViewById(R.id.EdytujImie)
        EdytujNazwisko = findViewById(R.id.EdytujNazwisko)
        EdytujEmail = findViewById(R.id.EdytujEmail)
        EdytujTelefon = findViewById(R.id.EdytujTelefon)
        EdytujAdres = findViewById(R.id.EdytujAdres)

        // Inicjalizacja elementów preferencji
        val waga = findViewById<EditText>(R.id.waga)
        val wzrost = findViewById<EditText>(R.id.wzrost)
        val wiek = findViewById<EditText>(R.id.wiek)
        val plec = findViewById<RadioGroup>(R.id.plec)
        val aktywnosc = findViewById<Spinner>(R.id.aktywnosc)
        val cel = findViewById<Spinner>(R.id.cel)
        val obliczButton = findViewById<Button>(R.id.zatwierdz)

        // Inicjalizacja obsługi zakładek (BEZ 'val', używamy zmiennych klasy)
        opcjaWybrana = findViewById(R.id.OpcjaWybrana)
        dane = findViewById(R.id.Dane)
        preferencje = findViewById(R.id.Preferencje)
        tlo = findViewById(R.id.Tlo)
        mainLayout = findViewById(R.id.mainLayout)

        val danePreferencje = getSharedPreferences("preferencje", Context.MODE_PRIVATE)
        val edycjaPreferencji = danePreferencje.edit()
        val app = application as DaneGlobalne
        var aktualnyUzytkownik = app.aktualnyUzytkownik

        // Zczytanie koloru tła na start
        val zapisanyKolor = danePreferencje.getInt("wybranyKolor", -1)
        if (zapisanyKolor != -1) {
            mainLayout.setBackgroundColor(zapisanyKolor)
        }

        // --- SCHEMATYCZNE WCZYTYWANIE DANYCH ---

        // 1. Najpierw adaptery dla Spinnerów
        aktywnosc.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("Siedzący", "Lekko aktywny", "Umiarkowany", "Aktywny", "Bardzo aktywny"))
        cel.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("Szybko schudnąć", "Schudnąć", "Utrzymać", "Przytyć", "Szybko przytyć"))

        // 2. Wypełnianie zakładki PREFERENCJE
        if (app.celKalorii != 0) {
            waga.setText(if(app.waga > 0) app.waga.toString() else "")
            wzrost.setText(if(app.wzrost > 0) app.wzrost.toString() else "")
            wiek.setText(if(app.wiek > 0) app.wiek.toString() else "")
            if (app.plec) plec.check(R.id.mezczyzna) else plec.check(R.id.kobieta)
            aktywnosc.setSelection(app.aktywnosc)
            cel.setSelection(app.cel)
        } else {
            aktywnosc.setSelection(2)
            cel.setSelection(2)
            plec.check(R.id.mezczyzna)
        }

        // 3. Wypełnianie zakładki DANE PROFILU
        if (aktualnyUzytkownik != null) {
            DaneLogowania.text = "Zalogowano jako: ${aktualnyUzytkownik.imie}"
            EdytujImie.setText(aktualnyUzytkownik.imie)
            EdytujNazwisko.setText(aktualnyUzytkownik.nazwisko)
            EdytujEmail.setText(aktualnyUzytkownik.email)
            EdytujTelefon.setText(aktualnyUzytkownik.telefon)
            EdytujAdres.setText(aktualnyUzytkownik.adres)
        }

        // --- LISTENERY ---

        obliczButton.setOnClickListener { policzKalorie() }
        ZatwierdzButton.setOnClickListener { zmienDane() }

        EdytujButton.setOnClickListener {
            EdytujImie.isEnabled = true
            EdytujNazwisko.isEnabled = true
            EdytujEmail.isEnabled = true
            EdytujTelefon.isEnabled = true
            EdytujAdres.isEnabled = true
            EdytujButton.isEnabled = false
            ZatwierdzButton.isEnabled = true
        }

        findViewById<ImageButton>(R.id.OpcjaWLewo).setOnClickListener {
            numerOpcji = if (numerOpcji == 0) 2 else numerOpcji - 1
            aktualizujWidok()
        }

        findViewById<ImageButton>(R.id.OpcjaWPrawo).setOnClickListener {
            numerOpcji = if (numerOpcji == 2) 0 else numerOpcji + 1
            aktualizujWidok()
        }

        // Przyciski Kolorów
        findViewById<Button>(R.id.color1).setOnClickListener { zastosujIZapiszKolor(androidx.core.content.ContextCompat.getColor(this, R.color.background_color), edycjaPreferencji) }
        findViewById<Button>(R.id.color2).setOnClickListener { zastosujIZapiszKolor(androidx.core.content.ContextCompat.getColor(this, R.color.grey_850), edycjaPreferencji) }
        findViewById<Button>(R.id.color3).setOnClickListener { zastosujIZapiszKolor(androidx.core.content.ContextCompat.getColor(this, R.color.grey_800), edycjaPreferencji) }
        findViewById<Button>(R.id.color4).setOnClickListener { zastosujIZapiszKolor(androidx.core.content.ContextCompat.getColor(this, R.color.grey_700), edycjaPreferencji) }
        findViewById<Button>(R.id.color5).setOnClickListener { zastosujIZapiszKolor(androidx.core.content.ContextCompat.getColor(this, R.color.grey_600), edycjaPreferencji) }

        ProfilButton.setOnClickListener { przenies(ProfilActivity::class.java)}
        HomeButton.setOnClickListener { przenies(HomeActivity::class.java)}
        LodowkaButton.setOnClickListener { przenies(LodowkaActivity::class.java)}
        TreningButton.setOnClickListener { przenies(TreningActivity::class.java)}
        DietaButton.setOnClickListener { przenies(DietaActivity::class.java)}

        WylogujButton.setOnClickListener {
            edycjaPreferencji.apply {
                putInt(KEY_IS_LOGGED, 0)
                putString(KEY_LOGIN, null)
                putString(KEY_HASLO, null)
            }.apply()
            app.wyczysc()
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    // --- FUNKCJE POMOCNICZE ---

    fun aktualizujWidok() {
        dane.visibility = View.GONE
        preferencje.visibility = View.GONE
        tlo.visibility = View.GONE

        when(numerOpcji) {
            0 -> { opcjaWybrana.text = "D A N E"; dane.visibility = View.VISIBLE }
            1 -> { opcjaWybrana.text = "P R E F E R E N C J E"; preferencje.visibility = View.VISIBLE }
            2 -> { opcjaWybrana.text = "T Ł O"; tlo.visibility = View.VISIBLE }
        }
    }

    private fun zastosujIZapiszKolor(kolor: Int, edytor: android.content.SharedPreferences.Editor) {
        mainLayout.setBackgroundColor(kolor)
        edytor.putInt("wybranyKolor", kolor).apply()
        Toast.makeText(this, "Zmieniono kolor", Toast.LENGTH_SHORT).show()
    }

    fun zmienDane() {
        val app = application as DaneGlobalne
        val aktualnyUzytkownik = app.aktualnyUzytkownik
        if(aktualnyUzytkownik != null) {
            EdytujImie.isEnabled = false
            EdytujNazwisko.isEnabled = false
            EdytujEmail.isEnabled = false
            EdytujTelefon.isEnabled = false
            EdytujAdres.isEnabled = false
            EdytujButton.isEnabled = true
            ZatwierdzButton.isEnabled = false

            val imie = EdytujImie.text.toString()
            val nazwisko = EdytujNazwisko.text.toString()
            val email = EdytujEmail.text.toString()
            val telefon = EdytujTelefon.text.toString()
            val adres = EdytujAdres.text.toString()

            val note = mutableMapOf<String, Any>(
                getString(R.string.KEY_IMIE_STRING) to imie,
                getString(R.string.KEY_NAZWISKO_STRING) to nazwisko,
                getString(R.string.KEY_EMAIL_STRING) to email,
                getString(R.string.KEY_TELEFON_STRING) to telefon,
                getString(R.string.KEY_ADRES_STRING) to adres
            )
            db.collection("Loginy").document("Dane logowania ${aktualnyUzytkownik.login}").update(note)

            aktualnyUzytkownik.imie = imie
            aktualnyUzytkownik.nazwisko = nazwisko
            aktualnyUzytkownik.email = email
            aktualnyUzytkownik.telefon = telefon
            aktualnyUzytkownik.adres = adres
            DaneLogowania.text = "Zalogowano jako: ${imie}"
            Toast.makeText(this, "Dane zaktualizowane", Toast.LENGTH_SHORT).show()
        }
    }

    fun policzKalorie() {
        val app = application as DaneGlobalne
        val danePreferencje = getSharedPreferences("preferencje", Context.MODE_PRIVATE)
        val edytor = danePreferencje.edit()

        try {
            val w = findViewById<EditText>(R.id.waga).text.toString().replace(',', '.').toDouble()
            val wz = findViewById<EditText>(R.id.wzrost).text.toString().replace(',', '.').toDouble()
            val wie = findViewById<EditText>(R.id.wiek).text.toString().toInt()
            val isM = findViewById<RadioGroup>(R.id.plec).checkedRadioButtonId == R.id.mezczyzna
            val aktIdx = findViewById<Spinner>(R.id.aktywnosc).selectedItemPosition
            val celIdx = findViewById<Spinner>(R.id.cel).selectedItemPosition

            val s = if (isM) 5.0 else -161.0
            val bmr = 10.0 * w + 6.25 * wz - 5.0 * wie + s
            val wsp = listOf(1.2, 1.375, 1.55, 1.725, 1.9)[aktIdx]
            val delta = listOf(-1000.0, -500.0, 0.0, 300.0, 700.0)[celIdx]

            val tdee = bmr * wsp
            var kcal = tdee + delta
            if (kcal < (if(isM) 1500.0 else 1200.0)) kcal = if(isM) 1500.0 else 1200.0

            edytor.apply {
                putInt("celKalorii", kcal.toInt())
                putFloat("waga", w.toFloat())
                putFloat("wzrost", wz.toFloat())
                putInt("wiek", wie)
                putBoolean("plec", isM)
                putInt("aktywnosc", aktIdx)
                putInt("cel", celIdx)
            }.apply()

            app.celKalorii = kcal.toInt()
            app.waga = w.toFloat()
            app.wzrost = wz.toFloat()
            app.wiek = wie
            app.plec = isM
            app.aktywnosc = aktIdx
            app.cel = celIdx

            Toast.makeText(this, "Nowy cel: ${kcal.toInt()} kcal", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Błędne dane", Toast.LENGTH_SHORT).show()
        }
    }

    fun przenies(Cel: Class<out Activity>) {
        startActivity(Intent(this, Cel))
    }
}