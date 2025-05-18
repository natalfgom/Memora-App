package com.example.memora_app.juegos

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.memora_app.R
import com.example.memora_app.modelos.PreguntaMemoria
import com.example.memora_app.modelos.PreguntasMemoriaData
import com.github.chrisbanes.photoview.PhotoView
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate

class JuegoMemoriaActivity : AppCompatActivity() {

    private lateinit var preguntas: List<PreguntaMemoria>
    private var indice = 0
    private var aciertos = 0
    private var pistasUsadas = 0
    private lateinit var pacienteId: String
    private lateinit var dificultad: String
    private val categoria = "Memoria"
    private lateinit var botonSiguiente: Button
    private lateinit var botonPista: Button
    private var pistaMostrada = false
    private var enFaseObservacion = true
    private val respuestasSeleccionadas = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        val start = System.currentTimeMillis()

        setContentView(R.layout.activity_memoria)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.juego_memoria_unico)

        pacienteId = intent.getStringExtra("pacienteId") ?: return
        dificultad = intent.getStringExtra("dificultad") ?: return

        preguntas = PreguntasMemoriaData.getPreguntasPorDificultad(dificultad)

        botonSiguiente = findViewById(R.id.botonSiguiente)
        botonPista = findViewById(R.id.botonPista)

        if (preguntas.isNotEmpty()) {
            mostrarImagenObservacion(preguntas[indice])
        } else {
            Toast.makeText(this, "No hay preguntas disponibles", Toast.LENGTH_LONG).show()
            finish()
        }

        botonSiguiente.setOnClickListener {
            if (enFaseObservacion) {
                enFaseObservacion = false
                mostrarPregunta(preguntas[indice])
                return@setOnClickListener
            }

            if (dificultad == "Dificultad Baja") {
                val seleccion = respuestasSeleccionadas.firstOrNull()
                if (seleccion == null) {
                    Toast.makeText(this, "Selecciona una respuesta", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val correcta = preguntas[indice].opciones[preguntas[indice].respuestaCorrecta]
                if (seleccion == correcta) aciertos++
            } else {
                val correctas = preguntas[indice].respuestasCorrectas ?: listOf()
                val seleccionadas = respuestasSeleccionadas
                val aciertosPregunta = seleccionadas.count { opcion ->
                    correctas.contains(preguntas[indice].opciones.indexOf(opcion))
                }
                aciertos += aciertosPregunta
            }

            if (indice < preguntas.size - 1) {
                indice++
                pistaMostrada = false
                enFaseObservacion = true
                mostrarImagenObservacion(preguntas[indice])
            } else {
                guardarResultado()
            }
        }

        botonPista.setOnClickListener {
            if (!pistaMostrada) {
                val pregunta = preguntas[indice]
                pregunta.imagenPista?.let {
                    val resId = resources.getIdentifier(it, "drawable", packageName)
                    findViewById<PhotoView>(R.id.imagenPregunta).setImageResource(resId)
                    pistaMostrada = true
                    pistasUsadas++


                    val scrollView = findViewById<ScrollView>(R.id.scrollView)
                    scrollView.post {
                        scrollView.fullScroll(ScrollView.FOCUS_UP)
                    }
                }
            }
        }

        val end = System.currentTimeMillis()
        Log.d("PERF", "Setup de MemoriaActivity en ${end - start} ms")

    }

    private fun mostrarImagenObservacion(p: PreguntaMemoria) {
        val imagenObservacion = findViewById<PhotoView>(R.id.imagenObservacion)
        val imagenPregunta = findViewById<PhotoView>(R.id.imagenPregunta)
        val enunciado = findViewById<TextView>(R.id.enunciadoPregunta)
        val texto = findViewById<TextView>(R.id.textoPregunta)
        val opcionesGroup = findViewById<RadioGroup>(R.id.opcionesGroup)

        val resId = resources.getIdentifier(p.imagenOriginal ?: "", "drawable", packageName)
        imagenObservacion.setImageResource(resId)

        opcionesGroup.visibility = View.GONE
        texto.visibility = View.GONE
        imagenObservacion.visibility = View.VISIBLE
        imagenPregunta.visibility = View.GONE
        botonPista.visibility = View.GONE

        enunciado.text = getString(R.string.texto_observar_imagen)
        enunciado.gravity = Gravity.CENTER
        enunciado.textSize = 22f
        enunciado.setTextColor(Color.parseColor("#1A237E"))
        enunciado.setPadding(16, 24, 16, 12)
        enunciado.typeface = ResourcesCompat.getFont(this, R.font.alike)
        botonSiguiente.text = getString(R.string.siguiente)
        botonSiguiente.isEnabled = true
    }

    private fun mostrarPregunta(p: PreguntaMemoria) {
        val imagenObservacion = findViewById<PhotoView>(R.id.imagenObservacion)
        val imagenPregunta = findViewById<PhotoView>(R.id.imagenPregunta)
        val textoView = findViewById<TextView>(R.id.textoPregunta)
        val enunciadoView = findViewById<TextView>(R.id.enunciadoPregunta)
        val opcionesGroup = findViewById<RadioGroup>(R.id.opcionesGroup)

        opcionesGroup.visibility = View.VISIBLE
        imagenObservacion.visibility = View.GONE
        imagenPregunta.visibility = View.VISIBLE

        val resId = resources.getIdentifier(p.imagen, "drawable", packageName)
        imagenPregunta.setImageResource(resId)

        textoView.visibility = if (p.texto != null) View.VISIBLE else View.GONE
        textoView.text = p.texto ?: ""
        enunciadoView.text = p.pregunta

        textoView.gravity = Gravity.CENTER
        enunciadoView.gravity = Gravity.CENTER
        textoView.textSize = 18f
        enunciadoView.textSize = 22f
        enunciadoView.setTextColor(Color.parseColor("#1A237E"))
        enunciadoView.setPadding(16, 24, 16, 12)

        opcionesGroup.removeAllViews()
        botonSiguiente.text = if (indice == preguntas.size - 1) "Finalizar" else "Siguiente"
        botonSiguiente.isEnabled = false
        botonPista.visibility = View.VISIBLE

        respuestasSeleccionadas.clear()

        val esMultiple = dificultad != "Dificultad Baja"
        val maxRespuestas = if (esMultiple) p.respuestasCorrectas?.size ?: 1 else 1

        p.opciones.forEach { opcion ->
            val opcionView = TextView(this).apply {
                text = opcion
                textSize = 18f
                setPadding(40, 24, 40, 24)
                setTextColor(Color.BLACK)
                background = getDrawable(R.drawable.selector_opcion_memoria)
                layoutParams = RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.MATCH_PARENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 20, 0, 20)
                }
                isClickable = true
                isFocusable = true
                gravity = Gravity.CENTER
                setOnClickListener {
                    if (esMultiple) {
                        if (respuestasSeleccionadas.contains(opcion)) {
                            respuestasSeleccionadas.remove(opcion)
                            isSelected = false
                        } else {
                            if (respuestasSeleccionadas.size < maxRespuestas) {
                                respuestasSeleccionadas.add(opcion)
                                isSelected = true
                            } else {
                                Toast.makeText(this@JuegoMemoriaActivity, "Máximo $maxRespuestas respuestas", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        respuestasSeleccionadas.clear()
                        respuestasSeleccionadas.add(opcion)
                        resetSeleccionVisual(opcionesGroup)
                        isSelected = true
                    }
                    botonSiguiente.isEnabled = respuestasSeleccionadas.size == maxRespuestas
                }
            }
            opcionesGroup.addView(opcionView)
        }

        val scrollView = findViewById<ScrollView>(R.id.scrollView)
        scrollView.post {
            scrollView.fullScroll(ScrollView.FOCUS_UP)
        }
    }

    private fun resetSeleccionVisual(group: RadioGroup) {
        for (i in 0 until group.childCount) {
            val child = group.getChildAt(i)
            if (child is TextView) {
                child.isSelected = false
            }
        }
    }

    private fun calcularPuntuacionFinal(): Int {
        return when (dificultad) {
            "Dificultad Baja" -> when (aciertos) {
                0 -> 0
                1 -> if (pistasUsadas > 0) 1 else 0
                2 -> if (pistasUsadas >= 2) 2 else if (pistasUsadas == 1) 3 else 0
                3 -> if (pistasUsadas >= 2) 3 else if (pistasUsadas == 1) 4 else 5
                else -> 0
            }
            "Dificultad Media" -> when (aciertos) {
                0 -> 0
                1 -> 1
                2 -> if (pistasUsadas > 0) 2 else 3
                3 -> if (pistasUsadas > 0) 3 else 4
                4 -> if (pistasUsadas > 0) 4 else 5
                else -> 0
            }
            "Dificultad Alta" -> when (aciertos) {
                0 -> 0
                in 1..2 -> 1
                in 3..4 -> if (pistasUsadas > 0) 2 else 3
                in 5..6 -> if (pistasUsadas > 0) 4 else 5
                else -> 0
            }
            else -> 0
        }
    }

    private fun guardarResultado() {
        val fecha = LocalDate.now().toString()
        val puntajeFinal = calcularPuntuacionFinal()

        val resultado = mapOf(
            "categoria" to categoria,
            "puntaje" to puntajeFinal,
            "fecha" to fecha,
            "dificultad" to dificultad
        )

        val ref = FirebaseFirestore.getInstance()
            .collection("Pacientes").document(pacienteId)
            .collection("Juegos").document(categoria)
            .collection("Fechas").document(fecha)

        ref.set(resultado)

        Toast.makeText(this, "¡Juego completado! Puntos: $puntajeFinal", Toast.LENGTH_LONG).show()

        val intent = Intent(this, ResultadosMemoriaActivity::class.java)

        intent.putExtra("puntaje", puntajeFinal)
        intent.putExtra("total", 5)
        startActivity(intent)
        finish()
    }
}
