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
import com.example.aplikacjagit.room.Cwiczenie
import com.example.aplikacjagit.room.Dodane
import com.example.aplikacjagit.room.Lodowka
import com.example.aplikacjagit.room.PlanWynik
import com.example.aplikacjagit.room.Przepis
import com.example.aplikacjagit.room.PrzepisWynik
import com.example.aplikacjagit.room.ProduktPotrzebny
import com.example.aplikacjagit.room.Wykonane
import java.util.Locale

class ProduktAdapter(private val onAddClick: (Produkt) -> Unit) : RecyclerView.Adapter<ProduktAdapter.ProduktViewHolder>() {

    private var lista = mutableListOf<Produkt>()

    fun stworzProdukt(nowaLista: List<Produkt>) {
        lista = nowaLista.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProduktViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.produkt_item, parent, false)
        return ProduktViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProduktViewHolder, position: Int) {
        val produkt = lista[position]
        holder.nazwa.text = produkt.nazwa
        holder.kcal.text = "${produkt.kalorycznosc} kcal / 100g"
        holder.makro.text = "B: ${produkt.bialka} | T: ${produkt.tluszcze} | W: ${produkt.weglowodany}"

        holder.btnDodaj.setOnClickListener { onAddClick(produkt) }
    }

    override fun getItemCount() = lista.size

    class ProduktViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nazwa: TextView = view.findViewById(R.id.nazwa)
        val kcal: TextView = view.findViewById(R.id.kalorycznosc)
        val makro: TextView = view.findViewById(R.id.makro)
        val btnDodaj: ImageButton = view.findViewById(R.id.DodajButton)
    }
}

class DodaneAdapter(private val onDeleteClick: (Dodane) -> Unit) : RecyclerView.Adapter<DodaneAdapter.DodaneViewHolder>() {

    private var lista = mutableListOf<Dodane>()

    fun stworzDodane(nowaLista: List<Dodane>) {
        lista = nowaLista.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DodaneViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.dodane_item, parent, false)
        return DodaneViewHolder(view)
    }

    override fun onBindViewHolder(holder: DodaneViewHolder, position: Int) {
        val item = lista[position]

        holder.nazwa.text = item.nazwa ?: "-"
        holder.gramy.text = "${item.ilosc}g"
        holder.kcal.text = "${item.sumaKalorii} kcal"

        // ZAOKRĄGLANIE makroskładników dla pojedynczego produktu
        val b = String.format(Locale.US, "%.1f", item.sumaBialek ?: 0.0)
        val t = String.format(Locale.US, "%.1f", item.sumaTluszczy ?: 0.0)
        val w = String.format(Locale.US, "%.1f", item.sumaWeglowodanow ?: 0.0)

        holder.makro.text = "B: $b  T: $t  W: $w"

        holder.btnUsun.setOnClickListener { onDeleteClick(item) }
    }

    override fun getItemCount() = lista.size

    class DodaneViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // ID muszą być identyczne jak w XML wyżej!
        val nazwa: TextView = view.findViewById(R.id.nazwaDodane)
        val gramy: TextView = view.findViewById(R.id.gramyDodane)
        val kcal: TextView = view.findViewById(R.id.kcalDodane)
        val makro: TextView = view.findViewById(R.id.makroDodane)
        val btnUsun: ImageButton = view.findViewById(R.id.UsunButton)
    }
}

class PrzepisyAdapter(private val onPrzepisClick: (PrzepisWynik) -> Unit) :
    androidx.recyclerview.widget.ListAdapter<PrzepisWynik, PrzepisyAdapter.PrzepisyViewHolder>(PrzepisDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrzepisyViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.przepis_item, parent, false)
        return PrzepisyViewHolder(v)
    }

    override fun onBindViewHolder(holder: PrzepisyViewHolder, position: Int) {
        // Przekazujemy funkcję kliknięcia do bind
        holder.bind(getItem(position), onPrzepisClick)
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

        fun bind(przepis: PrzepisWynik, onClick: (PrzepisWynik) -> Unit) {
            nazwaPrzepisu.text = przepis.nazwa ?: "-"
            kalorycznoscPrzepisu.text = przepis.kalorycznosc?.let { "${it}kcal" } ?: "-"
            bialka.text = przepis.bialka?.let { "B: ${formatNumber(it)}" } ?: "B:-"
            tluszcze.text = przepis.tluszcze?.let { "T: ${formatNumber(it)}" } ?: "T:-"
            weglowodany.text = przepis.weglowodany?.let { "W: ${formatNumber(it)}" } ?: "W:-"
            opis.text = przepis.opis ?: ""
            waga.text = "${przepis.listaIlosci.sum()}g"

            // Obsługa kliknięcia w cały element (kartę przepisu)
            itemView.setOnClickListener { onClick(przepis) }

            if (produktyAdapter == null) {
                produktyAdapter = ProduktyPotrzebneAdapter()
                produktyRecycler.apply {
                    layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.VERTICAL, false)
                    adapter = produktyAdapter
                    isNestedScrollingEnabled = false
                }
            }

            // Ustawiamy listę produktów (składników) przepisu
            produktyAdapter?.submitList(przepis.produktyPotrzebne)
        }

        private fun formatNumber(d: Double?): String {
            if (d == null) return "-"
            // To zawsze zwróci liczbę z jednym miejscem po przecinku, np. "10.5" lub "10.0"
            return String.format("%.1f", d)
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


class LodowkaAdapter(private val onDeleteClick: (Lodowka) -> Unit) : RecyclerView.Adapter<LodowkaAdapter.LodowkaViewHolder>() {

    private var lista = mutableListOf<Lodowka>()

    fun stworzLodowka(nowaLista: List<Lodowka>) {
        lista = nowaLista.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LodowkaViewHolder {
        // Upewnij się, że używasz poprawnego layoutu (np. lodowka_item lub dodane_item)
        val view = LayoutInflater.from(parent.context).inflate(R.layout.lodowka_item, parent, false)
        return LodowkaViewHolder(view)
    }

    override fun onBindViewHolder(holder: LodowkaViewHolder, position: Int) {
        val item = lista[position]
        holder.nazwa.text = item.nazwa
        holder.ilosc.text = "${item.ilosc} szt./g"

        holder.btnUsun.setOnClickListener {
            onDeleteClick(item)
        }
    }

    override fun getItemCount() = lista.size

    class LodowkaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nazwa: TextView = view.findViewById(R.id.nazwa)
        val ilosc: TextView = view.findViewById(R.id.iloscGram) // lub id które masz w lodowka_item
        val btnUsun: ImageButton = view.findViewById(R.id.UsunButton)
    }
}

class CwiczeniaAdapter(private val onAddClick: (Cwiczenie) -> Unit) : RecyclerView.Adapter<CwiczeniaAdapter.CwiczenieViewHolder>() {
    private var lista = mutableListOf<Cwiczenie>()

    fun stworzCwiczenie(nowaLista: List<Cwiczenie>) {
        lista = nowaLista.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CwiczenieViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cwiczenie_item, parent, false)
        return CwiczenieViewHolder(view)
    }

    override fun onBindViewHolder(holder: CwiczenieViewHolder, position: Int) {
        val cwiczenie = lista[position]
        holder.nazwa.text = cwiczenie.nazwa
        holder.partia.text = cwiczenie.partiaMiesniowa
        holder.przyciskDodaj.setOnClickListener { onAddClick(cwiczenie) }
    }

    override fun getItemCount() = lista.size

    class CwiczenieViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nazwa: TextView = view.findViewById(R.id.nazwaCwiczenia)
        val partia: TextView = view.findViewById(R.id.partiaMiesniowa)
        val przyciskDodaj: ImageButton = view.findViewById(R.id.dodajDoTreningu)
    }
}

class WykonaneAdapter(private val onDeleteClick: (Wykonane) -> Unit) : RecyclerView.Adapter<WykonaneAdapter.WykonaneViewHolder>() {
    private var lista = mutableListOf<Wykonane>()

    fun stworzWykonane(nowaLista: List<Wykonane>) {
        lista = nowaLista.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WykonaneViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.wykonane_item, parent, false)
        return WykonaneViewHolder(view)
    }

    override fun onBindViewHolder(holder: WykonaneViewHolder, position: Int) {
        val item = lista[position]
        holder.nazwa.text = item.nazwa
        holder.seriePowt.text = "${item.serie} x ${item.powtorzenia}"
        holder.ciezar.text = "${item.ciezar} kg"
        holder.przyciskUsun.setOnClickListener { onDeleteClick(item) }
    }

    override fun getItemCount() = lista.size

    class WykonaneViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nazwa: TextView = view.findViewById(R.id.nazwaWykonanego)
        val seriePowt: TextView = view.findViewById(R.id.seriePowtorzenia)
        val ciezar: TextView = view.findViewById(R.id.ciezar)
        val przyciskUsun: ImageButton = view.findViewById(R.id.usunWykonane)
    }
}

class PlanyAdapter(private val onStartClick: (PlanWynik) -> Unit) : RecyclerView.Adapter<PlanyAdapter.PlanViewHolder>() {

    private var lista = mutableListOf<PlanWynik>()

    fun stworzPlany(nowaLista: List<PlanWynik>) {
        lista = nowaLista.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.plan_item, parent, false)
        return PlanViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        val plan = lista[position]
        holder.nazwa.text = plan.nazwa
        holder.opis.text = plan.opis

        // Wyświetlanie nazw ćwiczeń (jeśli je zmapowałeś w ViewModelu)
        holder.listaCwiczenTekst.text = if (plan.nazwyCwiczen.isNotEmpty()) {
            "Ćwiczenia: " + plan.nazwyCwiczen.joinToString(", ")
        } else {
            "Brak przypisanych ćwiczeń"
        }

        holder.przyciskStart.setOnClickListener {
            onStartClick(plan)
        }
    }

    override fun getItemCount() = lista.size

    class PlanViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nazwa: TextView = view.findViewById(R.id.nazwaPlanu)
        val opis: TextView = view.findViewById(R.id.opisPlanu)
        val listaCwiczenTekst: TextView = view.findViewById(R.id.listaCwiczenTekst)
        val przyciskStart: Button = view.findViewById(R.id.startPlanButton)
    }
}