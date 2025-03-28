package com.example.memora_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.memora_app.configuracionmedico.vertest
import com.example.memora_app.pruebas.MMSEActivity
import com.example.memora_app.recuerdos.VerRecuerdos
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class inicio_paciente_activity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.inicio_paciente)

        Log.d("InicioPaciente", "Actividad iniciada")

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)



        val btnRecuerdos = findViewById<LinearLayout>(R.id.recuerdos)
        val btnEstadisticas = findViewById<LinearLayout>(R.id.estadisticas)
        val btnJuegos = findViewById<LinearLayout>(R.id.juegos)
        val btnHabitos = findViewById<LinearLayout>(R.id.habitos)
        val btnPruebas = findViewById<LinearLayout>(R.id.pruebas)

        btnRecuerdos.setOnClickListener {
            startActivity(Intent(this, VerRecuerdos::class.java))
        }

        btnEstadisticas.setOnClickListener {
            Toast.makeText(this, "Estadísticas seleccionadas", Toast.LENGTH_SHORT).show()
        }

        btnJuegos.setOnClickListener {
            Toast.makeText(this, "Juegos seleccionados", Toast.LENGTH_SHORT).show()
        }

        btnHabitos.setOnClickListener {
            startActivity(Intent(this, Habitos_Activity::class.java))
        }

        btnPruebas.setOnClickListener {
            startActivity(Intent(this, vertest::class.java))
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
