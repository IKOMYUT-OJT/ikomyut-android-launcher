package com.ikomyut.launcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Listens for system boot event and launches the Kiosk Activity.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            val serviceIntent = Intent(context, BootService::class.java)
            context.startService(serviceIntent)
        }
    }
}
