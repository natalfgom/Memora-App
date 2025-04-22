package com.example.memora_app.juegos

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

object DificultadManager {

    private val db = FirebaseFirestore.getInstance()

    fun inicializarDesdeMMSE(pacienteId: String) {
        val pacienteRef = db.collection("Pacientes").document(pacienteId)
        val dificultadesRef = pacienteRef.collection("Dificultades")
        val actualRef = dificultadesRef.document("Actual")

        actualRef.get().addOnSuccessListener { doc ->
            val campos = doc.data
            if (campos == null || campos.isEmpty()) {
                Log.d(
                    "DificultadManager",
                    "Documento Actual no existe o está vacío. Buscando pruebas..."
                )

                val pruebasRef = pacienteRef.collection("Pruebas")
                pruebasRef.get().addOnSuccessListener { result ->
                    val documentos = result.documents
                    Log.d(
                        "DificultadManager",
                        "Se encontraron ${documentos.size} documentos de prueba."
                    )

                    val pruebaReciente = documentos
                        .filter { it.contains("Fecha") }
                        .sortedByDescending { it.getString("Fecha") }
                        .firstOrNull()

                    val fuente = pruebaReciente

                    if (fuente != null) {
                        Log.d("DificultadManager", "Usando prueba con ID: ${fuente.id}")
                    } else {
                        Log.w(
                            "DificultadManager",
                            "No se encontró ninguna prueba válida. Usando valores por defecto."
                        )
                    }

                    val data = fuente?.data as? Map<String, Any>
                    Log.d("DificultadManager", "Data cruda del documento fuente: $data")

                    val dificultades = if (data != null) {
                        mapOf(
                            "Comprensión" to mapOf(
                                "Dificultad" to getValorDificultad(data, "Comprensión"),
                                "Puntaje" to getValorPuntaje(data, "Comprensión")
                            ),
                            "Cálculo" to mapOf(
                                "Dificultad" to getValorDificultad(data, "Cálculo"),
                                "Puntaje" to getValorPuntaje(data, "Cálculo")
                            ),
                            "Memoria" to mapOf(
                                "Dificultad" to getValorDificultad(data, "Memoria"),
                                "Puntaje" to getValorPuntaje(data, "Memoria")
                            ),
                            "Orientación" to mapOf(
                                "Dificultad" to getValorDificultad(data, "Orientación"),
                                "Puntaje" to getValorPuntaje(data, "Orientación")
                            ),
                            "fechaUltimaActualizacion" to Timestamp.now(),
                            "MMSE_usado" to fuente.id
                        )
                    } else {
                        mapOf(
                            "Comprensión" to mapOf(
                                "Dificultad" to "Dificultad Media",
                                "Puntaje" to 0
                            ),
                            "Cálculo" to mapOf("Dificultad" to "Dificultad Media", "Puntaje" to 0),
                            "Memoria" to mapOf("Dificultad" to "Dificultad Media", "Puntaje" to 0),
                            "Orientación" to mapOf(
                                "Dificultad" to "Dificultad Media",
                                "Puntaje" to 0
                            ),
                            "fechaUltimaActualizacion" to Timestamp.now(),
                            "MMSE_usado" to ""
                        )
                    }

                    val hoy = LocalDate.now().toString()
                    dificultadesRef.document("Actual").set(dificultades)
                    dificultadesRef.document(hoy).set(dificultades)

                    Log.d("DificultadManager", "Dificultades iniciales guardadas correctamente.")
                }.addOnFailureListener {
                    Log.e("DificultadManager", "Error al obtener las pruebas: ${it.message}")
                }
            } else {
                Log.d(
                    "DificultadManager",
                    "Documento Actual ya existe y tiene datos. No se hace nada."
                )
            }
        }.addOnFailureListener {
            Log.e("DificultadManager", "Error al obtener documento Actual: ${it.message}")
        }
    }



    fun resultadosActualizar(pacienteId: String) {
        val pacienteRef = db.collection("Pacientes").document(pacienteId)
        val dificultadesRef = pacienteRef.collection("Dificultades")
        val actualRef = dificultadesRef.document("Actual")
        val hoy = LocalDate.now().toString()
        val hoyRef = dificultadesRef.document(hoy)

        val pruebasRef = pacienteRef.collection("Pruebas")
        pruebasRef.get().addOnSuccessListener { result ->
            val documentos = result.documents
            Log.d("DificultadManager", "Se encontraron ${documentos.size} documentos de prueba.")

            val pruebaReciente = documentos
                .filter { it.contains("Fecha") }
                .sortedByDescending { it.getString("Fecha") }
                .firstOrNull()

            val fuente = pruebaReciente

            if (fuente != null) {
                Log.d("DificultadManager", "Usando prueba con ID: ${fuente.id}")
            } else {
                Log.w(
                    "DificultadManager",
                    "No se encontró ninguna prueba válida. Usando valores por defecto."
                )
            }

            val data = fuente?.data as? Map<String, Any>
            Log.d("DificultadManager", "Data cruda del documento fuente: $data")

            val dificultades = if (data != null) {
                mapOf(
                    "Comprensión" to mapOf(
                        "Dificultad" to getValorDificultad(data, "Comprensión"),
                        "Puntaje" to getValorPuntaje(data, "Comprensión")
                    ),
                    "Cálculo" to mapOf(
                        "Dificultad" to getValorDificultad(data, "Cálculo"),
                        "Puntaje" to getValorPuntaje(data, "Cálculo")
                    ),
                    "Memoria" to mapOf(
                        "Dificultad" to getValorDificultad(data, "Memoria"),
                        "Puntaje" to getValorPuntaje(data, "Memoria")
                    ),
                    "Orientación" to mapOf(
                        "Dificultad" to getValorDificultad(data, "Orientación"),
                        "Puntaje" to getValorPuntaje(data, "Orientación")
                    ),
                    "fechaUltimaActualizacion" to Timestamp.now(),
                    "MMSE_usado" to fuente?.id.orEmpty()
                )
            } else {
                mapOf(
                    "Comprensión" to mapOf("Dificultad" to "Dificultad Media", "Puntaje" to 0),
                    "Cálculo" to mapOf("Dificultad" to "Dificultad Media", "Puntaje" to 0),
                    "Memoria" to mapOf("Dificultad" to "Dificultad Media", "Puntaje" to 0),
                    "Orientación" to mapOf("Dificultad" to "Dificultad Media", "Puntaje" to 0),
                    "fechaUltimaActualizacion" to Timestamp.now(),
                    "MMSE_usado" to ""
                )
            }


            actualRef.set(dificultades)
            Log.d("DificultadManager", "Documento 'Actual' actualizado correctamente.")


            hoyRef.get().addOnSuccessListener { docHoy ->
                if (!docHoy.exists()) {
                    hoyRef.set(dificultades)
                    Log.d("DificultadManager", "Documento del día '$hoy' creado correctamente.")
                } else {
                    Log.d(
                        "DificultadManager",
                        "Documento del día '$hoy' ya existía. No se sobrescribe."
                    )
                }
            }.addOnFailureListener {
                Log.e("DificultadManager", "Error al verificar el documento del día: ${it.message}")
            }
        }.addOnFailureListener {
            Log.e("DificultadManager", "Error al obtener las pruebas: ${it.message}")
        }
    }

    fun actualizarDificultades(pacienteId: String, forzar: Boolean = false, dias: Int = 7) {
        val pacienteRef = db.collection("Pacientes").document(pacienteId)
        val dificultadesRef = pacienteRef.collection("Dificultades")
        val actualRef = dificultadesRef.document("Actual")
        val hoy = LocalDate.now()

        actualRef.get().addOnSuccessListener { doc ->
            val timestampUltima = doc.get("fechaUltimaActualizacion") as? Timestamp
            val fechaUltima = timestampUltima?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
            val yaActualizadoHoy = fechaUltima == hoy
            val haPasadoTiempo = timestampUltima == null || hanPasado7Dias(timestampUltima)

            Log.d("DificultadManager", "Fecha anterior: $fechaUltima")
            Log.d("DificultadManager", "Fecha actual: $hoy")
            Log.d("DificultadManager", "Ya actualizado hoy: $yaActualizadoHoy")
            Log.d("DificultadManager", "Han pasado $dias días: $haPasadoTiempo")

            if (!forzar) {
                if (yaActualizadoHoy) {
                    Log.d("DificultadManager", "Ya se actualizó hoy. No se vuelve a actualizar.")
                    return@addOnSuccessListener
                }

                if (!haPasadoTiempo) {
                    Log.d("DificultadManager", "No han pasado $dias días. No se actualiza.")
                    return@addOnSuccessListener
                }
            }

            Log.d("DificultadManager", "Se procede a actualizar (forzar = $forzar, días = $dias)")

            val categorias = listOf("Comprensión", "Cálculo", "Memoria", "Orientación")
            val fechas = (0 until dias).map { LocalDate.now().minusDays(it.toLong()).toString() }

            val dificultadesAnteriores = mutableMapOf<String, String>()
            for (cat in categorias) {
                val diff = (doc.get(cat) as? Map<*, *>)?.get("Dificultad") as? String ?: "Dificultad Media"
                dificultadesAnteriores[cat] = diff
            }

            val nuevasDificultades = mutableMapOf<String, Map<String, Any>>()
            var pendientes = categorias.size

            categorias.forEach { categoria ->
                val juegosRef = pacienteRef.collection("Juegos").document(categoria).collection("Fechas")

                juegosRef.get().addOnSuccessListener { allDocs ->
                    val puntajes = fechas.mapNotNull { fecha ->
                        allDocs.documents.find { it.id == fecha }?.getLong("puntaje")?.toInt()
                    }

                    Log.d("DificultadManager", "📈 Categoría: $categoria → Puntajes últimos $dias días: $puntajes")

                    val media = if (puntajes.isNotEmpty()) puntajes.average().toInt() else 0
                    val dificultadAnterior = dificultadesAnteriores[categoria] ?: "Dificultad Media"

                    val nuevaDificultad = when {
                        media >= 4 -> when (dificultadAnterior) {
                            "Dificultad Baja" -> "Dificultad Media"
                            "Dificultad Media" -> "Dificultad Alta"
                            else -> "Dificultad Alta"
                        }

                        media in 2..3 -> dificultadAnterior

                        else -> when (dificultadAnterior) {
                            "Dificultad Alta" -> "Dificultad Media"
                            "Dificultad Media" -> "Dificultad Baja"
                            else -> "Dificultad Baja"
                        }
                    }

                    Log.d("DificultadManager", "📊 Categoría: $categoria → Media: $media → Nueva: $nuevaDificultad")

                    nuevasDificultades[categoria] = mapOf(
                        "Dificultad" to nuevaDificultad,
                        "Puntaje" to media
                    )

                    pendientes--
                    if (pendientes == 0) {
                        val resultadoFinal = mutableMapOf<String, Any>(
                            "fechaUltimaActualizacion" to com.google.firebase.Timestamp.now()
                        )
                        resultadoFinal.putAll(nuevasDificultades)

                        dificultadesRef.document("Actual").set(resultadoFinal)
                        dificultadesRef.document(hoy.toString()).set(resultadoFinal)
                        Log.d("DificultadManager", "✅ Actualización de dificultades completada.")
                    }
                }
            }
        }
    }






    private fun getValorDificultad(data: Map<String, Any>?, categoria: String): String {
        val submap = data?.get(categoria) as? Map<*, *>
        return submap?.get("Dificultad") as? String ?: "Dificultad Media"
    }

    private fun getValorPuntaje(data: Map<String, Any>?, categoria: String): Int {
        val submap = data?.get(categoria) as? Map<*, *>
        return (submap?.get("Puntaje") as? Number)?.toInt() ?: 0
    }

    fun hanPasado7Dias(timestamp: Timestamp): Boolean {
        val zona = ZoneId.systemDefault()
        val fechaAnterior = timestamp.toDate().toInstant().atZone(zona).toLocalDate()
        val fechaHoy = LocalDate.now(zona)
        val dias = ChronoUnit.DAYS.between(fechaAnterior, fechaHoy)

        Log.d("DificultadManager", "Fecha anterior: $fechaAnterior")
        Log.d("DificultadManager", "Fecha actual: $fechaHoy")
        Log.d("DificultadManager", "Días transcurridos: $dias")

        return dias >= 7
    }


}
