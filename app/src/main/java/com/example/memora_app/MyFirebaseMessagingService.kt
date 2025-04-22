package com.example.memora_app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.memora_app.LoginActivity

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "Mensaje recibido de: ${remoteMessage.from}")

        // Si es un mensaje con notificación
        remoteMessage.notification?.let {
            Log.d("FCM", "Notificación: ${it.title}, ${it.body}")
            mostrarNotificacion(it.title ?: "Título", it.body ?: "Texto")
        }
    }

    private fun mostrarNotificacion(titulo: String, mensaje: String) {
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val canalId = "canal_default"
        val sonido = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificacionBuilder = NotificationCompat.Builder(this, canalId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setAutoCancel(true)
            .setSound(sonido)
            .setContentIntent(pendingIntent)

        val notificacionManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                canalId,
                "Canal por defecto",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificacionManager.createNotificationChannel(canal)
        }

        notificacionManager.notify(0, notificacionBuilder.build())
    }
}
