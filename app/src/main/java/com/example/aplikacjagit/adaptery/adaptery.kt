package com.example.aplikacjagit.adaptery

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikacjagit.room.Produkt
import com.example.aplikacjagit.R

class ProduktAdapter(
    private val onAddClick: (Produkt) -> Unit = {}
) : RecyclerView.Adapter<ProduktAdapter.ProduktViewHolder>() {

    private var listaProduktow: MutableList<Produkt> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProduktViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.produkt_item, parent, false)
        return ProduktViewHolder(v)
    }

    override fun onBindViewHolder(holder: ProduktViewHolder, position: Int) {
        val produkt = listaProduktow[position]
        holder.nazwaProduktu.text = produkt.nazwa ?: ""
        holder.kalorycznoscProduktu.text = produkt.kalorycznosc?.toString() ?: ""

        holder.btnDodaj.setOnClickListener {
            holder.btnDodaj.isEnabled = false
            onAddClick(produkt)
            holder.btnDodaj.postDelayed({ holder.btnDodaj.isEnabled = true }, 600)
        }
    }

    override fun getItemCount(): Int = listaProduktow.size

    fun stworzProdukt(produkty: MutableList<Produkt>) {
        listaProduktow = produkty
        notifyDataSetChanged()
    }

    inner class ProduktViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nazwaProduktu: TextView = view.findViewById(R.id.nazwa)
        val kalorycznoscProduktu: TextView = view.findViewById(R.id.kalorycznosc)
        val btnDodaj: Button = view.findViewById(R.id.DodajButton)
    }
}
