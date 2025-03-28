package com.example.memora_app.pruebas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.memora_app.InformacionpersonalActivity
import com.example.memora_app.R
import com.example.memora_app.inicio_paciente_activity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ResultadosMMSERgActivity : AppCompatActivity() {
    private lateinit var btnVolver: Button
    private val db = FirebaseFirestore.getInstance()

    private var puntuacionOrientacion = 0
    private var puntuacionMemoria = 0
    private var puntuacionCalculo = 0
    private var puntuacionComprension = 0
    private var puntuacionTotal = 0
    private var clasificacionFinal = ""
    private var pacienteID: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resultados_mmse)

        btnVolver = findViewById(R.id.btnVolver)

        // Recibir el ID del paciente
        pacienteID = intent.getStringExtra("paciente_id") ?: ""

        obtenerResultados()
        btnVolver.setOnClickListener {
            startActivity(Intent(this, inicio_paciente_activity::class.java))
            true
        }
    }

    private fun obtenerResultados() {
        if (pacienteID.isEmpty()) {
            Toast.makeText(this, "Error: ID de paciente no encontrado", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener la fecha actual
        val fechaActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        db.collection("Pacientes").document(pacienteID)
            .collection("Pruebas").document("Prueba inicial "+fechaActual)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    puntuacionOrientacion = document.get("Orientación.Puntaje")?.toString()?.toIntOrNull() ?: 0
                    puntuacionMemoria = document.get("Memoria.Puntaje")?.toString()?.toIntOrNull() ?: 0
                    puntuacionCalculo = document.get("Cálculo.Puntaje")?.toString()?.toIntOrNull() ?: 0
                    puntuacionComprension = document.get("Comprensión.Puntaje")?.toString()?.toIntOrNull() ?: 0



                    actualizarInterfaz(document)
                } else {
                    Toast.makeText(this, "No hay resultados para hoy", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al obtener los resultados", Toast.LENGTH_SHORT).show()
            }
    }

    private fun actualizarInterfaz(document: DocumentSnapshot) {
        findViewById<TextView>(R.id.tvPuntajeOrientacion).text =
            "Orientación: ${document.getLong("Orientación.Puntaje") ?: 0} - ${document.getString("Orientación.Dificultad") ?: "No evaluado"}"

        findViewById<TextView>(R.id.tvPuntajeMemoria).text =
            "Memoria: ${document.getLong("Memoria.Puntaje") ?: 0} - ${document.getString("Memoria.Dificultad") ?: "No evaluado"}"

        findViewById<TextView>(R.id.tvPuntajeCalculo).text =
            "Cálculo: ${document.getLong("Cálculo.Puntaje") ?: 0} - ${document.getString("Cálculo.Dificultad") ?: "No evaluado"}"

        findViewById<TextView>(R.id.tvPuntajeComprension).text =
            "Comprensión: ${document.getLong("Comprensión.Puntaje") ?: 0} - ${document.getString("Comprensión.Dificultad") ?: "No evaluado"}"


    }



}
