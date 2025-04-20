package com.example.memora_app.juegos

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.memora_app.InformacionpersonalActivity
import com.example.memora_app.R
import com.example.memora_app.inicio_cuidador_activity
import com.example.memora_app.inicio_medico_activity
import com.example.memora_app.inicio_paciente_activity
import com.google.firebase.firestore.FirebaseFirestore

class ExplicacionOrientacionActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var textoExplicacion: TextView
    private lateinit var pinInput: EditText
    private lateinit var botonComenzar: Button
    private lateinit var dniPaciente: String
    private var dificultad: String = "Dificultad Media"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explicacion_comprension2)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        firestore = FirebaseFirestore.getInstance()
        dniPaciente = intent.getStringExtra("pacienteId") ?: run {
            Toast.makeText(this, "No se recibió el ID del paciente", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d("ExplicacionOrientacion", "Paciente recibido: $dniPaciente")

        textoExplicacion = findViewById(R.id.textoExplicacion)
        pinInput = findViewById(R.id.pinInput)
        botonComenzar = findViewById(R.id.btnComenzar)

        firestore.collection("Pacientes")
            .document(dniPaciente)
            .collection("Dificultades")
            .document("Actual")
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    Log.d("ExplicacionOrientacion", "Documento 'Actual' encontrado")
                    val campo = doc.get("Orientación") as? Map<*, *>
                    if (campo != null) {
                        dificultad = campo["Dificultad"] as? String ?: "Dificultad Media"
                        Log.d("ExplicacionOrientacion", "Dificultad obtenida: $dificultad")
                        Toast.makeText(this, "Dificultad: $dificultad", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.w("ExplicacionOrientacion", "Campo 'Orientación' no encontrado o mal formado")
                        Toast.makeText(this, "No se encontró el campo 'Orientación'", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.w("ExplicacionOrientacion", "No existe el documento 'Actual'")
                    Toast.makeText(this, "No existe dificultad actual para el paciente", Toast.LENGTH_SHORT).show()
                }
                mostrarTextoSegunDificultad(dificultad)
            }
            .addOnFailureListener { e ->
                Log.e("ExplicacionOrientacion", "Error leyendo dificultad: ${e.message}")
                Toast.makeText(this, "Error leyendo dificultad: ${e.message}", Toast.LENGTH_LONG).show()
                mostrarTextoSegunDificultad("Dificultad Media")
            }

        botonComenzar.setOnClickListener {
            val pinIngresado = pinInput.text.toString().trim()

            if (pinIngresado.length != 4) {
                Toast.makeText(this, "Introduce un PIN de 4 dígitos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            firestore.collection("Cuidadores")
                .whereEqualTo("dniPaciente", dniPaciente)
                .get()
                .addOnSuccessListener { result ->
                    if (!result.isEmpty) {
                        val cuidador = result.documents[0]
                        val pinReal = cuidador.getString("pin")
                        Log.d("ExplicacionOrientacion", "PIN en Firestore: $pinReal")

                        if (pinIngresado == pinReal) {
                            Log.i("ExplicacionOrientacion", "PIN correcto. Iniciando juego.")
                            val intent = Intent(this, JuegoOrientacionActivity::class.java)
                            intent.putExtra("pacienteId", dniPaciente)
                            intent.putExtra("dificultad", dificultad.trim())
                            startActivity(intent)
                            finish()
                        } else {
                            Log.w("ExplicacionOrientacion", "PIN incorrecto. Ingresado: $pinIngresado / Real: $pinReal")
                            Toast.makeText(this, "PIN incorrecto", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.w("ExplicacionOrientacion", "No se encontró cuidador para DNI: $dniPaciente")
                        Toast.makeText(this, "No se encontró cuidador para este paciente", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ExplicacionOrientacion", "Error al verificar PIN: ${e.message}")
                    Toast.makeText(this, "Error al verificar PIN: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun mostrarTextoSegunDificultad(dificultad: String) {
        val mensaje = when (dificultad) {
            "Dificultad Baja" -> "Se mostrará una imagen y el paciente deberá reconocer el lugar en el que se encuentra. ¡Ánimo!"
            "Dificultad Media" -> "El paciente deberá decidir si se encuentra dentro o fuera de un lugar según la imagen mostrada. ¡Presta atención!"
            "Dificultad Alta" -> "Se mostrará un plano y el paciente deberá imaginar giros o movimientos para deducir en qué sitio acabaría. ¡Vamos allá!"
            else -> "Nivel no especificado. Se cargará un nivel intermedio por defecto."
        }

        textoExplicacion.text = mensaje
        Log.d("ExplicacionOrientacion", "Texto mostrado: $mensaje")
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
