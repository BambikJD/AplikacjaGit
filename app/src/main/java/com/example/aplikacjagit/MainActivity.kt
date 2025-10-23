package com.example.aplikacjagit

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.aplikacjagit.ui.theme.AplikacjaGitTheme

class MainActivity : ComponentActivity() {
    private lateinit var ButtonMain: Button
    private lateinit var TextViewMain: TextView
    private lateinit var ButtonNew: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout)
        setContentView(R.layout.new_layout)

        ButtonMain =  findViewById(R.id.ButtonMain)
        TextViewMain =  findViewById(R.id.TextViewMain)


        ButtonMain.text = getString(R.string.login_button)

        ButtonMain.setOnClickListener {
            setContentView(R.layout.new_layout)
        }



    }

}

