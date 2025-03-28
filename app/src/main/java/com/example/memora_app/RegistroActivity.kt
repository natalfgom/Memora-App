package com.example.memora_app

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.memora_app.pruebas.mmseRegistro
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class RegistroActivity : AppCompatActivity() {

    private lateinit var spinnerRol: Spinner
    private lateinit var layoutMedico: LinearLayout
    private lateinit var layoutPaciente: LinearLayout
    private lateinit var layoutCuidador: LinearLayout
    private lateinit var etFechaNacimiento: EditText
    private lateinit var btnGuardar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Registro"
            setDisplayHomeAsUpEnabled(true)
        }

        spinnerRol = findViewById(R.id.spinnerRol)
        layoutMedico = findViewById(R.id.layoutMedico)
        layoutPaciente = findViewById(R.id.layoutPaciente)
        layoutCuidador = findViewById(R.id.layoutCuidador)
        etFechaNacimiento = findViewById(R.id.etFechaNacimiento)
        btnGuardar = findViewById(R.id.btnGuardar)

        spinnerRol.adapter = ArrayAdapter.createFromResource(
            this,
            R.array.roles_array,
            android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        spinnerRol.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                actualizarFormularioSegunRol(spinnerRol.selectedItem.toString())
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        etFechaNacimiento.setOnClickListener { mostrarDatePicker() }
        btnGuardar.setOnClickListener { if (validarDatos()) guardarEnFirebase() }
    }

    private fun actualizarFormularioSegunRol(rol: String) {
        layoutMedico.visibility = View.GONE
        layoutPaciente.visibility = View.GONE
        layoutCuidador.visibility = View.GONE
        when (rol) {
            "Médico" -> layoutMedico.visibility = View.VISIBLE
            "Paciente" -> layoutPaciente.visibility = View.VISIBLE
            "Cuidador" -> layoutCuidador.visibility = View.VISIBLE
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {  // Esta es la flecha
            finish()  // Cierra la actividad actual
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    private fun mostrarDatePicker() {
        val c = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            etFechaNacimiento.setText("$day/${month + 1}/$year")
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()

        etFechaNacimiento.error = null

    }

    private fun validarDatos(): Boolean {
        fun campo(id: Int) = findViewById<EditText>(id)

        val dniRegex = Regex("^[0-9]{8}[A-Za-z]$")
        val telefonoRegex = Regex("^[0-9]{9}$")
        val fechaRegex = Regex("^\\d{1,2}/\\d{1,2}/\\d{4}$")

        val obligatorios = listOf(
            R.id.etNombre, R.id.etApellidos, R.id.etDni, R.id.etTelefono,
            R.id.etCorreo, R.id.etRepetirCorreo, R.id.etPassword, R.id.etRepetirPassword, R.id.etFechaNacimiento
        )

        var esValido = true

        obligatorios.forEach {
            if (campo(it).text.isBlank()) {
                campo(it).error = "Campo obligatorio"
                esValido = false
            } else {
                campo(it).error = null // Limpia error si ya se rellenó
            }
        }

        if (!campo(R.id.etDni).text.matches(dniRegex)) {
            campo(R.id.etDni).error = "Formato inválido de DNI"
            esValido = false
        }

        if (!campo(R.id.etTelefono).text.matches(telefonoRegex)) {
            campo(R.id.etTelefono).error = "Teléfono inválido"
            esValido = false
        }

        if (!campo(R.id.etFechaNacimiento).text.matches(fechaRegex)) {
            campo(R.id.etFechaNacimiento).error = "Formato inválido (DD/MM/YYYY)"
            esValido = false
        } else {
            val fechaNacimiento = campo(R.id.etFechaNacimiento).text.toString()
            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            try {
                val fechaNac = sdf.parse(fechaNacimiento)
                val calendarNac = Calendar.getInstance().apply { time = fechaNac }
                val hoy = Calendar.getInstance()
                hoy.add(Calendar.YEAR, -18)
                if (calendarNac.after(hoy)) {
                    campo(R.id.etFechaNacimiento).error = "Debes tener al menos 18 años"
                    esValido = false
                }
            } catch (e: Exception) {
                campo(R.id.etFechaNacimiento).error = "Fecha inválida"
                esValido = false
            }
        }

        if (campo(R.id.etCorreo).text.toString() != campo(R.id.etRepetirCorreo).text.toString()) {
            campo(R.id.etRepetirCorreo).error = "Los correos no coinciden"
            esValido = false
        }

        if (campo(R.id.etPassword).text.toString() != campo(R.id.etRepetirPassword).text.toString()) {
            campo(R.id.etRepetirPassword).error = "Las contraseñas no coinciden"
            esValido = false
        }

        if (spinnerRol.selectedItem.toString() == "Paciente" &&
            campo(R.id.etVincularMedico).text.isBlank()) {
            campo(R.id.etVincularMedico).error = "Debes introducir un médico"
            esValido = false
        }

        if (spinnerRol.selectedItem.toString() == "Cuidador") {
            if (campo(R.id.etVincularPaciente).text.isBlank()) {
                campo(R.id.etVincularPaciente).error = "Debes introducir un paciente"
                esValido = false
            }
            val pin = campo(R.id.etPin).text.toString()
            if (pin.length != 4 || !pin.all { it.isDigit() }) {
                campo(R.id.etPin).error = "El PIN debe tener 4 dígitos numéricos"
                esValido = false
            }
        }

        return esValido
    }



    private fun verificarUnicidad(
        db: FirebaseFirestore,
        dni: String,
        correo: String,
        telefono: String,
        callback: (Boolean, Boolean, Boolean) -> Unit
    ) {
        val colecciones = listOf("Medicos", "Pacientes", "Cuidadores")
        var dniUnico = true
        var correoUnico = true
        var telefonoUnico = true
        var pendientes = colecciones.size

        colecciones.forEach { coleccion ->
            db.collection(coleccion).get().addOnSuccessListener { documentos ->
                for (documento in documentos) {
                    val datos = documento.data
                    if (datos["dni"] == dni) dniUnico = false
                    if (datos["correo"] == correo) correoUnico = false
                    if (datos["telefono"] == telefono) telefonoUnico = false
                }
                pendientes--
                if (pendientes == 0) {
                    callback(dniUnico, correoUnico, telefonoUnico)
                }
            }.addOnFailureListener {
                pendientes--
                if (pendientes == 0) {
                    callback(dniUnico, correoUnico, telefonoUnico)
                }
            }
        }
    }

    private fun verificarExistencia(
        db: FirebaseFirestore,
        coleccion: String,
        documentoId: String,
        callback: (Boolean) -> Unit
    ) {
        db.collection(coleccion).document(documentoId).get()
            .addOnSuccessListener { documento ->
                callback(documento.exists())
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    private fun guardarDatosEnFirestore(
        db: FirebaseFirestore,
        coleccion: String,
        documentoId: String,
        datos: HashMap<String, Any>,
        correo: String,
        password: String
    ) {
        db.collection(coleccion).document(documentoId).set(datos)
            .addOnSuccessListener {
                crearUsuarioEnAuth(correo, password, datos["rol"] as String? ?: "")
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar en Firestore", Toast.LENGTH_SHORT).show()
            }
    }


    private fun crearUsuarioEnAuth(correo: String, password: String, rol: String) {
        val auth = FirebaseAuth.getInstance()
        auth.createUserWithEmailAndPassword(correo, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    cerrarTeclado()
                    if (rol == "Paciente") {
                        startActivity(Intent(this, mmseRegistro::class.java))
                    } else {
                        startActivity(Intent(this, LoginActivity::class.java))
                    }
                    finish()
                } else {
                    findViewById<EditText>(R.id.etCorreo).error = "Error al crear en Auth: ${task.exception?.message}"
                }
            }
    }


    private fun cerrarTeclado() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }


    private fun guardarEnFirebase() {
        val db = FirebaseFirestore.getInstance()

        val dni = findViewById<EditText>(R.id.etDni).text.toString()
        val correo = findViewById<EditText>(R.id.etCorreo).text.toString()
        val telefono = findViewById<EditText>(R.id.etTelefono).text.toString()
        val password = findViewById<EditText>(R.id.etPassword).text.toString()

        verificarUnicidad(db, dni, correo, telefono) { dniUnico, correoUnico, telefonoUnico ->
            if (!dniUnico) findViewById<EditText>(R.id.etDni).error = "DNI ya registrado"
            if (!correoUnico) findViewById<EditText>(R.id.etCorreo).error = "Correo ya registrado"
            if (!telefonoUnico) findViewById<EditText>(R.id.etTelefono).error = "Teléfono ya registrado"
            if (!dniUnico || !correoUnico || !telefonoUnico) return@verificarUnicidad

            val datos = recogerDatosComunes()

            when (spinnerRol.selectedItem.toString()) {
                "Médico" -> guardarDatosEnFirestore(db, "Medicos", dni, datos, correo, password)
                "Paciente" -> {
                    val idMedico = findViewById<EditText>(R.id.etVincularMedico).text.toString()
                    verificarExistencia(db, "Medicos", idMedico) { existe ->
                        if (existe) {
                            datos["idMedico"] = idMedico
                            guardarDatosEnFirestore(db, "Pacientes", dni, datos, correo, password)
                        } else {
                            findViewById<EditText>(R.id.etVincularMedico).error = "El médico no existe"
                        }
                    }
                }
                "Cuidador" -> {
                    val dniPaciente = findViewById<EditText>(R.id.etVincularPaciente).text.toString()
                    verificarExistencia(db, "Pacientes", dniPaciente) { existe ->
                        if (existe) {
                            datos["dniPaciente"] = dniPaciente
                            datos["pin"] = findViewById<EditText>(R.id.etPin).text.toString()
                            guardarDatosEnFirestore(db, "Cuidadores", dni, datos, correo, password)
                        } else {
                            findViewById<EditText>(R.id.etVincularPaciente).error = "El paciente no existe"
                        }
                    }
                }
            }
        }
    }

    private fun recogerDatosComunes(): HashMap<String, Any> {
        return hashMapOf(
            "nombre" to findViewById<EditText>(R.id.etNombre).text.toString(),
            "apellidos" to findViewById<EditText>(R.id.etApellidos).text.toString(),
            "dni" to findViewById<EditText>(R.id.etDni).text.toString(),
            "telefono" to findViewById<EditText>(R.id.etTelefono).text.toString(),
            "correo" to findViewById<EditText>(R.id.etCorreo).text.toString(),
            "fechaNacimiento" to findViewById<EditText>(R.id.etFechaNacimiento).text.toString(),
            "rol" to spinnerRol.selectedItem.toString(),
            "pruebaSolicitada" to false
        )
    }


}