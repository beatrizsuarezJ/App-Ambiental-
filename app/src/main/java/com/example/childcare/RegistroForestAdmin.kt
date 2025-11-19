package com.example.childcare

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
    private var imagenSeleccionadaUri: Uri? = null

    // CÓDIGO NUEVO: launcher para galería
    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imagenSeleccionadaUri = uri
            imgPlanta.setImageURI(uri)
        } else {
            Toast.makeText(this, "No se seleccionó imagen", Toast.LENGTH_SHORT).show()
        }
    }


    // CÓDIGO NUEVO: Permisos
    private val REQUEST_GALLERY_PERMISSION = 2000

    private fun solicitarPermisoGaleria() {
        val permiso = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permiso) == PackageManager.PERMISSION_GRANTED) {
            abrirGaleria()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(permiso), REQUEST_GALLERY_PERMISSION)
        }
    }

    private fun abrirGaleria() {
        pickImage.launch("image/*")
    }
    // -------------------------

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
            solicitarPermisoGaleria()  // ***CAMBIO IMPORTANTE***
        }

        btnRegistrar.setOnClickListener {
            registrarPlanta()
        }

        btnVolver.setOnClickListener {
            val i = Intent(this, Index_ChildCare::class.java)
            startActivity(i)
        }
    }

    // ---------------------------
    // CÓDIGO NUEVO: Resultado permisos
    // ---------------------------
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_GALLERY_PERMISSION) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                abrirGaleria()
            } else {
                Toast.makeText(this, "❌ Permiso denegado para acceder a la galería", Toast.LENGTH_LONG).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    // ---------------------------

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

        val plantaId = databaseReference.push().key ?: run {
            Toast.makeText(this, "Error al generar ID", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = imagenSeleccionadaUri
        if (uri != null) {
            subirImagenYGuardar(plantaId, uri, nombre, grosor, altura, hojas, total)
        } else {
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
        val fotoRef = storage.child("plantas/$plantaId.jpg")

        fotoRef.putFile(uri)
            .addOnSuccessListener {
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
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al subir imagen", Toast.LENGTH_LONG).show()
            }
    }

    private fun guardarEnDatabase(plantaId: String, planta: Planta) {
        databaseReference.child(plantaId).setValue(planta)
            .addOnSuccessListener {
                Toast.makeText(this, "✅ Planta registrada", Toast.LENGTH_LONG).show()
                limpiarCampos()
            }
            .addOnFailureListener {
                Toast.makeText(this, "❌ Error al guardar", Toast.LENGTH_LONG).show()
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
