package com.example.childcare

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Planta(
    val nombre: String? = null,
    val grosor_cm: Double? = null,
    val altura_cm: Double? = null,
    val hojas: Int? = null,
    val total: Int? = null,
    val creadoEn: Long? = null,
    val imagenUrl: String? = null      // <-- nuevo para subir img
) : Parcelable

@Parcelize
data class PlantaItem(
    val id: String,
    val planta: Planta
) : Parcelable