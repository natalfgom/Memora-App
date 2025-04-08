package com.example.memora_app.pruebas

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.example.memora_app.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.*

class ComprensionActivity : AppCompatActivity() {
    private lateinit var tvPalabra: TextView
    private lateinit var gridLayout: GridLayout
    private lateinit var btnSiguiente: Button
    private lateinit var contenedorGrid: FrameLayout

    private val imagenes = listOf(
        Pair("Lavadora", R.drawable.lavadora),
        Pair("Cocina", R.drawable.cocina),
        Pair("Fuente", R.drawable.fuente),
        Pair("Maceta", R.drawable.maceta),
        Pair("Ventana", R.drawable.ventana),
        Pair("Bañera", R.drawable.banera),
        Pair("Escalera", R.drawable.escalera),
        Pair("Casa", R.drawable.casaa)
    )

    private val db = FirebaseFirestore.getInstance()
    private var pacienteID: String = ""
    private var palabrasMostradas = mutableListOf<Pair<String, Int>>()
    private var intentos = 0
    private var correctos = 0
    private var palabraActual: Pair<String, Int>? = null
    private var seleccionRealizada = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comprension)

        tvPalabra = findViewById(R.id.tvPalabra)
        gridLayout = findViewById(R.id.gridLayout)
        btnSiguiente = findViewById(R.id.btnSiguiente)
        contenedorGrid = findViewById(R.id.contenedorGrid)

        // Recibir el ID del paciente
        pacienteID = intent.getStringExtra("paciente_id") ?: ""

        if (pacienteID.isEmpty()) {
            Toast.makeText(this, "Error: ID del paciente no encontrado", Toast.LENGTH_LONG).show()
            Log.e("ComprensionActivity", "Paciente ID no encontrado")
            finish()
            return
        }

        btnSiguiente.setOnClickListener {
            if (seleccionRealizada) {
                iniciarPrueba()
            } else {
                Toast.makeText(this, "Debes seleccionar una imagen", Toast.LENGTH_SHORT).show()
            }
        }

        iniciarPrueba()
    }

    private fun iniciarPrueba() {
        if (intentos < 5) { // Se realizarán 5 rondas
            intentos++
            seleccionRealizada = false
            btnSiguiente.visibility = View.GONE

            val palabrasRestantes = imagenes.filter { !palabrasMostradas.contains(it) }
            if (palabrasRestantes.isEmpty()) {
                finalizarPrueba()
                return
            }

            palabraActual = palabrasRestantes.shuffled().first()
            palabrasMostradas.add(palabraActual!!)
            tvPalabra.text = palabraActual!!.first

            mostrarOpciones()
        } else {
            finalizarPrueba()
        }
    }

    private fun mostrarOpciones() {
        gridLayout.removeAllViews()
        gridLayout.visibility = View.VISIBLE
        gridLayout.columnCount = 2

        contenedorGrid.visibility = View.VISIBLE

        val opciones = imagenes.shuffled() // Se muestran todas las imágenes en cada intento

        Handler(Looper.getMainLooper()).postDelayed({
            opciones.forEach { opcion ->
                val imageView = ImageView(this).apply {
                    setImageResource(opcion.second)
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = 300
                        height = 300
                        setMargins(50, 20, 50, 20)
                    }
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    adjustViewBounds = true
                    setOnClickListener { verificarRespuesta(opcion, this) }
                }
                gridLayout.addView(imageView)
            }
        }, 200)
    }

    private fun verificarRespuesta(seleccionada: Pair<String, Int>, imageView: ImageView) {
        seleccionRealizada = true
        btnSiguiente.visibility = View.VISIBLE

        gridLayout.children.forEach {
            it.alpha = 0.5f
            it.isEnabled = false
        }
        imageView.alpha = 1.0f

        if (seleccionada == palabraActual) {
            correctos++
        }
    }

    private fun finalizarPrueba() {
        guardarResultadosEnFirebase {
            val intent = Intent(this, ResultadosMMSEActivity::class.java)
            intent.putExtra("comprension_resultado", correctos)
            intent.putExtra("paciente_id", pacienteID)
            startActivity(intent)

        }
    }

    private fun guardarResultadosEnFirebase(onSuccess: () -> Unit) {
        if (pacienteID.isEmpty()) {
            Log.e("Firebase", "No se puede guardar en Firebase: ID de paciente vacío.")
            return
        }

        val fechaActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val resultado = hashMapOf(
            "Comprensión" to mapOf(
                "Puntaje" to correctos,
                "Dificultad" to clasificarDificultad(correctos)
            )
        )

        db.collection("Pacientes").document(pacienteID)
            .collection("Pruebas").document(fechaActual)
            .set(resultado, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("Firebase", "Resultado de comprensión guardado con éxito en Firebase para la fecha $fechaActual")
                onSuccess() // Llamar a la función cuando el guardado sea exitoso
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error al guardar resultado de comprensión en Firebase", e)
            }
    }

    private fun clasificarDificultad(puntaje: Int): String {
        return when {
            puntaje >= 4 -> "Dificultad Alta"
            puntaje in 2..3 -> "Dificultad Media"
            else -> "Dificultad Baja"
        }
    }
}
