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

    fun comprobarYActualizarDesdeMMSE(pacienteId: String) {
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

            // 1. Actualiza siempre el documento "Actual"
            actualRef.set(dificultades)
            Log.d("DificultadManager", "Documento 'Actual' actualizado correctamente.")

            // 2. Solo crea el documento del día si no existe
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

    fun actualizarDificultades(pacienteId: String, forzar: Boolean = false) {
        val pacienteRef = db.collection("Pacientes").document(pacienteId)
        val dificultadesRef = pacienteRef.collection("Dificultades")
        val actualRef = dificultadesRef.document("Actual")
        val hoy = LocalDate.now().toString()

        actualRef.get().addOnSuccessListener { doc ->
            val fechaUltima = doc.get("fechaUltimaActualizacion") as? Timestamp

            // 🔁 Ahora se permite forzar la actualización
            if (forzar || fechaUltima == null || hanPasado7Dias(fechaUltima)) {
                Log.d("DificultadManager", "Actualizando dificultades (forzar = $forzar)")

                val categorias = listOf("Comprensión", "Cálculo", "Memoria", "Orientación")
                val fechas = (0..6).map { LocalDate.now().minusDays(it.toLong()).toString() }
                val nuevasDificultades = mutableMapOf<String, Map<String, Any>>()
                var pendientes = categorias.size

                categorias.forEach { categoria ->
                    val juegosRef = pacienteRef.collection("Juegos")
                        .document(categoria)
                        .collection("Fechas")

                    juegosRef.get().addOnSuccessListener { allDocs ->
                        val puntajes = fechas.mapNotNull { fecha ->
                            allDocs.documents.find { it.id == fecha }?.getLong("puntaje")?.toInt()
                        }

                        val media = if (puntajes.isNotEmpty()) puntajes.average().toInt() else 0
                        val dificultad = when {
                            media >= 4 -> "Dificultad Alta"
                            media in 2..3 -> "Dificultad Media"
                            else -> "Dificultad Baja"
                        }

                        nuevasDificultades[categoria] = mapOf(
                            "Dificultad" to dificultad,
                            "Puntaje" to media
                        )

                        pendientes--
                        if (pendientes == 0) {
                            // Construimos el resultado final
                            val resultadoFinal = mutableMapOf<String, Any>(
                                "fechaUltimaActualizacion" to Timestamp.now()
                            )
                            resultadoFinal.putAll(nuevasDificultades)

                            // Actualizamos "Actual"
                            dificultadesRef.document("Actual").set(resultadoFinal)
                                .addOnSuccessListener {
                                    Log.d("DificultadManager", "Documento 'Actual' actualizado.")
                                }
                                .addOnFailureListener {
                                    Log.e(
                                        "DificultadManager",
                                        "Error al actualizar 'Actual': ${it.message}"
                                    )
                                }

                            // Creamos historial del día
                            dificultadesRef.document(hoy).set(resultadoFinal)
                                .addOnSuccessListener {
                                    Log.d(
                                        "DificultadManager",
                                        "Historial de dificultades guardado para '$hoy'."
                                    )
                                }
                                .addOnFailureListener {
                                    Log.e(
                                        "DificultadManager",
                                        "Error al guardar historial de '$hoy': ${it.message}"
                                    )
                                }
                        }
                    }.addOnFailureListener {
                        Log.e(
                            "DificultadManager",
                            "Error al obtener juegos de $categoria: ${it.message}"
                        )
                    }
                }

            } else {
                Log.d(
                    "DificultadManager",
                    "No se actualiza porque no han pasado 7 días y no se forzó."
                )
            }
        }.addOnFailureListener {
            Log.e("DificultadManager", "Error al obtener documento 'Actual': ${it.message}")
        }
    }


    fun obtenerDificultadActual(pacienteId: String, categoria: String, callback: (String) -> Unit) {
        db.collection("Pacientes").document(pacienteId)
            .collection("Dificultades").document("Actual")
            .get().addOnSuccessListener { doc ->
                val dificultad = (doc.get(categoria) as? Map<*, *>)?.get("Dificultad") as? String
                callback(dificultad ?: "Dificultad Media")
            }.addOnFailureListener {
                callback("Dificultad Media")
            }
    }

    private fun getValorDificultad(data: Map<String, Any>?, categoria: String): String {
        val submap = data?.get(categoria) as? Map<*, *>
        val dificultad = submap?.get("Dificultad") as? String
        Log.d(
            "DificultadManager",
            "Categoría: $categoria → Dificultad leída: ${dificultad ?: "NULL"}"
        )
        return dificultad ?: "Dificultad Media"
    }

    private fun getValorPuntaje(data: Map<String, Any>?, categoria: String): Int {
        val submap = data?.get(categoria) as? Map<*, *>
        val puntaje = (submap?.get("Puntaje") as? Number)?.toInt()
        Log.d("DificultadManager", "Categoría: $categoria → Puntaje leído: ${puntaje ?: "0"}")
        return puntaje ?: 0
    }

    private fun calcularNuevaDificultad(actual: String, media: Double): String {
        return when (actual) {
            "Dificultad Baja" -> if (media > 3) "Dificultad Media" else "Dificultad Baja"
            "Dificultad Media" -> when {
                media > 3 -> "Dificultad Alta"
                media < 2 -> "Dificultad Baja"
                else -> "Dificultad Media"
            }

            "Dificultad Alta" -> if (media < 3) "Dificultad Media" else "Dificultad Alta"
            else -> "Dificultad Media"
        }
    }

    fun hanPasado7Dias(timestamp: Timestamp): Boolean {
        val fechaAnterior = timestamp.toDate().toInstant()
        val ahora = Instant.now()
        return ChronoUnit.DAYS.between(fechaAnterior, ahora) >= 7

    }
}
