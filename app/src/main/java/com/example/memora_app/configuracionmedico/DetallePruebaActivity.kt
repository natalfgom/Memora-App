package com.example.memora_app.configuracionmedico

import android.content.Intent
import android.os.Bundle
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
import com.google.firebase.firestore.FirebaseFirestore

class DetallePruebaActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resultado)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        // Opcional: Mostrar botón de volver atrás (si lo necesitas)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val pacienteId = intent.getStringExtra("PACIENTE_ID") ?: return
        val pruebaId = intent.getStringExtra("PRUEBA_ID") ?: return

        val tvPuntajeOrientacion: TextView = findViewById(R.id.tvPuntajeOrientacion)
        val tvPuntajeMemoria: TextView = findViewById(R.id.tvPuntajeMemoria)
        val tvPuntajeCalculo: TextView = findViewById(R.id.tvPuntajeCalculo)
        val tvPuntajeComprension: TextView = findViewById(R.id.tvPuntajeComprension)

        // Obtener los detalles de la prueba
        val pruebaRef = db.collection("Pacientes").document(pacienteId)
            .collection("Pruebas").document(pruebaId)

        pruebaRef.get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val puntajeOrientacion = doc.getLong("Orientación.Puntaje") ?: 0
                val dificultadOrientacion = doc.getString("Orientación.Dificultad") ?: "Desconocido"

                val puntajeMemoria = doc.getLong("Memoria.Puntaje") ?: 0
                val dificultadMemoria = doc.getString("Memoria.Dificultad") ?: "Desconocido"

                val puntajeCalculo = doc.getLong("Cálculo.Puntaje") ?: 0
                val dificultadCalculo = doc.getString("Cálculo.Dificultad") ?: "Desconocido"

                val puntajeComprension = doc.getLong("Comprensión.Puntaje") ?: 0
                val dificultadComprension = doc.getString("Comprensión.Dificultad") ?: "Desconocido"

                tvPuntajeOrientacion.text = "Orientación: $puntajeOrientacion ($dificultadOrientacion)"
                tvPuntajeMemoria.text = "Memoria: $puntajeMemoria ($dificultadMemoria)"
                tvPuntajeCalculo.text = "Cálculo: $puntajeCalculo ($dificultadCalculo)"
                tvPuntajeComprension.text = "Comprensión: $puntajeComprension ($dificultadComprension)"
            } else {
                tvPuntajeOrientacion.text = "Prueba no encontrada"
                tvPuntajeMemoria.text = ""
                tvPuntajeCalculo.text = ""
                tvPuntajeComprension.text = ""
            }
        }


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
