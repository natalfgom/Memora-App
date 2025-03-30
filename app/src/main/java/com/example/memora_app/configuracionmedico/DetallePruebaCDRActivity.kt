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

class DetallePruebaCDRActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var btnVerRespuestas: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_cdr)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Datos del intent
        val fecha = intent.getStringExtra("FECHA_PRUEBA") ?: return
        val dniPaciente = intent.getStringExtra("DNI_PACIENTE") ?: return
        val cuidadorId = intent.getStringExtra("CUIDADOR_ID") ?: return
        val docId = "CDR-$fecha"
        val docRespuestasId = "Respuestas-$fecha"

        // Referencias UI
        val tvClasificacion = findViewById<TextView>(R.id.tvResultadoCDR)
        val tvMemoria = findViewById<TextView>(R.id.tvResultadoMemoria)
        val tvOrientacion = findViewById<TextView>(R.id.tvResultadoOrientacion)
        val tvJuicio = findViewById<TextView>(R.id.tvResultadoJuicio)
        val tvActividades = findViewById<TextView>(R.id.tvResultadoActividades)
        val tvCuidado = findViewById<TextView>(R.id.tvResultadoCuidado)

        btnVerRespuestas = findViewById(R.id.btnVerRespuestas)


        // Cargar resumen
        db.collection("Cuidadores").document(cuidadorId)
            .collection("PruebasCDR").document(docId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val clasificacion = doc.getString("Clasificación") ?: "-"
                    val memoria = (doc["Memoria"] as? Map<*, *>)?.get("Puntuación Máxima") ?: "-"
                    val orientacion = (doc["Orientación"] as? Map<*, *>)?.get("Puntuación Máxima") ?: "-"
                    val juicio = (doc["Juicio"] as? Map<*, *>)?.get("Puntuación Máxima") ?: "-"
                    val actividades = (doc["Actividades"] as? Map<*, *>)?.get("Puntuación Máxima") ?: "-"
                    val cuidado = (doc["Cuidado"] as? Map<*, *>)?.get("Puntuación Máxima") ?: "-"

                    tvClasificacion.text = "Clasificación: $clasificacion"
                    tvMemoria.text = "Memoria: $memoria"
                    tvOrientacion.text = "Orientación: $orientacion"
                    tvJuicio.text = "Juicio: $juicio"
                    tvActividades.text = "Actividades: $actividades"
                    tvCuidado.text = "Cuidado: $cuidado"
                } else {
                    Toast.makeText(this, "Prueba no encontrada", Toast.LENGTH_SHORT).show()
                }
            }

        // Verificar si existe documento de respuestas
        db.collection("Cuidadores").document(cuidadorId)
            .collection("PruebasCDR").document(docRespuestasId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    btnVerRespuestas.setOnClickListener {
                        val intent = Intent(this, DetalleRespuestasCDRActivity::class.java)
                        intent.putExtra("CUIDADOR_ID", cuidadorId)
                        intent.putExtra("FECHA_PRUEBA", fecha)
                        intent.putExtra("DNI_PACIENTE", dniPaciente)
                        startActivity(intent)
                    }
                } else {
                    btnVerRespuestas.visibility = Button.GONE
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
        val rol = getSharedPreferences("PREFS_MEMORA", MODE_PRIVATE).getString("rol", "")
        val intent = when (rol) {
            "Médico" -> Intent(this, inicio_medico_activity::class.java)
            "Paciente" -> Intent(this, inicio_paciente_activity::class.java)
            "Cuidador" -> Intent(this, inicio_cuidador_activity::class.java)
            else -> return
        }
        startActivity(intent)
    }
}
