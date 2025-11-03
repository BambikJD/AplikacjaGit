package com.example.aplikacjagit.adaptery

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikacjagit.room.Produkt
import com.example.aplikacjagit.R
import com.example.aplikacjagit.room.Dodane

class ProduktAdapter(
    private val addOnClick: (Produkt, Int) -> Unit = { _, _ -> }
) : RecyclerView.Adapter<ProduktAdapter.ProduktViewHolder>() {

    private var listaProduktow: MutableList<Produkt> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProduktViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.produkt_item, parent, false)
        return ProduktViewHolder(v)
    }

    override fun onBindViewHolder(holder: ProduktViewHolder, position: Int) {
        val produkt = listaProduktow[position]
        holder.nazwaProduktu.text = produkt.nazwa.toString()
        holder.kalorycznoscProduktu.text = "${produkt.kalorycznosc?.toString()} kcal/100g"
        holder.bialka.text = "B: ${produkt.bialka.toString()}"
        holder.tluszcze.text = "T: ${produkt.tluszcze.toString()}"
        holder.weglowodany.text = "W: ${produkt.weglowodany.toString()}"

        holder.DodajButton.setOnClickListener {
            val text = holder.iloscGram.text.toString()
            val gramy = text.toIntOrNull()
            if (gramy == null || gramy <= 0) {
                Toast.makeText(holder.itemView.context, "Podaj poprawną ilość w gramach", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            holder.iloscGram.setText("")
            holder.DodajButton.isEnabled = false
            addOnClick(produkt, gramy)
            holder.DodajButton.postDelayed({ holder.DodajButton.isEnabled = true }, 600)
        }

    }

    override fun getItemCount(): Int = listaProduktow.size

    fun stworzProdukt(produkty: MutableList<Produkt>) {
        listaProduktow = produkty
        notifyDataSetChanged()
    }

    inner class ProduktViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nazwaProduktu: TextView = view.findViewById(R.id.nazwa)
        val bialka: TextView = view.findViewById(R.id.bialka)
        val tluszcze: TextView = view.findViewById(R.id.tluszcze)
        val weglowodany: TextView = view.findViewById(R.id.weglowodany)
        val kalorycznoscProduktu: TextView = view.findViewById(R.id.kalorycznosc)
        val DodajButton: ImageButton = view.findViewById(R.id.DodajButton)
        val iloscGram: EditText = view.findViewById(R.id.iloscGram)
    }
}

class  DodaneAdapter(
    private val deleteOnClick: (Dodane) -> Unit = {}
    ) : RecyclerView.Adapter<DodaneAdapter.DodaneViewHolder>() {

    private var listaDodanych: MutableList<Dodane> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DodaneViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.dodane_item, parent, false)
        return DodaneViewHolder(v)
    }

    override fun onBindViewHolder(holder: DodaneViewHolder, position: Int) {
        val produkt = listaDodanych[position]
        holder.nazwaProduktu.text = produkt.nazwa
        holder.kalorycznoscProduktu.text = "${produkt.sumaKalorii?.toString()}kcal"
        holder.iloscGram.text = "${produkt.ilosc?.toString()}g"
        holder.bialka.text = "B: ${produkt.sumaBialek.toString()}"
        holder.tluszcze.text = "T: ${produkt.sumaTluszczy.toString()}"
        holder.weglowodany.text = "W: ${produkt.sumaWeglowodanow.toString()}"

        holder.UsunButton.setOnClickListener {
            holder.UsunButton.isEnabled = false
            deleteOnClick(produkt)
            holder.UsunButton.postDelayed({ holder.UsunButton.isEnabled = true }, 600)
        }
    }

    override fun getItemCount(): Int = listaDodanych.size

    fun stworzDodane(dodane: MutableList<Dodane>) {
        listaDodanych = dodane
        notifyDataSetChanged()
    }

    inner class DodaneViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nazwaProduktu: TextView = view.findViewById(R.id.nazwa)
        val bialka: TextView = view.findViewById(R.id.bialka)
        val tluszcze: TextView = view.findViewById(R.id.tluszcze)
        val weglowodany: TextView = view.findViewById(R.id.weglowodany)
        val kalorycznoscProduktu: TextView = view.findViewById(R.id.kalorycznosc)
        val iloscGram: TextView = view.findViewById(R.id.iloscGram)
        val UsunButton: ImageButton = view.findViewById(R.id.UsunButton)
    }
}
