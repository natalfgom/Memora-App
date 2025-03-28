package com.example.memora_app.pruebas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.memora_app.R

class ExplicacionComprensionRgActivity : AppCompatActivity() {
    private lateinit var btnComenzar: Button
    private lateinit var tvExplicacion: TextView

    private lateinit var pacienteID: String  // Variable para almacenar el ID del paciente

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explicacion_comprension)

        tvExplicacion = findViewById(R.id.tvExplicacion)
        btnComenzar = findViewById(R.id.btnComenzar)

        // Obtener el ID del paciente desde el Intent
        pacienteID = intent.getStringExtra("paciente_id") ?: ""

        // Verificar que se haya recibido correctamente el ID del paciente
        if (pacienteID.isEmpty()) {
            finish() // Cerrar la actividad si no se recibió el ID del paciente
        }

        btnComenzar.setOnClickListener {
            val intent = Intent(this, ComprensionRgActivity::class.java)
            intent.putExtra("paciente_id", pacienteID) // Pasar el ID a la siguiente actividad
            startActivity(intent)
            finish()
        }
    }
}
