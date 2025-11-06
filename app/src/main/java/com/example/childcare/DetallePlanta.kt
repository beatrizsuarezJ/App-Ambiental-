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

    // ---- NUEVO: imagen en detalle ----
    private lateinit var imgPlantaDetalle: ImageView
    private lateinit var btnCambiarFoto: MaterialButton
    private var imagenSeleccionadaUri: Uri? = null

    private lateinit var dbRef: DatabaseReference
    private var plantId: String? = null

    // Guarda el URI del último PDF generado para poder compartirlo
    private var uriPdfGenerado: Uri? = null

    // Abrir galería para elegir imagen
    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imagenSeleccionadaUri = uri
            // Vista previa inmediata
            imgPlantaDetalle.setImageURI(uri)
            // Subir y actualizar URL en Firebase
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

        // ---- NUEVO: views de imagen ----
        imgPlantaDetalle = findViewById(R.id.imgPlantaDetalle)
        btnCambiarFoto = findViewById(R.id.btnCambiarFoto)

        plantId = intent.getStringExtra("plantId")
        if (plantId == null) {
            Toast.makeText(this, "Sin ID de planta", Toast.LENGTH_SHORT).show()
            finish(); return
        }

        dbRef = FirebaseDatabase.getInstance().getReference("plantas")
        cargarDatos(plantId!!)

        btnEditar.setOnClickListener { setEditing(true) }
        btnGuardar.setOnClickListener { guardarCambios() }
        btnEliminar.setOnClickListener { confirmarEliminacion() }

        // PDF
        btnExportarPdf.setOnClickListener { exportarPdf() }
        btnCompartirPdf.setOnClickListener { compartirPdf() }

        // Imagen: abrir galería
        btnCambiarFoto.setOnClickListener { pickImage.launch("image/*") }
    }

    private fun setEditing(enabled: Boolean) {
        etNombre.isEnabled = enabled
        etGrosor.isEnabled = enabled
        etAltura.isEnabled = enabled
        etHojas.isEnabled  = enabled
        etTotal.isEnabled  = enabled

        btnGuardar.isEnabled = enabled
        btnEditar.isEnabled = !enabled

        // Si quieres que solo pueda cambiar foto en modo edición, déjalo así:
        btnCambiarFoto.isEnabled = enabled
    }

    private fun cargarDatos(id: String) {
        dbRef.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val p = snapshot.getValue(Planta::class.java) ?: run {
                    Toast.makeText(this@DetallePlanta, "Planta no encontrada", Toast.LENGTH_SHORT).show()
                    finish(); return
                }
                etNombre.setText(p.nombre ?: "")
                etGrosor.setText(p.grosor_cm?.toString() ?: "")
                etAltura.setText(p.altura_cm?.toString() ?: "")
                etHojas.setText(p.hojas?.toString() ?: "")
                etTotal.setText(p.total?.toString() ?: "")

                // ---- NUEVO: cargar imagen si existe ----
                val url = p.imagenUrl
                if (!url.isNullOrBlank()) {
                    Glide.with(this@DetallePlanta)
                        .load(url)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .into(imgPlantaDetalle)
                } else {
                    imgPlantaDetalle.setImageResource(R.drawable.ic_image_placeholder)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun guardarCambios() {
        val nombre = etNombre.text?.toString()?.trim().orEmpty()
        val grosor = etGrosor.text?.toString()?.trim()?.toDoubleOrNull()
        val altura = etAltura.text?.toString()?.trim()?.toDoubleOrNull()
        val hojas  = etHojas.text?.toString()?.trim()?.toIntOrNull()
        val total  = etTotal.text?.toString()?.trim()?.toIntOrNull()

        if (nombre.isEmpty() || grosor == null || altura == null || hojas == null || total == null) {
            Toast.makeText(this, "Revisa los datos", Toast.LENGTH_SHORT).show()
            return
        }
        val id = plantId ?: return

        val updates = mapOf(
            "nombre" to nombre,
            "grosor_cm" to grosor,
            "altura_cm" to altura,
            "hojas" to hojas,
            "total" to total
        )

        setEditing(false)

        dbRef.child(id).updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Cambios guardados", Toast.LENGTH_LONG).show()
                setEditing(false)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                setEditing(true)
            }
    }

    // --------- SUBIR NUEVA FOTO Y ACTUALIZAR imagenUrl ---------
    private fun subirNuevaFoto() {
        val id = plantId ?: return
        val uri = imagenSeleccionadaUri ?: return

        btnCambiarFoto.isEnabled = false
        Toast.makeText(this, "Subiendo imagen...", Toast.LENGTH_SHORT).show()

        val storageRef = FirebaseStorage.getInstance().reference
        // Sobrescribe la foto de esta planta (si quieres conservar versiones, agrega timestamp al nombre)
        val fotoRef = storageRef.child("plantas/$id.jpg")

        fotoRef.putFile(uri)
            .addOnSuccessListener {
                fotoRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    dbRef.child(id).child("imagenUrl").setValue(downloadUri.toString())
                        .addOnSuccessListener {
                            Toast.makeText(this, "✅ Imagen actualizada", Toast.LENGTH_LONG).show()
                            // Recargar con Glide (forzando refresco)
                            Glide.with(this)
                                .load(downloadUri.toString())
                                .placeholder(R.drawable.ic_image_placeholder)
                                .into(imgPlantaDetalle)
                            imagenSeleccionadaUri = null
                            btnCambiarFoto.isEnabled = true
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error al guardar URL: ${e.message}", Toast.LENGTH_LONG).show()
                            btnCambiarFoto.isEnabled = true
                        }
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Error al obtener URL: ${e.message}", Toast.LENGTH_LONG).show()
                    btnCambiarFoto.isEnabled = true
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al subir imagen: ${e.message}", Toast.LENGTH_LONG).show()
                btnCambiarFoto.isEnabled = true
            }
    }

    // --------- ELIMINAR ---------
    private fun confirmarEliminacion() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar planta")
            .setMessage("¿Seguro que deseas eliminar este registro? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ -> eliminarPlanta() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarPlanta() {
        val id = plantId ?: return
        btnEliminar.isEnabled = false
        btnGuardar.isEnabled = false
        btnEditar.isEnabled  = false

        dbRef.child(id).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Registro eliminado correctamente", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { e ->
                btnEliminar.isEnabled = true
                btnGuardar.isEnabled  = btnGuardar.isEnabled || false
                btnEditar.isEnabled   = true
                Toast.makeText(this, "Error al eliminar: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    // --------- PDF: GENERAR Y GUARDAR EN DESCARGAS ---------
    private fun exportarPdf() {
        val nombre = etNombre.text?.toString().orEmpty()
        val grosor = etGrosor.text?.toString().orEmpty()
        val altura = etAltura.text?.toString().orEmpty()
        val hojas  = etHojas.text?.toString().orEmpty()
        val total  = etTotal.text?.toString().orEmpty()

        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas

        // Fondo con imagen (usa tu drawable)
        try {
            val bg = BitmapFactory.decodeResource(resources, R.drawable.icon_forest)
            if (bg != null) {
                val bgScaled = android.graphics.Bitmap.createScaledBitmap(bg, pageInfo.pageWidth, pageInfo.pageHeight, true)
                canvas.drawBitmap(bgScaled, 0f, 0f, null)
                if (bgScaled != bg) bgScaled.recycle()
                bg.recycle()
            } else {
                canvas.drawColor(Color.WHITE)
            }
        } catch (e: Exception) {
            canvas.drawColor(Color.WHITE)
        }

        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 22f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val textPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 14f
            isAntiAlias = true
        }
        val stroke = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 2f
            isAntiAlias = true
        }

        var y = 80f
        canvas.drawText("Reporte de Planta", 60f, y, titlePaint)
        y += 16f
        canvas.drawLine(60f, y, (pageInfo.pageWidth - 60).toFloat(), y, stroke)
        y += 30f

        fun linea(label: String, value: String) {
            canvas.drawText("$label: $value", 60f, y, textPaint); y += 22f
        }

        linea("Nombre", nombre.ifBlank { "-" })
        linea("Grosor (cm)", grosor.ifBlank { "-" })
        linea("Altura (cm)", altura.ifBlank { "-" })
        linea("Número de hojas", hojas.ifBlank { "-" })
        linea("Total de plantas", total.ifBlank { "-" })
        linea("Generado", java.util.Date().toString())

        pdf.finishPage(page)

        val fileName = "Planta_${nombre.ifBlank { "sin_nombre" }}_${System.currentTimeMillis()}.pdf"
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    contentResolver.openOutputStream(uri)?.use { out -> pdf.writeTo(out) }
                    values.clear()
                    values.put(MediaStore.Downloads.IS_PENDING, 0)
                    contentResolver.update(uri, values, null, null)
                    uriPdfGenerado = uri
                    Toast.makeText(this, "PDF guardado en Descargas", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "No se pudo crear el archivo", Toast.LENGTH_LONG).show()
                }
            } else {
                val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = java.io.File(downloads, fileName)
                java.io.FileOutputStream(file).use { out -> pdf.writeTo(out) }
                uriPdfGenerado = Uri.fromFile(file)
                Toast.makeText(this, "PDF guardado: ${file.absolutePath}", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al crear PDF: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            pdf.close()
        }
    }

    // --------- PDF: COMPARTIR ---------
    private fun compartirPdf() {
        val uri = uriPdfGenerado
        if (uri == null) {
            Toast.makeText(this, "Primero genera el PDF", Toast.LENGTH_SHORT).show()
            return
        }
        val share = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(share, "Compartir PDF"))
    }
}
