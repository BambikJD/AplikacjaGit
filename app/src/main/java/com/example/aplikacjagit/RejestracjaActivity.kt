package com.example.aplikacjagit

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.firestore.FirebaseFirestore

class RejestracjaActivity : ComponentActivity() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private lateinit var PowrotButton: ImageButton // Zmienione na ImageButton
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

        // Inicjalizacja
        PowrotButton = findViewById(R.id.PowrotButton)
        ZarejestrujButton = findViewById(R.id.ZarejestrujButton)
        WprowadzLogin = findViewById(R.id.WprowadzLogin)
        WprowadzHaslo = findViewById(R.id.WprowadzHaslo)
        WprowadzEmail = findViewById(R.id.WprowadzEmail)
        WprowadzTelefon = findViewById(R.id.WprowadzTelefon)
        WprowadzAdres = findViewById(R.id.WprowadzAdres)
        WprowadzImie = findViewById(R.id.WprowadzImie)
        WprowadzNazwisko = findViewById(R.id.WprowadzNazwisko)

        PowrotButton.setOnClickListener {
            finish() // Po prostu zamyka tę stronę i wraca do logowania
        }

        ZarejestrujButton.setOnClickListener {
            if (walidacjaPol()) {
                zarejestruj()
            }
        }
    }

    private fun walidacjaPol(): Boolean {
        if (WprowadzLogin.text.isEmpty() || WprowadzHaslo.text.isEmpty() || WprowadzEmail.text.isEmpty()) {
            Toast.makeText(this, "Login, Hasło i Email są wymagane!", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun zarejestruj() {
        val KEY_LOGIN = getString(R.string.KEY_LOGIN_STRING)
        val KEY_HASLO = getString(R.string.KEY_HASLO_STRING)
        val KEY_ADRES = getString(R.string.KEY_ADRES_STRING)
        val KEY_EMAIL = getString(R.string.KEY_EMAIL_STRING)
        val KEY_TELEFON = getString(R.string.KEY_TELEFON_STRING)
        val KEY_IMIE = getString(R.string.KEY_IMIE_STRING)
        val KEY_NAZWISKO = getString(R.string.KEY_NAZWISKO_STRING)

        val login = WprowadzLogin.text.toString()
        val haslo = WprowadzHaslo.text.toString()

        ZarejestrujButton.isEnabled = false
        ZarejestrujButton.text = "SPRAWDZANIE..."

        db.collection("Loginy").get().addOnSuccessListener { result ->
            var czyZajety = false
            for (document in result) {
                if (login == document.getString(KEY_LOGIN)) {
                    czyZajety = true
                    break
                }
            }

            if (czyZajety) {
                Toast.makeText(this, "Ten login jest już zajęty!", Toast.LENGTH_LONG).show()
                ZarejestrujButton.isEnabled = true
                ZarejestrujButton.text = "ZAŁÓŻ KONTO"
            } else {
                val userMap = hashMapOf(
                    KEY_LOGIN to login,
                    KEY_HASLO to haslo,
                    KEY_IMIE to WprowadzImie.text.toString(),
                    KEY_NAZWISKO to WprowadzNazwisko.text.toString(),
                    KEY_EMAIL to WprowadzEmail.text.toString(),
                    KEY_TELEFON to WprowadzTelefon.text.toString(),
                    KEY_ADRES to WprowadzAdres.text.toString()
                )

                db.collection("Loginy").document("Dane logowania $login").set(userMap)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Konto założone pomyślnie!", Toast.LENGTH_SHORT).show()
                        finish() // Wraca do ekranu logowania
                    }
                    .addOnFailureListener {
                        ZarejestrujButton.isEnabled = true
                        ZarejestrujButton.text = "ZAŁÓŻ KONTO"
                        Toast.makeText(this, "Błąd bazy danych. Spróbuj później.", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}