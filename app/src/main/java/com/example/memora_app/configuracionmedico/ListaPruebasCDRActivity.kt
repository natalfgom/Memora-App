package com.example.memora_app.configuracionmedico

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.memora_app.InformacionpersonalActivity
import com.example.memora_app.R
import com.example.memora_app.inicio_cuidador_activity
import com.example.memora_app.inicio_medico_activity
import com.example.memora_app.inicio_paciente_activity
import com.example.memora_app.pruebas.CDRSBActivity
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ListaPruebasCDRActivity : AppCompatActivity() {

    private lateinit var listaPruebasLayout: LinearLayout
    private lateinit var buscarEditText: EditText
    private lateinit var btnSolicitarPruebaCDR: Button
    private lateinit var btnRealizarPruebaCDR: Button

    private val pruebas = mutableListOf<Map<String, String>>()
    private lateinit var dniPaciente: String
    private lateinit var cuidadorId: String

    private var esMedico = false
    private var esCuidador = false

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_pruebas)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        listaPruebasLayout = findViewById(R.id.listaPruebasLayout)
        buscarEditText = findViewById(R.id.buscarPruebasEditText)
        btnSolicitarPruebaCDR = findViewById(R.id.btnSolicitarPrueba)
        btnRealizarPruebaCDR = findViewById(R.id.btnRealizarPrueba)

        dniPaciente = intent.getStringExtra("DNI_PACIENTE") ?: return

        verificarRolUsuario()

        buscarEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val filtro = s.toString().lowercase()
                val filtradas = pruebas.filter { it["fecha"]?.lowercase()?.contains(filtro) == true }
                actualizarLista(filtradas)
            }
        })


        btnSolicitarPruebaCDR.setOnClickListener {
            db.collection("Cuidadores").document(cuidadorId)
                .update("pruebaCDRSolicitada", true)
                .addOnSuccessListener {
                    Toast.makeText(this, "Prueba solicitada al cuidador", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al solicitar prueba", Toast.LENGTH_SHORT).show()
                }


        }

        // Cuidador realiza la prueba CDR
        btnRealizarPruebaCDR.setOnClickListener {
            startActivity(Intent(this, CDRSBActivity::class.java).apply {
                putExtra("DNI_PACIENTE", dniPaciente)
            })

            db.collection("Cuidadores").document(cuidadorId)
                .update("pruebaCDRSolicitada", false)
                .addOnSuccessListener { btnRealizarPruebaCDR.visibility = View.GONE }
        }
    }

    private fun verificarRolUsuario() {
        val usuarioId = FirebaseAuth.getInstance().currentUser?.email ?: return

        db.collection("Medicos").whereEqualTo("correo", usuarioId).get().addOnSuccessListener { docs ->
            if (!docs.isEmpty) {
                esMedico = true
                btnSolicitarPruebaCDR.visibility = View.VISIBLE
            }
        }

        db.collection("Cuidadores").whereEqualTo("correo", usuarioId).get().addOnSuccessListener { docs ->
            if (!docs.isEmpty) {
                esCuidador = true
                cuidadorId = docs.documents[0].id
                db.collection("Cuidadores").document(cuidadorId).get().addOnSuccessListener { doc ->
                    val solicitada = doc.getBoolean("pruebaCDRSolicitada") ?: false
                    btnRealizarPruebaCDR.visibility = if (solicitada) View.VISIBLE else View.GONE
                }
            }
        }

        cargarPruebas()
    }

    private fun cargarPruebas() {
        db.collection("Cuidadores").whereEqualTo("dniPaciente", dniPaciente).get()
            .addOnSuccessListener { docs ->
                val cuidadorDoc = docs.documents.firstOrNull()
                if (cuidadorDoc == null) {
                    mostrarTexto("No se encontró cuidador para este paciente.")
                    return@addOnSuccessListener
                }
                cuidadorId = cuidadorDoc.id

                db.collection("Cuidadores").document(cuidadorId)
                    .collection("PruebasCDR").get()
                    .addOnSuccessListener { snapshot ->
                        pruebas.clear()
                        for (doc in snapshot.documents) {
                            val id = doc.id
                            if (id.startsWith("CDR-")) {
                                val fecha = id.removePrefix("CDR-")
                                pruebas.add(mapOf("id" to id, "fecha" to fecha))
                            }
                        }
                        actualizarLista(pruebas)
                    }
            }
    }

    private fun actualizarLista(lista: List<Map<String, String>>) {
        listaPruebasLayout.removeAllViews()

        if (lista.isEmpty()) {
            mostrarTexto("No hay pruebas registradas.")
            return
        }

        for (prueba in lista) {
            val pruebaId = prueba["id"] ?: continue
            val fecha = prueba["fecha"] ?: "Fecha desconocida"

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
                text = "CDR-SB - $fecha"
                textSize = 18f
                setPadding(16, 16, 16, 16)
            }

            frame.addView(text)

            frame.setOnClickListener {
                val intent = Intent(this, DetallePruebaCDRActivity::class.java)
                intent.putExtra("DNI_PACIENTE", dniPaciente)
                intent.putExtra("FECHA_PRUEBA", fecha)
                intent.putExtra("CUIDADOR_ID", cuidadorId)
                startActivity(intent)
            }

            listaPruebasLayout.addView(frame)
        }
    }

    private fun mostrarTexto(texto: String) {
        listaPruebasLayout.removeAllViews()
        listaPruebasLayout.addView(TextView(this).apply {
            text = texto
            textSize = 18f
            setPadding(24, 24, 24, 24)
        })
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
