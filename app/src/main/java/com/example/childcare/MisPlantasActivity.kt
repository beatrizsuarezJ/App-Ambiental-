package com.example.childcare

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MisPlantasActivity : AppCompatActivity() {

    private lateinit var recyclerMisPlantas: RecyclerView
    private lateinit var adapter: MisPlantasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_plantas)

        recyclerMisPlantas = findViewById(R.id.recyclerMisPlantas)
        recyclerMisPlantas.layoutManager = LinearLayoutManager(this)

        // Recibir la lista de plantas desde el intent
        val plantas = intent.getParcelableArrayListExtra<PlantaItem>("plantas") ?: listOf()
        adapter = MisPlantasAdapter(plantas.toMutableList())
        recyclerMisPlantas.adapter = adapter
    }
}
