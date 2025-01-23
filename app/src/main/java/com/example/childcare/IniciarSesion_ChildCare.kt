package com.example.childcare

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class IniciarSesion_ChildCare : AppCompatActivity() {

    /*Declaramos dos varibales las cuales son las que nos daran
la autenticacion para poder loguearnos*/
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.iniciar_sesion_child_care)

        //Hacemos un codigo para ocultar y mostrar contraseña
        val passwordEditTexto = findViewById<EditText>(R.id.edtPassword)
        val showPasswordCheckBox = findViewById<CheckBox>(R.id.showPasswordCheckBox)

        //hacemos uso de la declaracion de variables y hacemos un ciclo pa ver u ocultar
        showPasswordCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Mostrar contraseña
                passwordEditTexto.transformationMethod = null
            } else {
                // Ocultar contraseña
                passwordEditTexto.transformationMethod = PasswordTransformationMethod()
            }
            // Mueve el cursor al final del texto
            passwordEditTexto.setSelection(passwordEditTexto.text.length)
        }

        /*Declaramos variables del xml para darle funcionamiento el Login*/
        val btnCrearCuenta : TextView = findViewById(R.id.btnRegistrarse)
        val btnOlvidePassword : TextView = findViewById(R.id.btnOlvidePass)
        val btingresar: Button =findViewById(R.id.btnLoguear)
        val txtemail: TextView =findViewById(R.id.edtEmailCrearNew)
        val txtpass: TextView =findViewById(R.id.edtPassword)

        //iniciamos firebase
        firebaseAuth = Firebase.auth

        //le damos funcionalidad al boton de registrarse
        btnCrearCuenta.setOnClickListener {
            val i = Intent(this, CrearCuenta_ChildCare::class.java)
            startActivity(i)

        }

        //le damos funcionamiento ak boton de olvide mi contraseña
        btnOlvidePassword.setOnClickListener{
            val i = Intent(this, RecuperarPassword_ChildCare::class.java)
            startActivity(i)
        }

        // Funcionalidad del botón "Ingresar"
        btingresar.setOnClickListener()
        {
            try{
                signIn(txtemail.text.toString(),txtpass.text.toString())
            } catch (e: Exception) {
                mostrarToastPersonalizadoAdvertencia(this, "¡Ingresa tu Email y Contraseña!")
            }
        }
    }

    //le damos funcionamiento al singnIn para poder loguearnos
    private fun signIn(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) { // si la autenticacion es exitosa
                    val user = firebaseAuth.currentUser
                    val verifica = user?.isEmailVerified
                    if (verifica == true) { //si el correo es vericado
                        val currentUserRef = FirebaseDatabase.getInstance().getReference("Usuarios_ChildCare").child(user.uid)
                        currentUserRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                val userRole = dataSnapshot.child("rol").getValue(Int::class.java)
                                if (userRole == 0) {
                                    // Redirigir al administrador a la actividad de administrador
                                    val intent = Intent(this@IniciarSesion_ChildCare, Index_ChildCare::class.java)
                                    startActivity(intent) //dirige al index del admionistradoer
                                } else {
                                    // Redirigir al usuario normal a la actividad de usuario normal
                                    val intent = Intent(this@IniciarSesion_ChildCare, Index_ChildCare_User::class.java)
                                    intent.putExtra("id",user.uid.toString())
                                    startActivity(intent) //dirige al index del usuario
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                // Manejar error de base de datos
                            }
                        })
                    } else {
                        //Toast.makeText(baseContext, "No ha verificado su correo", Toast.LENGTH_SHORT).show()
                        mostrarToastPersonalizadoAdvertencia(this, "¡El Correo No Ha Sido Verificado!")
                    }
                } else {
                    //Toast.makeText(baseContext, "Error de Email o Contraseña", Toast.LENGTH_SHORT).show()
                    mostrarToastPersonalizadoError(this, "¡Error De Email o Contraseña!")
                }
            }
    }

    //funcion para el diseño del toast de advertencia
    private fun mostrarToastPersonalizadoAdvertencia(context: Context, mensaje: String) {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.toast_advertencia, null)

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

    //funcion para el diseño del toast de error
    fun mostrarToastPersonalizadoError(context: Context, mensaje: String) {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.toast_error, null)

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

}