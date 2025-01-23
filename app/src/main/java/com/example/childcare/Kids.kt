package com.example.childcare

data class Kids(
    val nombre:String = "",
    val apellidos:String="",
    val asistencia:String = "",
    val edad:String="",
    val genero:String="",
    val alergia:String="",
    val madre:String="",
    val padre:String="",
    val telefono:String="",
    val id:String=""
) {
    constructor(nombre: String, asistencia: String) : this("", "", "","","","","","","","")
}