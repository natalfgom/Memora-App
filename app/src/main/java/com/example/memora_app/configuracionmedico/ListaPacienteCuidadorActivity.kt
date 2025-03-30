package com.example.memora_app.configuracionmedico

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.memora_app.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ListaPacienteCuidadorActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var listaPacientesLayout: LinearLayout
    private lateinit var buscarPacientesEditText: EditText
    private val pacientes = mutableListOf<Map<String, String>>() // id + dni

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_paciente_cuidador)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        listaPacientesLayout = findViewById(R.id.listaPacientesLayout)
        buscarPacientesEditText = findViewById(R.id.buscarPacientesEditText)

        val correoCuidador = FirebaseAuth.getInstance().currentUser?.email

        if (correoCuidador == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_LONG).show()
            return
        }

        db.collection("Cuidadores").whereEqualTo("correo", correoCuidador).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    Toast.makeText(this, "No se encontró cuidador", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                val dniPaciente = snapshot.documents.first().getString("dniPaciente") ?: return@addOnSuccessListener

                db.collection("Pacientes").whereEqualTo("dni", dniPaciente).get()
                    .addOnSuccessListener { pacientesSnap ->
                        if (pacientesSnap.isEmpty) {
                            mostrarTexto("No hay pacientes asociados.")
                        } else {
                            pacientes.clear()
                            for (doc in pacientesSnap.documents) {
                                val id = doc.id
                                val dni = doc.getString("dni") ?: continue
                                pacientes.add(mapOf("id" to id, "dni" to dni))
                            }
                            actualizarListaPacientes(pacientes)
                        }
                    }
            }

        buscarPacientesEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val filtro = s.toString().lowercase()
                val filtrados = pacientes.filter { it["dni"]?.lowercase()?.contains(filtro) == true }
                actualizarListaPacientes(filtrados)
            }
        })
    }

    private fun actualizarListaPacientes(lista: List<Map<String, String>>) {
        listaPacientesLayout.removeAllViews()

        for (paciente in lista) {
            val pacienteId = paciente["id"] ?: ""
            val dni = paciente["dni"] ?: "Desconocido"

            val frame = FrameLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(16, 16, 16, 16)
                }
                setBackgroundResource(R.drawable.bordered_edittext_celeste)
                setPadding(16, 16, 16, 16)
            }

            val text = TextView(this).apply {
                text = "DNI: $dni"
                textSize = 18f
                setPadding(16, 16, 16, 16)
            }

            frame.addView(text)

            frame.setOnClickListener {
                val intent = Intent(this, OpcionesPacienteActivity::class.java)
                intent.putExtra("PACIENTE_ID", pacienteId)
                intent.putExtra("DNI_PACIENTE", dni)
                startActivity(intent)
            }

            listaPacientesLayout.addView(frame)
        }
    }

    private fun mostrarTexto(texto: String) {
        listaPacientesLayout.removeAllViews()
        listaPacientesLayout.addView(TextView(this).apply {
            text = texto
            textSize = 18f
            setPadding(24, 24, 24, 24)
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
