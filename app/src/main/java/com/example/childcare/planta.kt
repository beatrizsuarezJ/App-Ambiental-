package com.example.childcare

data class Planta(
    val nombre: String? = null,
    val grosor_cm: Double? = null,
    val altura_cm: Double? = null,
    val hojas: Int? = null,
    val total: Int? = null,
    val creadoEn: Long? = null,
    val imagenUrl: String? = null      // <-- nuevo para subir img
)

data class PlantaItem(
    val id: String,
    val planta: Planta
)