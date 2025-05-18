package com.example.memora_app.recuerdos

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.memora_app.InformacionpersonalActivity
import com.example.memora_app.R
import com.example.memora_app.inicio_cuidador_activity
import com.example.memora_app.inicio_medico_activity
import com.example.memora_app.inicio_paciente_activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Base64
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.io.InputStream

class subida : AppCompatActivity() {

    private lateinit var imageViewPreview: ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var etDescription: EditText
    private lateinit var btnUpload: Button
    private var imageUri: Uri? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var dniPaciente: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.subida_recuerdos)


        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)


        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()



        imageViewPreview = findViewById(R.id.imageViewPreview)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        etDescription = findViewById(R.id.etDescription)
        btnUpload = findViewById(R.id.btnUpload)


        obtenerDniPaciente()


        btnSelectImage.setOnClickListener {
            selectImageFromGallery()
        }


        btnUpload.setOnClickListener {
            if (dniPaciente != null) {
                crearSubcoleccionSiNoExiste(dniPaciente!!)
            } else {
                Toast.makeText(this, "No se encontró el paciente asociado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Método para seleccionar una imagen de la galería
    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            imageViewPreview.setImageURI(imageUri) // Mostrar imagen seleccionada
        }
    }


    private fun obtenerDniPaciente() {
        val correo = auth.currentUser?.email ?: return

        db.collection("Cuidadores")
            .whereEqualTo("correo", correo)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents) {
                        dniPaciente = document.getString("dniPaciente")
                        if (dniPaciente.isNullOrEmpty()) {
                            Toast.makeText(this, "El cuidador no tiene un paciente asignado", Toast.LENGTH_LONG).show()
                        } else {
                            crearSubcoleccionSiNoExiste(dniPaciente!!)
                        }
                    }
                } else {
                    Toast.makeText(this, "No se encontró el cuidador en Firestore", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al obtener el DNI del paciente: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }


    private fun crearSubcoleccionSiNoExiste(dniPaciente: String) {
        val pacienteRef = db.collection("Pacientes").document(dniPaciente)

        pacienteRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                uploadImageToImgur(dniPaciente)
            } else {
                pacienteRef.set(mapOf("info" to "Paciente creado automáticamente"))
                    .addOnSuccessListener {
                        uploadImageToImgur(dniPaciente)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al crear el paciente en Firestore", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }


    private fun uploadImageToImgur(dniPaciente: String) {
        if (imageUri == null) {
            Toast.makeText(this, "Selecciona una imagen primero", Toast.LENGTH_SHORT).show()
            return
        }

        val inputStream: InputStream? = contentResolver.openInputStream(imageUri!!)
        val bytes = inputStream?.readBytes()
        val base64Image = Base64.encodeToString(bytes, Base64.NO_WRAP)

        val url = "https://api.imgur.com/3/image"

        val requestBody = JSONObject().apply {
            put("image", base64Image)
        }

        val startUpload = System.currentTimeMillis()

        val request = object : JsonObjectRequest(
            Request.Method.POST, url, requestBody,
            { response ->
                val endUpload = System.currentTimeMillis()
                Log.d("PERF", "Subida a Imgur completada en ${endUpload - startUpload} ms")

                val imageUrl = response.getJSONObject("data").getString("link")
                saveDataToFirestore(dniPaciente, imageUrl, etDescription.text.toString())
            },
            { error ->
                val endUpload = System.currentTimeMillis()
                Log.e("PERF", "Fallo en subida a Imgur tras ${endUpload - startUpload} ms")
                Toast.makeText(this, "Error al subir a Imgur: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Client-ID 2e998cdfcee39a6"
                return headers
            }
        }

        Volley.newRequestQueue(this).add(request)

    }


    private fun saveDataToFirestore(dniPaciente: String, imageUrl: String, description: String) {
        val recuerdo = hashMapOf(
            "imageUrl" to imageUrl,
            "description" to description,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("Pacientes").document(dniPaciente)
            .collection("Recuerdos")
            .add(recuerdo)
            .addOnSuccessListener {
                Toast.makeText(this, "Recuerdo guardado con éxito", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar en Firestore", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
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
