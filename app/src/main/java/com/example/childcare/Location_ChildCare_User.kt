package com.example.childcare

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions

class Location_ChildCare_User : AppCompatActivity(), OnMapReadyCallback {

    //declaramos una variable unircersal dentro de esta clase
    private lateinit var googleMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_child_care_user)

        //declaramos variable para regresar a la otra activitu
        val btnRegresarUser = findViewById<ImageView>(R.id.btnRegresar)

        btnRegresarUser.setOnClickListener {
            val i = Intent(this, Index_ChildCare::class.java)
            startActivity(i)
        }

        //declaeramos variable apra el mapa, se usa un fragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        //agregamos marcadores con ubicaciones y titulos
        //declaramos variable para obtener la ubicacion de la escuela
                                            //17.20165473657195, -93.00871197687101
        val geolocalizacion = LatLng(17.20165473657195, -93.00871197687101)
        googleMap.addMarker(MarkerOptions().position(geolocalizacion).title("Kinder ChildCare"))

                                            //17.204356565709787, -93.01574422240188
        val geolocalizacion2 = LatLng(17.204356565709787, -93.01574422240188)
        googleMap.addMarker(MarkerOptions().position(geolocalizacion2).title("UTSelva Rayon"))


        //ajustamos la camara para que los marcadores sean visisbles
        googleMap.setOnMapLoadedCallback {
            val bounds = LatLngBounds.Builder()
                .include(geolocalizacion)
                .include(geolocalizacion2)
                .build()

            val padding = 100 // Márgenes en píxeles alrededor del límite
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))

            //isZoomControlsEnabled:
            //Este método permite habilitar o deshabilitar los controles de zoom en el mapa.
            // Los controles de zoom son botones que permiten a los usuarios ampliar y reducir el mapa con facilidad.
            // Al habilitar esta opción, se mostrarán los botones de zoom en la interfaz de usuario del mapa.
            googleMap.uiSettings.isZoomControlsEnabled = true

            // Habilitar el botón para centrar en la ubicación actual
            googleMap.uiSettings.isMyLocationButtonEnabled = true

            //Muestra una brújula en la interfaz del mapa para indicar la dirección del norte.
            googleMap.uiSettings.isCompassEnabled = true

        }
    }

    override fun onBackPressed() {
        // Eliminar la llamada a super.onBackPressed() para evitar que el usuario regrese atrás
    }
}