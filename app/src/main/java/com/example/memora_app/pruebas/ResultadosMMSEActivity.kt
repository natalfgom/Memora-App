package com.example.memora_app.pruebas

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.memora_app.InformacionpersonalActivity
import com.example.memora_app.R
import com.example.memora_app.inicio_cuidador_activity
import com.example.memora_app.inicio_medico_activity
import com.example.memora_app.inicio_paciente_activity
import com.example.memora_app.juegos.DificultadManager
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ResultadosMMSEActivity : AppCompatActivity() {

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
        setContentView(R.layout.activity_resultado)

        Log.d("Resultados", "Entrando en ResultadosMMSEActivity")

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)



        // Inicializar vistas


        // Recibir el ID del paciente
        pacienteID = intent.getStringExtra("paciente_id") ?: ""

        if (pacienteID.isEmpty()) {
            Toast.makeText(this, "ID de paciente no recibido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d("Resultados", "Paciente ID recibido: $pacienteID")

        // Comprobar y actualizar dificultades desde MMSE
        DificultadManager.resultadosActualizar(pacienteID)

        // Cargar resultados desde Firebase
        obtenerResultados()


    }

    private fun obtenerResultados() {
        val fechaActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        db.collection("Pacientes").document(pacienteID)
            .collection("Pruebas").document(fechaActual)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val orientacionMap = document.get("Orientación") as? Map<*, *>
                    val memoriaMap = document.get("Memoria") as? Map<*, *>
                    val calculoMap = document.get("Cálculo") as? Map<*, *>
                    val comprensionMap = document.get("Comprensión") as? Map<*, *>

                    puntuacionOrientacion = (orientacionMap?.get("Puntaje") as? Long)?.toInt() ?: 0
                    puntuacionMemoria = (memoriaMap?.get("Puntaje") as? Long)?.toInt() ?: 0
                    puntuacionCalculo = (calculoMap?.get("Puntaje") as? Long)?.toInt() ?: 0
                    puntuacionComprension = (comprensionMap?.get("Puntaje") as? Long)?.toInt() ?: 0

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            R.id.menu_principal -> {
                irAInicioSegunRol()
                true
            }
            R.id.menu_informacion_personal -> {
                startActivity(Intent(this, InformacionpersonalActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun irAInicioSegunRol() {
        val sharedPreferences = getSharedPreferences("PREFS_MEMORA", MODE_PRIVATE)
        val rol = sharedPreferences.getString("rol", "")

        val intent = when (rol) {
            "Médico" -> Intent(this, inicio_medico_activity::class.java)
            "Paciente" -> Intent(this, inicio_paciente_activity::class.java)
            "Cuidador" -> Intent(this, inicio_cuidador_activity::class.java)
            else -> {
                Toast.makeText(this, "Rol desconocido", Toast.LENGTH_SHORT).show()
                return
            }
        }

        startActivity(intent)
    }
}
