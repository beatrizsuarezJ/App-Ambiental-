package com.example.childcare

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import org.imaginativeworld.whynotimagecarousel.ImageCarousel
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem

class ProductoresModerables_MiniAdmin : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_productores_moderables_mini_admin)

        // Inicializar Firebase
        firebaseAuth = Firebase.auth
        database = FirebaseDatabase.getInstance()

        // Mostrar un toast de bienvenida
        mostrarToastPersonalizadoEntrar(this, "¡Bienvenido A App Ambiental!")

        // Obtener nombre del usuario
        val currentUser = firebaseAuth.currentUser
        val uid = currentUser?.uid
        if (uid != null) {
            val userRef = database.reference.child("Usuarios_ChildCare").child(uid)
            userRef.child("nombre").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val nombreUsuario = dataSnapshot.getValue(String::class.java)
                    actualizarInterfazUsuario(nombreUsuario)
                }
                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }

        // Botón para abrir lista de productores
        val btnProductores = findViewById<Button>(R.id.btnProductores)
        btnProductores.setOnClickListener {
            val i = Intent(this, ListaDeProductores::class.java)
            startActivity(i)
        }

        // Botón para cerrar sesión
        val btnSalir = findViewById<ImageView>(R.id.logOutExitSesion)
        btnSalir.setOnClickListener {
            signOut()
        }

        // Configurar ImageCarousel
        val carousel: ImageCarousel = findViewById(R.id.carousel)
        val list = mutableListOf<CarouselItem>()
        list.add(CarouselItem("https://i.pinimg.com/736x/50/08/cb/5008cbf9384338415f28dbe692af0544.jpg"))
        list.add(CarouselItem("https://i.pinimg.com/736x/46/f5/06/46f5063c1f1094f7aaf274e88450c8a5.jpg"))
        list.add(CarouselItem("https://i.pinimg.com/736x/86/60/8f/86608fb9bb9cfa88e0d02c992c2f5e4c.jpg"))
        list.add(CarouselItem("https://i.pinimg.com/736x/b4/27/4c/b4274cc13a02b35241157a9134c05b5e.jpg"))
        list.add(CarouselItem("https://i.pinimg.com/736x/a4/4a/20/a44a20a0885a741dfd191158050f2036.jpg"))
        list.add(CarouselItem("https://i.pinimg.com/736x/c7/5f/bb/c75fbb65bbab52834d8694f73c4b2031.jpg"))
        list.add(CarouselItem("https://i.pinimg.com/736x/e3/1f/12/e31f123922627004e5ea18e082afebfe.jpg"))
        list.add(CarouselItem("https://i.pinimg.com/736x/94/8b/e2/948be2fb0f7fe77e98c993852e9f9e27.jpg"))
        carousel.addData(list)
        carousel.autoPlay = true
        carousel.autoPlayDelay = 2500
        carousel.showNavigationButtons = false
    }

    // Función para mostrar toast personalizado de entrada
    private fun mostrarToastPersonalizadoEntrar(context: Context, mensaje: String) {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.toast_succes, null)
        val text = layout.findViewById<TextView>(R.id.text)
        text.text = mensaje
        val icon = layout.findViewById<ImageView>(R.id.icon)
        icon.setImageResource(R.drawable.advertencia)
        with(Toast(context)) {
            duration = Toast.LENGTH_SHORT
            view = layout
            show()
        }
    }

    // Función para cerrar sesión
    private fun signOut() {
        firebaseAuth.signOut()
        mostrarToastLogout(this, "¡Sesión Cerrada Exitosamente!")
        val intent = Intent(this, IniciarSesion_ChildCare::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // Toast personalizado para logout
    private fun mostrarToastLogout(context: Context, mensaje: String) {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.toast_logout, null)
        val text = layout.findViewById<TextView>(R.id.text)
        text.text = mensaje
        val icon = layout.findViewById<ImageView>(R.id.icon)
        icon.setImageResource(R.drawable.cerrar_sesion)
        with(Toast(context)) {
            duration = Toast.LENGTH_SHORT
            view = layout
            show()
        }
    }

    // Actualizar TextView con el nombre del usuario
    private fun actualizarInterfazUsuario(nombreUsuario: String?) {
        val textViewNombreUsuario = findViewById<TextView>(R.id.userActual)
        textViewNombreUsuario.text = nombreUsuario
    }

    override fun onBackPressed() {
        // Evitar volver atrás
    }
}
