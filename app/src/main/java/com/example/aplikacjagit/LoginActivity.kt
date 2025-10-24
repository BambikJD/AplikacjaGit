package com.example.aplikacjagit

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : ComponentActivity() {
    private lateinit var ZalogujButton: Button
    private lateinit var EnterPassword: EditText
    private lateinit var EnterLogin: EditText
    private lateinit var LoginMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        ZalogujButton =  findViewById(R.id.ZalogujButton)
        EnterLogin = findViewById(R.id.EnterLogin)
        EnterPassword = findViewById(R.id.EnterPassword)
        LoginMessage = findViewById(R.id.LoginMessage)

        ZalogujButton.setOnClickListener {
            if(EnterLogin.text.toString() == "admin" && EnterPassword.text.toString() == "admin") {
                val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                startActivity(intent)
                Toast.makeText(this@LoginActivity, "Zalogowano", Toast.LENGTH_SHORT).show()
            } else {
                LoginMessage.visibility = View.VISIBLE
                LoginMessage.text = "Wprowadź prawidłowe dane"
            }
        }

    }

}

