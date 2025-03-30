package com.example.memora_app.configuracionmedico

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.memora_app.R
import com.example.memora_app.inicio_cuidador_activity
import com.example.memora_app.inicio_medico_activity
import com.example.memora_app.inicio_paciente_activity
import com.google.firebase.firestore.FirebaseFirestore

class DetalleRespuestasCDRActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var layoutRespuestas: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_respuestas_cdr)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        layoutRespuestas = findViewById(R.id.layoutRespuestas)

        val cuidadorId = intent.getStringExtra("CUIDADOR_ID") ?: return
        val fecha = intent.getStringExtra("FECHA_PRUEBA") ?: return
        val docId = "Respuestas-$fecha"

        // 🔍 Leer respuestas directamente
        db.collection("Cuidadores").document(cuidadorId)
            .collection("PruebasCDR").document(docId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val respuestas = doc.data ?: return@addOnSuccessListener
                    for (clave in respuestas.keys.sortedBy { it.removePrefix("Pregunta ").toIntOrNull() ?: 0 }) {
                        val datos = respuestas[clave] as? Map<*, *> ?: continue
                        val pregunta = datos["Pregunta"]?.toString() ?: "-"
                        val respuesta = datos["Respuesta"]?.toString() ?: "-"
                        val puntos = datos["Puntos"]?.toString() ?: "-"

                        val item = TextView(this).apply {
                            text = "• $pregunta\nRespuesta: $respuesta\nPuntos: $puntos"
                            setPadding(24, 24, 24, 24)
                            textSize = 16f
                            setTextColor(resources.getColor(R.color.black))
                            background = resources.getDrawable(R.drawable.bordered_edittext_celeste, null)
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(0, 0, 0, 24)
                            }
                        }

                        layoutRespuestas.addView(item)
                    }
                } else {
                    Toast.makeText(this, "Respuestas no encontradas", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar respuestas", Toast.LENGTH_SHORT).show()
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
