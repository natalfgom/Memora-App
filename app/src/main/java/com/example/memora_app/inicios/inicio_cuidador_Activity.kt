package com.example.memora_app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.memora_app.configuracionmedico.DetallePruebaCDRActivity
import com.example.memora_app.configuracionmedico.ListaPruebasCDRActivity
import com.example.memora_app.configuracionmedico.OpcionesMedico
import com.example.memora_app.configuracionmedico.OpcionesPacienteActivity
import com.example.memora_app.configuracionmedico.vertest
import com.example.memora_app.estadisticas.EstadisticasFiltradasActivity
import com.example.memora_app.pruebas.CDRSBActivity
import com.example.memora_app.recuerdos.subida
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore



class inicio_cuidador_activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.inicio_cuidador)


        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Crear canal de notificaciones para el cuidador (una sola vez)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "canal_cuidador",
                "Notificaciones del cuidador",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones sobre pruebas CDR solicitadas"
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

// Escuchar si hay solicitud de prueba CDR para el cuidador
        val correoUsuario = FirebaseAuth.getInstance().currentUser?.email

        if (correoUsuario != null) {
            FirebaseFirestore.getInstance().collection("Cuidadores").get()
                .addOnSuccessListener { documentos ->
                    for (doc in documentos) {
                        val correo = doc.getString("correo")
                        if (correo == correoUsuario) {
                            val dniCuidador = doc.getString("dni")
                            if (dniCuidador != null) {
                                FirebaseFirestore.getInstance()
                                    .collection("Cuidadores")
                                    .document(dniCuidador)
                                    .addSnapshotListener { snapshot, e ->
                                        if (e != null || snapshot == null || !snapshot.exists()) {
                                            Log.w("NotiCuidador", "No se pudo obtener snapshot")
                                            return@addSnapshotListener
                                        }

                                        val pruebaSolicitada = snapshot.getBoolean("pruebaCDRSolicitada") ?: false
                                        if (pruebaSolicitada) {
                                            Log.d("NotiCuidador", "pruebaCDRSolicitada = true → mostrando notificación")

                                            val dniPaciente = doc.getString("dniPaciente")

                                            val intent = Intent(this, OpcionesPacienteActivity::class.java).apply {
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                putExtra("DNI_PACIENTE", dniPaciente)

                                            }

                                            val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

                                            val builder = NotificationCompat.Builder(this, "canal_cuidador")
                                                .setSmallIcon(R.drawable.pruebas)
                                                .setContentTitle("Nueva prueba CDR disponible")
                                                .setContentText("Hay una nueva prueba CDR disponible para uno de tus pacientes.")
                                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                                .setContentIntent(pendingIntent)
                                                .setAutoCancel(true)

                                            with(NotificationManagerCompat.from(this)) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                    if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                                        requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1002)
                                                    }
                                                }

                                                notify(1002, builder.build())
                                            }
                                        }
                                    }
                            }
                            break
                        }
                    }
                }
                .addOnFailureListener {
                    Log.w("Firestore", "Error obteniendo cuidador", it)
                }


        }




        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val btnRecuerdos = findViewById<LinearLayout>(R.id.recuerdos)
        val btnEstadisticas = findViewById<LinearLayout>(R.id.estadisticas)
        val btnPruebas = findViewById<LinearLayout>(R.id.pruebas)
        val btnHabitos = findViewById<LinearLayout>(R.id.habitos)


        btnRecuerdos.setOnClickListener {
            startActivity(Intent(this, subida::class.java))
            true
        }

        btnEstadisticas.setOnClickListener {
            val correoUsuario = FirebaseAuth.getInstance().currentUser?.email

            if (correoUsuario == null) {
                Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = FirebaseFirestore.getInstance()
            db.collection("Cuidadores").get()
                .addOnSuccessListener { documentos ->
                    var encontrado = false

                    for (doc in documentos) {
                        val correo = doc.getString("correo")
                        if (correo == correoUsuario) {
                            val dniPaciente = doc.getString("dniPaciente")
                            if (dniPaciente != null) {
                                val intent = Intent(this, EstadisticasFiltradasActivity::class.java)
                                intent.putExtra("PACIENTE_ID", dniPaciente)
                                intent.putExtra("DNI_PACIENTE", dniPaciente) // Si también lo necesitas
                                startActivity(intent)
                                encontrado = true
                                break
                            }
                        }
                    }

                    if (!encontrado) {
                        Toast.makeText(this, "No se encontró paciente asignado", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al buscar el cuidador", Toast.LENGTH_SHORT).show()
                }
        }

        btnPruebas.setOnClickListener {
            startActivity(Intent(this, OpcionesPacienteActivity::class.java))
            true
        }

        btnHabitos.setOnClickListener {
            startActivity(Intent(this, Habitos_Activity::class.java))
            true
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
