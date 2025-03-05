package com.example.memora_app

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class RegistroActivity : AppCompatActivity() {

    private lateinit var spinnerRol: Spinner
    private lateinit var layoutMedico: LinearLayout
    private lateinit var layoutPaciente: LinearLayout
    private lateinit var layoutCuidador: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        // Inicializar toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Registro"
            setDisplayHomeAsUpEnabled(true)
        }

        // Inicializar vistas comunes
        spinnerRol = findViewById(R.id.spinnerRol)

        // Inicializar layouts adicionales
        layoutMedico = findViewById(R.id.layoutMedico)
        layoutPaciente = findViewById(R.id.layoutPaciente)
        layoutCuidador = findViewById(R.id.layoutCuidador)

        // Listener de cambio de rol
        spinnerRol.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val rolSeleccionado = spinnerRol.selectedItem.toString()
                actualizarFormularioSegunRol(rolSeleccionado)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
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
}
