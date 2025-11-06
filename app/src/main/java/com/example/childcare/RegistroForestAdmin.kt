package com.example.childcare

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class RegistroForestAdmin : AppCompatActivity() {

    private lateinit var etNombrePlanta: TextInputEditText
    private lateinit var etGrosor: TextInputEditText
    private lateinit var etAltura: TextInputEditText
    private lateinit var etHojas: TextInputEditText
    private lateinit var etTotalPlantas: TextInputEditText
    private lateinit var btnRegistrar: MaterialButton
    private lateinit var btnSeleccionarFoto: MaterialButton
    private lateinit var imgPlanta: ImageView

    private lateinit var databaseReference: DatabaseReference

    // Uri de la imagen elegida
    private var imagenSeleccionadaUri: Uri? = null

    // Launcher para abrir la galería
    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imagenSeleccionadaUri = uri
            imgPlanta.setImageURI(uri) // Vista previa
        } else {
            Toast.makeText(this, "No se seleccionó imagen", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_forest_admin)

        etNombrePlanta = findViewById(R.id.etNombrePlanta)
        etGrosor = findViewById(R.id.etGrosor)
        etAltura = findViewById(R.id.etAltura)
        etHojas = findViewById(R.id.etHojas)
        etTotalPlantas = findViewById(R.id.etTotalPlantas)
        btnRegistrar = findViewById(R.id.btnRegistrar)
        btnSeleccionarFoto = findViewById(R.id.btnSeleccionarFoto)
        imgPlanta = findViewById(R.id.imgPlanta)


        val btnVolver = findViewById<ImageView>(R.id.btnRegresar)

        databaseReference = FirebaseDatabase.getInstance().getReference("plantas")

        btnSeleccionarFoto.setOnClickListener {
            pickImage.launch("image/*")
        }

        btnRegistrar.setOnClickListener {
            registrarPlanta()
        }

        btnVolver.setOnClickListener {
            val i = Intent(this, Index_ChildCare::class.java)
            startActivity(i)
        }
    }

    private fun registrarPlanta() {
        val nombre = etNombrePlanta.text.toString().trim()
        val grosor = etGrosor.text.toString().trim()
        val altura = etAltura.text.toString().trim()
        val hojas = etHojas.text.toString().trim()
        val total = etTotalPlantas.text.toString().trim()

        if (nombre.isEmpty() || grosor.isEmpty() || altura.isEmpty() || hojas.isEmpty() || total.isEmpty()) {
            Toast.makeText(this, "⚠ Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // Primero generamos el ID
        val plantaId = databaseReference.push().key
        if (plantaId == null) {
            Toast.makeText(this, "Error al generar ID", Toast.LENGTH_SHORT).show()
            return
        }

        // Si hay imagen seleccionada, súbela primero a Storage y luego guarda la planta con la URL
        val uri = imagenSeleccionadaUri
        if (uri != null) {
            subirImagenYGuardar(plantaId, uri, nombre, grosor, altura, hojas, total)
        } else {
            // Guardar sin imagen
            val planta = Planta(
                nombre = nombre,
                grosor_cm = grosor.toDouble(),
                altura_cm = altura.toDouble(),
                hojas = hojas.toInt(),
                total = total.toInt(),
                creadoEn = System.currentTimeMillis(),
                imagenUrl = null
            )
            guardarEnDatabase(plantaId, planta)
        }
    }

    private fun subirImagenYGuardar(
        plantaId: String,
        uri: Uri,
        nombre: String,
        grosor: String,
        altura: String,
        hojas: String,
        total: String
    ) {
        val storage = FirebaseStorage.getInstance().reference
        // Ruta sugerida: plantas/{plantaId}.jpg
        val fotoRef = storage.child("plantas/$plantaId.jpg")

        // Subir archivo
        fotoRef.putFile(uri)
            .addOnSuccessListener {
                // Obtener URL de descarga
                fotoRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val planta = Planta(
                        nombre = nombre,
                        grosor_cm = grosor.toDouble(),
                        altura_cm = altura.toDouble(),
                        hojas = hojas.toInt(),
                        total = total.toInt(),
                        creadoEn = System.currentTimeMillis(),
                        imagenUrl = downloadUri.toString()
                    )
                    guardarEnDatabase(plantaId, planta)
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Error al obtener URL: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al subir imagen: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun guardarEnDatabase(plantaId: String, planta: Planta) {
        databaseReference.child(plantaId).setValue(planta)
            .addOnSuccessListener {
                Toast.makeText(this, "✅ Planta registrada", Toast.LENGTH_LONG).show()
                limpiarCampos()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "❌ Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun limpiarCampos() {
        etNombrePlanta.setText("")
        etGrosor.setText("")
        etAltura.setText("")
        etHojas.setText("")
        etTotalPlantas.setText("")
        imgPlanta.setImageResource(R.drawable.ic_image_placeholder)
        imagenSeleccionadaUri = null
    }
}
