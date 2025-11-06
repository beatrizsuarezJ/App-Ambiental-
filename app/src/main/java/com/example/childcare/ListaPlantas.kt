package com.example.childcare

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class ListaPlantas : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: PlantaAdapter
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_plantas)




        recycler = findViewById(R.id.recyclerPlantas)
        recycler.layoutManager = androidx.recyclerview.widget.GridLayoutManager(this, 2) // 2 columnas
        recycler.setHasFixedSize(true)

        adapter = PlantaAdapter(mutableListOf()) { item ->
            val i = Intent(this, DetallePlanta::class.java)
            i.putExtra("plantId", item.id)
            startActivity(i)
        }
        recycler.adapter = adapter


        dbRef = FirebaseDatabase.getInstance().getReference("plantas")
        escucharCambios()

    }

    private fun escucharCambios() {
        // Escucha en tiempo real (se actualiza sola la lista)
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nuevos = mutableListOf<PlantaItem>()
                for (child in snapshot.children) {
                    val planta = child.getValue(Planta::class.java)
                    val id = child.key
                    if (planta != null && id != null) {
                        nuevos.add(PlantaItem(id, planta))
                    }
                }
                adapter.replaceAll(nuevos)
            }

            override fun onCancelled(error: DatabaseError) {
                // Maneja error si quieres (Toast/Log)
            }
        })
    }
}
