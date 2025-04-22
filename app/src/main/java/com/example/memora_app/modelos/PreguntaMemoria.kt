package com.example.memora_app.modelos

data class PreguntaMemoria(
    val id: String,
    val dificultad: String,
    val imagenOriginal: String? = null,
    val imagen: String,
    val imagenPista: String? = null,
    val texto: String?,
    val pregunta: String,
    val opciones: List<String>,
    val respuestaCorrecta: Int = -1,
    val respuestasCorrectas: List<Int> = emptyList()
)
