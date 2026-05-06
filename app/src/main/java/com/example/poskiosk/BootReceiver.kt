package com.example.poskiosk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Listens for system boot event and launches the Kiosk Activity.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            val i = Intent(context, KioskActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(i)
        }
    }
}
