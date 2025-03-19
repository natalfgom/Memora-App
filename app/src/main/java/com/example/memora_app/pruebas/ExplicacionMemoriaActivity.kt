package com.example.memora_app.pruebas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.memora_app.R

class ExplicacionMemoriaActivity : AppCompatActivity() {
    private lateinit var btnComenzar: Button
    private lateinit var tvExplicacion: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explicacion_memoria)

        tvExplicacion = findViewById(R.id.tvExplicacion)
        btnComenzar = findViewById(R.id.btnComenzar)



        btnComenzar.setOnClickListener {
            val intent = Intent(this, MemoriaActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
