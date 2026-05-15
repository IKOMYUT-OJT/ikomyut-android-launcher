package com.ikomyut.launcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Listens for system shutdown/reboot to clear the default launcher preference.
 * This ensures that on the next boot, the system default launcher is shown 
 * until the BootService re-enables the Kiosk mode after 10 seconds.
 */
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
