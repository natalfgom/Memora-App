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
import com.example.memora_app.pruebas.MMSEActivity
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class vertest : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val pruebas = mutableListOf<Map<String, String>>()
    private lateinit var usuarioId: String
    private lateinit var btnSolicitarPrueba: Button
    private lateinit var btnRealizarPrueba: Button
    private lateinit var listaPruebasLayout: LinearLayout
    private lateinit var buscarPruebasEditText: EditText

    private var esMedico = false
    private var esCuidador = false
    private var esPaciente = false
    private var pacienteId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_pruebas)


        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)


        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        listaPruebasLayout = findViewById(R.id.listaPruebasLayout)
        buscarPruebasEditText = findViewById(R.id.buscarPruebasEditText)
        btnSolicitarPrueba = findViewById(R.id.btnSolicitarPrueba)
        btnRealizarPrueba = findViewById(R.id.btnRealizarPrueba)

        usuarioId = FirebaseAuth.getInstance().currentUser?.email ?: return


        verificarRolUsuario()


        buscarPruebasEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val filtro = s.toString().lowercase()
                val pruebasFiltradas = pruebas.filter { it["fecha"]?.lowercase()?.contains(filtro) == true }
                actualizarListaPruebas(pruebasFiltradas)
            }
            override fun afterTextChanged(s: Editable?) {}
        })


        btnSolicitarPrueba.setOnClickListener {
            db.collection("Pacientes").document(pacienteId)
                .update("pruebaSolicitada", true)
                .addOnSuccessListener {
                    Toast.makeText(this, "Se ha solicitado una nueva prueba", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al solicitar la prueba", Toast.LENGTH_SHORT).show()
                }



        }


        btnRealizarPrueba.setOnClickListener {
            startActivity(Intent(this, MMSEActivity::class.java).apply {
                putExtra("PACIENTE_ID", pacienteId)
            })
            db.collection("Pacientes").document(pacienteId)
                .update("pruebaSolicitada", false)
                .addOnSuccessListener {
                    btnRealizarPrueba.visibility = View.GONE
                }
        }
    }

    private fun verificarRolUsuario() {
        db.collection("Medicos").whereEqualTo("correo", usuarioId).get()
            .addOnSuccessListener { docs ->
                if (!docs.isEmpty) {
                    esMedico = true
                    pacienteId = intent.getStringExtra("PACIENTE_ID") ?: ""
                    btnSolicitarPrueba.visibility = View.VISIBLE
                    cargarPruebasPaciente(pacienteId)
                }
            }

        db.collection("Cuidadores").whereEqualTo("correo", usuarioId).get()
            .addOnSuccessListener { docs ->
                if (!docs.isEmpty) {
                    esCuidador = true
                    pacienteId = docs.documents[0].getString("dniPaciente") ?: ""
                    if (pacienteId.isNotEmpty()) {
                        cargarPruebasPaciente(pacienteId)
                    }
                }
            }

        db.collection("Pacientes").whereEqualTo("correo", usuarioId).get()
            .addOnSuccessListener { docs ->
                if (!docs.isEmpty) {
                    esPaciente = true
                    pacienteId = docs.documents[0].id
                    val pruebaSolicitada = docs.documents[0].getBoolean("pruebaSolicitada") ?: false
                    btnRealizarPrueba.visibility = if (pruebaSolicitada) View.VISIBLE else View.GONE
                    cargarPruebasPaciente(pacienteId)
                }
            }
    }

    private fun cargarPruebasPaciente(pacienteId: String) {
        val pruebasRef = db.collection("Pacientes").document(pacienteId).collection("Pruebas")
        pruebasRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.isEmpty) {
                val noPruebasText = TextView(this).apply {
                    text = "No hay pruebas registradas."
                    setPadding(16, 16, 16, 16)
                    textSize = 18f
                }
                listaPruebasLayout.addView(noPruebasText)
            } else {
                pruebas.clear()
                for (doc in snapshot.documents) {
                    val pruebaId = doc.id
                    val fecha = doc.getString("Fecha") ?: "Fecha desconocida"
                    pruebas.add(mapOf("id" to pruebaId, "fecha" to fecha))
                }
                actualizarListaPruebas(pruebas)
            }
        }
    }

    private fun actualizarListaPruebas(listaPruebas: List<Map<String, String>>) {
        listaPruebasLayout.removeAllViews()
        for (prueba in listaPruebas) {
            val pruebaId = prueba["id"] ?: ""
            val fecha = prueba["fecha"] ?: "Fecha desconocida"

            val pruebaContainer = FrameLayout(this).apply {
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

            val pruebaTextView = TextView(this).apply {
                text = "Prueba realizada el: $fecha"
                setPadding(16, 16, 16, 16)
                textSize = 18f
            }

            pruebaContainer.addView(pruebaTextView)

            pruebaContainer.setOnClickListener {
                val intent = Intent(this@vertest, DetallePruebaActivity::class.java)
                intent.putExtra("PACIENTE_ID", pacienteId)
                intent.putExtra("PRUEBA_ID", pruebaId)
                startActivity(intent)
            }

            listaPruebasLayout.addView(pruebaContainer)
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
