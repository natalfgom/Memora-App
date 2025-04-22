package com.example.memora_app.configuracionmedico

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.memora_app.InformacionpersonalActivity
import com.example.memora_app.R
import com.example.memora_app.inicio_cuidador_activity
import com.example.memora_app.inicio_medico_activity
import com.example.memora_app.inicio_paciente_activity

class OpcionesMedico : AppCompatActivity() {

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return true
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sleccion_pruebas)




        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)



        val pacienteLayout = findViewById<LinearLayout>(R.id.paciente)
        val cuidadorLayout = findViewById<LinearLayout>(R.id.cuidador)

        val pacienteId = intent.getStringExtra("PACIENTE_ID") ?: return
        val dniPaciente = intent.getStringExtra("DNI_PACIENTE") ?: return

        pacienteLayout.setOnClickListener {
            val intent = Intent(this, vertest::class.java)
            intent.putExtra("PACIENTE_ID", pacienteId)
            startActivity(intent)
        }

        cuidadorLayout.setOnClickListener {
            val intent = Intent(this, ListaPruebasCDRActivity::class.java)

            intent.putExtra("DNI_PACIENTE", dniPaciente)
            startActivity(intent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
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
        val rol = getSharedPreferences("PREFS_MEMORA", MODE_PRIVATE).getString("rol", "")
        val intent = when (rol) {
            "Médico" -> Intent(this, inicio_medico_activity::class.java)
            "Paciente" -> Intent(this, inicio_paciente_activity::class.java)
            "Cuidador" -> Intent(this, inicio_cuidador_activity::class.java)
            else -> return
        }
        startActivity(intent)
    }
}


