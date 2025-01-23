package com.example.childcare

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Asistencia_ChildCare : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdapterKids
    private lateinit var database: DatabaseReference
    private lateinit var usersArrayList: ArrayList<Kids>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asistencia_child_care)

        //para regresar al index
        val btnRegresar = findViewById<ImageView>(R.id.btnRegresar)



        btnRegresar.setOnClickListener{
            val i = Intent(this, Index_ChildCare::class.java)
            startActivity(i)
        }



        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        usersArrayList = arrayListOf()

        adapter = AdapterKids(usersArrayList, object : AdapterKids.OnItemClickListener {
            override fun onItemClick(user: Kids) {
                // Aquí puedes manejar el clic en un elemento del RecyclerView si lo necesitas
            }
        })
        // Escuchar cambios en los datos de Firebase
        recyclerView.adapter = adapter



        database = FirebaseDatabase.getInstance().getReference("Usuarios_ChildCare")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    usersArrayList.clear()
                    for (userSnapshot in snapshot.children) {
                        val userId = userSnapshot.key // Obtener el ID del usuario
                        Log.d("Asistencia_ChildCare", "ID del usuario: $userId") // Mostrar el ID del usuario en el registro

                        // Obtener la referencia al nodo "formulario" dentro de cada usuario
                        val formularioRef = userSnapshot.child("Formulario").ref

                        formularioRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                // Verificar si existe algún dato dentro del nodo "formulario"
                                if (dataSnapshot.exists()) {
                                    // Obtener los datos del formulario
                                    val nombre = dataSnapshot.child("nombre").getValue(String::class.java)
                                    val asistencia = dataSnapshot.child("asistencia").getValue(String::class.java)
                                    val id = dataSnapshot.child("id").getValue(String::class.java)



                                    // Crear un objeto Kids con los datos obtenidos
                                    val kid = Kids(nombre ?: "", "", asistencia ?: "", "", "", "", "", "", "",id?:"")

                                    // Agregar el objeto a la lista
                                    usersArrayList.add(kid)


                                    recyclerView.adapter = AdapterKids(usersArrayList, object : AdapterKids.OnItemClickListener {
                                        override fun onItemClick(user: Kids) {

                                            llamar(user.id.toString())
                                        }

                                    })


                                    // Notificar al adaptador sobre el cambio en los datos
                                    adapter.notifyDataSetChanged()
                                }
                            }



                            override fun onCancelled(databaseError: DatabaseError) {
                                // Manejar el error de la base de datos si es necesario
                            }
                        })


                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar el error de la base de datos si es necesario
            }
        })


    }

    private fun llamar(id:String)
    {

        val intent = Intent(this,kidsDatos::class.java)
        intent.putExtra("id",id)
        startActivity(intent)
    }


}

