package com.example.childcare

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import org.imaginativeworld.whynotimagecarousel.ImageCarousel
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem

class SuperAdmin : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    // Layouts
    private lateinit var layoutPrincipal: View
    private lateinit var layoutZonas: View
    private lateinit var layoutUsuarios: View
    private lateinit var layoutPlantas: View

    private var zonaActual = ""
    private var usuarioActualId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_super_admin)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Firebase
        firebaseAuth = Firebase.auth
        database = FirebaseDatabase.getInstance()

        // Layouts
        layoutPrincipal = findViewById(R.id.layoutPrincipal)
        layoutZonas = findViewById(R.id.layoutZonas)
        layoutUsuarios = findViewById(R.id.layoutUsuarios)
        layoutPlantas = findViewById(R.id.layoutPlantas)

        // Carousel
        val carousel: ImageCarousel = findViewById(R.id.carousel)
        val carouselList = mutableListOf(
            CarouselItem("https://i.pinimg.com/736x/50/08/cb/5008cbf9384338415f28dbe692af0544.jpg"),
            CarouselItem("https://i.pinimg.com/736x/46/f5/06/46f5063c1f1094f7aaf274e88450c8a5.jpg"),
            CarouselItem("https://i.pinimg.com/736x/86/60/8f/86608fb9bb9cfa88e0d02c992c2f5e4c.jpg"),
            CarouselItem("https://i.pinimg.com/736x/b4/27/4c/b4274cc13a02b35241157a9134c05b5e.jpg")
        )
        carousel.addData(carouselList)
        carousel.autoPlay = true
        carousel.autoPlayDelay = 2500
        carousel.showNavigationButtons = false

        // Toast de bienvenida
        mostrarToastPersonalizadoEntrar(this, "¡Bienvenido A App Ambiental")

        // Mostrar nombre del usuario
        val currentUser = firebaseAuth.currentUser
        val uid = currentUser?.uid
        if (uid != null) {
            val userRef = database.reference.child("Usuarios_ChildCare").child(uid)
            userRef.child("nombre").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nombreUsuario = snapshot.getValue(String::class.java)
                    actualizarInterfazUsuario(nombreUsuario)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }

        // Botón cerrar sesión
        findViewById<ImageView>(R.id.logOutExitSesion).setOnClickListener { signOut() }

        // Botón Zonas
        findViewById<View>(R.id.btnZonas).setOnClickListener { mostrarZonas() }

        // Botones regresar
        findViewById<View>(R.id.btnBackFromZonas).setOnClickListener { regresarPrincipal() }
        findViewById<View>(R.id.btnBackFromUsuarios).setOnClickListener { mostrarZonas() }
        findViewById<View>(R.id.btnBackFromPlantas).setOnClickListener { mostrarUsuariosActual() }
    }

    // --- Toasts ---
    private fun mostrarToastPersonalizadoEntrar(context: Context, mensaje: String) {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.toast_succes, null)
        val text = layout.findViewById<TextView>(R.id.text)
        text.text = mensaje
        val icon = layout.findViewById<ImageView>(R.id.icon)
        icon.setImageResource(R.drawable.advertencia)
        with (Toast(context)) { duration = Toast.LENGTH_SHORT; view = layout; show() }
    }

    private fun mostrarToastLogout(context: Context, mensaje: String) {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.toast_logout, null)
        val text = layout.findViewById<TextView>(R.id.text)
        text.text = mensaje
        val icon = layout.findViewById<ImageView>(R.id.icon)
        icon.setImageResource(R.drawable.cerrar_sesion)
        with (Toast(context)) { duration = Toast.LENGTH_SHORT; view = layout; show() }
    }

    // --- Función cerrar sesión ---
    private fun signOut() {
        firebaseAuth.signOut()
        mostrarToastLogout(this, "¡Sesión Cerrada Exitosamente!")
        val intent = Intent(this, IniciarSesion_ChildCare::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // --- Actualizar nombre ---
    private fun actualizarInterfazUsuario(nombreUsuario: String?) {
        val textViewNombreUsuario = findViewById<TextView>(R.id.userActual)
        textViewNombreUsuario.text = nombreUsuario
    }

    override fun onBackPressed() {
        // Evitar que el botón físico de retroceso haga nada
    }

    // --- Funciones de navegación ---
    private fun regresarPrincipal() {
        layoutPrincipal.visibility = View.VISIBLE
        layoutZonas.visibility = View.GONE
        layoutUsuarios.visibility = View.GONE
        layoutPlantas.visibility = View.GONE
    }

    private fun mostrarZonas() {
        layoutPrincipal.visibility = View.GONE
        layoutZonas.visibility = View.VISIBLE
        layoutUsuarios.visibility = View.GONE
        layoutPlantas.visibility = View.GONE

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerZonas)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val zonasRef = database.reference.child("Usuarios_ChildCare")
        zonasRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val zonasSet = mutableSetOf<String>()
                snapshot.children.forEach { userSnap ->
                    val zona = userSnap.child("zona").getValue(String::class.java)
                    if (zona != null) zonasSet.add(zona)
                }
                val listaZonas = zonasSet.map { Zona(it) }.toMutableList()
                val adapter = GenericAdapter(listaZonas, R.layout.item_simple_text) { zona, view ->
                    val tv = view.findViewById<TextView>(R.id.tvItem)
                    tv.text = zona.nombre
                    view.setOnClickListener { mostrarUsuariosZona(zona.nombre) }
                }
                recyclerView.adapter = adapter
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun mostrarUsuariosZona(nombreZona: String) {
        zonaActual = nombreZona
        layoutPrincipal.visibility = View.GONE
        layoutZonas.visibility = View.GONE
        layoutUsuarios.visibility = View.VISIBLE
        layoutPlantas.visibility = View.GONE

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerUsuarios)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val usuariosRef = database.reference.child("Usuarios_ChildCare")
        usuariosRef.orderByChild("zona").equalTo(nombreZona)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val listaUsuarios = snapshot.children.map { userSnap ->
                        Usuario(
                            uid = userSnap.key ?: "",
                            nombre = userSnap.child("nombre").getValue(String::class.java) ?: "",
                            correo_Electronico = userSnap.child("correo_Electronico").getValue(String::class.java) ?: "",
                            rol = userSnap.child("rol").getValue(Int::class.java) ?: 0,
                            zona = userSnap.child("zona").getValue(String::class.java) ?: ""
                        )
                    }.toMutableList()

                    val adapter = GenericAdapter(listaUsuarios, R.layout.item_simple_text) { usuario, view ->
                        val tv = view.findViewById<TextView>(R.id.tvItem)
                        tv.text = usuario.nombre
                        view.setOnClickListener { mostrarPlantasUsuario(usuario.uid) }
                    }
                    recyclerView.adapter = adapter
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun mostrarPlantasUsuario(uid: String) {
        usuarioActualId = uid
        layoutPrincipal.visibility = View.GONE
        layoutZonas.visibility = View.GONE
        layoutUsuarios.visibility = View.GONE
        layoutPlantas.visibility = View.VISIBLE

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerPlantas)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val plantasRef = database.reference.child("Usuarios_ChildCare").child(uid).child("plantas")
        plantasRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaPlantas = snapshot.children.map { plantaSnap ->
                    PlantaData(
                        nombre = plantaSnap.child("nombre").getValue(String::class.java) ?: "",
                        altura_cm = plantaSnap.child("altura_cm").getValue(Int::class.java) ?: 0,
                        grosor_cm = plantaSnap.child("grosor_cm").getValue(Int::class.java) ?: 0,
                        hojas = plantaSnap.child("hojas").getValue(Int::class.java) ?: 0,
                        total = plantaSnap.child("total").getValue(Int::class.java) ?: 0,
                        imagenUrl = plantaSnap.child("imagenUrl").getValue(String::class.java) ?: ""
                    )
                }.toMutableList()

                val adapter = GenericAdapter(listaPlantas, R.layout.item_simple_text) { planta, view ->
                    val tv = view.findViewById<TextView>(R.id.tvItem)
                    tv.text = planta.nombre
                }
                recyclerView.adapter = adapter
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun mostrarUsuariosActual() {
        mostrarUsuariosZona(zonaActual)
    }
}
