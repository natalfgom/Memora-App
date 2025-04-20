package com.example.memora_app.juegos

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.memora_app.InformacionpersonalActivity
import com.example.memora_app.R
import com.example.memora_app.inicio_cuidador_activity
import com.example.memora_app.inicio_medico_activity
import com.example.memora_app.inicio_paciente_activity
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate

class PantallaPreparacionJuegoActivity : AppCompatActivity() {

    private lateinit var pacienteId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_preparacion_juego)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        pacienteId = intent.getStringExtra("pacienteId") ?: return

        // Preparar dificultades antes de jugar
        DificultadManager.actualizarDificultades(pacienteId)

        val btnComprension = findViewById<LinearLayout>(R.id.btnComprension)
        val btnCalculo = findViewById<LinearLayout>(R.id.btnCalculo)
        val btnMemoria = findViewById<LinearLayout>(R.id.btnMemoria)
        val btnOrientacion = findViewById<LinearLayout>(R.id.btnOrientacion)

        btnComprension.setOnClickListener {
            verificarSiHaJugadoHoy(pacienteId, "Comprensión") { yaJugo ->
                if (yaJugo) {
                    Toast.makeText(this, "Ya has jugado Comprensión hoy. Vuelve mañana.", Toast.LENGTH_LONG).show()
                } else {
                    val intent = Intent(this, ExplicacionComprensionActivity::class.java)
                    intent.putExtra("pacienteId", pacienteId)
                    intent.putExtra("categoria", "Comprensión")
                    startActivity(intent)
                }
            }
        }

        btnCalculo.setOnClickListener {
            verificarSiHaJugadoHoy(pacienteId, "Cálculo") { yaJugo ->
                if (yaJugo) {
                    Toast.makeText(this, "Ya has jugado Cálculo hoy. Vuelve mañana.", Toast.LENGTH_LONG).show()
                } else {
                    val intent = Intent(this, ExplicacionCalculoActivity::class.java)
                    intent.putExtra("pacienteId", pacienteId)
                    intent.putExtra("categoria", "Cálculo")
                    startActivity(intent)
                }
            }
        }

        btnMemoria.setOnClickListener {
            verificarSiHaJugadoHoy(pacienteId, "Memoria") { yaJugo ->
                if (yaJugo) {
                    Toast.makeText(this, "Ya has jugado Memoria hoy. Vuelve mañana.", Toast.LENGTH_LONG).show()
                } else {
                    val intent = Intent(this, ExplicacionMemoriaJuegoActivity::class.java)
                    intent.putExtra("pacienteId", pacienteId)
                    intent.putExtra("categoria", "Memoria")
                    startActivity(intent)
                }
            }
        }

        btnOrientacion.setOnClickListener {
            verificarSiHaJugadoHoy(pacienteId, "Orientación") { yaJugo ->
                if (yaJugo) {
                    Toast.makeText(this, "Ya has jugado Memoria hoy. Vuelve mañana.", Toast.LENGTH_LONG).show()
                } else {
                    val intent = Intent(this, ExplicacionOrientacionActivity::class.java)
                    intent.putExtra("pacienteId", pacienteId)
                    intent.putExtra("categoria", "Orientación")
                    startActivity(intent)
                }
            }
        }
    }

    private fun verificarSiHaJugadoHoy(pacienteId: String, categoria: String, callback: (Boolean) -> Unit) {
        val fechaHoy = LocalDate.now().toString()
        val db = FirebaseFirestore.getInstance()

        db.collection("Pacientes")
            .document(pacienteId)
            .collection("Juegos")
            .document(categoria)
            .collection("Fechas")
            .document(fechaHoy)
            .get()
            .addOnSuccessListener { document ->
                // Si no existe, puede ser que sea la primera vez que juega -> puede jugar
                callback(document.exists())
            }
            .addOnFailureListener { e ->
                // En caso de error inesperado, mejor permitir jugar
                Toast.makeText(this, "No se pudo comprobar si ya jugó hoy. Se permitirá el acceso.", Toast.LENGTH_SHORT).show()
                callback(false)
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
