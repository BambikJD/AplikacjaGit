package com.example.aplikacjagit

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : ComponentActivity() {
    private lateinit var ZalogujButton: Button
    private lateinit var EnterPassword: EditText
    private lateinit var EnterLogin: EditText
    private lateinit var LoginMessage: TextView
    private lateinit var RejestracjaButton: Button
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val KEY_LOGIN = "login"
    private val KEY_HASLO = "haslo"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        ZalogujButton = findViewById(R.id.ZalogujButton)
        EnterLogin = findViewById(R.id.EnterLogin)
        EnterPassword = findViewById(R.id.EnterPassword)
        LoginMessage = findViewById(R.id.LoginMessage)
        RejestracjaButton = findViewById(R.id.RejestracjaButton)

        ZalogujButton.setOnClickListener {
           Logowanie()
        }
        RejestracjaButton.setOnClickListener {
            val intent = Intent(this@LoginActivity, RejestracjaActivity::class.java)
            startActivity(intent)
        }

    }
    private fun Logowanie(){
        db.collection("Loginy").get().addOnSuccessListener { result ->
            val login = EnterLogin.text.toString()
            val haslo = EnterPassword.text.toString()
            var czyZalogowano = 0
            for (document in result) {
                if (login == document.getString(KEY_LOGIN) && haslo == document.getString(
                        KEY_HASLO
                    )
                ) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Zalogowano",
                        Toast.LENGTH_LONG
                    ).show()
                    czyZalogowano = 1
                    val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                    startActivity(intent)
                }
            }
            if (czyZalogowano == 0) {
                Toast.makeText(
                    this@LoginActivity,
                    "Wprowadź prawidłowe dane ;)",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

