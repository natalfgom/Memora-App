package com.example.memora_app.juegos

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.memora_app.R
import com.example.memora_app.inicio_paciente_activity

class ResultadosMemoriaActivity : AppCompatActivity() {

    private lateinit var estrellas: List<ImageView>
    private lateinit var sonido: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resultados_comprension)

        val puntaje = intent.getIntExtra("puntaje", 0)
        val total = intent.getIntExtra("total", 0)
        val mensajeFinal = findViewById<TextView>(R.id.textoResultadoFinal)
        val btnVolver = findViewById<Button>(R.id.btnVolverInicio)

        mensajeFinal.text = "Has obtenido $puntaje de $total puntos."

        estrellas = listOf(
            findViewById(R.id.estrella1),
            findViewById(R.id.estrella2),
            findViewById(R.id.estrella3),
            findViewById(R.id.estrella4),
            findViewById(R.id.estrella5)
        )

        val porcentaje = puntaje.toDouble() / total
        val numeroEstrellas = when {
            porcentaje >= 0.9 -> 5
            porcentaje >= 0.7 -> 4
            porcentaje >= 0.5 -> 3
            porcentaje >= 0.3 -> 2
            else -> 1
        }

        reproducirAnimacion(numeroEstrellas)

        btnVolver.setOnClickListener {
            val intent = Intent(this, inicio_paciente_activity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }

    private fun reproducirAnimacion(cuantas: Int) {
        val handler = Handler(Looper.getMainLooper())
        val anim = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)

        for (i in 0 until cuantas) {
            handler.postDelayed({
                estrellas[i].setImageResource(R.drawable.estrella_completa)
                estrellas[i].startAnimation(anim)
                estrellas[i].visibility = View.VISIBLE
                estrellas[i].alpha = 1f
                // 🎵 Lógica preparada por si decides usar sonido más adelante:
                //sonido = MediaPlayer.create(this, R.raw.pop)
                //sonido.start()
            }, i * 300L)
        }
    }
}
