package com.example.childcare

import android.content.Context
import android.content.Intent
import android.content.LocusId
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class ModificarUss:AppCompatActivity() {



    private lateinit var databaseReference: DatabaseReference
    private lateinit var listener: ValueEventListener
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var editTextNombre:EditText
    private lateinit var editTextApellidos:EditText
    private lateinit var editTextEdad:EditText
    private lateinit var editTextGenero:EditText
    private lateinit var editTextAlergia:EditText
    private lateinit var editTextPadre :EditText
    private lateinit var editTextMadre:EditText
    private lateinit var editTextTelefono:EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.modificar)


        editTextNombre = findViewById(R.id.editTextNombre)
        editTextApellidos = findViewById(R.id.editTextApellidos)
        editTextEdad = findViewById(R.id.editTextEdad)
        editTextGenero = findViewById(R.id.editTextGenero)
        editTextAlergia = findViewById(R.id.editTextAlergia)
        editTextPadre = findViewById(R.id.editTextPadre)
        editTextMadre = findViewById(R.id.editTextMadre)
        editTextTelefono = findViewById(R.id.editTextTelefono)



        val btnEnviar = findViewById<Button>(R.id.btnModificar)



        firebaseAuth = Firebase.auth
        database = FirebaseDatabase.getInstance()

        val currentUser = firebaseAuth.currentUser
        val uid = currentUser?.uid


        btnEnviar.setOnClickListener {

            mostrarToastPersonalizadoEntrar(this, "¡Datos Modificados Exitosamente!")
            modifacarDatosUss("Usuarios_ChildCare/"+uid.toString()+"/Formulario",uid.toString())
            regresarHaInterfaz()
        }



        if (uid != null) {
            val database = FirebaseDatabase.getInstance()
            val referencia = database.getReference("Usuarios_ChildCare/"+uid.toString()+"/Formulario")

            listener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val nombre1 = dataSnapshot.child("nombre").getValue(String::class.java) ?: ""
                        val edad1 = dataSnapshot.child("edad").getValue(String::class.java) ?: ""
                        val genero1 = dataSnapshot.child("genero").getValue(String::class.java) ?: ""
                        val alergia1 = dataSnapshot.child("alergia").getValue(String::class.java) ?: ""
                        val nombrepadre1 = dataSnapshot.child("padre").getValue(String::class.java) ?: ""
                        val nombremadre1 = dataSnapshot.child("madre").getValue(String::class.java) ?: ""
                        val telefono1 = dataSnapshot.child("telefono").getValue(String::class.java) ?: ""
                        val apellido1 = dataSnapshot.child("apellidos").getValue(String::class.java) ?: ""

                        editTextNombre.setText(nombre1)
                        editTextApellidos.setText(apellido1)
                        editTextEdad.setText(edad1)
                        editTextGenero.setText(genero1)
                        editTextAlergia.setText(alergia1)
                        editTextPadre.setText(nombrepadre1)
                        editTextMadre.setText(nombremadre1)
                        editTextTelefono.setText(telefono1)


                    } else {
                        // Manejar el caso donde no hay datos disponibles
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Manejar errores de lectura de datos aquí
                }
            }

            // Agregar el listener en tiempo real
            referencia.addValueEventListener(listener)
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


    private fun modifacarDatosUss(ref:String,id:String){
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()

// Obtén una referencia a la ubicación del nodo que deseas modificar
        val referenciaNodo: DatabaseReference = database.getReference(ref)

// Define los nuevos datos que deseas establecer en el nodo
        val nuevosDatos: Map<String, Any> = mapOf(

            "nombre" to editTextNombre.text.toString(),
            "apellidos" to editTextApellidos.text.toString(),
            "edad" to editTextEdad.text.toString(),
            "genero" to editTextGenero.text.toString(),
            "alergia" to editTextAlergia.text.toString(),
            "padre" to editTextPadre.text.toString(),
            "madre" to editTextMadre.text.toString(),
            "telefono" to editTextTelefono.text.toString(),
            "asistencia" to "false",
            "id" to id



            // Añade más campos y valores según sea necesario
        )

// Utiliza el método setValue() para establecer los nuevos datos en el nodo
        referenciaNodo.setValue(nuevosDatos)
            .addOnSuccessListener {
                // La modificación de datos se realizó con éxito
                // Puedes realizar cualquier acción adicional aquí si es necesario
            }
            .addOnFailureListener { exception ->
                // Ocurrió un error al intentar modificar los datos
                // Maneja el error de acuerdo a tus necesidades
            }


    }

    // Función para cerrar sesión y redirigir al inicio de sesión
    private fun regresarHaInterfaz() {
        val i = Intent(this,InformacionDeNinos_ChildCare_User::class.java)
        startActivity(i)
    }

}