package com.example.memora_app.configuracionmedico

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.memora_app.R
import com.example.memora_app.inicio_cuidador_activity
import com.example.memora_app.inicio_medico_activity
import com.example.memora_app.inicio_paciente_activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ListaPacientesActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val pacientes = mutableListOf<Map<String, String>>() // Lista de pacientes en memoria
    private lateinit var listaPacientesLayout: LinearLayout
    private lateinit var buscarPacientesEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_pacientes)

        // Configurar Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        listaPacientesLayout = findViewById(R.id.listaPacientesLayout)
        buscarPacientesEditText = findViewById(R.id.buscarPacientesEditText)

        // Obtener el correo del médico actualmente autenticado
        val medicoCorreo = FirebaseAuth.getInstance().currentUser?.email

        if (medicoCorreo == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_LONG).show()
            return
        }

        // Buscar el ID del médico en Firestore usando su correo
        db.collection("Medicos").whereEqualTo("correo", medicoCorreo).get()
            .addOnSuccessListener { medicoSnapshot ->
                if (medicoSnapshot.isEmpty) {
                    Toast.makeText(this, "No se encontró un médico con este correo", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                val medicoId = medicoSnapshot.documents.first().id
                obtenerPacientes(medicoId)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al obtener el médico", Toast.LENGTH_LONG).show()
            }

        buscarPacientesEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val filtro = s.toString().lowercase()
                val pacientesFiltrados = pacientes.filter { it["dni"]?.lowercase()?.contains(filtro) == true }
                actualizarListaPacientes(pacientesFiltrados)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun obtenerPacientes(medicoId: String) {
        db.collection("Pacientes").whereEqualTo("idMedico", medicoId).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    val noPacientesText = TextView(this).apply {
                        text = "No hay pacientes registrados."
                        setPadding(16, 16, 16, 16)
                        textSize = 18f
                    }
                    listaPacientesLayout.addView(noPacientesText)
                } else {
                    pacientes.clear()
                    for (doc in snapshot.documents) {
                        val pacienteId = doc.id
                        val dni = doc.getString("dni") ?: "Desconocido"
                        pacientes.add(mapOf("id" to pacienteId, "dni" to dni))
                    }
                    actualizarListaPacientes(pacientes)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al obtener pacientes", Toast.LENGTH_LONG).show()
            }
    }

    private fun actualizarListaPacientes(listaPacientes: List<Map<String, String>>) {
        listaPacientesLayout.removeAllViews()

        for (paciente in listaPacientes) {
            val pacienteId = paciente["id"] ?: ""
            val dni = paciente["dni"] ?: "Desconocido"

            val pacienteContainer = FrameLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = 16
                    marginEnd = 16
                    topMargin = 16
                    bottomMargin = 16
                }
                setBackgroundResource(R.drawable.bordered_edittext_celeste)
                setPadding(16, 16, 16, 16)
            }

            val pacienteTextView = TextView(this).apply {
                text = "DNI: $dni"
                setPadding(16, 16, 16, 16)
                textSize = 18f
            }

            pacienteContainer.addView(pacienteTextView)

            pacienteContainer.setOnClickListener {
                val intent = Intent(this@ListaPacientesActivity, OpcionesMedico::class.java)
                intent.putExtra("PACIENTE_ID", pacienteId)
                intent.putExtra("DNI_PACIENTE", dni)
                startActivity(intent)
            }

            listaPacientesLayout.addView(pacienteContainer)
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
