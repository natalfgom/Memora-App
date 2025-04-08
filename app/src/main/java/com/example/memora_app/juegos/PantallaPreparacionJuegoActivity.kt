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



        DificultadManager.actualizarDificultades(pacienteId, forzar = true)


        // Corrección: los botones son LinearLayout en el layout
        findViewById<LinearLayout>(R.id.btnComprension).setOnClickListener {
            val intent = Intent(this, ExplicacionComprensionActivity::class.java)
            intent.putExtra("pacienteId", pacienteId)
            intent.putExtra("categoria", "Comprensión")
            startActivity(intent)
        }

        findViewById<LinearLayout>(R.id.btnCalculo).setOnClickListener {

        }

        findViewById<LinearLayout>(R.id.btnMemoria).setOnClickListener {

        }

        findViewById<LinearLayout>(R.id.btnOrientacion).setOnClickListener {

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
