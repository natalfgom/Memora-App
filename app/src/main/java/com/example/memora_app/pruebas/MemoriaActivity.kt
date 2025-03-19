package com.example.memora_app.pruebas

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.memora_app.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.*

class MemoriaActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var gridLayout: GridLayout
    private lateinit var btnValidar: Button
    private lateinit var tvInstrucciones: TextView

    private lateinit var pacienteID: String
    private val db = FirebaseFirestore.getInstance()
    private val handler = Handler(Looper.getMainLooper())

    private val imagenes = listOf(
        R.drawable.calamar, R.drawable.cangrejo, R.drawable.elefante, R.drawable.gato,
        R.drawable.mariposa, R.drawable.pinguino, R.drawable.serpiente, R.drawable.gorila
    )

    private var imagenesMostradas = mutableListOf<Int>()
    private var seleccionadas = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memoria)

        imageView = findViewById(R.id.imageView)
        gridLayout = findViewById(R.id.gridLayout)
        btnValidar = findViewById(R.id.btnValidar)
        tvInstrucciones = findViewById(R.id.tvInstrucciones)

        pacienteID = intent.getStringExtra("paciente_id") ?: ""

        if (pacienteID.isEmpty()) {
            Toast.makeText(this, "Error: ID del paciente no encontrado", Toast.LENGTH_LONG).show()
            Log.e("MemoriaActivity", "Paciente ID no encontrado")
            finish()
        }

        iniciarJuego()
    }

    private fun iniciarJuego() {
        imagenesMostradas = imagenes.shuffled().take(3).toMutableList()
        Log.d("MemoriaActivity", "Imágenes seleccionadas: $imagenesMostradas")

        tvInstrucciones.text = "Observe las imágenes"
        imageView.visibility = View.VISIBLE
        gridLayout.visibility = View.GONE
        btnValidar.visibility = View.GONE

        mostrarImagenesSecuencialmente(0)
    }

    private fun mostrarImagenesSecuencialmente(index: Int) {
        if (index < imagenesMostradas.size) {
            runOnUiThread {
                imageView.setImageResource(imagenesMostradas[index])
                Log.d("MemoriaActivity", "Mostrando imagen: ${imagenesMostradas[index]}")
            }

            handler.postDelayed({
                mostrarImagenesSecuencialmente(index + 1)
            }, 3000) // Mostrar cada imagen por 3 segundos
        } else {
            runOnUiThread { iniciarSeleccion() }
        }
    }

    private fun iniciarSeleccion() {
        tvInstrucciones.text = getString(R.string.seleccione_imagenes) // Texto desde resources
        imageView.visibility = View.GONE
        gridLayout.visibility = View.VISIBLE
        btnValidar.visibility = View.VISIBLE

        gridLayout.removeAllViews()
        gridLayout.columnCount = 3  // Mantener distribución en 3 columnas

        val opciones = imagenes.shuffled() // Se muestran **todas** las imágenes en cada intento

        Handler(Looper.getMainLooper()).postDelayed({
            opciones.forEach { opcion ->
                val imageView = ImageView(this).apply {
                    setImageResource(opcion)
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = 300
                        height = 300
                        setMargins(20, 20, 20, 20) // Espaciado uniforme
                    }
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    adjustViewBounds = true
                    setOnClickListener { seleccionarImagen(this, opcion) }
                }
                gridLayout.addView(imageView)
            }
        }, 200)

        btnValidar.setOnClickListener {
            if (seleccionadas.size < 3) {
                Toast.makeText(this, getString(R.string.debes_seleccionar_imagenes), Toast.LENGTH_SHORT).show()
            } else {
                validarRespuestas()
            }
        }
    }




    private fun seleccionarImagen(imageView: ImageView, imagen: Int) {
        if (seleccionadas.contains(imagen)) {
            seleccionadas.remove(imagen)
            imageView.alpha = 1.0f
        } else if (seleccionadas.size < 3) {
            seleccionadas.add(imagen)
            imageView.alpha = 0.5f
        }
    }

    private fun validarRespuestas() {
        val aciertos = seleccionadas.count { imagenesMostradas.contains(it) }
        Log.d("MemoriaActivity", "Número de aciertos: $aciertos")

        guardarResultadosEnFirebase(aciertos)

        val intent = Intent(this, ExplicacionComprensionActivity::class.java)
        intent.putExtra("paciente_id", pacienteID)
        startActivity(intent)
        finish()
    }

    private fun guardarResultadosEnFirebase(memoria: Int) {
        if (pacienteID.isEmpty()) {
            Log.e("Firebase", "No se puede guardar en Firebase: ID de paciente vacío.")
            return
        }

        // Obtener la fecha actual en formato "yyyy-MM-dd"
        val fechaActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val resultado = hashMapOf(
            "Memoria" to mapOf(
                "Puntaje" to memoria,
                "Dificultad" to clasificarDificultad(memoria)
            )
        )

        db.collection("Pacientes").document(pacienteID)
            .collection("Pruebas").document(fechaActual) // Guardar los resultados con la fecha actual
            .set(resultado, SetOptions.merge()) // Evita sobrescribir otros datos previos
            .addOnSuccessListener {
                Log.d("Firebase", "Resultado de memoria guardado con éxito en Firebase para la fecha $fechaActual")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error al guardar resultado de memoria en Firebase", e)
            }
    }

    private fun clasificarDificultad(puntaje: Int): String {
        return when {
            puntaje == 3 -> "Dificultad Alta (Normal)"
            puntaje in 1..2 -> "Dificultad Media (Leve Deterioro)"
            else -> "Dificultad Baja (Deterioro Severo)"
        }
    }
}
