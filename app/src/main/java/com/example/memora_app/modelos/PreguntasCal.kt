package com.example.memora_app.modelos

object PreguntasCal {

    fun getPreguntasPorDificultadCal(dificultad: String): List<Pregunta> {
        return getTodasLasPreguntas().filter { it.dificultad.equals(dificultad, ignoreCase = true) }
    }

    private fun getTodasLasPreguntas(): List<Pregunta> {
        return listOf(

            Pregunta(
                id = "cal_fac_1",
                dificultad = "Dificultad Baja",
                imagen = null,
                texto = null,
                pregunta = "¿Cuánto es 3 + 5?",
                opciones = listOf("6", "8", "9", "10"),
                respuestaCorrecta = 1
            ),
            Pregunta(
                id = "cal_fac_2",
                dificultad = "Dificultad Baja",
                imagen = null,
                texto = null,
                pregunta = "¿Cuánto es 9 - 4?",
                opciones = listOf("5", "6", "4", "3"),
                respuestaCorrecta = 0
            ),
            Pregunta(
                id = "cal_fac_3",
                dificultad = "Dificultad Baja",
                imagen = null,
                texto = null,
                pregunta = "¿Cuánto es 6 + 2?",
                opciones = listOf("7", "9", "8", "6"),
                respuestaCorrecta = 2
            ),
            Pregunta(
                id = "cal_fac_4",
                dificultad = "Dificultad Baja",
                imagen = null,
                texto = null,
                pregunta = "¿Cuánto es 10 - 7?",
                opciones = listOf("2", "3", "4", "5"),
                respuestaCorrecta = 1
            ),
            Pregunta(
                id = "cal_fac_5",
                dificultad = "Dificultad Baja",
                imagen = null,
                texto = null,
                pregunta = "¿Cuánto es 4 + 4?",
                opciones = listOf("6", "9", "8", "7"),
                respuestaCorrecta = 2
            ),


            Pregunta(
                id = "cal_med_1",
                dificultad = "Dificultad Media",
                imagen = null,
                texto = "Recuerda aplicar primero la multiplicación",
                pregunta = "¿Cuánto es 6 + 2 × 3?",
                opciones = listOf("24", "12", "18", "10"),
                respuestaCorrecta = 1
            ),
            Pregunta(
                id = "cal_med_2",
                dificultad = "Dificultad Media",
                imagen = null,
                texto = "Resuelve lo que está dentro del paréntesis primero",
                pregunta = "¿Cuánto es (10 - 4) × 2?",
                opciones = listOf("12", "8", "10", "14"),
                respuestaCorrecta = 0
            ),
            Pregunta(
                id = "cal_med_3",
                dificultad = "Dificultad Media",
                imagen = null,
                texto = null,
                pregunta = "¿Cuánto es 5 × 3 - 4?",
                opciones = listOf("11", "12", "15", "9"),
                respuestaCorrecta = 0
            ),
            Pregunta(
                id = "cal_med_4",
                dificultad = "Dificultad Media",
                imagen = null,
                texto = null,
                pregunta = "¿Cuánto es 7 + 6 / 2?",
                opciones = listOf("8", "10", "11", "13"),
                respuestaCorrecta = 1
            ),
            Pregunta(
                id = "cal_med_5",
                dificultad = "Dificultad Media",
                imagen = null,
                texto = null,
                pregunta = "¿Cuánto es 9 - 3 × 2?",
                opciones = listOf("3", "9", "6", "0"),
                respuestaCorrecta = 0
            ),


            Pregunta(
                id = "cal_dif_1",
                dificultad = "Dificultad Alta",
                imagen = null,
                texto = "Reparte 4 caramelos a cada uno de los 3 amigos",
                pregunta = "Tienes 12 caramelos y das 3 a cada uno de tus 3 amigos. ¿Cuántos te quedan?",
                opciones = listOf("3", "4", "6", "9"),
                respuestaCorrecta = 1
            ),
            Pregunta(
                id = "cal_dif_2",
                dificultad = "Dificultad Alta",
                imagen = null,
                texto = "Velocidad × tiempo = distancia",
                pregunta = "Si un tren recorre 60 km en una hora, ¿cuántos km recorrerá en 3 horas?",
                opciones = listOf("120", "90", "180", "150"),
                respuestaCorrecta = 2
            ),
            Pregunta(
                id = "cal_dif_3",
                dificultad = "Dificultad Alta",
                imagen = null,
                texto = null,
                pregunta = "Tienes 15€. Compras 2 lápices a 2€ y una libreta a 5€. ¿Cuánto te queda?",
                opciones = listOf("4", "6", "5", "3"),
                respuestaCorrecta = 1
            ),
            Pregunta(
                id = "cal_dif_4",
                dificultad = "Dificultad Alta",
                imagen = null,
                texto = null,
                pregunta = "Una caja tiene 4 filas de 6 manzanas. ¿Cuántas hay en total?",
                opciones = listOf("24", "12", "18", "30"),
                respuestaCorrecta = 0
            ),
            Pregunta(
                id = "cal_dif_5",
                dificultad = "Dificultad Alta",
                imagen = null,
                texto = null,
                pregunta = "Pedro tenía 50€. Compró una camiseta por 18€ y un pantalón por 25€. ¿Cuánto le queda?",
                opciones = listOf("7", "8", "10", "9"),
                respuestaCorrecta = 0
            )
        )
    }
}
