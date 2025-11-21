package com.example.childcare

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MisPlantasAdapter(
    private var listaMisPlantas: MutableList<PlantaItem>
) : RecyclerView.Adapter<MisPlantasAdapter.MiPlantaVH>() {

    // ViewHolder: define cómo se ve cada item de la lista
    inner class MiPlantaVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombrePlanta: TextView = itemView.findViewById(R.id.tvNombreMiPlanta)
        private val tvAlturaPlanta: TextView = itemView.findViewById(R.id.tvAlturaMiPlanta)
        private val tvGrosorPlanta: TextView = itemView.findViewById(R.id.tvGrosorMiPlanta)
        private val tvHojasPlanta: TextView = itemView.findViewById(R.id.tvHojasMiPlanta)
        private val tvTotalPlanta: TextView = itemView.findViewById(R.id.tvTotalMiPlanta)
        private val ivMiPlanta: ImageView = itemView.findViewById(R.id.ivMiPlanta)

        // Bind: aquí "pegamos" los datos al layout
        fun bind(plantaItem: PlantaItem) {
            val planta = plantaItem.planta
            tvNombrePlanta.text = planta.nombre
            tvAlturaPlanta.text = "Altura: ${planta.altura_cm} cm"
            tvGrosorPlanta.text = "Grosor: ${planta.grosor_cm} cm"
            tvHojasPlanta.text = "Hojas: ${planta.hojas}"
            tvTotalPlanta.text = "Total: ${planta.total}"

            // Cargar imagen con Glide
            Glide.with(itemView.context)
                .load(planta.imagenUrl)
                .placeholder(R.drawable.ic_placeholder)
                .into(ivMiPlanta)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MiPlantaVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mi_planta, parent, false)
        return MiPlantaVH(view)
    }

    override fun onBindViewHolder(holder: MiPlantaVH, position: Int) {
        holder.bind(listaMisPlantas[position])
    }

    override fun getItemCount(): Int = listaMisPlantas.size

    fun update(nuevaLista: List<PlantaItem>) {
        listaMisPlantas.clear()
        listaMisPlantas.addAll(nuevaLista)
        notifyDataSetChanged()
    }
}
