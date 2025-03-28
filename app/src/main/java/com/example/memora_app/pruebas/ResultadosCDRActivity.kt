package com.example.memora_app.pruebas

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.memora_app.R
import com.example.memora_app.inicio_cuidador_activity
import com.example.memora_app.recuerdos.subida
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ResultadosCDRActivity : AppCompatActivity() {

    private lateinit var tvClasificacion: TextView
    private lateinit var tvMemoria: TextView
    private lateinit var tvOrientacion: TextView
    private lateinit var tvJuicio: TextView
    private lateinit var tvActividades: TextView
    private lateinit var tvCuidado: TextView
    private lateinit var btnVolver: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var cuidadorID: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.resultado_cdr)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        tvClasificacion = findViewById(R.id.tvResultadoCDR)
        tvMemoria = findViewById(R.id.tvResultadoMemoria)
        tvOrientacion = findViewById(R.id.tvResultadoOrientacion)
        tvJuicio = findViewById(R.id.tvResultadoJuicio)
        tvActividades = findViewById(R.id.tvResultadoActividades)
        tvCuidado = findViewById(R.id.tvResultadoCuidado)
        btnVolver = findViewById(R.id.btnVolverCDR)

        btnVolver.setOnClickListener {
            startActivity(Intent(this, inicio_cuidador_activity::class.java))
            true
        }

        obtenerCuidadorIDYResultados()
    }

    private fun obtenerCuidadorIDYResultados() {
        val correoUsuario = auth.currentUser?.email ?: return
        db.collection("Cuidadores")
            .whereEqualTo("correo", correoUsuario)
            .get()
            .addOnSuccessListener { documentos ->
                if (!documentos.isEmpty) {
                    cuidadorID = documentos.documents.first().id
                    cargarResultados()
                } else {
                    Toast.makeText(this, "Cuidador no encontrado", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al obtener datos", Toast.LENGTH_LONG).show()
                finish()
            }
    }

    private fun cargarResultados() {
        val fechaActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val docRef = db.collection("Cuidadores")
            .document(cuidadorID)
            .collection("PruebasCDR")
            .document("CDR-$fechaActual")

        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val clasificacion = document.getString("Clasificación") ?: "Sin clasificar"
                    val memoria = (document["Memoria"] as? Map<*, *>)?.get("Puntuación Máxima") ?: "-"
                    val orientacion = (document["Orientación"] as? Map<*, *>)?.get("Puntuación Máxima") ?: "-"
                    val juicio = (document["Juicio"] as? Map<*, *>)?.get("Puntuación Máxima") ?: "-"
                    val actividades = (document["Actividades"] as? Map<*, *>)?.get("Puntuación Máxima") ?: "-"
                    val cuidado = (document["Cuidado"] as? Map<*, *>)?.get("Puntuación Máxima") ?: "-"

                    tvClasificacion.text = "Clasificación: $clasificacion"
                    tvMemoria.text = "Memoria: $memoria"
                    tvOrientacion.text = "Orientación: $orientacion"
                    tvJuicio.text = "Juicio: $juicio"
                    tvActividades.text = "Actividades: $actividades"
                    tvCuidado.text = "Cuidado: $cuidado"
                } else {
                    Toast.makeText(this, "No hay resultados guardados", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Log.e("ResultadosCDR", "Error al cargar resultados", e)
                Toast.makeText(this, "Error al cargar resultados", Toast.LENGTH_LONG).show()
                finish()
            }
    }
}
