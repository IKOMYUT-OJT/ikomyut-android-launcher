package com.ikomyut.launcher

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper

/**
 * Service that waits for 10 seconds after boot before launching the Kiosk Activity.
 * This allows the default system launcher to be visible briefly as requested.
 */
class BootService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Handler(Looper.getMainLooper()).postDelayed({
            val launchIntent = Intent(this, KioskActivity::class.java)
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(launchIntent)
            stopSelf()
        }, 10000) // 10 seconds delay
        
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
