package com.example.childcare

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.os.Handler
import com.bumptech.glide.Glide


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)


        // Cargar el GIF usando Glide en el ImageView
        Glide.with(this)
            .load(R.drawable.app_ambiental) // Cambia esto por el identificador correcto de tu GIF
            .into(findViewById(R.id.gifSplash))

        // Opcional: agregar un temporizador para que el splash screen desaparezca después de cierto tiempo
        Handler().postDelayed({
            // Este código se ejecutará después del tiempo especificado
            // Inicia la actividad principal de tu aplicación
            val intent = Intent(this, IniciarSesion_ChildCare::class.java)
            startActivity(intent)
            // Aplica animación de transición
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
            // Cierra esta actividad
            finish()
        }, 1500) // 6500 milisegundos (2 segundos) de tiempo de espera

    }
}