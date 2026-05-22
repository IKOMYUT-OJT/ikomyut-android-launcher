package com.ikomyut.launcher

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.UserManager
import android.widget.Toast

/**
 * Manages device policy administration and lockdown settings.
 * Relies on Device Owner status to restrict hardware and OS navigation.
 */
class KioskManager(private val context: Context) {
    private val mDpm: DevicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val adminName: ComponentName = ComponentName(context, DeviceAdminReceiver::class.java)
    private val prefs = context.getSharedPreferences("KioskPrefs", Context.MODE_PRIVATE)

    fun isDeviceOwner(): Boolean {
        return mDpm.isDeviceOwnerApp(context.packageName)
    }

    fun isLocked(): Boolean {
        return prefs.getBoolean("kiosk_locked", true)
    }

    fun setKioskLockedState(locked: Boolean) {
        prefs.edit().putBoolean("kiosk_locked", locked).apply()
    }

    /**
     * Activates kiosk locking. Disables keyguard, status bar, and sets launcher activity as persistent default.
     */
    fun lockKiosk(whitelist: List<String>) {
        try {
            if (isDeviceOwner()) {
                val aliasName = ComponentName(context, "${context.packageName}.KioskLauncherAlias")
                
                // Enable launcher activity alias
                context.packageManager.setComponentEnabledSetting(
                    aliasName,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )

                // Build lock task packages whitelist
                val fullWhitelist = whitelist.toMutableList().apply {
                    add(context.packageName)
                    add("com.android.settings")
                    add("com.telpo.printer")
                    add("com.iposprinter.iposprinterservice")
                    add("woyou.aidlservice.jiuiv5")
                    add("com.sunmi.printerhelper")
                    add("com.android.printspooler")
                    add("com.android.bips")
                }
                mDpm.setLockTaskPackages(adminName, fullWhitelist.toTypedArray())

                // Restrict system UI interactions
                mDpm.setKeyguardDisabled(adminName, true)
                mDpm.setStatusBarDisabled(adminName, true)

                // Add launcher as persistent default home
                val filter = IntentFilter(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    addCategory(Intent.CATEGORY_DEFAULT)
                }
                mDpm.addPersistentPreferredActivity(adminName, filter, aliasName)

                // Disable all system features during lock task mode on modern Android
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    mDpm.setLockTaskFeatures(adminName, DevicePolicyManager.LOCK_TASK_FEATURE_NONE)
                }

                // Prevent safety reboots & resets
                mDpm.addUserRestriction(adminName, UserManager.DISALLOW_SAFE_BOOT)
                mDpm.addUserRestriction(adminName, UserManager.DISALLOW_FACTORY_RESET)
                
                setKioskLockedState(true)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Lock Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Unlocks kiosk mode, reenabling system navigation bar and removing the persistent launcher alias.
     */
    fun unlockKiosk(persist: Boolean = true) {
        try {
            if (isDeviceOwner()) {
                val aliasName = ComponentName(context, "${context.packageName}.KioskLauncherAlias")
                
                // Disable launcher activity alias so system goes back to stock launcher
                context.packageManager.setComponentEnabledSetting(
                    aliasName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )

                mDpm.clearPackagePersistentPreferredActivities(adminName, context.packageName)
                mDpm.setStatusBarDisabled(adminName, false)
                mDpm.setKeyguardDisabled(adminName, false)
                mDpm.clearUserRestriction(adminName, UserManager.DISALLOW_SAFE_BOOT)
                
                // Clear the whitelisted lock task packages so other apps use standard screen pinning
                mDpm.setLockTaskPackages(adminName, arrayOf(context.packageName))
                
                if (persist) {
                    setKioskLockedState(false)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("KioskManager", "Unlock Error", e)
        }
    }

    fun getAdminComponent(): ComponentName = adminName
}
