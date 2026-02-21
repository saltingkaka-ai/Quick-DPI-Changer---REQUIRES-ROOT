package com.dpi.changer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.dpi.changer.MainActivity
import com.dpi.changer.R
import com.dpi.changer.util.RootUtil

class DPIService : Service() {

    companion object {
        const val CHANNEL_ID = "dpi_service_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_SET_DPI = "com.dpi.changer.SET_DPI"
        const val EXTRA_DPI = "extra_dpi"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SET_DPI -> {
                val dpi = intent.getIntExtra(EXTRA_DPI, 400)
                handleSetDPI(dpi)
            }
        }
        
        startForeground(NOTIFICATION_ID, buildNotification())
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "DPI Changer Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Persistent DPI control notification"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val notificationLayout = RemoteViews(packageName, R.layout.notification_dpi_control)
        
        // Build notification with custom layout
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dpi)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(notificationLayout)
            .setCustomBigContentView(notificationLayout)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()
    }

    private fun handleSetDPI(dpi: Int) {
        if (dpi > 1000) {
            // Send broadcast to show warning
            sendBroadcast(Intent("com.dpi.changer.SHOW_WARNING").apply {
                putExtra("dpi", dpi)
            })
            return
        }
        RootUtil.setDPI(dpi)
    }
}