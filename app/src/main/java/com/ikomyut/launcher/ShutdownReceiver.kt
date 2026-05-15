package com.ikomyut.launcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ShutdownReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_SHUTDOWN == intent.action || "android.intent.action.REBOOT" == intent.action) {
            val kioskManager = KioskManager(context)
            if (kioskManager.isDeviceOwner()) {
                // Clear the default launcher so the built-in one shows on next boot
                // But do NOT save the unlocked state permanently
                kioskManager.unlockKiosk(persist = false)
            }
        }
    }
}
