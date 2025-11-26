package com.example.childcare

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import android.view.LayoutInflater
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class CrearCuenta_ChildCare : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_cuenta_child_care)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        firebaseAuth = Firebase.auth

        val txtNombreUsuarioNuevo: TextView = findViewById(R.id.edtNameUsuario)
        val txtEmailNuevo: TextView = findViewById(R.id.edtEmailCrearNew)
        val btnCrearCuenta : Button = findViewById(R.id.btnCrearCuenta)

        // ------------------- SPINNER MUNICIPIOS ----------------------
        val spinnerMunicipios = findViewById<Spinner>(R.id.spinnerMunicipios)
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.municipios_chiapas,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMunicipios.adapter = adapter
        // -----------------------------------------------------------

        btnCrearCuenta.setOnClickListener {
            try {
                val nombre = txtNombreUsuarioNuevo.text.toString()
                val email = txtEmailNuevo.text.toString()
                val pass1 = findViewById<EditText>(R.id.edtPassword).text.toString()
                val pass2 = findViewById<EditText>(R.id.edtPasswordCrearNwe).text.toString()

                if(nombre.isEmpty()){
                    txtNombreUsuarioNuevo.error = "Este Campo Es Obligatorio!!!"
                    txtNombreUsuarioNuevo.requestFocus()
                    return@setOnClickListener
                }

                if (email.isEmpty()){
                    txtEmailNuevo.error = "Este Campo Es Obligatorio!!"
                    txtEmailNuevo.requestFocus()
                    return@setOnClickListener
                }

                if(pass1.isEmpty() || pass2.isEmpty()){
                    Toast.makeText(this,
                        "*****ERROR*****\n Ambos campos de contraseña son obligatorios",
                        Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                if(pass1 == pass2){
                    val municipioSeleccionado = spinnerMunicipios.selectedItem.toString()
                    createAccount(email, pass1, nombre, municipioSeleccionado)
                } else {
                    Toast.makeText(
                        baseContext,
                        "*****Error***** \n Verifica Que Las Contraseñas Coincidan",
                        Toast.LENGTH_SHORT
                    ).show()
                    findViewById<EditText>(R.id.edtPassword).requestFocus()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                mostrarToastPersonalizadoAdvertencia(
                    this,
                    "¡Requisita Los Campos Correspondientes!"
                )
            }
        }
    }

    //función para el diseño del toast de advertencia
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

    private fun createAccount(email: String, password: String, nombre: String, municipio: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    val userID = user?.uid
                    val databaseReference = FirebaseDatabase.getInstance().getReference()
                    val userRole = 2

                    val newUser = User(
                        Nombre = nombre,
                        Correo_Electronico = email,
                        rol = userRole,
                        Zona = municipio
                    )

                    userID?.let {
                        databaseReference.child("Usuarios_ChildCare")
                            .child(user.uid)
                            .setValue(newUser)
                    }

                    sendEmailVerification()
                    mostrarToastPersonalizadoSuccess(
                        this,
                        "¡Cuenta creada correctamente!\n¡Requiere Verificación!"
                    )
                    btnCrearExit()

                } else {
                    mostrarToastPersonalizadoError(
                        this,
                        "¡Algo salió mal. Error!\n" + task.exception
                    )
                }
            }
    }

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

    private fun mostrarToastPersonalizadoSuccess(context: Context, mensaje: String) {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.toast_succes, null)

        val text = layout.findViewById<TextView>(R.id.text)
        text.text = mensaje

        val icon = layout.findViewById<ImageView>(R.id.icon)
        icon.setImageResource(R.drawable.aceptar)

        with (Toast(context)) {
            duration = Toast.LENGTH_LONG
            view = layout
            show()
        }
    }

    private fun sendEmailVerification() {
        val user: FirebaseUser = firebaseAuth.currentUser!!
        user.sendEmailVerification().addOnCompleteListener(this) { task ->
            if (!task.isSuccessful){
                Toast.makeText(
                    baseContext,
                    "Error al enviar el correo de verificación",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun btnCrearExit() {
        firebaseAuth.signOut()
        val i = Intent(this, IniciarSesion_ChildCare::class.java)
        startActivity(i)
    }
}
