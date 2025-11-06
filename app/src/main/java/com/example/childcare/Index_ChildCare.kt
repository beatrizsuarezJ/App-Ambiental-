package com.example.childcare

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.core.view.View
import com.google.firebase.ktx.Firebase
import org.imaginativeworld.whynotimagecarousel.ImageCarousel
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem

class Index_ChildCare : AppCompatActivity() {

    // Declara una instancia de FirebaseAuth
    private lateinit var firebaseAuth: FirebaseAuth

    // Declara una referencia a Firebase Realtime Database
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_index_child_care)

        //mostrar un toast para de bienbenido
        mostrarToastPersonalizadoEntrar(this, "¡Bienvenido A App Ambiental")

        //declaramos botnes de la pantalla principal
        val btnAsistenciAdmin = findViewById<Button>(R.id.btnAsistencia)
        val btnTemperaturaAdmin = findViewById<Button>(R.id.btnTemperatura)
        val btnRegistro = findViewById<Button>(R.id.btnUbicaionAdmin)

        val btnRegistroPlanta = findViewById<Button>(R.id.btnRegistroPlantas)

        val btnListDePlantas = findViewById<Button>(R.id.btnListaPlantas)


        //damos funcionalidad a ambos botenes!
        //btnAgregarFoto
        btnAsistenciAdmin.setOnClickListener {
            val i = Intent(this, Asistencia_ChildCare::class.java)
            startActivity(i)
        }
        //btnControlDePlantas
        btnTemperaturaAdmin.setOnClickListener {
            val i = Intent(this, Temperatura_ChildCare::class.java)
            startActivity(i)
        }
        //Geolocalizacion
        btnRegistro.setOnClickListener{
            val i = Intent(this,Location_ChildCare_User::class.java)
            startActivity(i)
        }

        //Boton para el registro
        btnRegistroPlanta.setOnClickListener {
            val i = Intent(this, RegistroForestAdmin::class.java)
            startActivity(i)
        }

        //Boton para las listas de las plantas
        btnListDePlantas.setOnClickListener {
            val i = Intent(this, ListaPlantas::class.java)
            startActivity(i)
        }


        // Inicializar Firebase Auth y Realtime Database
        firebaseAuth = Firebase.auth
        database = FirebaseDatabase.getInstance()

        // Obtener el usuario actualmente autenticado
        val currentUser = firebaseAuth.currentUser
        val uid = currentUser?.uid

        // Verificar si el usuario está autenticado
        if (uid != null) {
            // Si el usuario está autenticado, se obtiene una referencia a la ubicación en la base de datos
            val userRef = database.reference.child("Usuarios_ChildCare").child(uid)

            // Se agrega un listener para obtener el nombre del usuario desde la base de datos
            userRef.child("nombre").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val nombreUsuario = dataSnapshot.getValue(String::class.java)
                    actualizarInterfazUsuario(nombreUsuario)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Manejar errores de la base de datos, si es necesario
                }
            })
        }

        // Configuración del botón para cerrar sesión en este caso imagen
        val btnSalir = findViewById<ImageView>(R.id.logOutExitSesion)
        btnSalir.setOnClickListener {
            signOut()
        }

        // Configuración del ImageCarousel
        val carousel: ImageCarousel = findViewById(R.id.carousel)
        val list = mutableListOf<CarouselItem>()

        // Agregar elementos al carousel
        list.add(CarouselItem("https://i.pinimg.com/736x/50/08/cb/5008cbf9384338415f28dbe692af0544.jpg"))
        list.add(CarouselItem("https://i.pinimg.com/736x/46/f5/06/46f5063c1f1094f7aaf274e88450c8a5.jpg"))
        list.add(CarouselItem("https://i.pinimg.com/736x/86/60/8f/86608fb9bb9cfa88e0d02c992c2f5e4c.jpg"))
        list.add(CarouselItem("https://i.pinimg.com/736x/b4/27/4c/b4274cc13a02b35241157a9134c05b5e.jpg"))
        list.add(CarouselItem("https://i.pinimg.com/736x/a4/4a/20/a44a20a0885a741dfd191158050f2036.jpg"))
        list.add(CarouselItem("https://i.pinimg.com/736x/c7/5f/bb/c75fbb65bbab52834d8694f73c4b2031.jpg"))
        list.add(CarouselItem("https://i.pinimg.com/736x/e3/1f/12/e31f123922627004e5ea18e082afebfe.jpg"))
        list.add(CarouselItem("https://i.pinimg.com/736x/94/8b/e2/948be2fb0f7fe77e98c993852e9f9e27.jpg"))
        carousel.addData(list)

        // Configurar el ImageCarousel para el desplazamiento automático
        carousel.autoPlay = true
        carousel.autoPlayDelay = 2500
        carousel.showNavigationButtons = false
        carousel.addData(list)
    }

    //funcion para el diseño del toast de acces
    private fun mostrarToastPersonalizadoEntrar(context: Context, mensaje: String) {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.toast_succes, null)

        val text = layout.findViewById<TextView>(R.id.text)
        text.text = mensaje

        val icon = layout.findViewById<ImageView>(R.id.icon)
        icon.setImageResource(R.drawable.advertencia)

        with (Toast(context)) {
            duration = Toast.LENGTH_SHORT
            view = layout
            show()
        }
    }

    // Método para cerrar sesión
    private fun signOut() {
        // Cerrar sesión en Firebase Auth
        firebaseAuth.signOut()
        //mostramos un toast personalizado
        mostrarToastLogout(this,"¡Sesión Cerrada Exitosamente!") // Mostrar el toast antes de iniciar una nueva actividad
        // Redirigir al usuario a la pantalla de inicio de sesión
        val intent = Intent(this, IniciarSesion_ChildCare::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Finalizar la actividad actual
    }
    //funcion para el toast de cerrar sesion
    fun mostrarToastLogout(context: Context, mensaje: String) {
        //declaramos variables
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.toast_logout, null)

        val text = layout.findViewById<TextView>(R.id.text)
        text.text = mensaje

        val icon = layout.findViewById<ImageView>(R.id.icon)
        icon.setImageResource(R.drawable.cerrar_sesion)

        with (Toast(context)) {
            duration = Toast.LENGTH_SHORT
            view = layout
            show()
        }
    }

    // Método para actualizar la interfaz de usuario con el nombre del usuario
    private fun actualizarInterfazUsuario(nombreUsuario: String?) {
        val textViewNombreUsuario = findViewById<TextView>(R.id.userActual)
        textViewNombreUsuario.text = nombreUsuario
    }

    override fun onBackPressed() {
        // Eliminar la llamada a super.onBackPressed() para evitar que el usuario regrese atrás
    }

    fun signOut(view: android.view.View) {}

}
