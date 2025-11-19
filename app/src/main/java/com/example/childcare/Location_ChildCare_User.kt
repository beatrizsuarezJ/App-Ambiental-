package com.example.childcare

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions

class Location_ChildCare_User : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap

    // Código de solicitud de permisos
    private val LOCATION_PERMISSION_REQUEST = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_child_care_user)

        val btnRegresarUser = findViewById<ImageView>(R.id.btnRegresar)
        btnRegresarUser.setOnClickListener {
            val i = Intent(this, Index_ChildCare::class.java)
            startActivity(i)
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Primero pedimos permisos
        solicitarPermisosUbicacion()

        // Marcadores
        val geolocalizacion = LatLng(17.20165473657195, -93.00871197687101)
        googleMap.addMarker(MarkerOptions().position(geolocalizacion).title("Kinder ChildCare"))

        val geolocalizacion2 = LatLng(17.204356565709787, -93.01574422240188)
        googleMap.addMarker(MarkerOptions().position(geolocalizacion2).title("UTSelva Rayon"))

        googleMap.setOnMapLoadedCallback {
            val bounds = LatLngBounds.Builder()
                .include(geolocalizacion)
                .include(geolocalizacion2)
                .build()

            val padding = 100
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))

            googleMap.uiSettings.isZoomControlsEnabled = true
            googleMap.uiSettings.isCompassEnabled = true
        }
    }

    // ---------------------------
    //      PERMISOS
    // ---------------------------

    private fun solicitarPermisosUbicacion() {
        val permisoFine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val permisoCoarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)

        if (permisoFine == PackageManager.PERMISSION_GRANTED &&
            permisoCoarse == PackageManager.PERMISSION_GRANTED) {
            activarUbicacion()
        } else {
            // Muestra el cuadro de diálogo de permisos
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST
            )
        }
    }

    private fun activarUbicacion() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        googleMap.isMyLocationEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = true
    }

    // Resultado del cuadro de permisos
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                activarUbicacion()
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onBackPressed() {
        // Deshabilitar atrás
    }
}
