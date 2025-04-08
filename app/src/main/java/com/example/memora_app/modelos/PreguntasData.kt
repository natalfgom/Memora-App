package com.example.memora_app.modelos

object PreguntasData {

    fun getPreguntasPorDificultadYCategoria(nivel: String): List<Pregunta> {
        return when (nivel) {
            "Dificultad Baja" -> listOf(
                Pregunta(
                    id = "F1",
                    dificultad = "Dificultad Baja",
                    imagen = "helado",
                    texto = null,
                    pregunta = "¿Qué hace el niño?",
                    opciones = listOf("Juega", "Come un helado", "Corre"),
                    respuestaCorrecta = 1
                ),
                Pregunta(
                    id = "F2",
                    dificultad = "Dificultad Baja",
                    imagen = "libro",
                    texto = null,
                    pregunta = "¿Qué está haciendo la mujer?",
                    opciones = listOf("Ver la tele", "Leer un libro", "Mirar su ordenador"),
                    respuestaCorrecta = 1
                ),
                Pregunta(
                    id = "F3",
                    dificultad = "Dificultad Baja",
                    imagen = "pelota",
                    texto = null,
                    pregunta = "¿Qué hace el niño?",
                    opciones = listOf("Juega al balón", "Come un helado", "Duerme"),
                    respuestaCorrecta = 0
                ),
                Pregunta(
                    id = "F4",
                    dificultad = "Dificultad Baja",
                    imagen = "perro",
                    texto = null,
                    pregunta = "¿Qué hace el perro?",
                    opciones = listOf("Beber", "Comer", "Correr"),
                    respuestaCorrecta = 0
                ),
                Pregunta(
                    id = "F5",
                    dificultad = "Dificultad Baja",
                    imagen = "coche",
                    texto = null,
                    pregunta = "¿Qué es?",
                    opciones = listOf("Coche", "Autobús", "Tren"),
                    respuestaCorrecta = 0
                )
            )

            "Dificultad Media" -> listOf(
                Pregunta(
                    id = "M1",
                    dificultad = "Dificultad Media",
                    imagen = null,
                    texto = "Marta fue al mercado y compró naranjas y plátanos.",
                    pregunta = "¿Qué compró Marta?",
                    opciones = listOf("Manzanas", "Plátanos y naranjas", "Uvas"),
                    respuestaCorrecta = 1
                ),
                Pregunta(
                    id = "M2",
                    dificultad = "Dificultad Media",
                    imagen = null,
                    texto = "Pedro tiene un perro negro y uno blanco.",
                    pregunta = "¿Qué color tienen los perros de Pedro?",
                    opciones = listOf("Negro y blanco", "Marrón y gris", "Negro y marrón"),
                    respuestaCorrecta = 0
                ),
                Pregunta(
                    id = "M3",
                    dificultad = "Dificultad Media",
                    imagen = null,
                    texto = "Laura desayunó tostadas con miel y un vaso de leche.",
                    pregunta = "¿Qué comió Laura?",
                    opciones = listOf("Cereales", "Tostadas con miel", "Pan con mantequilla"),
                    respuestaCorrecta = 1
                ),
                Pregunta(
                    id = "M4",
                    dificultad = "Dificultad Media",
                    imagen = null,
                    texto = "El gato duerme siempre en el sofá rojo de la sala.",
                    pregunta = "¿Dónde duerme el gato?",
                    opciones = listOf("En la cama", "En el sofá rojo", "En la cocina"),
                    respuestaCorrecta = 1
                ),
                Pregunta(
                    id = "M5",
                    dificultad = "Dificultad Media",
                    imagen = null,
                    texto = "El autobús llegó a tiempo y todos los estudiantes subieron rápidamente.",
                    pregunta = "¿Qué hicieron los estudiantes?",
                    opciones = listOf("Esperaron", "Corrieron", "Subieron al autobús"),
                    respuestaCorrecta = 2
                )
            )

            "Dificultad Alta" -> listOf(
                Pregunta(
                    id = "D1",
                    dificultad = "Dificultad Alta",
                    imagen = null,
                    texto = "Carlos salió de casa con su paraguas. El cielo estaba gris y el viento soplaba fuerte.",
                    pregunta = "¿Qué tiempo hacía?",
                    opciones = listOf("Hacía calor", "Estaba soleado", "Llovía"),
                    respuestaCorrecta = 2
                ),
                Pregunta(
                    id = "D2",
                    dificultad = "Dificultad Alta",
                    imagen = null,
                    texto = "Ana llegó tarde al colegio porque su autobús tuvo un accidente en la carretera y tuvo que esperar mucho.",
                    pregunta = "¿Por qué llegó tarde Ana?",
                    opciones = listOf("Se quedó dormida", "Hubo un accidente", "Perdió el autobús"),
                    respuestaCorrecta = 1
                ),
                Pregunta(
                    id = "D3",
                    dificultad = "Dificultad Alta",
                    imagen = null,
                    texto = "El abuelo de Juan toma té todas las tardes mientras lee el periódico en su sillón favorito.",
                    pregunta = "¿Qué hace el abuelo por la tarde?",
                    opciones = listOf("Ve la televisión", "Juega a las cartas", "Lee y toma té"),
                    respuestaCorrecta = 2
                ),
                Pregunta(
                    id = "D4",
                    dificultad = "Dificultad Alta",
                    imagen = null,
                    texto = "Marcos se puso su abrigo y bufanda antes de salir. Afuera, el termómetro marcaba 3 grados.",
                    pregunta = "¿Qué estación del año podría ser?",
                    opciones = listOf("Verano", "Invierno", "Primavera"),
                    respuestaCorrecta = 1
                ),
                Pregunta(
                    id = "D5",
                    dificultad = "Dificultad Alta",
                    imagen = null,
                    texto = "Julia no trajo paraguas y terminó empapada al llegar a casa. Había charcos por todas partes.",
                    pregunta = "¿Qué había ocurrido?",
                    opciones = listOf("Hacía viento", "Había llovido", "Nevaba"),
                    respuestaCorrecta = 1
                )
            )

            else -> emptyList()
        }
    }
}
