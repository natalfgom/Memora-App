package com.example.memora_app.pruebas

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.memora_app.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.*

class CDRSBActivity : AppCompatActivity() {

    private lateinit var layout: LinearLayout
    private lateinit var btnFinalizar: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var cuidadorID: String = ""

    private val secciones = listOf("Memoria", "Orientación", "Juicio", "Actividades", "Cuidado")
    private val seccionPreguntas = mutableMapOf<String, MutableList<Spinner>>()
    private val respuestasMap = mutableMapOf<String, Map<String, Any>>()  // Para guardar respuestas

    private val puntuacionesPorPregunta: Map<Int, List<Double>> = mapOf(
        1 to listOf(0.0, 0.0, 1.0),
        2 to listOf(0.0, 0.0, 1.0, 2.0),
        3 to listOf(0.0, 3.0, 2.0, 1.0),
        4 to listOf(0.0, 2.0, 1.0, 0.5),
        5 to listOf(0.0, 0.0, 1.0, 3.0, 2.0),
        6 to listOf(0.0, 0.0, 1.0, 3.0, 2.0),
        7 to listOf(0.0, 0.0, 2.0),
        8 to listOf(0.0, 0.0, 1.0, 4.0, 2.0),
        9 to listOf(0.0, 0.0, 0.5, 1.0, 2.0, 4.0),
        10 to listOf(0.0, 0.0, 1.0, 2.0),
        11 to listOf(0.0, 0.0, 1.0, 2.0),
        12 to listOf(0.0, 0.0, 1.0, 0.5),
        13 to listOf(0.0, 0.0, 1.0, 3.0, 2.0),
        14 to listOf(0.0, 0.0, 1.0, 4.0, 3.0),
        15 to listOf(0.0, 0.0, 1.0, 0.0),
        16 to listOf(0.0, 0.0, 2.0, 1.0),
        17 to listOf(0.0, 0.0, 1.0, 3.0),
        18 to listOf(0.0, 0.0, 1.0, 2.0),
        19 to listOf(0.0, 0.0, 1.0),
        20 to listOf(0.0, 1.0, 0.0, 2.0, 0.0),
        21 to listOf(0.0, 0.0, 1.0, 3.0),
        22 to listOf(0.0, 0.0, 2.0, 3.0),
        23 to listOf(0.0, 0.0, 2.0, 4.0),
        24 to listOf(0.0, 0.0, 1.5, 3.0)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cdrsb_test)

        layout = findViewById(R.id.layoutPreguntas)
        btnFinalizar = findViewById(R.id.btnFinalizar)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        obtenerCuidadorID()
        inicializarPreguntas()

        btnFinalizar.setOnClickListener {
            if (!respuestasValidas()) {
                Toast.makeText(this, "Responde todas las preguntas", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (cuidadorID.isNotEmpty()) {
                val resultados = calcularResultados()

                guardarResultados(resultados) {
                    guardarRespuestas()
                    // Aquí lanzas la activity de resultados
                    val intent = Intent(this, ResultadosCDRActivity::class.java)
                    startActivity(intent)
                    finish()
                }

            } else {
                Toast.makeText(this, "Cuidador no identificado", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun obtenerCuidadorID() {
        val correoUsuario = auth.currentUser?.email ?: return
        db.collection("Cuidadores")
            .whereEqualTo("correo", correoUsuario)
            .get()
            .addOnSuccessListener { documentos ->
                if (!documentos.isEmpty) {
                    cuidadorID = documentos.documents.first().id
                } else {
                    Toast.makeText(this, "Cuidador no encontrado", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error de conexión", Toast.LENGTH_LONG).show()
                finish()
            }
    }

    private fun inicializarPreguntas() {
        for (i in 1..24) {
            val seccion = when (i) {
                in 1..4 -> "Memoria"
                in 5..8 -> "Orientación"
                in 9..14 -> "Juicio"
                in 15..20 -> "Actividades"
                else -> "Cuidado"
            }

            val textoPregunta = getString(resources.getIdentifier("cdrsb_q$i", "string", packageName))
            val opciones = resources.getStringArray(resources.getIdentifier("cdrsb_q${i}_options", "array", packageName))

            val textView = TextView(this).apply {
                text = textoPregunta
                textSize = 18f
                setPadding(0, 20, 0, 8)
            }

            val spinner = Spinner(this).apply {
                adapter = ArrayAdapter(this@CDRSBActivity, android.R.layout.simple_spinner_dropdown_item, opciones)
            }

            layout.addView(textView)
            layout.addView(spinner)

            seccionPreguntas.getOrPut(seccion) { mutableListOf() }.add(spinner)
        }
    }

    private fun respuestasValidas(): Boolean {
        return seccionPreguntas.values.flatten().none { it.selectedItemPosition == 0 }
    }

    private fun calcularResultados(): Map<String, Any> {
        val resultados = mutableMapOf<String, Any>()
        var totalScore = 0.0
        var preguntaIndex = 1

        for (seccion in secciones) {
            val spinners = seccionPreguntas[seccion] ?: continue
            var maxPuntaje = 0.0

            for (spinner in spinners) {
                val seleccion = spinner.selectedItemPosition
                val textoPregunta = getString(resources.getIdentifier("cdrsb_q$preguntaIndex", "string", packageName))
                val opciones = resources.getStringArray(resources.getIdentifier("cdrsb_q${preguntaIndex}_options", "array", packageName))
                val respuesta = opciones[seleccion]

                val puntos = puntuacionesPorPregunta[preguntaIndex]?.getOrNull(seleccion) ?: 0.0
                if (puntos > maxPuntaje) maxPuntaje = puntos

                respuestasMap["Pregunta $preguntaIndex"] = mapOf(
                    "Pregunta" to textoPregunta,
                    "Respuesta" to respuesta,
                    "Puntos" to puntos
                )

                preguntaIndex++
            }

            totalScore += maxPuntaje
            resultados[seccion] = mapOf("Puntuación Máxima" to maxPuntaje)
        }

        val severidad = when {
            totalScore <= 1.5 -> "Función Cognitiva Normal"
            totalScore <= 4.0 -> "Deterioro Cognitivo Leve"
            totalScore <= 9.0 -> "Demencia Leve"
            totalScore <= 15.5 -> "Demencia Moderada"
            else -> "Demencia Severa"
        }

        resultados["Total"] = totalScore
        resultados["Clasificación"] = severidad

        return resultados
    }

    private fun guardarResultados(resultados: Map<String, Any>, onSuccess: () -> Unit) {
        val fechaActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val ref = db.collection("Cuidadores").document(cuidadorID)
            .collection("PruebasCDR").document("CDR-$fechaActual")

        ref.set(resultados, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Puntuación guardada", Toast.LENGTH_SHORT).show()
                onSuccess()  // Llama al callback
            }
            .addOnFailureListener { e ->
                Log.e("CDRSBActivity", "Error al guardar puntuación", e)
            }
    }


    private fun guardarRespuestas() {
        val fechaActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val ref = db.collection("Cuidadores").document(cuidadorID)
            .collection("PruebasCDR").document("Respuestas-$fechaActual")

        ref.set(respuestasMap, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Respuestas guardadas", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("CDRSBActivity", "Error al guardar respuestas", e)
            }
    }
}
