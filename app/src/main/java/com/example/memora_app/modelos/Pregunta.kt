package com.example.memora_app.modelos


data class Pregunta(
    val id: String,
    val dificultad: String = "",
    val imagen: String?,
    val texto: String?,
    val pregunta: String,
    val opciones: List<String>,
    val respuestaCorrecta: Int,

)