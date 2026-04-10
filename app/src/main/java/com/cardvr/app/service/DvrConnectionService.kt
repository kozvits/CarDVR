package com.cardvr.app.service

import android.app.*
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.cardvr.app.MainActivity
import com.cardvr.app.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DvrConnectionService : Service() {

    companion object {
        const val CHANNEL_ID = "dvr_connection"
        const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun buildNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("CarDVR")
            .setContentText("Подключено к регистратору")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentIntent(pi)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Подключение к регистратору",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Активное подключение к DVR"
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
