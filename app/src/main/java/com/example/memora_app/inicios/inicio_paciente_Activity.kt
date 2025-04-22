package com.example.memora_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.memora_app.configuracionmedico.vertest
import com.example.memora_app.estadisticas.EstadisticasFiltradasActivity
import com.example.memora_app.juegos.ExplicacionComprensionActivity

import com.example.memora_app.juegos.PantallaPreparacionJuegoActivity

import com.example.memora_app.recuerdos.VerRecuerdos
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager

import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


class inicio_paciente_activity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.inicio_paciente)

        Log.d("InicioPaciente", "Actividad iniciada")

        // Crear canal de notificaciones (una sola vez)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "canal_paciente",
                "Notificaciones del paciente",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones sobre nuevas pruebas"
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Obtener token de FCM (opcional)
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }

                val token = task.result
                Log.d("FCM", "Token: $token")
            }

        // Detectar si hay solicitud de prueba activa
        val correoUsuario = FirebaseAuth.getInstance().currentUser?.email

        if (correoUsuario != null) {
            FirebaseFirestore.getInstance().collection("Pacientes").get()
                .addOnSuccessListener { documentos ->
                    for (doc in documentos) {
                        val correo = doc.getString("correo")
                        if (correo == correoUsuario) {
                            val dniPaciente = doc.getString("dni")
                            if (dniPaciente != null) {
                                FirebaseFirestore.getInstance()
                                    .collection("Pacientes")
                                    .document(dniPaciente)
                                    .addSnapshotListener { snapshot, e ->
                                        if (e != null || snapshot == null || !snapshot.exists()) {
                                            Log.w("NotiPaciente", "No se pudo obtener snapshot")
                                            return@addSnapshotListener
                                        }

                                        val pruebaSolicitada = snapshot.getBoolean("pruebaSolicitada") ?: false
                                        if (pruebaSolicitada) {
                                            Log.d("NotiPaciente", "pruebaSolicitada = true → mostrando notificación")

                                            val intent = Intent(this, vertest::class.java).apply {
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            }
                                            val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

                                            val builder = NotificationCompat.Builder(this, "canal_paciente")
                                                .setSmallIcon(R.drawable.pruebas)
                                                .setContentTitle("Nueva prueba disponible")
                                                .setContentText("Tu médico ha solicitado una nueva prueba. ¡Realizala cuando puedas!")
                                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                                .setContentIntent(pendingIntent)
                                                .setAutoCancel(true)

                                            with(NotificationManagerCompat.from(this)) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                    if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                                        requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
                                                    }
                                                }

                                                notify(1001, builder.build())
                                            }
                                        }
                                    }
                            }
                            break
                        }
                    }
                }
                .addOnFailureListener {
                    Log.w("Firestore", "Error obteniendo paciente", it)
                }
        }

        // Toolbar y botones
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val btnRecuerdos = findViewById<LinearLayout>(R.id.recuerdos)
        val btnEstadisticas = findViewById<LinearLayout>(R.id.estadisticas)
        val btnJuegos = findViewById<LinearLayout>(R.id.juegos)
        val btnHabitos = findViewById<LinearLayout>(R.id.habitos)
        val btnPruebas = findViewById<LinearLayout>(R.id.pruebas)

        btnRecuerdos.setOnClickListener {
            startActivity(Intent(this, VerRecuerdos::class.java))
        }

        btnEstadisticas.setOnClickListener {
            val correoUsuario = FirebaseAuth.getInstance().currentUser?.email

            if (correoUsuario == null) {
                Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirebaseFirestore.getInstance().collection("Pacientes").get()
                .addOnSuccessListener { documentos ->
                    var encontrado = false

                    for (doc in documentos) {
                        val correo = doc.getString("correo")
                        if (correo == correoUsuario) {
                            val dniPaciente = doc.getString("dni")
                            val pacienteId = doc.id
                            if (dniPaciente != null) {
                                val intent = Intent(this, EstadisticasFiltradasActivity::class.java)
                                intent.putExtra("PACIENTE_ID", pacienteId)
                                intent.putExtra("DNI_PACIENTE", dniPaciente)
                                startActivity(intent)
                                encontrado = true
                                break
                            }
                        }
                    }

                    if (!encontrado) {
                        Toast.makeText(this, "No se encontró el paciente con tu correo", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al buscar el paciente", Toast.LENGTH_SHORT).show()
                }
        }

        btnJuegos.setOnClickListener {
            val correoUsuario = FirebaseAuth.getInstance().currentUser?.email

            if (correoUsuario == null) {
                Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirebaseFirestore.getInstance().collection("Pacientes").get()
                .addOnSuccessListener { documentos ->
                    var encontrado = false

                    for (doc in documentos) {
                        val correo = doc.getString("correo")
                        if (correo == correoUsuario) {
                            val dniPaciente = doc.getString("dni")
                            if (dniPaciente != null) {
                                val intent = Intent(this, PantallaPreparacionJuegoActivity::class.java)
                                intent.putExtra("pacienteId", dniPaciente)
                                startActivity(intent)
                                encontrado = true
                                break
                            }
                        }
                    }

                    if (!encontrado) {
                        Toast.makeText(this, "No se encontró el paciente con tu correo", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al buscar el paciente", Toast.LENGTH_SHORT).show()
                }
        }

        btnHabitos.setOnClickListener {
            startActivity(Intent(this, Habitos_Activity::class.java))
        }

        btnPruebas.setOnClickListener {
            startActivity(Intent(this, vertest::class.java))
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
