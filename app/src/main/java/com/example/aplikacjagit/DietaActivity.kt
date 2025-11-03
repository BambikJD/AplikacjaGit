package com.example.aplikacjagit

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikacjagit.adaptery.ProduktAdapter
import com.example.aplikacjagit.room.Dodane
import com.example.aplikacjagit.room.ProduktyViewModel
import java.time.LocalDate
import java.util.Date

class DietaActivity : ComponentActivity() {

    private lateinit var ProfilButton: Button
    private lateinit var HomeButton: Button
    private lateinit var LodowkaButton: Button
    private lateinit var TreningButton: Button
    private lateinit var DietaButton: Button
    private lateinit var DataDnia: TextView

    private lateinit var widokProdukty: RecyclerView
    private lateinit var produktAdapter: ProduktAdapter
    private lateinit var ProduktyViewModel: ProduktyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dieta)

        widokProdukty = findViewById(R.id.widokProdukty)
        produktAdapter = ProduktAdapter { produkt ->
            val idProduktu = produkt.id
            if (idProduktu == null) {
                android.widget.Toast.makeText(this, "Produkt nie ma id, najpierw zapisz produkt", android.widget.Toast.LENGTH_SHORT).show()
                return@ProduktAdapter
            }

            val dodane = Dodane(
                idProduktu = idProduktu,
                ilosc = 1,
                data = LocalDate.now() as Date?
            )

            ProduktyViewModel.insertDodane(dodane)

            android.widget.Toast.makeText(this, "Dodano ${produkt.nazwa} do listy", android.widget.Toast.LENGTH_SHORT).show()
        }

        widokProdukty.adapter = produktAdapter
        widokProdukty.layoutManager = LinearLayoutManager(this)
        ProduktyViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application))[ProduktyViewModel::class.java]

        ProduktyViewModel.wszystkieProdukty.observe(this){
            produktAdapter.stworzProdukt(it)
        }

        ProfilButton =  findViewById(R.id.ProfilButton)
        HomeButton =  findViewById(R.id.HomeButton)
        LodowkaButton =  findViewById(R.id.LodowkaButton)
        TreningButton =  findViewById(R.id.TreningButton)
        DietaButton =  findViewById(R.id.DietaButton)
        DataDnia = findViewById(R.id.DataDnia)

        DataDnia.text = java.time.LocalDate.now().toString()

        ProfilButton.setOnClickListener {
            val intent = Intent(this@DietaActivity, ProfilActivity::class.java)
            startActivity(intent)
            // Toast.makeText(this@HomeActivity, "nacisnieto", Toast.LENGTH_SHORT).show()
        }

        HomeButton.setOnClickListener {
            val intent = Intent(this@DietaActivity, HomeActivity::class.java)
            startActivity(intent)
            // Toast.makeText(this@HomeActivity, "nacisnieto", Toast.LENGTH_SHORT).show()
        }

        LodowkaButton.setOnClickListener {
            val intent = Intent(this@DietaActivity, LodowkaActivity::class.java)
            startActivity(intent)
            // Toast.makeText(this@HomeActivity, "nacisnieto", Toast.LENGTH_SHORT).show()
        }

        TreningButton.setOnClickListener {
            val intent = Intent(this@DietaActivity, TreningActivity::class.java)
            startActivity(intent)
            // Toast.makeText(this@HomeActivity, "nacisnieto", Toast.LENGTH_SHORT).show()
        }

        DietaButton.setOnClickListener {
            val intent = Intent(this@DietaActivity, DietaActivity::class.java)
            startActivity(intent)
            // Toast.makeText(this@HomeActivity, "nacisnieto", Toast.LENGTH_SHORT).show()
        }

    }

}

