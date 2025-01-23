package com.example.childcare

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
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
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class CrearCuenta_ChildCare : AppCompatActivity() {

    // Declaración de la instancia de FirebaseAuth
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_cuenta_child_care)

        // Establecer la orientación de la pantalla a vertical
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Llama a la función para manejar la visibilidad de la contraseña
        setupPasswordVisibilityToggle(R.id.edtPassword, R.id.showPasswordCheckBox)
        setupPasswordVisibilityToggle(R.id.edtPasswordCrearNwe, R.id.showPasswordCheckBox2)

        // Inicializa FirebaseAuth
        firebaseAuth = Firebase.auth

        // Inicializar vistas
        val txtNombreUsuarioNuevo: TextView = findViewById(R.id.edtNameUsuario)
        val txtEmailNuevo: TextView = findViewById(R.id.edtEmailCrearNew)
        val btnCrearCuenta : Button = findViewById(R.id.btnCrearCuenta)

        // Configurar onClickListener para el botón de crear cuenta
        btnCrearCuenta.setOnClickListener {
            try {
                // Obtener valores de los campos
                val nombre = txtNombreUsuarioNuevo.text.toString()
                val email = txtEmailNuevo.text.toString()
                val pass1 = findViewById<EditText>(R.id.edtPassword).text.toString()
                val pass2 = findViewById<EditText>(R.id.edtPasswordCrearNwe).text.toString()

                // Validación de campos
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
                    Toast.makeText(this, "\t\t\t\t*****ERROR*****\n Ambos campos de contraseña son obligatorios",
                        Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                // Verificar si las contraseñas coinciden
                if(pass1 == pass2){
                    createAccount(email, pass1, nombre)
                } else {
                    Toast.makeText(baseContext, "\t\t\t\t\t*****Error***** \n Verifica Que Las Contraseñas Coincidan",Toast.LENGTH_SHORT).show()
                    findViewById<EditText>(R.id.edtPassword).requestFocus()
                }
            } catch (e: Exception) {
                // Manejo de la excepción
                e.printStackTrace() // Esto imprimirá el rastreo de la pila en la consola
                // Puedes manejar la excepción de acuerdo a tus necesidades
                // Por ejemplo, mostrar un mensaje de error al usuario
                mostrarToastPersonalizadoAdvertencia(this, "¡Requisita Los Campos Correspondientes!")
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

    // Función para manejar la visibilidad de la contraseña
    private fun setupPasswordVisibilityToggle(editTextId: Int, checkBoxId: Int) {
        val passwordEditText = findViewById<EditText>(editTextId)
        val showPasswordCheckBox = findViewById<CheckBox>(checkBoxId)

        showPasswordCheckBox.setOnCheckedChangeListener { _, isChecked ->
            passwordEditText.transformationMethod = if (isChecked) null else PasswordTransformationMethod()
            passwordEditText.setSelection(passwordEditText.text.length)
        }
    }

    // Función para crear una cuenta en Firebase
    private fun createAccount(email: String, password: String, nombre: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Obtener el usuario actual
                    val user = firebaseAuth.currentUser
                    val userID = user?.uid
                    val databaseReference = FirebaseDatabase.getInstance().getReference()
                    val userRole = if (nombre == "admin") 0 else 1
                    val newUser = User(nombre, email, userRole)
                    userID?.let {
                        // Guardar información del usuario en la base de datos
                        databaseReference.child("Usuarios_ChildCare").child(user.uid).setValue(newUser)
                    }

                    // Enviar correo de verificación
                    sendEmailVerification()
                    mostrarToastPersonalizadoSuccess(this, "¡Cuenta creada correctamente!\n¡Requiere Verificación!")
                    // Cerrar sesión y redirigir al inicio de sesión
                    btnCrearExit()
                    ///Toast.makeText(baseContext, "Cuenta creada correctamente\nRequiere Verificación", Toast.LENGTH_SHORT).show()
                } else {
                    // Mostrar mensaje de error si la creación de cuenta falla
                    mostrarToastPersonalizadoError(this, "¡Algo salió mal. Error!\n" + task.exception)
                    //Toast.makeText(baseContext, "Algo salió mal. Error" + task.exception, Toast.LENGTH_SHORT).show()
                }
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

    //funcion para el diseño del toast de acces
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

    // Función para enviar correo de verificación
    private fun sendEmailVerification() {
        val user: FirebaseUser = firebaseAuth.currentUser!!
        user.sendEmailVerification().addOnCompleteListener(this) {task ->
            if (!task.isSuccessful){
                Toast.makeText(baseContext, "Error al enviar el correo de verificación", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Función para cerrar sesión y redirigir al inicio de sesión
    private fun btnCrearExit() {
        firebaseAuth.signOut()
        val i = Intent(this,IniciarSesion_ChildCare::class.java)
        startActivity(i)
    }
}
