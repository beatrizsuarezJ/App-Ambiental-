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
import com.google.firebase.ktx.Firebase

class RecuperarPassword_ChildCare : AppCompatActivity() {

    /*Declaramos una variable para hacer la recuperacion de la contraseña*/
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recuperar_password_child_care)

        /*Declaramos nuestras variables para la llamada a la BD con el XML*/

        val txtmail : TextView = findViewById(R.id.txtemailRestablecer)
        val btnCambiar : Button = findViewById(R.id.btnLoguear)

        //le damos funcion al boton de cambiar paswword
        btnCambiar.setOnClickListener()
        {
            try {
                sendPasswordReset(txtmail.text.toString())
            } catch (e: Exception) {
                mostrarToastPersonalizadoAdvertencia(this, "¡Ingresa tu Email para Recuperar tu Contraseña!")
            }

        }

        //Inicializamos Firebase
        firebaseAuth= Firebase.auth
    }

    private fun sendPasswordReset(email: String) {

        //iniciamos firebase
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener() { task ->
                if (task.isSuccessful){
                    //damos un mensjae en pantalla y automaticamente nos mande al login luego de hacer le proceso
                    mostrarToastPersonalizadoSucces(this, "¡Correo de Cambio de Contraseña Enviado! \n ¡Revisa Tu Correo!")
                    btnCrearExit()
                }
            }

    }

    //Creamos una funcion para que al hacer click en el boton Crear me regrese al Login
    private fun btnCrearExit()
    {
        firebaseAuth.signOut()
        val i = Intent(this,IniciarSesion_ChildCare::class.java)
        startActivity(i)
    }

    //funcion para el diseño del toast de acces
    private fun mostrarToastPersonalizadoSucces(context: Context, mensaje: String) {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.toast_succes, null)

        val text = layout.findViewById<TextView>(R.id.text)
        text.text = mensaje

        val icon = layout.findViewById<ImageView>(R.id.icon)
        icon.setImageResource(R.drawable.aceptar)

        with (Toast(context)) {
            duration = Toast.LENGTH_SHORT
            view = layout
            show()
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
}