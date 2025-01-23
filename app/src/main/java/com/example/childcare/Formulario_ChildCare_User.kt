package com.example.childcare


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference

class Formulario_ChildCare_User : AppCompatActivity() {

    private lateinit var nombreEditText: EditText
    private lateinit var apellidosEditText: EditText
    private lateinit var edadEditText: EditText
    private lateinit var generoEditText: EditText
    private lateinit var alergiaEditText: EditText
    private lateinit var padreEditText: EditText
    private lateinit var madreEditText: EditText
    private lateinit var telefonoEditText: EditText
    private lateinit var enviarButton: Button

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private val PICK_IMAGE_REQUEST = 71
    private var filePath: Uri? = null
    private lateinit var storageReference: StorageReference

    val storage = FirebaseStorage.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_formulario_child_care_user)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        storageReference = FirebaseStorage.getInstance().reference


        val buttonChoose = findViewById<Button>(R.id.buttonChoose)
        val buttonUpload = findViewById<Button>(R.id.buttonUpload)

        buttonChoose.setOnClickListener {
            chooseImage()
        }

        buttonUpload.setOnClickListener {
            uploadImage()
        }

        // Inicializar vistas
        nombreEditText = findViewById(R.id.nombreEditText)
        apellidosEditText = findViewById(R.id.apellidosEditText)
        edadEditText = findViewById(R.id.edadEditText)
        generoEditText = findViewById(R.id.generoEditText)
        alergiaEditText = findViewById(R.id.alergiaEditText)
        padreEditText = findViewById(R.id.padreEditText)
        madreEditText = findViewById(R.id.madreEditText)
        telefonoEditText = findViewById(R.id.telefonoEditText)
        enviarButton = findViewById(R.id.btnEnviarDataFormulario)


        firebaseAuth = Firebase.auth
        database = FirebaseDatabase.getInstance()

        val currentUser = firebaseAuth.currentUser
        val uid = currentUser?.uid

        // Configurar el listener de clic para el botón "Enviar"
        enviarButton.setOnClickListener {
            // Validar que todos los campos estén llenos
            if (validarCamposLlenos()) {
                // Obtener los valores ingresados por el usuario
                val nombre = nombreEditText.text.toString()
                val apellidos = apellidosEditText.text.toString()
                val edad = edadEditText.text.toString()
                val genero = generoEditText.text.toString()
                val alergia = alergiaEditText.text.toString()
                val padre = padreEditText.text.toString()
                val madre = madreEditText.text.toString()
                val telefono = telefonoEditText.text.toString()

                // Escribir los datos en la base de datos de Firebase
                escribirDatosEnFirebase(nombre, apellidos, edad, genero, alergia, padre, madre, telefono, asistencia = "false",uid.toString())

                // Mostrar mensaje de éxito
                mostrarMensajeExito()
                finish()
                irAlIndex()
            } else {
                // Mostrar mensaje de error si algún campo está vacío
                mostrarMensajeError()
            }
        }
    }

    private fun irAlIndex() {
        val intent = Intent(this, Index_ChildCare_User::class.java)
        startActivity(intent)
    }

    private fun validarCamposLlenos(): Boolean {
        return nombreEditText.text.isNotBlank() &&
                apellidosEditText.text.isNotBlank() &&
                edadEditText.text.isNotBlank() &&
                generoEditText.text.isNotBlank() &&
                alergiaEditText.text.isNotBlank() &&
                padreEditText.text.isNotBlank() &&
                madreEditText.text.isNotBlank() &&
                telefonoEditText.text.isNotBlank()
    }

    private fun escribirDatosEnFirebase(
        nombre: String,
        apellidos: String,
        edad: String,
        genero: String,
        alergia: String,
        padre: String,
        madre: String,
        telefono: String,
        asistencia:String,
        id:String
    ) {
        // Obtener el ID del usuario actualmente autenticado
        val uid = FirebaseAuth.getInstance().currentUser?.uid

// Verificar si el usuario está autenticado
        if (uid != null) {
            // Obtener referencia a la base de datos de Firebase
            val database = FirebaseDatabase.getInstance()
            val referencia = database.getReference("Usuarios_ChildCare/$uid")

            // Aquí defines el ID manualmente
            val idManual = "Formulario"

            // Crear un nuevo nodo para el formulario con el ID manual y establecer los valores
            val nuevoFormulario = referencia.child(idManual)
            nuevoFormulario.child("nombre").setValue(nombre)
            nuevoFormulario.child("apellidos").setValue(apellidos)
            nuevoFormulario.child("edad").setValue(edad)
            nuevoFormulario.child("genero").setValue(genero)
            nuevoFormulario.child("alergia").setValue(alergia)
            nuevoFormulario.child("padre").setValue(padre)
            nuevoFormulario.child("madre").setValue(madre)
            nuevoFormulario.child("telefono").setValue(telefono)
            nuevoFormulario.child("id").setValue(id)
            nuevoFormulario.child("asistencia").setValue("false")



            val databaseReference = FirebaseDatabase.getInstance().getReference("tarjetas")
            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var encontrado = "false"
                        for (tarjetaSnapshot in dataSnapshot.children) {
                            val disponible = tarjetaSnapshot.child("disponible").getValue(String::class.java)

                            if (disponible.toString() == "false" && encontrado.toString() =="false") {
                                val nuevoNodo = tarjetaSnapshot.ref.child("id")
                                nuevoNodo.setValue(id)

                                val estado = tarjetaSnapshot.ref.child("disponible")
                                estado.setValue("true")
                                encontrado = "true"
                                break
                            }
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Manejar errores de lectura de datos aquí
                }
            })


        }

    }

    private fun mostrarMensajeExito() {
        Snackbar.make(
            findViewById(android.R.id.content),
            "Datos enviados correctamente",
            Snackbar.LENGTH_SHORT
        ).setBackgroundTint(ContextCompat.getColor(this, R.color.colorSuccess))
            .show()
    }

    private fun mostrarMensajeError() {
        Snackbar.make(
            findViewById(android.R.id.content),
            "Por favor, complete todos los campos",
            Snackbar.LENGTH_SHORT
        ).setBackgroundTint(ContextCompat.getColor(this, R.color.colorError))
            .show()
    }

    private fun chooseImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            filePath = data.data
        }
    }

    private fun uploadImage() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (filePath != null && userId != null) {
            // Construir el nombre del archivo utilizando el ID del usuario
            val nombreArchivo = "$userId.jpg"

            // Crear la referencia al archivo en Firebase Storage
            val ref = storageReference.child("imagenes/$nombreArchivo")

            // Crear los metadatos con el token de acceso
            val metadata = StorageMetadata.Builder()
                .setCustomMetadata("token", userId.toString())
                .build()

            // Subir la imagen al Firebase Storage con los metadatos personalizados
            ref.putFile(filePath!!, metadata)
                .addOnSuccessListener {
                    //Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                    mostrarToastPersonalizadoEntrar(this, "¡Imagen Cargada Exitosamente A Firebase!")
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed " + e.message, Toast.LENGTH_SHORT).show()
                }
        } else {
            //Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
            mostrarToastPersonalizadoError(this, "¡Ninguna Imagen Seleccionada!")
        }
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
            duration = Toast.LENGTH_LONG
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
