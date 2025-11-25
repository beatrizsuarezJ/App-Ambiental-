package com.example.childcare

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.DateFormat
import java.util.*

class PlantasAdapter(
    private val items: List<PlantaData>,
    private val onItemClick: (PlantaData) -> Unit
) : RecyclerView.Adapter<PlantasAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.imgPlanta)
        val nombre: TextView = view.findViewById(R.id.txtNombrePlanta)
        val altura: TextView = view.findViewById(R.id.txtAltura)
        val grosor: TextView = view.findViewById(R.id.txtGrosor)
        val hojas: TextView = view.findViewById(R.id.txtHojas)
        val total: TextView = view.findViewById(R.id.txtTotal)
        val fecha: TextView = view.findViewById(R.id.txtFecha)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_planta_superadmin, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = items[position]

        holder.nombre.text = p.nombre.ifEmpty { "Sin nombre" }
        holder.altura.text = "Altura: ${p.altura_cm} cm"
        holder.grosor.text = "Grosor: ${p.grosor_cm} cm"
        holder.hojas.text = "Hojas: ${p.hojas}"
        holder.total.text = "Total: ${p.total}"

        // Formatear fecha si existe creadoEn; si no, lo quitamos.
        // PlantaData no tiene creadoEn por ahora; si lo agregas, usa: DateFormat.getDateTimeInstance(...).format(Date(creadoEn))
        holder.fecha.visibility = View.GONE

        // Carga de imagen con Glide (fallback si url vacío)
        if (p.imagenUrl.isNotBlank()) {
            Glide.with(holder.img.context)
                .load(p.imagenUrl)
                .centerCrop()
                .placeholder(R.color.downloadPdf) // placeholder neutro, ajusta si quieres
                .error(R.drawable.ic_broken_image) // agrega un drawable para error
                .into(holder.img)
        } else {
            holder.img.setImageResource(R.drawable.ic_broken_image)
        }

        // Animación simple (fade + translate)
        holder.itemView.alpha = 0f
        holder.itemView.translationY = 30f
        holder.itemView.animate()
            .alpha(1f)
            .translationY(0f)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setDuration(280)
            .start()

        holder.itemView.setOnClickListener { onItemClick(p) }
    }

    override fun getItemCount(): Int = items.size
}
