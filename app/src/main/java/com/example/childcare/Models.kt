package com.example.childcare

// Modelo para Zonas
data class Zona(
    val nombre: String = ""
)

// Modelo para Usuario
data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val correo_Electronico: String = "",
    val rol: Int = 0,
    val zona: String = ""
)

// Modelo para Plantas (renombrado para no chocar con planta.kt)
data class PlantaData(
    val nombre: String = "",
    val altura_cm: Int = 0,
    val grosor_cm: Int = 0,
    val hojas: Int = 0,
    val total: Int = 0,
    val imagenUrl: String = ""
)
