package com.example.memora_app.juegos

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.memora_app.R
import com.example.memora_app.modelos.Pregunta
import com.example.memora_app.modelos.PreguntasData
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate

class JuegoComprensionActivity : AppCompatActivity() {

    private lateinit var preguntas: List<Pregunta>
    private var indice = 0
    private var aciertos = 0
    private lateinit var pacienteId: String
    private val categoria = "Comprensión"
    private lateinit var botonSiguiente: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.juego_comprension)

        pacienteId = intent.getStringExtra("pacienteId") ?: return
        val dificultadRecibida = intent.getStringExtra("dificultad")?.trim()

        botonSiguiente = findViewById(R.id.botonSiguiente)
        botonSiguiente.textSize = 18f
        botonSiguiente.setPadding(20, 20, 20, 20)
        botonSiguiente.isEnabled = false

        if (dificultadRecibida != null) {
            Log.d("JuegoComprension", "Dificultad recibida por intent: '$dificultadRecibida'")
            preguntas = PreguntasData.getPreguntasPorDificultadYCategoria(dificultadRecibida)
            Log.d("JuegoComprension", "Preguntas cargadas: ${preguntas.size}")
            if (preguntas.isNotEmpty()) {
                mostrarPregunta(preguntas[indice])
            } else {
                Toast.makeText(this, "No hay preguntas para esta categoría/dificultad", Toast.LENGTH_LONG).show()
                finish()
            }
        } else {
            Toast.makeText(this, "Dificultad no recibida", Toast.LENGTH_SHORT).show()
            finish()
        }

        botonSiguiente.setOnClickListener {
            val seleccion = obtenerRespuestaSeleccionada()
            if (seleccion == null) {
                Toast.makeText(this, "Selecciona una respuesta", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (seleccion == preguntas[indice].respuestaCorrecta) aciertos++

            if (indice < preguntas.size - 1) {
                indice++
                mostrarPregunta(preguntas[indice])
            } else {
                guardarResultado()
            }
        }
    }

    private fun mostrarPregunta(p: Pregunta) {
        val imageView = findViewById<ImageView>(R.id.imagenPregunta)
        val textoView = findViewById<TextView>(R.id.textoPregunta)
        val enunciadoView = findViewById<TextView>(R.id.enunciadoPregunta)
        val opcionesGroup = findViewById<RadioGroup>(R.id.opcionesGroup)

        imageView.visibility = if (p.imagen != null) View.VISIBLE else View.GONE
        textoView.visibility = if (p.texto != null) View.VISIBLE else View.GONE

        p.imagen?.let {
            val resId = resources.getIdentifier(it, "drawable", packageName)
            imageView.setImageResource(resId)
        }

        textoView.text = p.texto ?: ""
        enunciadoView.text = p.pregunta

        textoView.gravity = Gravity.CENTER
        enunciadoView.gravity = Gravity.CENTER

        textoView.textSize = 18f
        enunciadoView.textSize = 22f
        enunciadoView.setTextColor(Color.parseColor("#1A237E"))
        enunciadoView.setPadding(16, 24, 16, 12)

        opcionesGroup.removeAllViews()
        botonSiguiente.isEnabled = false

        p.opciones.forEachIndexed { i, opcion ->
            val radio = RadioButton(this).apply {
                text = opcion
                id = View.generateViewId()
                tag = i
                textSize = 18f
                setTextColor(Color.parseColor("#37474F"))
                setPadding(32, 24, 32, 24)
                layoutParams = RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.MATCH_PARENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 12, 0, 12)
                }
                background = getDrawable(R.drawable.radio_background)
                buttonDrawable = null
            }
            opcionesGroup.addView(radio)
        }

        opcionesGroup.setOnCheckedChangeListener { _, _ ->
            botonSiguiente.isEnabled = true
        }
    }

    private fun obtenerRespuestaSeleccionada(): Int? {
        val opcionesGroup = findViewById<RadioGroup>(R.id.opcionesGroup)
        val checkedId = opcionesGroup.checkedRadioButtonId
        val radioButton = opcionesGroup.findViewById<RadioButton>(checkedId)
        return radioButton?.tag as? Int
    }

    private fun guardarResultado() {
        val fecha = LocalDate.now().toString()
        val dificultadActual = preguntas.firstOrNull()?.dificultad ?: "Desconocida"

        val resultado = mapOf(
            "categoria" to categoria,
            "puntaje" to aciertos,
            "fecha" to fecha,
            "dificultad" to dificultadActual
        )

        val timestamp = System.currentTimeMillis()
        val docId = "$fecha-$timestamp"

        val ref = FirebaseFirestore.getInstance()
            .collection("Pacientes").document(pacienteId)
            .collection("Juegos").document(categoria)
            .collection("Fechas").document(docId)

        ref.set(resultado)

        Toast.makeText(this, "¡Juego completado! Aciertos: $aciertos", Toast.LENGTH_LONG).show()
        finish()

        val intent = Intent(this, ResultadosComprensionActivity::class.java)
        intent.putExtra("puntaje", aciertos)
        intent.putExtra("total", preguntas.size)
        startActivity(intent)
        finish()

    }
}
