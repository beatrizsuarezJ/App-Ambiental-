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
import com.google.firebase.auth.FirebaseAuth
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

    // Recibe imagen desde CameraActivity → PreviewActivity
    private val getPhotoResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val uri = result.data?.getParcelableExtra<Uri>("image_uri")
                if (uri != null) {
                    imagenSeleccionadaUri = uri
                    imgPlanta.setImageURI(uri)
                }
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

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Error: usuario no autenticado", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        databaseReference = FirebaseDatabase.getInstance()
            .getReference("Usuarios_ChildCare")
            .child(uid)
            .child("plantas")

        // Abrir cámara estilo WhatsApp
        btnSeleccionarFoto.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            getPhotoResult.launch(intent)
        }

        btnRegistrar.setOnClickListener { registrarPlanta(uid) }

        btnVolver.setOnClickListener {
            startActivity(Intent(this, Index_ChildCare::class.java))
        }
    }

    private fun registrarPlanta(uid: String) {
        val nombre = etNombrePlanta.text.toString().trim()
        val grosor = etGrosor.text.toString().trim()
        val altura = etAltura.text.toString().trim()
        val hojas = etHojas.text.toString().trim()
        val total = etTotalPlantas.text.toString().trim()

        if (nombre.isEmpty() || grosor.isEmpty() || altura.isEmpty()
            || hojas.isEmpty() || total.isEmpty()
        ) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val plantaId = databaseReference.push().key ?: return

        val uri = imagenSeleccionadaUri
        if (uri != null) {
            subirImagenYGuardar(plantaId, uid, uri, nombre, grosor, altura, hojas, total)
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
        uid: String,
        uri: Uri,
        nombre: String,
        grosor: String,
        altura: String,
        hojas: String,
        total: String
    ) {

        val fotoRef = FirebaseStorage.getInstance().reference
            .child("plantas/$uid/$plantaId.jpg")

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
                Toast.makeText(this, "Planta registrada", Toast.LENGTH_LONG).show()
                limpiarCampos()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar", Toast.LENGTH_LONG).show()
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
