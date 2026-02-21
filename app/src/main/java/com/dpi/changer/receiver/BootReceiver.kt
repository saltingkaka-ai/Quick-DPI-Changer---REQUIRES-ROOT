package com.dpi.changer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.dpi.changer.service.DPIService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Restart service on boot
            val serviceIntent = Intent(context, DPIService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
        }
    }
}