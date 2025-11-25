package com.example.childcare

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
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

        // Evitar botón físico Back
        onBackPressedDispatcher.addCallback(this) {
            Toast.makeText(this@SuperAdmin, "Acción no permitida", Toast.LENGTH_SHORT).show()
        }

        // Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // Firebase
        firebaseAuth = Firebase.auth
        database = FirebaseDatabase.getInstance()

        // Instancias layouts
        layoutPrincipal = findViewById(R.id.layoutPrincipal)
        layoutZonas = findViewById(R.id.layoutZonas)
        layoutUsuarios = findViewById(R.id.layoutUsuarios)
        layoutPlantas = findViewById(R.id.layoutPlantas)

        // Carrusel
        configurarCarousel()

        // Toast bienvenida
        mostrarToastPersonalizadoEntrar(this, "¡Bienvenido a App Ambiental!")

        // Cargar nombre del usuario
        cargarNombreUsuario()

        // Botón cerrar sesión
        findViewById<ImageView>(R.id.logOutExitSesion).setOnClickListener { signOut() }

        // Botón entrar a Zonas
        findViewById<View>(R.id.btnZonas).setOnClickListener { mostrarZonas() }

        // Botones regresar
        findViewById<View>(R.id.btnBackFromZonas).setOnClickListener { regresarPrincipal() }
        findViewById<View>(R.id.btnBackFromUsuarios).setOnClickListener { mostrarZonas() }
        findViewById<View>(R.id.btnBackFromPlantas).setOnClickListener { mostrarUsuariosActual() }
    }

    // -------------------------------------------------------------------------
    // C A R R U S E L
    // -------------------------------------------------------------------------
    private fun configurarCarousel() {
        val carousel: ImageCarousel = findViewById(R.id.carousel)
        carousel.addData(
            listOf(
                CarouselItem("https://i.pinimg.com/736x/50/08/cb/5008cbf9384338415f28dbe692af0544.jpg"),
                CarouselItem("https://i.pinimg.com/736x/46/f5/06/46f5063c1f1094f7aaf274e88450c8a5.jpg"),
                CarouselItem("https://i.pinimg.com/736x/86/60/8f/86608fb9bb9cfa88e0d02c992c2f5e4c.jpg"),
                CarouselItem("https://i.pinimg.com/736x/b4/27/4c/b4274cc13a02b35241157a9134c05b5e.jpg")
            )
        )
        carousel.autoPlay = true
        carousel.autoPlayDelay = 2500
        carousel.showNavigationButtons = false
    }

    // -------------------------------------------------------------------------
    // U S U A R I O
    // -------------------------------------------------------------------------
    private fun cargarNombreUsuario() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        val ref = database.reference.child("Usuarios_ChildCare").child(uid).child("nombre")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                findViewById<TextView>(R.id.userActual).text =
                    snapshot.getValue(String::class.java) ?: ""
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun signOut() {
        firebaseAuth.signOut()
        mostrarToastLogout(this, "¡Sesión Cerrada Exitosamente!")
        startActivity(Intent(this, IniciarSesion_ChildCare::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    // -------------------------------------------------------------------------
    // T O A S T S
    // -------------------------------------------------------------------------
    private fun mostrarToastPersonalizadoEntrar(context: Context, mensaje: String) {
        val view = LayoutInflater.from(context).inflate(R.layout.toast_succes, null)
        view.findViewById<TextView>(R.id.text).text = mensaje
        view.findViewById<ImageView>(R.id.icon).setImageResource(R.drawable.advertencia)

        Toast(context).apply {
            duration = Toast.LENGTH_SHORT
            this.view = view
            show()
        }
    }

    private fun mostrarToastLogout(context: Context, mensaje: String) {
        val view = LayoutInflater.from(context).inflate(R.layout.toast_logout, null)
        view.findViewById<TextView>(R.id.text).text = mensaje
        view.findViewById<ImageView>(R.id.icon).setImageResource(R.drawable.cerrar_sesion)

        Toast(context).apply {
            duration = Toast.LENGTH_SHORT
            this.view = view
            show()
        }
    }

    // -------------------------------------------------------------------------
    // N A V E G A C I Ó N   D E   L A Y O U T S
    // -------------------------------------------------------------------------
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

        findViewById<TextView>(R.id.titleZonas).text = "ZONAS"

        val recycler = findViewById<RecyclerView>(R.id.recyclerZonas)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        val ref = database.reference.child("Usuarios_ChildCare")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val zonas = snapshot.children
                    .mapNotNull { it.child("zona").getValue(String::class.java) }
                    .toSet()
                    .map { Zona(it) }

                recycler.adapter = GenericAdapter(zonas, R.layout.item_simple_text) { z, view ->
                    view.findViewById<TextView>(R.id.tvItem).text = z.nombre
                    view.setOnClickListener { mostrarUsuariosZona(z.nombre) }
                }
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

        findViewById<TextView>(R.id.titleUsuarios).text = "USUARIOS — Zona: $nombreZona"

        val recycler = findViewById<RecyclerView>(R.id.recyclerUsuarios)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        val ref = database.reference.child("Usuarios_ChildCare")
        ref.orderByChild("zona").equalTo(nombreZona)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val usuarios = snapshot.children.map {
                        Usuario(
                            uid = it.key ?: "",
                            nombre = it.child("nombre").getValue(String::class.java) ?: "",
                            correo_Electronico = it.child("correo_Electronico").getValue(String::class.java) ?: "",
                            rol = it.child("rol").getValue(Int::class.java) ?: 0,
                            zona = it.child("zona").getValue(String::class.java) ?: ""
                        )
                    }

                    recycler.adapter =
                        GenericAdapter(usuarios, R.layout.item_simple_text) { u, view ->
                            view.findViewById<TextView>(R.id.tvItem).text = u.nombre
                            view.setOnClickListener { mostrarPlantasUsuario(u.uid, u.nombre) }
                        }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }


    private fun mostrarPlantasUsuario(uid: String, nombreUsuario: String) {
        usuarioActualId = uid

        layoutPrincipal.visibility = View.GONE
        layoutZonas.visibility = View.GONE
        layoutUsuarios.visibility = View.GONE
        layoutPlantas.visibility = View.VISIBLE

        findViewById<TextView>(R.id.titlePlantas).text = "PLANTAS — $nombreUsuario"

        val recycler = findViewById<RecyclerView>(R.id.recyclerPlantas)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        val ref = database.reference.child("Usuarios_ChildCare").child(uid).child("plantas")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val plantas = snapshot.children.map {
                    PlantaData(
                        nombre = it.child("nombre").getValue(String::class.java) ?: "",
                        altura_cm = it.child("altura_cm").getValue(Int::class.java) ?: 0,
                        grosor_cm = it.child("grosor_cm").getValue(Int::class.java) ?: 0,
                        hojas = it.child("hojas").getValue(Int::class.java) ?: 0,
                        total = it.child("total").getValue(Int::class.java) ?: 0,
                        imagenUrl = it.child("imagenUrl").getValue(String::class.java) ?: "",
                    )
                }

                recycler.adapter = PlantasAdapter(plantas) { planta ->
                    Toast.makeText(this@SuperAdmin, planta.nombre, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun mostrarUsuariosActual() {
        mostrarUsuariosZona(zonaActual)
    }
}
