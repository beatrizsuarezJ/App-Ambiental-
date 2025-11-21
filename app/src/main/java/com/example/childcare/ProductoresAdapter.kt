package com.example.childcare

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class ProductoresAdapter(
    private var lista: MutableList<User>
) : RecyclerView.Adapter<ProductoresAdapter.ProductorVH>() {

    inner class ProductorVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        private val tvCorreo: TextView = itemView.findViewById(R.id.tvCorreo)
        private val tvZona: TextView = itemView.findViewById(R.id.tvZona)

        fun bind(u: User) {
            tvNombre.text = u.Nombre
            tvCorreo.text = u.Correo_Electronico
            tvZona.text = u.Zona

            // CLICK: cargar plantas del productor
            itemView.setOnClickListener {
                u.id?.let { idProd ->
                    cargarPlantasDelProductor(idProd)
                } ?: run {
                    Toast.makeText(itemView.context, "ID del productor no disponible", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun cargarPlantasDelProductor(productorId: String) {
            val database = FirebaseDatabase.getInstance()
            val plantasRef = database.getReference("Usuarios_ChildCare")
                .child(productorId)
                .child("plantas")

            plantasRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val listaPlantas = mutableListOf<PlantaItem>()
                    for (plantSnapshot in snapshot.children) {
                        val planta = plantSnapshot.getValue(Planta::class.java)
                        val id = plantSnapshot.key ?: ""
                        if (planta != null) {
                            listaPlantas.add(PlantaItem(id, planta))
                        }
                    }

                    if (listaPlantas.isNotEmpty()) {
                        // Abrir activity con lista de plantas
                        val intent = Intent(itemView.context, MisPlantasActivity::class.java)
                        intent.putParcelableArrayListExtra("plantas", ArrayList(listaPlantas))
                        itemView.context.startActivity(intent)
                    } else {
                        Toast.makeText(itemView.context, "Este productor no tiene plantas", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(itemView.context, "Error al cargar plantas: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductorVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_productor, parent, false)
        return ProductorVH(view)
    }

    override fun onBindViewHolder(holder: ProductorVH, position: Int) {
        holder.bind(lista[position])
    }

    override fun getItemCount(): Int = lista.size

    fun update(newList: List<User>) {
        lista.clear()
        lista.addAll(newList)
        notifyDataSetChanged()
    }
}
