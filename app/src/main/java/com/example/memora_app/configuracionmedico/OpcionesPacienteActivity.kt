package com.example.memora_app.configuracionmedico

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class OpcionesPacienteActivity : AppCompatActivity() {

    private lateinit var pacienteLayout: LinearLayout
    private lateinit var cuidadorLayout: LinearLayout

    private var pacienteId: String? = null
    private var dniPaciente: String? = null

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

        pacienteLayout = findViewById(R.id.paciente)
        cuidadorLayout = findViewById(R.id.cuidador)

        cargarDatosPacienteParaCuidador()
    }

    private fun cargarDatosPacienteParaCuidador() {
        val emailCuidador = FirebaseAuth.getInstance().currentUser?.email

        if (emailCuidador.isNullOrEmpty()) {
            Toast.makeText(this, "Correo del cuidador no encontrado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        FirebaseFirestore.getInstance()
            .collection("Cuidadores")
            .whereEqualTo("correo", emailCuidador)
            .get()
            .addOnSuccessListener { documentos ->
                if (!documentos.isEmpty) {
                    val document = documentos.documents.first()
                    dniPaciente = document.getString("dniPaciente")
                    pacienteId = dniPaciente

                    if (dniPaciente.isNullOrEmpty()) {
                        Toast.makeText(
                            this,
                            "Este cuidador no tiene paciente asignado.",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        asignarListeners()
                    }
                } else {
                    Toast.makeText(
                        this,
                        "No existe el registro del cuidador con este correo.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al cargar datos: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
    }

    private fun asignarListeners() {
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
