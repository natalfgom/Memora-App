package com.example.memora_app.pruebas

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.memora_app.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import com.google.firebase.firestore.SetOptions

import java.util.*

class mmseRegistro : AppCompatActivity() {
    private lateinit var spnCountry: Spinner
    private lateinit var spnFecha: Spinner
    private lateinit var spnCalculo: Spinner
    private lateinit var btnMemoria: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var pacienteID: String = ""

    private var fechaCorrecta: String = ""
    private val paisCorrecto = "España"
    private val numerosCorrectos = listOf("93", "86", "79", "72", "65")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mmse_test)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        spnCountry = findViewById(R.id.spnCountry)
        spnFecha = findViewById(R.id.spnFecha)
        spnCalculo = findViewById(R.id.spnCalculo)
        btnMemoria = findViewById(R.id.btnMemoria)

        fechaCorrecta = obtenerFechaActual()
        configurarFechas()

        obtenerPacienteID()

        btnMemoria.setOnClickListener {
            if (validarSeleccion()) {
                evaluarRespuestas()
            } else {
                Toast.makeText(this, "Debes seleccionar todas las respuestas antes de continuar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun configurarFechas() {
        val fechasBase = listOf("10/04/2023", "15/06/2024", fechaCorrecta, "20/12/2022").shuffled()
        val fechas = listOf("Seleccione una fecha") + fechasBase

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, fechas)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnFecha.adapter = adapter
    }

    private fun obtenerFechaActual(): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    }

    private fun obtenerPacienteID() {
        val correoUsuario = auth.currentUser?.email

        if (correoUsuario.isNullOrEmpty()) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_LONG).show()
            Log.e("MMSEActivity", "Usuario no autenticado. Cerrando actividad.")
            finish()
            return
        }

        db.collection("Pacientes")
            .whereEqualTo("correo", correoUsuario)
            .get()
            .addOnSuccessListener { documentos ->
                if (!documentos.isEmpty) {
                    pacienteID = documentos.documents.first().id
                    Log.d("MMSEActivity", "Paciente ID obtenido: $pacienteID")

                    // Guardamos el ID en SharedPreferences
                    val prefs = getSharedPreferences("PREFS_MEMORA", MODE_PRIVATE)
                    prefs.edit().putString("pacienteID", pacienteID).apply()
                } else {
                    Toast.makeText(this, "Error: No se encontró el paciente en la base de datos", Toast.LENGTH_LONG).show()
                    Log.e("MMSEActivity", "No se encontró el paciente con correo: $correoUsuario")
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al obtener datos del paciente", Toast.LENGTH_LONG).show()
                Log.e("MMSEActivity", "Error al obtener paciente: ", e)
                finish()
            }
    }

    private fun validarSeleccion(): Boolean {
        val paisSeleccionado = spnCountry.selectedItem.toString()
        val fechaSeleccionada = spnFecha.selectedItem.toString()
        val resultadoCalculo = spnCalculo.selectedItem.toString()

        return !(paisSeleccionado == "Seleccione un país" ||
                fechaSeleccionada == "Seleccione una fecha" ||
                resultadoCalculo == "Seleccione la opción correcta")
    }

    private fun evaluarRespuestas() {
        var puntuacionOrientacion = 0
        var puntuacionCalculo = 0

        val paisSeleccionado = spnCountry.selectedItem.toString()
        if (paisSeleccionado == paisCorrecto) puntuacionOrientacion += 2

        val fechaSeleccionada = spnFecha.selectedItem.toString()
        val fechaCorrectaSplit = fechaCorrecta.split("/")
        val fechaSeleccionadaSplit = fechaSeleccionada.split("/")

        if (fechaSeleccionada == fechaCorrecta) {
            puntuacionOrientacion += 3
        } else if (fechaSeleccionadaSplit.size == 3 && fechaSeleccionadaSplit[2] == fechaCorrectaSplit[2]) {
            puntuacionOrientacion += 1
        }

        val respuestaCalculo = spnCalculo.selectedItem.toString().split(",").map { it.trim() }
        if (respuestaCalculo == numerosCorrectos) puntuacionCalculo += 5

        guardarResultadosEnFirebase(puntuacionOrientacion, puntuacionCalculo)

        val intent = Intent(this, ExplicacionMemoriaRgActivity::class.java)
        intent.putExtra("paciente_id", pacienteID)
        startActivity(intent)
        finish()
    }

    private fun guardarResultadosEnFirebase(orientacion: Int, calculo: Int) {
        if (pacienteID.isEmpty()) {
            Log.e("Firebase", "No se puede guardar en Firebase: ID de paciente vacío.")
            return
        }

        val fechaActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val resultado = hashMapOf(
            "Fecha" to fechaActual,
            "Orientación" to mapOf(
                "Puntaje" to orientacion,
                "Dificultad" to clasificarDificultad("Orientación", orientacion)
            ),
            "Cálculo" to mapOf(
                "Puntaje" to calculo,
                "Dificultad" to clasificarDificultad("Cálculo", calculo)
            )
        )

        db.collection("Pacientes").document(pacienteID)
            .collection("Pruebas").document("Prueba inicial " + fechaActual) // Se usa la fecha como identificador
            .set(resultado, SetOptions.merge()) // Evita sobrescribir otros datos previos
            .addOnSuccessListener {
                Log.d("Firebase", "Resultados guardados correctamente en Firebase con fecha $fechaActual")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error al guardar resultados en Firebase", e)
            }
    }

    /**
     * Asigna un nivel de dificultad basado en el puntaje obtenido.
     */
    private fun clasificarDificultad(area: String, puntaje: Int): String {
        return when (area) {
            "Orientación" -> when {
                puntaje == 5 -> "Dificultad Alta"
                puntaje in 2..3 -> "Dificultad Media"
                else -> "Dificultad Baja"
            }
            "Cálculo" -> when {
                puntaje in 4..5 -> "Dificultad Alta"

                else -> "Dificultad Baja"
            }
            else -> "No clasificado"
        }
    }
}
