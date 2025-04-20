package com.example.memora_app.modelos

object PreguntasMemoriaData {

    fun getPreguntasPorDificultad(nivel: String): List<PreguntaMemoria> {
        return when (nivel) {

            "Dificultad Baja" -> listOf(
                PreguntaMemoria(
                    id = "B1",
                    dificultad = "Dificultad Baja",
                    imagenOriginal = "salon",
                    imagen = "salonsinlibreta",
                    imagenPista = "salonsinlibreta_pista",
                    texto = null,
                    pregunta = "¿Qué objeto falta en la imagen?",
                    opciones = listOf("Teléfono", "Campana", "Libreta"),
                    respuestaCorrecta = 2
                ),
                PreguntaMemoria(
                    id = "B2",
                    dificultad = "Dificultad Baja",
                    imagenOriginal = "cocinaa",
                    imagen = "cocinasincampana",
                    imagenPista = "cocinasincampana_pista",
                    texto = null,
                    pregunta = "¿Qué objeto falta en la cocina?",
                    opciones = listOf("Campana", "Flor", "Plato"),
                    respuestaCorrecta = 0
                ),
                PreguntaMemoria(
                    id = "B3",
                    dificultad = "Dificultad Baja",
                    imagenOriginal = "bano",
                    imagen = "banosinalfombra",
                    imagenPista = "banosinalfombra_pista",
                    texto = null,
                    pregunta = "¿Qué objeto falta en el baño?",
                    opciones = listOf("Cortina", "Toalla", "Alfombra"),
                    respuestaCorrecta = 2
                )
            )

            "Dificultad Media" -> listOf(
                PreguntaMemoria(
                    id = "M1",
                    dificultad = "Dificultad Media",
                    imagenOriginal = "salon",
                    imagen = "salonsinlibretatelefono",
                    imagenPista = "salonsinlibretatelefono_pista",
                    texto = null,
                    pregunta = "¿Qué dos objetos faltan?",
                    opciones = listOf("Campana", "Libreta", "Teléfono", "Jarrón", "Reloj", "Llaves"),
                    respuestasCorrectas = listOf(1, 2)
                ),
                PreguntaMemoria(
                    id = "M2",
                    dificultad = "Dificultad Media",
                    imagenOriginal = "cocinaa",
                    imagen = "cocinasincampanaflor",
                    imagenPista = "cocinasincampanaflor_pista",
                    texto = null,
                    pregunta = "¿Qué dos objetos faltan en la imagen?",
                    opciones = listOf("Florero", "Campana", "Alfombra", "Teléfono", "Puerta", "Frutero"),
                    respuestasCorrectas = listOf(0, 1)
                )
            )

            "Dificultad Alta" -> listOf(
                PreguntaMemoria(
                    id = "A1",
                    dificultad = "Dificultad Alta",
                    imagenOriginal = "salon",
                    imagen = "salonsinlibretatelefonotelevision",
                    imagenPista = "salonsinlibretatelefonotelevision_pista",
                    texto = null,
                    pregunta = "¿Qué tres objetos faltan?",
                    opciones = listOf("Campana", "Libreta", "Flor", "Cortina", "Teléfono", "Televisión", "Reloj", "Llaves"),
                    respuestasCorrectas = listOf(1, 4, 5)
                ),
                PreguntaMemoria(
                    id = "A2",
                    dificultad = "Dificultad Alta",
                    imagenOriginal = "cocinaa",
                    imagen = "cocinasincampanaflorplato",
                    imagenPista = "cocinasincampanaflorplato_pista",
                    texto = null,
                    pregunta = "¿Qué objetos faltan en esta imagen?",
                    opciones = listOf("Campana", "Plato", "Pizarra", "Teléfono", "Coche", "Flor", "Puerta", "Frutero"),
                    respuestasCorrectas = listOf(0, 1, 5)
                )
            )

            else -> emptyList()
        }
    }
}
