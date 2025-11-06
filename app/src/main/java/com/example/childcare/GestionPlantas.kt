package com.example.childcare

import android.content.ContentValues
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.*

import java.io.File
import java.io.FileOutputStream

class GestionPlanta : AppCompatActivity() {

    private lateinit var etNombre: TextInputEditText
    private lateinit var etGrosor: TextInputEditText
    private lateinit var etAltura: TextInputEditText
    private lateinit var etHojas: TextInputEditText
    private lateinit var etTotal: TextInputEditText
    private lateinit var btnGuardar: MaterialButton
    private lateinit var btnExportarPdf: MaterialButton

    private lateinit var dbRef: DatabaseReference
    private var plantaId: String? = null
    private var plantaActual: Planta? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestion_planta)

        etNombre = findViewById(R.id.etNombreEdit)
        etGrosor = findViewById(R.id.etGrosorEdit)
        etAltura = findViewById(R.id.etAlturaEdit)
        etHojas = findViewById(R.id.etHojasEdit)
        etTotal = findViewById(R.id.etTotalEdit)
        btnGuardar = findViewById(R.id.btnGuardarCambios)


        dbRef = FirebaseDatabase.getInstance().getReference("plantas")
        plantaId = intent.getStringExtra("plantId")

        if (plantaId.isNullOrEmpty()) {
            Toast.makeText(this, "Falta plantId en el Intent", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        cargarPlanta(plantaId!!)

        btnGuardar.setOnClickListener { guardarCambios() }
        btnExportarPdf.setOnClickListener { exportarPdf() }

        // Bloquear atrás si así lo deseas:
        // onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
        //     override fun handleOnBackPressed() { }
        // })
    }

    private fun cargarPlanta(id: String) {
        dbRef.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                plantaActual = snapshot.getValue(Planta::class.java)
                if (plantaActual == null) {
                    Toast.makeText(this@GestionPlanta, "No se encontró la planta", Toast.LENGTH_LONG).show()
                    finish(); return
                }
                // Rellenar UI
                etNombre.setText(plantaActual?.nombre ?: "")
                etGrosor.setText(plantaActual?.grosor_cm?.toString() ?: "")
                etAltura.setText(plantaActual?.altura_cm?.toString() ?: "")
                etHojas.setText(plantaActual?.hojas?.toString() ?: "")
                etTotal.setText(plantaActual?.total?.toString() ?: "")
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@GestionPlanta, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun guardarCambios() {
        val nombre = etNombre.text.toString().trim()
        val grosor = etGrosor.text.toString().trim()
        val altura = etAltura.text.toString().trim()
        val hojas  = etHojas.text.toString().trim()
        val total  = etTotal.text.toString().trim()

        if (nombre.isEmpty() || grosor.isEmpty() || altura.isEmpty() || hojas.isEmpty() || total.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val actualizada = Planta(
            nombre = nombre,
            grosor_cm = grosor.toDouble(),
            altura_cm = altura.toDouble(),
            hojas = hojas.toInt(),
            total = total.toInt(),
            creadoEn = plantaActual?.creadoEn ?: System.currentTimeMillis()
        )

        dbRef.child(plantaId!!).setValue(actualizada)
            .addOnSuccessListener {
                plantaActual = actualizada
                Toast.makeText(this, "Cambios guardados", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun exportarPdf() {
        val p = plantaActual ?: Planta(
            nombre = etNombre.text.toString().trim(),
            grosor_cm = etGrosor.text.toString().toDoubleOrNull(),
            altura_cm = etAltura.text.toString().toDoubleOrNull(),
            hojas = etHojas.text.toString().toIntOrNull(),
            total = etTotal.text.toString().toIntOrNull(),
            creadoEn = System.currentTimeMillis()
        )

        // Crear documento PDF simple
        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 (en puntos ~ 72dpi)
        val page = pdf.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 20f
            isFakeBoldText = true
        }
        val textPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 14f
        }

        var y = 60f
        canvas.drawText("Reporte de Planta", 60f, y, titlePaint); y += 30f
        canvas.drawText("Nombre: ${p.nombre ?: "-"}", 60f, y, textPaint); y += 22f
        canvas.drawText("Grosor: ${p.grosor_cm ?: "-"} cm", 60f, y, textPaint); y += 22f
        canvas.drawText("Altura: ${p.altura_cm ?: "-"} cm", 60f, y, textPaint); y += 22f
        canvas.drawText("Hojas: ${p.hojas ?: "-"}", 60f, y, textPaint); y += 22f
        canvas.drawText("Total: ${p.total ?: "-"}", 60f, y, textPaint); y += 22f
        canvas.drawText("Generado: ${java.util.Date(System.currentTimeMillis())}", 60f, y, textPaint)

        pdf.finishPage(page)

        // Guardar en Descargas
        val fileName = "Planta_${p.nombre ?: "sin_nombre"}_${System.currentTimeMillis()}.pdf"

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // API 29+: usar MediaStore (no requiere permisos)
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                uri?.let {
                    contentResolver.openOutputStream(it)?.use { out ->
                        pdf.writeTo(out)
                    }
                    values.clear()
                    values.put(MediaStore.Downloads.IS_PENDING, 0)
                    contentResolver.update(it, values, null, null)
                    Toast.makeText(this, "PDF guardado en Descargas", Toast.LENGTH_LONG).show()
                } ?: run {
                    Toast.makeText(this, "No se pudo crear el archivo", Toast.LENGTH_LONG).show()
                }
            } else {
                // API <= 28: guardar en /Downloads (puede requerir permiso)
                val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloads, fileName)
                FileOutputStream(file).use { out -> pdf.writeTo(out) }
                Toast.makeText(this, "PDF guardado en: ${file.absolutePath}", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al crear PDF: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            pdf.close()
        }
    }
}

