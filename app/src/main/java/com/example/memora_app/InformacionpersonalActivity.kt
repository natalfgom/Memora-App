package com.example.memora_app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class InformacionpersonalActivity : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etApellidos: EditText
    private lateinit var etdni: EditText
    private lateinit var etCorreo: EditText
    private lateinit var etTelefono: EditText
    private lateinit var etNuevoPin: EditText
    private lateinit var etPasswordActual: EditText
    private lateinit var etNuevaPassword: EditText
    private lateinit var btnGuardar: Button
    private lateinit var btnCerrarSesion: Button

    private val db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.informacion_personal)

        etNombre = findViewById(R.id.etNombre)
        etApellidos = findViewById(R.id.etApellidos)
        etCorreo = findViewById(R.id.etCorreo)
        etTelefono = findViewById(R.id.etTelefono)
        etNuevoPin = findViewById(R.id.etNuevoPin)
        etPasswordActual = findViewById(R.id.etPasswordActual)
        etNuevaPassword = findViewById(R.id.etNuevaPassword)
        etdni = findViewById(R.id.etdni)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)

        etNombre.isEnabled = false
        etApellidos.isEnabled = false
        etdni.isEnabled = false


        auth = FirebaseAuth.getInstance()

        cargarDatos()

        btnGuardar.setOnClickListener {
            verificarPasswordActual { esCorrecta ->
                if (esCorrecta) {
                    verificarUnicidad { esUnico ->
                        if (esUnico) {
                            guardarCambios()
                        } else {
                            Toast.makeText(this, "Correo o teléfono ya están en uso", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Contraseña actual incorrecta", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnCerrarSesion.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        // Configurar Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Opcional: Mostrar botón de volver atrás (si lo necesitas)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
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

    private fun cargarDatos() {
        val correo = auth.currentUser?.email ?: return

        val colecciones = listOf("Medicos", "Pacientes", "Cuidadores")

        for (coleccion in colecciones) {
            db.collection(coleccion)
                .whereEqualTo("correo", correo)
                .get()
                .addOnSuccessListener { documentos ->
                    if (!documentos.isEmpty) {
                        val doc = documentos.documents.first()
                        etNombre.setText(doc.getString("nombre"))
                        etApellidos.setText(doc.getString("apellidos"))
                        etCorreo.setText(correo)
                        etTelefono.setText(doc.getString("telefono"))
                        etdni.setText(doc.getString("dni"))

                        // Guardamos el rol en SharedPreferences (por si lo necesitamos después)
                        val rol = doc.getString("rol") ?: ""
                        val prefs = getSharedPreferences("PREFS_MEMORA", MODE_PRIVATE)
                        prefs.edit().putString("rol", rol).apply()

                        if (rol == "Cuidador") {
                            etNuevoPin.visibility = View.VISIBLE
                            etNuevoPin.setText(doc.getString("pin"))
                        } else {
                            etNuevoPin.visibility = View.GONE
                        }

                    }
                }
        }
    }

    private fun verificarPasswordActual(callback: (Boolean) -> Unit) {
        val user = auth.currentUser
        val passwordActual = etPasswordActual.text.toString().trim()
        if (user == null || passwordActual.isEmpty()) {
            callback(false)
            return
        }

        val credential = com.google.firebase.auth.EmailAuthProvider
            .getCredential(user.email!!, passwordActual)

        user.reauthenticate(credential)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    private fun verificarUnicidad(callback: (Boolean) -> Unit) {
        val nuevoCorreo = etCorreo.text.toString().trim()
        val nuevoTelefono = etTelefono.text.toString().trim()
        val user = auth.currentUser
        if (user == null) {
            callback(false)
            return
        }

        val colecciones = listOf("Medicos", "Pacientes", "Cuidadores")
        var esUnico = true
        var pendientes = colecciones.size

        for (coleccion in colecciones) {
            db.collection(coleccion).get().addOnSuccessListener { documentos ->
                for (documento in documentos) {
                    val datos = documento.data
                    val correoExistente = datos["correo"].toString()
                    val telefonoExistente = datos["telefono"].toString()

                    if (correoExistente == nuevoCorreo && correoExistente != user.email) {
                        esUnico = false
                    }
                    if (telefonoExistente == nuevoTelefono && telefonoExistente != etTelefono.text.toString()) {
                        esUnico = false
                    }
                }
                pendientes--
                if (pendientes == 0) {
                    callback(esUnico)
                }
            }.addOnFailureListener {
                pendientes--
                if (pendientes == 0) {
                    callback(false)
                }
            }
        }
    }

    private fun guardarCambios() {
        val correo = auth.currentUser?.email ?: return
        val user = auth.currentUser ?: return

        val datosActualizados = mutableMapOf(
            "nombre" to etNombre.text.toString(),
            "apellidos" to etApellidos.text.toString(),
            "telefono" to etTelefono.text.toString(),
            "correo" to etCorreo.text.toString()
        )

        if (etNuevoPin.text.toString().isNotBlank()) {
            datosActualizados["pin"] = etNuevoPin.text.toString()
        }

        val colecciones = listOf("Medicos", "Pacientes", "Cuidadores")

        for (coleccion in colecciones) {
            db.collection(coleccion)
                .whereEqualTo("correo", correo)
                .get()
                .addOnSuccessListener { documentos ->
                    if (!documentos.isEmpty) {
                        val docId = documentos.documents.first().id
                        db.collection(coleccion).document(docId)
                            .update(datosActualizados as Map<String, Any>)
                            .addOnSuccessListener {
                                if (correo != etCorreo.text.toString().trim()) {
                                    user.updateEmail(etCorreo.text.toString().trim())
                                        .addOnSuccessListener {
                                            limpiarCamposPassword()
                                            Toast.makeText(this, "Datos actualizados", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(this, "Error al actualizar el correo", Toast.LENGTH_SHORT).show()
                                        }
                                } else {
                                    limpiarCamposPassword()
                                    Toast.makeText(this, "Datos actualizados", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                }
        }

        if (etNuevaPassword.text.toString().isNotBlank()) {
            user.updatePassword(etNuevaPassword.text.toString().trim())
                .addOnSuccessListener {
                    limpiarCamposPassword()
                    Toast.makeText(this, "Contraseña actualizada", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al actualizar contraseña", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun limpiarCamposPassword() {
        etPasswordActual.setText("")
        etNuevaPassword.setText("")
    }
}
