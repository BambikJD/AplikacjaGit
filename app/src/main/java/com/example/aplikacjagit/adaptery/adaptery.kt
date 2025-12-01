package com.example.aplikacjagit.adaptery

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikacjagit.room.Produkt
import com.example.aplikacjagit.R
import com.example.aplikacjagit.room.Dodane
import com.example.aplikacjagit.room.Przepis
import com.example.aplikacjagit.room.PrzepisWynik
import com.example.aplikacjagit.room.ProduktPotrzebny

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
class PrzepisyAdapter : androidx.recyclerview.widget.ListAdapter<PrzepisWynik, PrzepisyAdapter.PrzepisyViewHolder>(PrzepisDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrzepisyViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.przepis_item, parent, false)
        return PrzepisyViewHolder(v)
    }

    override fun onBindViewHolder(holder: PrzepisyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PrzepisyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nazwaPrzepisu: TextView = view.findViewById(R.id.nazwa)
        val bialka: TextView = view.findViewById(R.id.bialka)
        val tluszcze: TextView = view.findViewById(R.id.tluszcze)
        val weglowodany: TextView = view.findViewById(R.id.weglowodany)
        val kalorycznoscPrzepisu: TextView = view.findViewById(R.id.kalorycznosc)
        val opis: TextView = view.findViewById(R.id.opis)
        val waga: TextView = view.findViewById(R.id.iloscGram)
        val produktyRecycler: RecyclerView = view.findViewById(R.id.produktyPotrzebne)
        var produktyAdapter: ProduktyPotrzebneAdapter? = null

        fun bind(przepis: PrzepisWynik) {
            nazwaPrzepisu.text = przepis.nazwa ?: "-"
            kalorycznoscPrzepisu.text = przepis.kalorycznosc?.let { "${it}kcal" } ?: "-"
            bialka.text = przepis.bialka?.let { "B: ${formatNumber(it)}" } ?: "B:-"
            tluszcze.text = przepis.tluszcze?.let { "T: ${formatNumber(it)}" } ?: "T:-"
            weglowodany.text = przepis.weglowodany?.let { "W: ${formatNumber(it)}" } ?: "W:-"
            opis.text = przepis.opis ?: ""
            waga.text = przepis.listaIlosci.sum().toString()

            if (produktyAdapter == null) {
                produktyAdapter = ProduktyPotrzebneAdapter()
                produktyRecycler.apply {
                    layoutManager =
                        LinearLayoutManager(itemView.context, LinearLayoutManager.VERTICAL, false)
                    adapter = produktyAdapter
                    isNestedScrollingEnabled = false
                }
            }

            // ustaw listę produktów (już z nazwami i ilościami)
            produktyAdapter?.submitList(przepis.produktyPotrzebne)
        }

        private fun formatNumber(d: Double?): String {
            if (d == null) return "-"
            return if (d % 1.0 == 0.0) d.toInt().toString() else String.format("%.1f", d)
        }
    }

    class PrzepisDiff : androidx.recyclerview.widget.DiffUtil.ItemCallback<PrzepisWynik>() {
        override fun areItemsTheSame(oldItem: PrzepisWynik, newItem: PrzepisWynik): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: PrzepisWynik, newItem: PrzepisWynik): Boolean = oldItem == newItem
    }
}

class ProduktyPotrzebneAdapter :
    ListAdapter<ProduktPotrzebny, ProduktyPotrzebneAdapter.Holder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.produkt_potrzebny_item, parent, false)
        return Holder(v)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
        private val nazwaTv: TextView = view.findViewById(R.id.nazwaProduktPotrzebny)
        private val iloscTv: TextView = view.findViewById(R.id.iloscProduktPotrzebny)
        fun bind(p: ProduktPotrzebny) {
            nazwaTv.text = p.nazwa
            iloscTv.text = "${p.ilosc} g"
        }
    }

    object Diff : DiffUtil.ItemCallback<ProduktPotrzebny>() {
        override fun areItemsTheSame(oldItem: ProduktPotrzebny, newItem: ProduktPotrzebny) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ProduktPotrzebny, newItem: ProduktPotrzebny) =
            oldItem == newItem
    }
}