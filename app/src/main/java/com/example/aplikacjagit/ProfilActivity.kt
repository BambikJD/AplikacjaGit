package com.example.aplikacjagit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class ProfilActivity : ComponentActivity() {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

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

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.profil)

        val KEY_ADRES = getString(R.string.KEY_ADRES_STRING)
        val KEY_EMAIL = getString(R.string.KEY_EMAIL_STRING)
        val KEY_TELEFON = getString(R.string.KEY_TELEFON_STRING)
        val KEY_LOGIN = getString(R.string.KEY_LOGIN_STRING) // klucze do danych chwilowych lokalnych z klasiIFunkcje
        val KEY_HASLO = getString(R.string.KEY_HASLO_STRING)
        val KEY_IMIE = getString(R.string.KEY_IMIE_STRING)
        val KEY_NAZWISKO = getString(R.string.KEY_NAZWISKO_STRING)
        val KEY_IS_LOGGED = getString(R.string.KEY_IS_LOGGED_STRING)

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

        val app = application as DaneGlobalne
        var aktualnyUzytkownik = app.aktualnyUzytkownik

        if(aktualnyUzytkownik != null) {
            DaneLogowania.text = "Zalogowano jako: ${aktualnyUzytkownik.imie}"
            EdytujImie.setText(aktualnyUzytkownik.imie)
            EdytujNazwisko.setText(aktualnyUzytkownik.nazwisko)
            EdytujEmail.setText(aktualnyUzytkownik.email)
            EdytujTelefon.setText(aktualnyUzytkownik.telefon)
            EdytujAdres.setText(aktualnyUzytkownik.adres)
        }

        EdytujButton.setOnClickListener {
            EdytujImie.isEnabled = true
            EdytujNazwisko.isEnabled = true
            EdytujEmail.isEnabled = true
            EdytujTelefon.isEnabled = true
            EdytujAdres.isEnabled = true
            EdytujButton.isEnabled = false
            ZatwierdzButton.isEnabled = true
        }

        ZatwierdzButton.setOnClickListener {
            zmienDane()
        }

        PowrotButton.setOnClickListener {
            val intent = Intent(this@ProfilActivity, HomeActivity::class.java)
            startActivity(intent)
            // Toast.makeText(this@HomeActivity, "nacisnieto", Toast.LENGTH_SHORT).show()
        }

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

            Toast.makeText(this@ProfilActivity, "zmieniono", Toast.LENGTH_SHORT).show()

        }
    }
}

