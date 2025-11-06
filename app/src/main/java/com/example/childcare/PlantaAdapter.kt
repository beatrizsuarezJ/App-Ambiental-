package com.example.childcare

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlantaAdapter(
    private val items: MutableList<PlantaItem>,
    private val onClick: (PlantaItem) -> Unit
) : RecyclerView.Adapter<PlantaAdapter.VH>() {

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNombre: TextView = itemView.findViewById(R.id.txtNombre)
        val txtDetalles: TextView = itemView.findViewById(R.id.txtDetalles)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_planta_card, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val p = item.planta
        holder.txtNombre.text = p.nombre ?: "(sin nombre)"
        holder.txtDetalles.text = "Grosor: ${p.grosor_cm ?: "-"} cm • Altura: ${p.altura_cm ?: "-"} cm"
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = items.size

    fun replaceAll(newItems: List<PlantaItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}

