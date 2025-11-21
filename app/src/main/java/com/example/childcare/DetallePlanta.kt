package com.example.childcare

import android.content.ContentValues
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

class DetallePlanta : AppCompatActivity() {

    private lateinit var etNombre: TextInputEditText
    private lateinit var etGrosor: TextInputEditText
    private lateinit var etAltura: TextInputEditText
    private lateinit var etHojas: TextInputEditText
    private lateinit var etTotal: TextInputEditText

    private lateinit var btnEditar: MaterialButton
    private lateinit var btnGuardar: MaterialButton
    private lateinit var btnEliminar: MaterialButton
    private lateinit var btnExportarPdf: MaterialButton
    private lateinit var btnCompartirPdf: MaterialButton

    private lateinit var imgPlantaDetalle: ImageView
    private lateinit var btnCambiarFoto: MaterialButton
    private var imagenSeleccionadaUri: Uri? = null

    private lateinit var dbRef: DatabaseReference
    private var plantId: String? = null

    private var uriPdfGenerado: Uri? = null

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imagenSeleccionadaUri = uri
            imgPlantaDetalle.setImageURI(uri)
            subirNuevaFoto()
        } else {
            Toast.makeText(this, "No se seleccionó imagen", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_planta)

        etNombre = findViewById(R.id.etNombre)
        etGrosor = findViewById(R.id.etGrosor)
        etAltura = findViewById(R.id.etAltura)
        etHojas = findViewById(R.id.etHojas)
        etTotal = findViewById(R.id.etTotal)

        btnEditar = findViewById(R.id.btnEditar)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnEliminar = findViewById(R.id.btnEliminar)
        btnExportarPdf = findViewById(R.id.btnExportarPdf)
        btnCompartirPdf = findViewById(R.id.btnCompartirPdf)

        imgPlantaDetalle = findViewById(R.id.imgPlantaDetalle)
        btnCambiarFoto = findViewById(R.id.btnCambiarFoto)

        plantId = intent.getStringExtra("plantId")
        if (plantId == null) {
            Toast.makeText(this, "Sin ID de planta", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 🔥 USAR UID DEL USUARIO
        val uid = FirebaseAuth.getInstance().currentUser!!.uid

        // 🔥 NUEVA RUTA REAL
        dbRef = FirebaseDatabase.getInstance()
            .getReference("Usuarios_ChildCare")
            .child(uid)
            .child("plantas")

        cargarDatos(plantId!!)

        btnEditar.setOnClickListener { setEditing(true) }
        btnGuardar.setOnClickListener { guardarCambios() }
        btnEliminar.setOnClickListener { confirmarEliminacion() }

        btnExportarPdf.setOnClickListener { exportarPdf() }
        btnCompartirPdf.setOnClickListener { compartirPdf() }

        btnCambiarFoto.setOnClickListener { pickImage.launch("image/*") }
    }

    private fun setEditing(enabled: Boolean) {
        etNombre.isEnabled = enabled
        etGrosor.isEnabled = enabled
        etAltura.isEnabled = enabled
        etHojas.isEnabled = enabled
        etTotal.isEnabled = enabled

        btnGuardar.isEnabled = enabled
        btnEditar.isEnabled = !enabled
        btnCambiarFoto.isEnabled = enabled
    }

    private fun cargarDatos(id: String) {
        dbRef.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val p = snapshot.getValue(Planta::class.java)
                if (p == null) {
                    Toast.makeText(this@DetallePlanta, "Planta no encontrada", Toast.LENGTH_SHORT).show()
                    finish()
                    return
                }

                etNombre.setText(p.nombre ?: "")
                etGrosor.setText(p.grosor_cm?.toString() ?: "")
                etAltura.setText(p.altura_cm?.toString() ?: "")
                etHojas.setText(p.hojas?.toString() ?: "")
                etTotal.setText(p.total?.toString() ?: "")

                if (!p.imagenUrl.isNullOrEmpty()) {
                    Glide.with(this@DetallePlanta)
                        .load(p.imagenUrl)
                        .into(imgPlantaDetalle)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun guardarCambios() {
        val nombre = etNombre.text.toString().trim()
        val grosor = etGrosor.text.toString().toDoubleOrNull()
        val altura = etAltura.text.toString().toDoubleOrNull()
        val hojas = etHojas.text.toString().toIntOrNull()
        val total = etTotal.text.toString().toIntOrNull()

        if (nombre.isEmpty() || grosor == null || altura == null || hojas == null || total == null) {
            Toast.makeText(this, "Por favor revisa los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val updates = mapOf(
            "nombre" to nombre,
            "grosor_cm" to grosor,
            "altura_cm" to altura,
            "hojas" to hojas,
            "total" to total
        )

        dbRef.child(plantId!!).updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Cambios guardados", Toast.LENGTH_SHORT).show()
                setEditing(false)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
            }
    }

    private fun subirNuevaFoto() {
        val id = plantId ?: return
        val uri = imagenSeleccionadaUri ?: return

        val storageRef = FirebaseStorage.getInstance().reference
            .child("plantas/$id.jpg")

        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { url ->
                    dbRef.child(id).child("imagenUrl").setValue(url.toString())
                    Toast.makeText(this, "Imagen actualizada", Toast.LENGTH_SHORT).show()

                    Glide.with(this)
                        .load(url)
                        .into(imgPlantaDetalle)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al subir imagen", Toast.LENGTH_SHORT).show()
            }
    }

    private fun confirmarEliminacion() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar planta")
            .setMessage("¿Seguro que deseas eliminar esta planta?")
            .setPositiveButton("Sí") { _, _ -> eliminarPlanta() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarPlanta() {
        dbRef.child(plantId!!).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Planta eliminada", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show()
            }
    }

    // ---------------- PDF -----------------

    private fun exportarPdf() {
        val nombre = etNombre.text.toString()
        val grosor = etGrosor.text.toString()
        val altura = etAltura.text.toString()
        val hojas = etHojas.text.toString()
        val total = etTotal.text.toString()

        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas

        canvas.drawColor(Color.WHITE)

        val titlePaint = Paint().apply {
            textSize = 22f
            isFakeBoldText = true
            color = Color.BLACK
        }

        val textPaint = Paint().apply {
            textSize = 16f
            color = Color.DKGRAY
        }

        var y = 80f

        canvas.drawText("Reporte de Planta", 60f, y, titlePaint)
        y += 40f

        canvas.drawText("Nombre: $nombre", 60f, y, textPaint); y += 25f
        canvas.drawText("Grosor: $grosor cm", 60f, y, textPaint); y += 25f
        canvas.drawText("Altura: $altura cm", 60f, y, textPaint); y += 25f
        canvas.drawText("Hojas: $hojas", 60f, y, textPaint); y += 25f
        canvas.drawText("Total de plantas: $total", 60f, y, textPaint); y += 25f

        pdf.finishPage(page)

        val fileName = "Planta_${nombre}_${System.currentTimeMillis()}.pdf"

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }

                val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                val out = contentResolver.openOutputStream(uri!!)
                pdf.writeTo(out)
                out!!.close()

                values.put(MediaStore.Downloads.IS_PENDING, 0)
                contentResolver.update(uri, values, null, null)

                uriPdfGenerado = uri

                Toast.makeText(this, "PDF guardado en Descargas", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error generando PDF", Toast.LENGTH_SHORT).show()
        }

        pdf.close()
    }

    private fun compartirPdf() {
        if (uriPdfGenerado == null) {
            Toast.makeText(this, "Primero genera el PDF", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uriPdfGenerado)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(intent, "Compartir PDF"))
    }
}
