package com.example.aplikacjagit

import android.content.Intent
import android.os.Bundle
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

    private val KEY_LOGIN = "login"
    private val KEY_HASLO = "password"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rejestracja)

        PowrotButton =  findViewById(R.id.PowrotButton)
        ZarejestrujButton =  findViewById(R.id.ZarejestrujButton)
        WprowadzLogin =  findViewById(R.id.EnterLogin)
        WprowadzHaslo =  findViewById(R.id.WprowadzHaslo)

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
        val login = WprowadzLogin.text.toString()
        val haslo = WprowadzHaslo.text.toString()

        val note = mutableMapOf<String, Any>()
        note.put(KEY_LOGIN, login)
        note.put(KEY_HASLO, haslo)

        db.collection("Loginy").document("Dane logowania ${login}").set(note)
            .addOnSuccessListener {
                Toast.makeText(this@RejestracjaActivity, "dziala", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this@RejestracjaActivity, "nie dziala", Toast.LENGTH_SHORT).show()
            }
    }
}

