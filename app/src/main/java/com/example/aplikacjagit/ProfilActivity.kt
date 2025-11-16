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
import androidx.core.view.isVisible

class ProfilActivity : ComponentActivity() {
    // Baza danych
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    // tworzenie zmiennych elemntow na stronie
    private lateinit var PowrotButton: Button
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
    // zaczecie aktywnosci
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.profil)
        // to jak nazywają się zmienne te w globalnych, po to żeby nie trzeba było za każdym razem tego pisać, ale nie jest to konieczne
        val KEY_ADRES = getString(R.string.KEY_ADRES_STRING)
        val KEY_EMAIL = getString(R.string.KEY_EMAIL_STRING)
        val KEY_TELEFON = getString(R.string.KEY_TELEFON_STRING)
        val KEY_LOGIN = getString(R.string.KEY_LOGIN_STRING) // klucze do danych chwilowych lokalnych z klasiIFunkcje
        val KEY_HASLO = getString(R.string.KEY_HASLO_STRING)
        val KEY_IMIE = getString(R.string.KEY_IMIE_STRING)
        val KEY_NAZWISKO = getString(R.string.KEY_NAZWISKO_STRING)
        val KEY_IS_LOGGED = getString(R.string.KEY_IS_LOGGED_STRING)
        //zczytanie id elementow ze strony do zmiennych
        ProfilButton =  findViewById(R.id.ProfilButton)
        HomeButton =  findViewById(R.id.HomeButton)
        LodowkaButton =  findViewById(R.id.LodowkaButton)
        TreningButton =  findViewById(R.id.TreningButton)
        DietaButton =  findViewById(R.id.DietaButton)

        PowrotButton =  findViewById(R.id.PowrotButton)
        DaneLogowania = findViewById(R.id.DaneLogowania)
        WylogujButton = findViewById(R.id.WylogujButton)
        EdytujButton = findViewById(R.id.EdytujButton)
        ZatwierdzButton = findViewById(R.id.ZatwierdzButton)

        EdytujImie = findViewById(R.id.EdytujImie)
        EdytujNazwisko = findViewById(R.id.EdytujNazwisko)
        EdytujEmail = findViewById(R.id.EdytujEmail)
        EdytujTelefon = findViewById(R.id.EdytujTelefon)
        EdytujAdres = findViewById(R.id.EdytujAdres)

        var waga = findViewById<EditText>(R.id.waga)
        var wzrost = findViewById<EditText>(R.id.wzrost)
        var wiek = findViewById<EditText>(R.id.wiek)
        var plec = findViewById<RadioGroup>(R.id.plec)
        var aktywnosc = findViewById<Spinner>(R.id.aktywnosc)
        var cel = findViewById<Spinner>(R.id.cel)
        var obliczButton = findViewById<Button>(R.id.zatwierdz)

        val opcjaWPrawo = findViewById<ImageButton>(R.id.OpcjaWPrawo)
        val opcjaWLewo = findViewById<ImageButton>(R.id.OpcjaWLewo)
        val opcjaWybrana = findViewById<TextView>(R.id.OpcjaWybrana)
        val dane = findViewById<LinearLayout>(R.id.Dane)
        val preferencje = findViewById<LinearLayout>(R.id.Preferencje)

        val danePreferencje = getSharedPreferences("preferencje", Context.MODE_PRIVATE)
        val edycjaPreferencji = danePreferencje.edit()

        // zczytanie danych globalnych do zmiennej
        val app = application as DaneGlobalne
        var aktualnyUzytkownik = app.aktualnyUzytkownik

        // glowny algorytm liczacy kalorie, ten algorytm robil czat z neta, potem przypisanie tylko do zmiennych globalnych i preferencji
        obliczButton.setOnClickListener { policzKalorie() }
        // funkcja zmiany danych, po prostu dużo kodu ale nic trudnego, zapytania do bazy danych i przklikanie wsyzstkich nowych danych
        ZatwierdzButton.setOnClickListener { zmienDane() }

        // wepelnianie danych z zakladki Preferencje twoimi danymi profilu
        if(app.celKalorii != null && app.celKalorii != 0) {
            aktywnosc.setSelection(app.aktywnosc)
            cel.setSelection(app.cel)
            waga.setText(app.waga.toString())
            wzrost.setText(app.wzrost.toString())
            wiek.setText(app.wiek.toString())
            if(app.plec == true) {
                plec.check(R.id.mezczyzna)
            } else {
                plec.check(R.id.kobieta)
            }
        } else {
            aktywnosc.setSelection(2)
            cel.setSelection(2)
            waga.setText("")
            wzrost.setText("")
            wiek.setText("")
            plec.check(R.id.mezczyzna)
        }
        // zczytanie danych użytkownika analogicznie do zakladki Profil
        if(aktualnyUzytkownik != null) {
            DaneLogowania.text = "Zalogowano jako: ${aktualnyUzytkownik.imie}"
            EdytujImie.setText(aktualnyUzytkownik.imie)
            EdytujNazwisko.setText(aktualnyUzytkownik.nazwisko)
            EdytujEmail.setText(aktualnyUzytkownik.email)
            EdytujTelefon.setText(aktualnyUzytkownik.telefon)
            EdytujAdres.setText(aktualnyUzytkownik.adres)
        }
        // zmiana pomiędzy edytowaniem a zatwierdzaniem żeby nie można było zmieniać kiedy nie trzeba
        EdytujButton.setOnClickListener {
            EdytujImie.isEnabled = true
            EdytujNazwisko.isEnabled = true
            EdytujEmail.isEnabled = true
            EdytujTelefon.isEnabled = true
            EdytujAdres.isEnabled = true
            EdytujButton.isEnabled = false
            ZatwierdzButton.isEnabled = true
        }

        // zmiana widzialności zakładek
        opcjaWLewo.setOnClickListener {
            opcjaWybrana.text = "D A N E"
            preferencje.visibility = View.GONE
            dane.visibility = View.VISIBLE
        }

        opcjaWPrawo.setOnClickListener {
            opcjaWybrana.text = "P R E F E R E N C J E"
            preferencje.visibility = View.VISIBLE
            dane.visibility = View.GONE
        }
        // Basic Listenery
        PowrotButton.setOnClickListener { przenies(HomeActivity::class.java)}
        ProfilButton.setOnClickListener { przenies(ProfilActivity::class.java)}
        HomeButton.setOnClickListener { przenies(HomeActivity::class.java)}
        LodowkaButton.setOnClickListener { przenies(LodowkaActivity::class.java)}
        TreningButton.setOnClickListener { przenies(TreningActivity::class.java)}
        DietaButton.setOnClickListener { przenies(DietaActivity::class.java)}

        WylogujButton.setOnClickListener {
            val danePreferencje = getSharedPreferences("preferencje", Context.MODE_PRIVATE)
            val edycjaPreferencji = danePreferencje.edit()
            edycjaPreferencji.apply {
                putInt(KEY_IS_LOGGED, 0)
                putString(KEY_LOGIN, null)
                putString(KEY_HASLO, null)
            }.apply()
            app.wyczysc()
            val intent = Intent(this@ProfilActivity, LoginActivity::class.java)
            startActivity(intent)
            // Toast.makeText(this@HomeActivity, "nacisnieto", Toast.LENGTH_SHORT).show()
        }

        // przykładowe wypełnienie spinnerów
        if (aktywnosc.adapter == null) {
            aktywnosc.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                listOf("Siedzący", "Lekko aktywny", "Umiarkowany", "Aktywny", "Bardzo aktywny")
            )
        }
        if (cel.adapter == null) {
            cel.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                listOf("Szybko schudnąć", "Schudnąć", "Utrzymać", "Przytyć", "Szybko przytyć")
            )
        }
    }

    fun zmienDane(){
        val app = application as DaneGlobalne
        var aktualnyUzytkownik = app.aktualnyUzytkownik
        if(aktualnyUzytkownik != null) {
            EdytujImie.isEnabled = false
            EdytujNazwisko.isEnabled = false
            EdytujEmail.isEnabled = false
            EdytujTelefon.isEnabled = false
            EdytujAdres.isEnabled = false
            EdytujButton.isEnabled = true
            ZatwierdzButton.isEnabled = false

            val KEY_LOGIN = getString(R.string.KEY_LOGIN_STRING)
            val KEY_HASLO = getString(R.string.KEY_HASLO_STRING)
            val KEY_ADRES = getString(R.string.KEY_ADRES_STRING)
            val KEY_EMAIL = getString(R.string.KEY_EMAIL_STRING)
            val KEY_TELEFON = getString(R.string.KEY_TELEFON_STRING)
            val KEY_IMIE = getString(R.string.KEY_IMIE_STRING)
            val KEY_NAZWISKO = getString(R.string.KEY_NAZWISKO_STRING)

            val email = EdytujEmail.text.toString()
            val telefon = EdytujTelefon.text.toString()
            val adres = EdytujAdres.text.toString()
            val imie = EdytujImie.text.toString()
            val nazwisko = EdytujNazwisko.text.toString()
            val login = aktualnyUzytkownik.login

            db.collection("Loginy").get().addOnSuccessListener { result ->
                for (document in result) {
                    if (login == document.getString(KEY_LOGIN)) {
                        val note = mutableMapOf<String, Any>()
                        note.put(KEY_IMIE, imie)
                        note.put(KEY_NAZWISKO, nazwisko)
                        note.put(KEY_EMAIL, email)
                        note.put(KEY_TELEFON, telefon)
                        note.put(KEY_ADRES, adres)
                        db.collection("Loginy").document("Dane logowania ${login}").update(note)
                    }
                }
            }

            aktualnyUzytkownik.imie = imie
            aktualnyUzytkownik.nazwisko = nazwisko
            aktualnyUzytkownik.email = email
            aktualnyUzytkownik.telefon = telefon
            aktualnyUzytkownik.adres = adres
            DaneLogowania.text = "Zalogowano jako: ${aktualnyUzytkownik.imie}"

            Toast.makeText(this@ProfilActivity, "zmieniono", Toast.LENGTH_SHORT).show()

        }
    }

    fun policzKalorie(){
        // 1) walidacja i parsowanie wejścia
        val danePreferencje = getSharedPreferences("preferencje", Context.MODE_PRIVATE)
        val edycjaPreferencji = danePreferencje.edit()

        val app = application as DaneGlobalne
        var aktualnyUzytkownik = app.aktualnyUzytkownik

        val wagaE = findViewById<EditText>(R.id.waga)
        val wzrostE = findViewById<EditText>(R.id.wzrost)
        val wiekE = findViewById<EditText>(R.id.wiek)
        val plecE = findViewById<RadioGroup>(R.id.plec)
        val aktywnoscE = findViewById<Spinner>(R.id.aktywnosc)
        val celE = findViewById<Spinner>(R.id.cel)

        val waga = wagaE.text.toString().replace(',', '.').toDouble()
        val wzrost = wzrostE.text.toString().replace(',', '.').toDouble()
        val wiek = wiekE.text.toString().toInt()

        if (waga == 0.0 || wzrost == 0.0 || wiek == 0) {
            Toast.makeText(this, "Wypełnij poprawnie wagę, wzrost i wiek", Toast.LENGTH_SHORT).show()
        }

        val plecIsMezczyzna = when (plecE.checkedRadioButtonId) {
            R.id.mezczyzna -> true
            R.id.kobieta -> false
            else -> true // domyślnie mężczyzna jeśli brak wyboru
        }

        val aktywnoscIndex = aktywnoscE.selectedItemPosition
        val wspolczynnikAktywnosci = when (aktywnoscIndex) {
            0 -> 1.2   // Siedzący
            1 -> 1.375 // Lekko aktywny
            2 -> 1.55  // Umiarkowany
            3 -> 1.725 // Aktywny
            4 -> 1.9   // Bardzo aktywny
            else -> 1.2
        }

        val celIndex = celE.selectedItemPosition
        val deltaKcal = when (celIndex) {
            0 -> -1000.0  // Szybko schudnąć
            1 -> -500.0   // Schudnąć
            2 -> 0.0      // Utrzymać
            3 -> 300.0    // Przytyć
            4 -> 700.0    // Szybko przytyć
            else -> 0.0
        }

        // 2) obliczenie BMR (Mifflin-St Jeor)
        val s = if (plecIsMezczyzna) 5.0 else -161.0
        val bmr = 10.0 * waga + 6.25 * wzrost - 5.0 * wiek + s

        // 3) TDEE
        val tdee = bmr * wspolczynnikAktywnosci

        // 4) docelowe kalorie z zabezpieczeniem minimalnym
        val minKalorie = if (plecIsMezczyzna) 1500.0 else 1200.0
        var doceloweKalorie = tdee + deltaKcal
        if (doceloweKalorie < minKalorie) doceloweKalorie = minKalorie

        // 5) estymacja zmiany masy (kg/tydz) przybliżeniem 7700 kcal/kg
        val kcalPerKg = 7700.0
        val estKgWeek = (doceloweKalorie - tdee) * 7.0 / kcalPerKg

        // 6) makroskładniki:
        // białko g/kg w zależności od celu (upraszczone)
        val bialkoNaKg = when (celIndex) {
            0 -> 2.0
            1 -> 1.8
            2 -> 1.4
            3 -> 1.6
            4 -> 1.6
            else -> 1.4
        }
        val bialkoGram = bialkoNaKg * waga
        val bialkoKcal = bialkoGram * 4.0

        // tłuszcz % kalorii
        val fatRatio = when (celIndex) {
            0 -> 0.25
            1 -> 0.27
            2 -> 0.30
            3 -> 0.30
            4 -> 0.32
            else -> 0.30
        }
        val fatKcal = doceloweKalorie * fatRatio
        val fatGram = fatKcal / 9.0

        // węglowodany reszta kalorii
        val pozostaleKcal = doceloweKalorie - (bialkoKcal + fatKcal)
        val carbsGram = if (pozostaleKcal > 0) pozostaleKcal / 4.0 else 0.0

        edycjaPreferencji.apply { // wsadzenie wynikow do preferencji żeby te dane nie ginęły
            putInt("celBialek", bialkoGram.toInt())
            putInt("celWeglowodanow", carbsGram.toInt())
            putInt("celTluszczy", fatGram.toInt())
            putInt("celKalorii", doceloweKalorie.toInt())
            putFloat("waga", waga.toFloat())
            putInt("wiek", wiek)
            putFloat("wzrost", wzrost.toFloat())
            putInt("cel", celE.selectedItemPosition)
            putInt("aktywnosc", aktywnoscE.selectedItemPosition)
            putBoolean("plec", plecIsMezczyzna)
        }.apply()
        // wsadzenie do zmiennych globalnych żeby można było na bieżąco z nich korzystać
        app.celBialek = danePreferencje.getInt("celBialek", 0)
        app.celWeglowodanow = danePreferencje.getInt("celWeglowodanow", 0)
        app.celTluszczy = danePreferencje.getInt("celTluszczy", 0)
        app.celKalorii = danePreferencje.getInt("celKalorii", 0)
        app.waga = danePreferencje.getFloat("waga", 0.0F)
        app.wiek = danePreferencje.getInt("wiek", 0)
        app.wzrost = danePreferencje.getFloat("wzrost", 0.0F)
        app.cel = danePreferencje.getInt("cel", 0)
        app.aktywnosc = danePreferencje.getInt("aktywnosc", 0)

    }

    fun przenies(Cel : Class<out Activity>){
        val intent = Intent(this@ProfilActivity, Cel)
        startActivity(intent)
    }
}

