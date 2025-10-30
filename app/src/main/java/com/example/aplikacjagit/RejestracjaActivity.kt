package com.example.aplikacjagit

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.firestore.FirebaseFirestore

class RejestracjaActivity : ComponentActivity() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private lateinit var PowrotButton: Button
    private lateinit var ZarejestrujButton: Button
    private lateinit var WprowadzLogin: EditText
    private lateinit var WprowadzHaslo: EditText
    private lateinit var WprowadzEmail: EditText
    private lateinit var WprowadzTelefon: EditText
    private lateinit var WprowadzAdres: EditText
    private lateinit var WprowadzImie: EditText
    private lateinit var WprowadzNazwisko: EditText



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rejestracja)

        PowrotButton =  findViewById(R.id.PowrotButton)
        ZarejestrujButton =  findViewById(R.id.ZarejestrujButton)
        WprowadzLogin =  findViewById(R.id.WprowadzLogin)
        WprowadzHaslo =  findViewById(R.id.WprowadzHaslo)
        WprowadzEmail =  findViewById(R.id.WprowadzEmail)
        WprowadzTelefon =  findViewById(R.id.WprowadzTelefon)
        WprowadzAdres =  findViewById(R.id.WprowadzAdres)
        WprowadzImie =  findViewById(R.id.WprowadzImie)
        WprowadzNazwisko =  findViewById(R.id.WprowadzNazwisko)

        PowrotButton.setOnClickListener {
            val intent = Intent(this@RejestracjaActivity, LoginActivity::class.java)
            startActivity(intent)
            // Toast.makeText(this@HomeActivity, "nacisnieto", Toast.LENGTH_SHORT).show()
        }

        ZarejestrujButton.setOnClickListener {
            zarejestruj()
        }

    }
    private fun zarejestruj(){

        val KEY_LOGIN = getString(R.string.KEY_LOGIN_STRING)
        val KEY_HASLO = getString(R.string.KEY_HASLO_STRING)
        val KEY_ADRES = getString(R.string.KEY_ADRES_STRING)
        val KEY_EMAIL = getString(R.string.KEY_EMAIL_STRING)
        val KEY_TELEFON = getString(R.string.KEY_TELEFON_STRING)
        val KEY_IMIE = getString(R.string.KEY_IMIE_STRING)
        val KEY_NAZWISKO = getString(R.string.KEY_NAZWISKO_STRING)

        val login = WprowadzLogin.text.toString()
        val haslo = WprowadzHaslo.text.toString()
        val email = WprowadzEmail.text.toString()
        val telefon = WprowadzTelefon.text.toString()
        val adres = WprowadzAdres.text.toString()
        val imie = WprowadzImie.text.toString()
        val nazwisko = WprowadzNazwisko.text.toString()
        var czyZajety = 0


        db.collection("Loginy").get().addOnSuccessListener { result ->
            for (document in result) {
                if (login == document.getString(KEY_LOGIN)) {
                    Toast.makeText(
                        this@RejestracjaActivity,
                        "Ten login jest zajęty",
                        Toast.LENGTH_LONG
                    ).show()
                    czyZajety = 1
                }
            }
            if (czyZajety == 0) {
                val note = mutableMapOf<String, Any>()
                note.put(KEY_LOGIN, login)
                note.put(KEY_HASLO, haslo)
                note.put(KEY_IMIE, imie)
                note.put(KEY_NAZWISKO, nazwisko)
                note.put(KEY_EMAIL, email)
                note.put(KEY_TELEFON, telefon)
                note.put(KEY_ADRES, adres)

                db.collection("Loginy").document("Dane logowania ${login}").set(note)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this@RejestracjaActivity,
                            "Konto założone",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(this@RejestracjaActivity, LoginActivity::class.java)
                        startActivity(intent)
                    }.addOnFailureListener {
                        Toast.makeText(
                            this@RejestracjaActivity,
                            "Spróbuj ponownie, coś nie działą",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }


    }
}

