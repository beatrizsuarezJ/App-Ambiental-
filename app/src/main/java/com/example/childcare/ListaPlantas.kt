package com.example.childcare

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.google.firebase.auth.FirebaseAuth


class ListaPlantas : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: PlantaAdapter
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_plantas)

        recycler = findViewById(R.id.recyclerPlantas)
        recycler.layoutManager = androidx.recyclerview.widget.GridLayoutManager(this, 2)
        recycler.setHasFixedSize(true)

        adapter = PlantaAdapter(mutableListOf()) { item ->
            val i = Intent(this, DetallePlanta::class.java)
            i.putExtra("plantId", item.id)
            startActivity(i)
        }
        recycler.adapter = adapter

        // ⬅ OBTENER UID
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        // ⬅ NUEVA RUTA CORRECTA
        dbRef = FirebaseDatabase.getInstance()
            .getReference("Usuarios_ChildCare")
            .child(uid!!)
            .child("plantas")

        escucharCambios()
    }

    private fun escucharCambios() {
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

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
