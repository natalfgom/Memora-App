package com.example.memora_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<Button>(R.id.registerButton)

        loginButton.setOnClickListener { realizarLogin() }

        registerButton.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }
    }

    private fun realizarLogin() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        // Limpiar errores antiguos
        emailEditText.error = null
        passwordEditText.error = null

        if (email.isEmpty()) {
            emailEditText.error = "Introduce tu correo"
            return
        }

        if (password.isEmpty()) {
            passwordEditText.error = "Introduce tu contraseña"
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    buscarUsuarioPorCorreo(email)
                } else {
                    passwordEditText.error = "Correo o contraseña incorrectos"
                }
            }
    }

    private fun buscarUsuarioPorCorreo(email: String) {
        emailEditText.error = null  // Limpia cualquier error anterior

        buscarEnColeccion("Medicos", email) { encontrado, rol ->
            if (encontrado) {
                redirigirSegunRol(rol)
            } else {
                buscarEnColeccion("Pacientes", email) { encontrado, rol ->
                    if (encontrado) {
                        redirigirSegunRol(rol)
                    } else {
                        buscarEnColeccion("Cuidadores", email) { encontrado, rol ->
                            if (encontrado) {
                                redirigirSegunRol(rol)
                            } else {
                                emailEditText.error = "Usuario no encontrado"
                                auth.signOut()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun buscarEnColeccion(
        coleccion: String,
        email: String,
        callback: (Boolean, String) -> Unit
    ) {
        db.collection(coleccion)
            .whereEqualTo("correo", email)
            .get()
            .addOnSuccessListener { documentos ->
                if (!documentos.isEmpty) {
                    val rol = documentos.documents.first().getString("rol") ?: ""
                    callback(true, rol)
                } else {
                    callback(false, "")
                }
            }
            .addOnFailureListener {
                emailEditText.error = "Error al buscar en $coleccion"
                callback(false, "")
            }
    }

    private fun redirigirSegunRol(rol: String) {
        emailEditText.error = null  // Limpia cualquier error visual previo

        val intent = when (rol) {
            "Médico" -> Intent(this, inicio_medico_activity::class.java)
            "Paciente" -> Intent(this, inicio_paciente_activity::class.java)
            "Cuidador" -> Intent(this, inicio_cuidador_activity::class.java)
            else -> {
                emailEditText.error = "Rol no reconocido"
                auth.signOut()
                return
            }
        }

        startActivity(intent)
        finish()
    }
}
