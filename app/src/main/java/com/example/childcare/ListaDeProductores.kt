package com.example.childcare

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ListaDeProductores : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductoresAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView

    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.getReference("Usuarios_ChildCare")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_de_productores)

        recyclerView = findViewById(R.id.rvProductores)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ProductoresAdapter(mutableListOf())
        recyclerView.adapter = adapter

        cargarProductoresDelSupervisor()
    }

    private fun cargarProductoresDelSupervisor() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "No hay usuario logueado", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE

        // Obtener datos del supervisor
        usersRef.child(currentUser.uid).get().addOnSuccessListener { snapSupervisor ->
            if (!snapSupervisor.exists()) {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Datos del usuario no encontrados", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            val rolValue = snapSupervisor.child("rol").value
            val esSupervisor = when (rolValue) {
                is Long -> rolValue == 1L
                is Double -> rolValue == 1.0
                is Int -> rolValue == 1
                else -> false
            }

            if (!esSupervisor) {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Usuario no es supervisor", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            val zonaSupervisor = snapSupervisor.child("zona").value?.toString() ?: ""
            if (zonaSupervisor.isBlank()) {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Zona no definida para el supervisor", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            // Obtener todos los productores de la misma zona
            usersRef.orderByChild("rol").equalTo(2.0).get()
                .addOnSuccessListener { snapshotProds ->
                    val lista = mutableListOf<User>()
                    for (userSnap in snapshotProds.children) {
                        val zonaUser = userSnap.child("zona").value?.toString() ?: ""
                        if (zonaUser == zonaSupervisor) {
                            val u = User(
                                id = userSnap.key,
                                Nombre = userSnap.child("nombre").value?.toString() ?: "",
                                Correo_Electronico = userSnap.child("correo_Electronico").value?.toString() ?: "",
                                rol = userSnap.child("rol").value?.toString()?.toInt() ?: 2,
                                Zona = zonaUser
                            )
                            lista.add(u)
                        }
                    }

                    progressBar.visibility = View.GONE
                    tvEmpty.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
                    adapter.update(lista)
                }
                .addOnFailureListener { e ->
                    progressBar.visibility = View.GONE
                    e.printStackTrace()
                    Toast.makeText(this, "Error al obtener productores: ${e.message}", Toast.LENGTH_LONG).show()
                }

        }.addOnFailureListener { e ->
            progressBar.visibility = View.GONE
            Toast.makeText(this, "Error al leer usuario: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
