package com.example.memora_app.estadisticas

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class EstadisticasFiltradasActivity : AppCompatActivity() {

    private lateinit var spinnerTipoDato: Spinner
    private lateinit var btnFechaInicio: Button
    private lateinit var btnFechaFin: Button
    private lateinit var tvFechaInicio: TextView
    private lateinit var tvFechaFin: TextView
    private lateinit var chartPuntuaciones: LineChart
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val db = FirebaseFirestore.getInstance()
    private lateinit var pacienteId: String
    private lateinit var dniPaciente: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_estadisticas_paciente)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        pacienteId = intent.getStringExtra("PACIENTE_ID") ?: ""
        dniPaciente = intent.getStringExtra("DNI_PACIENTE") ?: ""

        spinnerTipoDato = findViewById(R.id.spinnerTipoDato)
        btnFechaInicio = findViewById(R.id.btnFechaInicio)
        btnFechaFin = findViewById(R.id.btnFechaFin)
        tvFechaInicio = findViewById(R.id.tvFechaInicio)
        tvFechaFin = findViewById(R.id.tvFechaFin)
        chartPuntuaciones = findViewById(R.id.chartPuntuaciones)


        val tiposDeJuegos = listOf("Memoria", "Comprensión", "Cálculo", "Orientación")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tiposDeJuegos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipoDato.adapter = adapter


        val hoy = dateFormat.format(Calendar.getInstance().time)
        tvFechaInicio.text = hoy
        tvFechaFin.text = hoy


        val onUpdate: () -> Unit = {
            val selectedItem = spinnerTipoDato.selectedItem
            if (selectedItem != null) {
                val tipo = selectedItem.toString()
                val fInicio = tvFechaInicio.text.toString()
                val fFin = tvFechaFin.text.toString()

                Log.d("Estadisticas", "Spinner seleccionado: $tipo")
                Log.d("Estadisticas", "Fechas: $fInicio hasta $fFin")

                if (fInicio != "Sin fecha" && fFin != "Sin fecha") {
                    cargarJuego(tipo, fInicio, fFin)
                }
            }
        }


        btnFechaInicio.setOnClickListener { showDatePicker(tvFechaInicio, onUpdate) }
        btnFechaFin.setOnClickListener { showDatePicker(tvFechaFin, onUpdate) }

        spinnerTipoDato.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                onUpdate()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }


        onUpdate()
    }

    private fun showDatePicker(textView: TextView, onDateSet: () -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            calendar.set(year, month, day)
            textView.text = dateFormat.format(calendar.time)
            onDateSet()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun cargarJuego(area: String, fInicio: String, fFin: String) {
        chartPuntuaciones.clear()

        val inicio = try { dateFormat.parse(fInicio) } catch (e: Exception) {
            Log.e("Estadisticas", "Fecha de inicio inválida: $fInicio")
            return
        }

        val fin = try { dateFormat.parse(fFin) } catch (e: Exception) {
            Log.e("Estadisticas", "Fecha de fin inválida: $fFin")
            return
        }

        val entradas = mutableListOf<Entry>()
        val colores = mutableListOf<Int>()
        val fechas = mutableListOf<String>()
        val dificultades = mutableMapOf<String, String>()

        db.collection("Pacientes").document(pacienteId)
            .collection("Dificultades").get()
            .addOnSuccessListener { difs ->
                for (doc in difs.documents) {
                    if (!Regex("""\d{4}-\d{2}-\d{2}""").matches(doc.id)) continue
                    val dificultad = (doc.get(area) as? Map<*, *>)?.get("Dificultad") as? String ?: continue
                    dificultades[doc.id] = dificultad
                }
                val startStats = System.currentTimeMillis()

                db.collection("Pacientes").document(pacienteId)
                    .collection("Juegos").document(area)
                    .collection("Fechas").get()
                    .addOnSuccessListener { result ->
                        var index = 0
                        result.documents.sortedBy { it.id }.forEach { doc ->
                            val fecha = doc.id
                            val date = try { dateFormat.parse(fecha) } catch (_: Exception) { null } ?: return@forEach

                            if (date in inicio..fin) {
                                val puntaje = doc.getDouble("puntaje")?.toFloat()
                                if (puntaje == null) {
                                    Log.w("Estadisticas", "Documento sin puntaje: ${doc.id}")
                                    return@forEach
                                }

                                entradas.add(Entry(index.toFloat(), puntaje))
                                fechas.add(fecha)

                                val dificultad = doc.getString("dificultad") ?: ""


                                val color = when {
                                    dificultad.contains("Alta") -> Color.RED
                                    dificultad.contains("Media") -> Color.rgb(255, 165, 0)
                                    dificultad.contains("Baja") -> Color.GREEN
                                    else -> Color.GRAY
                                }

                                colores.add(color)
                                index++
                            }
                        }

                        val dataSet = LineDataSet(entradas, "Puntajes $area")
                        dataSet.circleColors = colores
                        dataSet.color = Color.DKGRAY
                        dataSet.valueTextSize = 12f
                        dataSet.setDrawValues(true)

                        chartPuntuaciones.data = LineData(dataSet)
                        chartPuntuaciones.xAxis.valueFormatter = IndexAxisValueFormatter(fechas)
                        chartPuntuaciones.xAxis.position = XAxis.XAxisPosition.BOTTOM
                        chartPuntuaciones.animateX(1000)
                        chartPuntuaciones.invalidate()

                        Log.d("Estadisticas", "Gráfica generada con ${entradas.size} puntos.")

                        val endStats = System.currentTimeMillis()
                        Log.d("PERF", "Carga de estadísticas de '$area' completada en ${endStats - startStats} ms")
                    }
                    .addOnFailureListener {
                        Log.e("Estadisticas", "Error obteniendo puntuaciones: ${it.message}")
                    }

            }
            .addOnFailureListener {
                Log.e("Estadisticas", "Error obteniendo dificultades: ${it.message}")
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
