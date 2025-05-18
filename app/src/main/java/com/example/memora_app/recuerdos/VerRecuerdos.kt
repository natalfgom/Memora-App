package com.example.memora_app.recuerdos

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.example.memora_app.InformacionpersonalActivity
import com.example.memora_app.R
import com.example.memora_app.inicio_cuidador_activity
import com.example.memora_app.inicio_medico_activity
import com.example.memora_app.inicio_paciente_activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.random.Random

class VerRecuerdos : AppCompatActivity() {

    private lateinit var imageViewRecuerdo: ImageView
    private lateinit var textViewDescripcion: TextView

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ver_recuerdos)



        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)


        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)


        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()


        imageViewRecuerdo = findViewById(R.id.imageViewRecuerdo)
        textViewDescripcion = findViewById(R.id.textViewDescripcion)



        obtenerRecuerdo()
    }

    private fun obtenerRecuerdo() {
        val usuarioEmail = auth.currentUser?.email ?: return


        db.collection("Pacientes")
            .whereEqualTo("correo", usuarioEmail)
            .get()
            .addOnSuccessListener { documentos ->
                if (!documentos.isEmpty) {
                    for (document in documentos) {
                        val dniPaciente = document.id
                        cargarRecuerdoAleatorio(dniPaciente)
                    }
                } else {
                    Toast.makeText(this, getString(R.string.error_paciente_no_encontrado), Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, getString(R.string.error_cargar_paciente), Toast.LENGTH_SHORT).show()
            }
    }

    private fun cargarRecuerdoAleatorio(dniPaciente: String) {
        val start = System.currentTimeMillis()

        db.collection("Pacientes").document(dniPaciente)
            .collection("Recuerdos")
            .get()
            .addOnSuccessListener { documentos ->
                val end = System.currentTimeMillis()
                Log.d("PERF", "Tiempo en cargar recuerdos desde Firestore: ${end - start} ms")

                if (!documentos.isEmpty) {

                    val recuerdoAleatorio = documentos.documents[Random.nextInt(documentos.size())]
                    val imageUrl = recuerdoAleatorio.getString("imageUrl") ?: ""
                    val descripcion = recuerdoAleatorio.getString("description") ?: getString(R.string.recuerdo_descripcion)
                    val fecha = recuerdoAleatorio.getLong("timestamp")?.let { timestamp ->
                        android.text.format.DateFormat.format("dd/MM/yyyy HH:mm", timestamp).toString()
                    } ?: getString(R.string.recuerdo_fecha)

                    textViewDescripcion.text = descripcion




                    Glide.with(this)
                        .load(imageUrl)
                        .into(imageViewRecuerdo)
                } else {
                    Toast.makeText(this, getString(R.string.error_sin_recuerdos), Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, getString(R.string.error_cargar_recuerdos), Toast.LENGTH_SHORT).show()
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