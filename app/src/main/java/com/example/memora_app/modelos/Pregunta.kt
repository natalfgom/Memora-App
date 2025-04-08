package com.example.memora_app.modelos


data class Pregunta(
    val id: String,
    val dificultad: String = "",
    val imagen: String?, // nombre del recurso en drawable, solo para nivel fácil
    val texto: String?,  // texto adicional para niveles medio y difícil
    val pregunta: String,
    val opciones: List<String>,
    val respuestaCorrecta: Int, // índice dentro de la lista de opciones

)