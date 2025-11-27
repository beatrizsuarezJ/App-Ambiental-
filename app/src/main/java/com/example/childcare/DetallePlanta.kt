package com.example.childcare

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

class DetallePlanta : AppCompatActivity() {

    // --- Views ---
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
    private lateinit var btnCambiarFoto: FloatingActionButton
    private lateinit var btnEliminarFotoFab: FloatingActionButton

    // --- State ---
    private var imagenSeleccionadaUri: Uri? = null
    private var fotoUriTemporal: Uri? = null
    private var plantId: String? = null
    private var uriPdfGenerado: Uri? = null

    // --- Animación FABs ---
    private var isEditingMode = false


    private lateinit var dbRef: DatabaseReference

    // ---- PERMISO DE CÁMARA () ----
    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) abrirCamara()
            else Toast.makeText(this, "Se requiere permiso de cámara", Toast.LENGTH_LONG).show()
        }


    // ---- GALERÍA ----
    private val launcherGaleria =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) abrirPreview(uri)
        }

    // ---- CÁMARA ----
    private val launcherCamara =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
            if (ok && fotoUriTemporal != null) abrirPreview(fotoUriTemporal!!)
            else Toast.makeText(this, "Error al tomar foto", Toast.LENGTH_SHORT).show()
        }

    // ---- PREVIEW ----
    private val previewLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val uriStr = result.data?.getStringExtra("photoUri")
                if (!uriStr.isNullOrEmpty()) {
                    val uri = Uri.parse(uriStr)
                    imagenSeleccionadaUri = uri

                    Glide.with(this).load(uri).circleCrop().into(imgPlantaDetalle)

                    subirNuevaFoto()
                }
            }
        }

    // ------------------------ OnCreate ------------------------
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

        imgPlantaDetalle = findViewById(R.id.imgPlanta)
        btnCambiarFoto = findViewById(R.id.btnCamara)
        btnEliminarFotoFab = findViewById(R.id.btnEliminarFoto)

        plantId = intent.getStringExtra("plantId")
        if (plantId == null) {
            Toast.makeText(this, "Sin ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            finish()
            return
        }

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

        btnCambiarFoto.setOnClickListener { mostrarDialogoImagen() }
        btnEliminarFotoFab.setOnClickListener { confirmarEliminarFotoDirecta() }

        setEditing(false)
    }


    // ---------------- VERIFICACIÓN PERMISO ----------------
    private fun verificarPermisoCamara() {
        val permiso = Manifest.permission.CAMERA

        if (ContextCompat.checkSelfPermission(this, permiso)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestCameraPermission.launch(permiso)
        } else {
            abrirCamara()
        }
    }


    // --------------- DIALOGO DE IMAGEN ----------------
    private fun mostrarDialogoImagen() {
        val v = LayoutInflater.from(this).inflate(R.layout.dialog_camera, null)
        val dlg = AlertDialog.Builder(this).setView(v).create()
        dlg.show()

        val btnTomarFoto = v.findViewById<LinearLayout>(R.id.btnTomarFoto)
        val btnGaleria = v.findViewById<LinearLayout>(R.id.btnGaleria)
        val btnEliminarDialog = v.findViewById<LinearLayout?>(R.id.btnEliminarFoto)

        btnTomarFoto.setOnClickListener {
            dlg.dismiss()
            verificarPermisoCamara()   // ← ← ← CORREGIDO
        }

        btnGaleria.setOnClickListener {
            dlg.dismiss()
            abrirGaleria()
        }

        btnEliminarDialog?.setOnClickListener {
            dlg.dismiss()
            eliminarFotoEnBDyStorage()
        }
    }


    private fun abrirGaleria() {
        launcherGaleria.launch("image/*")
    }


    private fun abrirCamara() {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "foto_${System.currentTimeMillis()}")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }

        fotoUriTemporal = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        launcherCamara.launch(fotoUriTemporal)
    }

    private fun abrirPreview(uri: Uri) {
        val intent = Intent(this, PreviewActivity::class.java)
        intent.putExtra("photoUri", uri.toString())
        previewLauncher.launch(intent)
    }


    // ---------------- CARGAR Y GUARDAR DATOS ----------------
    private fun cargarDatos(id: String) {
        dbRef.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val p = snapshot.getValue(Planta::class.java) ?: return

                etNombre.setText(p.nombre ?: "")
                etGrosor.setText(p.grosor_cm?.toString() ?: "")
                etAltura.setText(p.altura_cm?.toString() ?: "")
                etHojas.setText(p.hojas?.toString() ?: "")
                etTotal.setText(p.total?.toString() ?: "")

                if (!p.imagenUrl.isNullOrEmpty()) {
                    Glide.with(this@DetallePlanta)
                        .load(p.imagenUrl)
                        .circleCrop()
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
            Toast.makeText(this, "Campos incorrectos", Toast.LENGTH_SHORT).show()
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
    }


    // ---------------- SUBIR Y ELIMINAR FOTO ----------------
    private fun subirNuevaFoto() {
        val id = plantId ?: return
        val uri = imagenSeleccionadaUri ?: return

        val fileRef = FirebaseStorage.getInstance().reference
            .child("plantas/$id.jpg")

        fileRef.putFile(uri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { url ->
                    dbRef.child(id).child("imagenUrl").setValue(url.toString())

                    Glide.with(this)
                        .load(url)
                        .circleCrop()
                        .into(imgPlantaDetalle)
                }
            }
    }

    private fun confirmarEliminarFotoDirecta() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar imagen")
            .setMessage("¿Deseas eliminar la imagen?")
            .setPositiveButton("Sí") { _, _ -> eliminarFotoEnBDyStorage() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarFotoEnBDyStorage() {

        val id = plantId ?: return

        // 1. Leer imagenUrl REAL desde Firebase
        dbRef.child(id).child("imagenUrl").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val url = snapshot.getValue(String::class.java)

                // 2. Si no hay URL, no hay nada que borrar
                if (url.isNullOrEmpty()) {
                    imgPlantaDetalle.setImageResource(R.drawable.ic_image_placeholder)
                    Toast.makeText(this@DetallePlanta, "No hay imagen para eliminar", Toast.LENGTH_SHORT).show()
                    return
                }

                // 3. Obtener referencia REAL a Storage desde la URL
                val fileRef = FirebaseStorage.getInstance().getReferenceFromUrl(url)

                // 4. Eliminar archivo en Storage
                fileRef.delete()
                    .addOnSuccessListener {
                        // 5. Borrar URL de Firebase
                        dbRef.child(id).child("imagenUrl").removeValue()

                        imgPlantaDetalle.setImageResource(R.drawable.ic_image_placeholder)
                        imagenSeleccionadaUri = null

                        Toast.makeText(this@DetallePlanta, "Imagen eliminada", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this@DetallePlanta, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }



    // ---------------- ELIMINAR PLANTA ----------------
    private fun confirmarEliminacion() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar planta")
            .setMessage("¿Seguro?")
            .setPositiveButton("Sí") { _, _ -> eliminarPlanta() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarPlanta() {
        dbRef.child(plantId!!).removeValue()
        finish()
    }



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

        // -----------------------
        // Imagen de fondo desde drawable
        // -----------------------
        val bgBitmap = BitmapFactory.decodeResource(resources, R.drawable.membrete_ecoforest)
        val scaledBg = Bitmap.createScaledBitmap(bgBitmap, pageInfo.pageWidth, pageInfo.pageHeight, true)
        val paint = Paint().apply { alpha = 200 } // transparencia opcional
        canvas.drawBitmap(scaledBg, 0f, 0f, paint)

        // -----------------------
        // Título
        // -----------------------
        val titlePaint = Paint().apply {
            textSize = 24f
            isFakeBoldText = true
            color = Color.BLACK
        }
        canvas.drawText("Reporte de Planta", 60f, 60f, titlePaint)

        var y = 120f

        // -----------------------
        // Imagen de la planta
        // -----------------------
        imgPlantaDetalle.drawable?.let { drawable ->
            val bitmapPlanta = drawableToBitmap(drawable)
            val resizedBitmap = Bitmap.createScaledBitmap(bitmapPlanta, 200, 200, true)
            canvas.drawBitmap(resizedBitmap, 60f, y, null)
            y += 220f
        }

        // -----------------------
        // Datos con iconos
        // -----------------------
        val textPaint = Paint().apply {
            textSize = 18f
            color = Color.BLACK
        }
        val iconSize = 30
        val iconMargin = 10
        val startX = 60f

        val dataList = listOf(
            Pair(R.drawable.ic_name, "Nombre: $nombre"),
            Pair(R.drawable.ic_thickness, "Grosor (cm): $grosor"),
            Pair(R.drawable.ic_height, "Altura (cm): $altura"),
            Pair(R.drawable.ic_leaves, "Hojas: $hojas"),
            Pair(R.drawable.ic_total, "Total: $total")
        )

        for ((iconRes, text) in dataList) {
            val iconDrawable = ContextCompat.getDrawable(this, iconRes)
            iconDrawable?.let {
                val iconBitmap = drawableToBitmap(it)
                val iconScaled = Bitmap.createScaledBitmap(iconBitmap, iconSize, iconSize, true)
                canvas.drawBitmap(iconScaled, startX, y - iconSize + 8f, null)
            }
            canvas.drawText(text, startX + iconSize + iconMargin, y, textPaint)
            y += 40f
        }

        pdf.finishPage(page)

        // -----------------------
        // Guardar PDF
        // -----------------------
        val fileName = "Planta_${nombre}_${System.currentTimeMillis()}.pdf"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }

            val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)!!
            uriPdfGenerado = uri

            contentResolver.openOutputStream(uri)?.use { pdf.writeTo(it) }

            values.put(MediaStore.Downloads.IS_PENDING, 0)
            contentResolver.update(uri, values, null, null)

            Toast.makeText(this, "PDF guardado", Toast.LENGTH_SHORT).show()
        }

        pdf.close()
    }




    /**
     * Convierte cualquier Drawable (Bitmap o Vector) en Bitmap seguro
     */
    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        return if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else {
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth.coerceAtLeast(1),
                drawable.intrinsicHeight.coerceAtLeast(1),
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }










    private fun compartirPdf() {
        if (uriPdfGenerado == null) {
            Toast.makeText(this, "Genera el PDF primero", Toast.LENGTH_SHORT).show()
            return
        }

        val i = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uriPdfGenerado)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(i, "Compartir PDF"))
    }

    private fun setEditing(enabled: Boolean) {
        etNombre.isEnabled = enabled
        etGrosor.isEnabled = enabled
        etAltura.isEnabled = enabled
        etHojas.isEnabled = enabled
        etTotal.isEnabled = enabled

        btnGuardar.isEnabled = enabled
        btnEditar.isEnabled = !enabled

        // Guardamos estado actual
        isEditingMode = enabled

        // ANIMACIÓN SUAVE PARA LOS FAB
        val alphaValue = if (enabled) 1f else 0f

        btnCambiarFoto.animate()
            .alpha(alphaValue)
            .setDuration(250)
            .start()

        btnEliminarFotoFab.animate()
            .alpha(alphaValue)
            .setDuration(250)
            .start()

        // Evitar clicks cuando están invisibles
        btnCambiarFoto.isEnabled = enabled
        btnEliminarFotoFab.isEnabled = enabled
    }

}