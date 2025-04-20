package com.example.memora_app.modelos

object PreguntasOri {

    fun getPreguntasPorDificultadOri(dificultad: String): List<Pregunta> {
        return getTodasLasPreguntas().filter { it.dificultad.equals(dificultad, ignoreCase = true) }
    }

    private fun getTodasLasPreguntas(): List<Pregunta> {
        return listOf(

            // DIFICULTAD BAJA
            Pregunta(
                id = "ori_fac_1",
                dificultad = "Dificultad Baja",
                imagen = "playafuera",
                texto = null,
                pregunta = "¿Dónde estás?",
                opciones = listOf("En el desierto", "En la playa", "En un parque", "En un restaurante"),
                respuestaCorrecta = 1
            ),
            Pregunta(
                id = "ori_fac_2",
                dificultad = "Dificultad Baja",
                imagen = "casadentro",
                texto = null,
                pregunta = "¿Dónde estás?",
                opciones = listOf("Dentro de una casa", "Fuera de un colegio", "En la playa", "En un bosque"),
                respuestaCorrecta = 0
            ),
            Pregunta(
                id = "ori_fac_3",
                dificultad = "Dificultad Baja",
                imagen = "parque",
                texto = null,
                pregunta = "¿Dónde estás?",
                opciones = listOf("En el parque", "En un colegio", "En la cocina", "En un hospital"),
                respuestaCorrecta = 0
            ),
            Pregunta(
                id = "ori_fac_4",
                dificultad = "Dificultad Baja",
                imagen = "restaurantedentro",
                texto = null,
                pregunta = "¿Dónde estás?",
                opciones = listOf("En una casa", "Dentro de un restaurante", "En un parque", "En un colegio"),
                respuestaCorrecta = 1
            ),
            Pregunta(
                id = "ori_fac_5",
                dificultad = "Dificultad Baja",
                imagen = "colegiodentro",
                texto = null,
                pregunta = "¿Dónde estás?",
                opciones = listOf("En una casa", "En un bar", "Dentro de un colegio", "En la calle"),
                respuestaCorrecta = 2
            ),

            // DIFICULTAD MEDIA
            Pregunta(
                id = "ori_med_1",
                dificultad = "Dificultad Media",
                imagen = "casadentro",
                texto = null,
                pregunta = "¿Estás dentro o fuera de una casa?",
                opciones = listOf("Dentro", "Fuera"),
                respuestaCorrecta = 0
            ),
            Pregunta(
                id = "ori_med_2",
                dificultad = "Dificultad Media",
                imagen = "playafuera",
                texto = null,
                pregunta = "¿Estás dentro o fuera de un edificio?",
                opciones = listOf("Dentro", "Fuera"),
                respuestaCorrecta = 1
            ),
            Pregunta(
                id = "ori_med_3",
                dificultad = "Dificultad Media",
                imagen = "colegiodentro",
                texto = null,
                pregunta = "¿Estás dentro o fuera de un colegio?",
                opciones = listOf("Dentro", "Fuera"),
                respuestaCorrecta = 0
            ),
            Pregunta(
                id = "ori_med_4",
                dificultad = "Dificultad Media",
                imagen = "desiertofuera",
                texto = null,
                pregunta = "¿Estás dentro o fuera de un lugar cerrado?",
                opciones = listOf("Dentro", "Fuera"),
                respuestaCorrecta = 1
            ),
            Pregunta(
                id = "ori_med_5",
                dificultad = "Dificultad Media",
                imagen = "restaurantedentro",
                texto = null,
                pregunta = "¿Estás dentro o fuera de un restaurante?",
                opciones = listOf("Dentro", "Fuera"),
                respuestaCorrecta = 0
            ),

            // DIFICULTAD ALTA
            Pregunta(
                id = "ori_dif_1",
                dificultad = "Dificultad Alta",
                imagen = "plano1",
                texto = null,
                pregunta = "Estás en la entrada. Si giras a la derecha, ¿dónde estás?",
                opciones = listOf("Baño", "Salón", "Cocina", "Dormitorio"),
                respuestaCorrecta = 1
            ),
            Pregunta(
                id = "ori_dif_2",
                dificultad = "Dificultad Alta",
                imagen = "plano2",
                texto = null,
                pregunta = "Si sigues recto, ¿qué habitación hay?",
                opciones = listOf("Salón", "Cocina", "Baño", "Dormitorio"),
                respuestaCorrecta = 0
            ),
            Pregunta(
                id = "ori_dif_3",
                dificultad = "Dificultad Alta",
                imagen = "plano3",
                texto = null,
                pregunta = "Si caminas hacia abajo y luego hacia la derecha, ¿dónde entras?",
                opciones = listOf("Baño", "Dormitorio", "Salón", "Terraza"),
                respuestaCorrecta = 0
            ),
            Pregunta(
                id = "ori_dif_4",
                dificultad = "Dificultad Alta",
                imagen = "plano4",
                texto = null,
                pregunta = "Si andas en diagonal a la derecha, ¿dónde estás?",
                opciones = listOf("Estanque", "Árboles", "Plantas", "Parque"),
                respuestaCorrecta = 0
            ),
            Pregunta(
                id = "ori_dif_5",
                dificultad = "Dificultad Alta",
                imagen = "plano5",
                texto = null,
                pregunta = "Si andas hacia abajo y giras a la derecha, ¿qué habitación hay?",
                opciones = listOf("Aula", "Comedor", "Biblioteca", "Dormitorio"),
                respuestaCorrecta = 1
            )
        )
    }
}
