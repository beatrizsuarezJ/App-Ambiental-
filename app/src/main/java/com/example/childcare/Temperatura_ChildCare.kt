package com.example.childcare

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.database.*

class Temperatura_ChildCare : AppCompatActivity() {
    // Declara una referencia a tu base de datos de Firebase
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_temperatura_child_care)

        // Inicializa la referencia a tu base de datos de Firebase
        database = FirebaseDatabase.getInstance().getReference("ClimaSalon")

        // Para regresar al índice
        val btnRegresar = findViewById<ImageView>(R.id.btnRegresar)
        btnRegresar.setOnClickListener{
            val i = Intent(this, Index_ChildCare::class.java)
            startActivity(i)
        }

        // Obtener una referencia al TableLayout
        val tableLayout = findViewById<TableLayout>(R.id.tableLayout)

        // Agrega un Listener para escuchar los cambios en los datos de Firebase
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Limpiar la tabla antes de agregar nuevos datos
                tableLayout.removeAllViews()

                // Iterar sobre los hijos del nodo ClimaSalon
                for (snapshot in dataSnapshot.children) {
                    val dato = snapshot.key.toString() // Obtener el nombre del dato (Grados, Estado del Ventilador, Humedad)
                    val valor = snapshot.value.toString() // Obtener el valor del dato

                    // Agregar una nueva fila a la tabla con el dato y su valor
                    agregarFila(tableLayout, dato, valor)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar errores de lectura de la base de datos
                Log.w("Temperatura_ChildCare", "Error al leer los datos", error.toException())
            }
        })
    }

    // Función para agregar una fila a la tabla con un dato y su valor
    private fun agregarFila(tableLayout: TableLayout, dato: String, valor: String) {
        val tableRow = TableRow(this)
        tableRow.setBackgroundResource(R.drawable.cell_border) // Fondo con bordes

        val textViewDato = TextView(this)
        textViewDato.text = "$dato:"
        textViewDato.gravity = Gravity.START
        textViewDato.setTextAppearance(R.style.TableCell)

        // Crear la línea divisoria (View)
        val divider = View(this)
        val dividerParams = TableRow.LayoutParams(6, TableRow.LayoutParams.MATCH_PARENT) // Ancho de 1, altura que ocupe toda la celda
        dividerParams.setMargins(8, 8, 8, 8) // (Opcional) Agrega márgenes a la línea para espaciarla
        divider.layoutParams = dividerParams
        divider.setBackgroundColor(ContextCompat.getColor(this, android.R.color.black)) // Color negro

        val textViewValor = TextView(this)
        textViewValor.text = valor
        textViewValor.gravity = Gravity.END
        textViewValor.setTextAppearance(R.style.TableCell)

        tableRow.addView(textViewDato)
        tableRow.addView(divider) // Agregar la línea divisoria
        tableRow.addView(textViewValor)

        tableLayout.addView(tableRow)
    }



    // Eliminar la llamada a super.onBackPressed() para evitar que el usuario regrese atrás
    override fun onBackPressed() { }
}
