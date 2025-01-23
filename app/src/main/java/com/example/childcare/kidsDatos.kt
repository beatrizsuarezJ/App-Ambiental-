package com.example.childcare

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream

class kidsDatos :AppCompatActivity(){

    private lateinit var nombre: TextView
    private lateinit var apellidos:TextView
    private lateinit var edad: TextView
    private lateinit var genero: TextView
    private lateinit var alergia: TextView
    private lateinit var nombrepadre: TextView
    private lateinit var nombremadre: TextView
    private lateinit var telefono: TextView
    private lateinit var imgKidMooss: ImageView
    private lateinit var btnmodificar:Button
    private lateinit var databaseReference: DatabaseReference
    private lateinit var listener: ValueEventListener
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.admindatos)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val uid = intent.getStringExtra("id")

        //declaramos boton para regresar a el index
        val btnVolver = findViewById<ImageView>(R.id.btnRegresar)

        val btnDescargarPDF = findViewById<Button>(R.id.btnDownloadPDFs)

        btnDescargarPDF.setOnClickListener {
            createPDF(this)
        }

        btnVolver.setOnClickListener {
            val i = Intent(this, Asistencia_ChildCare::class.java)
            startActivity(i)
        }







        nombre = findViewById(R.id.textviewnombre)
        apellidos =  findViewById(R.id.textviwapellido)
        edad = findViewById(R.id.textviewedad)
        genero = findViewById(R.id.textviewgenero)
        alergia = findViewById(R.id.textviewalergia)
        nombremadre = findViewById(R.id.textviewnombremadre)
        nombrepadre = findViewById(R.id.textviewnombrepadre)
        telefono = findViewById(R.id.textviewnumero)
        imgKidMooss = findViewById(R.id.imageView)



        //firebaseAuth = Firebase.auth
        //database = FirebaseDatabase.getInstance()

        //val currentUser = firebaseAuth.currentUser



        if (uid != null) {
            val database = FirebaseDatabase.getInstance()
            val referencia = database.getReference("Usuarios_ChildCare/"+uid.toString()+"/Formulario")

            listener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val nombre1 = dataSnapshot.child("nombre").getValue(String::class.java) ?: ""
                        val apellidos1 = dataSnapshot.child("apellidos").getValue(String::class.java) ?: ""
                        val edad1 = dataSnapshot.child("edad").getValue(String::class.java) ?: ""
                        val genero1 = dataSnapshot.child("genero").getValue(String::class.java) ?: ""
                        val alergia1 = dataSnapshot.child("alergia").getValue(String::class.java) ?: ""
                        val nombrepadre1 = dataSnapshot.child("padre").getValue(String::class.java) ?: ""
                        val nombremadre1 = dataSnapshot.child("madre").getValue(String::class.java) ?: ""
                        val telefono1 = dataSnapshot.child("telefono").getValue(String::class.java) ?: ""

                        nombre.text = "Nombre :" + nombre1
                        apellidos.text = "Apellidos: " + apellidos1
                        edad.text = "Edad :" +  edad1
                        genero.text = "Genero :" + genero1
                        alergia.text = "Alergia" + alergia1
                        nombrepadre.text = "Nombre del padre : "+ nombrepadre1
                        nombremadre.text = "Nombre de la madre :" +nombremadre1
                        telefono.text = "Telefono :" + telefono1


                        val b = "imagenes/"+uid.toString()+".jpg"


                        val storage = FirebaseStorage.getInstance()
                        val storageRef = storage.reference
                        val imageRef = storageRef.child(b)
                        val token = uid.toString() // Reemplaza esto con tu token de acceso real
                        val urlWithTokenTask = imageRef.downloadUrl.addOnSuccessListener { uri ->
                            val urlWithToken = uri.toString() + "?token=$token"
                            // Cargar la imagen en el ImageView usando Glide
                            Glide.with(this@kidsDatos)
                                .load(urlWithToken)
                                .into(imgKidMooss)
                        }.addOnFailureListener { exception ->
                            // Manejar errores al generar la URL firmada
                        }

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

    private fun createPDF(context: Context) {
        // Crear un nuevo documento PDF
        val pdfDocument = PdfDocument()

        // Crear una página dentro del documento PDF
        val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
        val page = pdfDocument.startPage(pageInfo)

        // Escribir el contenido en la página
        val canvas = page.canvas

        val paint = Paint().apply {
            color = Color.BLACK // Puedes definir el color del texto aquí
            textSize = 16F // Tamaño del texto
            // Puedes definir otras propiedades del texto aquí, como el tipo de fuente, etc.
        }

        // Agregar la imagen al PDF
        val bitmap = (imgKidMooss.drawable as BitmapDrawable).bitmap
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, false)
        val centerX = (pageInfo.pageWidth - scaledBitmap.width) / 2F
        canvas.drawBitmap(scaledBitmap, centerX, 50F, paint)

        val datos = StringBuilder().apply {
            append(nombre.text.toString())
            append("\n")
            append(apellidos.text.toString())
            append("\n")
            append(edad.text.toString())
            append("\n")
            append(genero.text.toString())
            append("\n")
            append(alergia.text.toString())
            append("\n")
            append(nombrepadre.text.toString())
            append("\n")
            append(nombremadre.text.toString())
            append("\n")
            append(telefono.text.toString())
        }.toString()

        val lineas = datos.split("\n")
        val startY = 300F // Posición inicial de la primera línea después de la imagen

        // Dibuja cada línea de texto debajo de la imagen
        lineas.forEachIndexed { index, linea ->
            canvas.drawText(linea, 50F, startY + index * paint.textSize, paint)
        }

        // Finalizar la página
        pdfDocument.finishPage(page)

        // Guardar el documento PDF en la carpeta de descargas del almacenamiento externo
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        var fileName = "InfoUsuarioObtenidoDeAdmin.pdf"

        // Verificar si el archivo ya existe y cambiar el nombre si es necesario
        var counter = 1
        var file = File(directory, fileName)
        while (file.exists()) {
            fileName = "InfoUsuarioObtenidoDeAdmin_${counter++}.pdf"
            file = File(directory, fileName)
        }

        try {
            val tempFile = File.createTempFile(fileName, null, directory)

            val fileOutputStream = FileOutputStream(tempFile)
            pdfDocument.writeTo(fileOutputStream)
            pdfDocument.close()
            fileOutputStream.close()

            // Renombrar el archivo temporal con la extensión .pdf
            if (tempFile.renameTo(file)) {
                // El archivo se ha renombrado correctamente
                // Proporcionar una forma para que el usuario abra el PDF
                val contentUri = FileProvider.getUriForFile(context, "tu.paquete.nombre.fileprovider", file)
                val viewIntent = Intent().apply {
                    action = Intent.ACTION_VIEW
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    setDataAndType(contentUri, "application/pdf")
                }
                context.startActivity(viewIntent)

                Snackbar.make(
                    findViewById(android.R.id.content),
                    "DescargarPDF",
                    Snackbar.LENGTH_SHORT
                ).setBackgroundTint(ContextCompat.getColor(context, R.color.downloadPdf))
                    .show()
            } else {
                // No se pudo renombrar el archivo correctamente
                mostrarToastPersonalizadoError(context, "Error al guardar el archivo PDF")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            mostrarToastPersonalizadoError(context, "Error al guardar el archivo PDF")
        }
    }



    //funion para mostratr el toast
    private fun mostrarToastPersonalizadoError(context: Context, mensaje: String) {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.toast_error, null)

        val text = layout.findViewById<TextView>(R.id.text)
        text.text = mensaje

        val icon = layout.findViewById<ImageView>(R.id.icon)
        icon.setImageResource(R.drawable.error)

        with (Toast(context)) {
            duration = Toast.LENGTH_LONG
            view = layout
            show()
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
            duration = Toast.LENGTH_LONG
            view = layout
            show()
        }


    }

}