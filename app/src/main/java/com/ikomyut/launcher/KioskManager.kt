package com.ikomyut.launcher

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.UserManager
import android.widget.Toast

class KioskManager(private val context: Context) {
    private val mDpm: DevicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val adminName: ComponentName = ComponentName(context, DeviceAdminReceiver::class.java)
    private val prefs = context.getSharedPreferences("KioskPrefs", Context.MODE_PRIVATE)

    fun isDeviceOwner(): Boolean {
        return mDpm.isDeviceOwnerApp(context.packageName)
    }

    fun isLocked(): Boolean {
        return prefs.getBoolean("kiosk_locked", true) // Locked by default
    }

    fun setKioskLockedState(locked: Boolean) {
        prefs.edit().putBoolean("kiosk_locked", locked).apply()
    }

    fun lockKiosk(whitelist: List<String>) {
        try {
            if (isDeviceOwner()) {
                // 1. Enable Kiosk Launcher Alias
                val aliasName = ComponentName(context, "${context.packageName}.KioskLauncherAlias")
                context.packageManager.setComponentEnabledSetting(
                    aliasName,
                    android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    android.content.pm.PackageManager.DONT_KILL_APP
                )

                // 2. Set Lock Task Packages
                val fullWhitelist = whitelist.toMutableList()
                fullWhitelist.add(context.packageName)
                fullWhitelist.add("com.android.settings")
                fullWhitelist.add("com.telpo.printer")
                mDpm.setLockTaskPackages(adminName, fullWhitelist.toTypedArray())

                // 3. Disable Keyguard and Status Bar
                mDpm.setKeyguardDisabled(adminName, true)
                mDpm.setStatusBarDisabled(adminName, true)

                // 4. Set as Default Launcher (Persistent Preferred Activity)
                val filter = IntentFilter(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    addCategory(Intent.CATEGORY_DEFAULT)
                }
                mDpm.addPersistentPreferredActivity(adminName, filter, aliasName)

                // 5. Configure Lock Task Features
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    mDpm.setLockTaskFeatures(adminName, DevicePolicyManager.LOCK_TASK_FEATURE_NONE)
                }

                // 6. Restrictions
                mDpm.addUserRestriction(adminName, UserManager.DISALLOW_SAFE_BOOT)
                mDpm.addUserRestriction(adminName, UserManager.DISALLOW_FACTORY_RESET)
                
                setKioskLockedState(true)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Lock Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun unlockKiosk(persist: Boolean = true) {
        try {
            if (isDeviceOwner()) {
                // 1. Disable Kiosk Launcher Alias
                val aliasName = ComponentName(context, "${context.packageName}.KioskLauncherAlias")
                context.packageManager.setComponentEnabledSetting(
                    aliasName,
                    android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    android.content.pm.PackageManager.DONT_KILL_APP
                )

                // 2. Clear Default Launcher Preference
                mDpm.clearPackagePersistentPreferredActivities(adminName, context.packageName)

                // 3. Restore System UI
                mDpm.setStatusBarDisabled(adminName, false)
                mDpm.setKeyguardDisabled(adminName, false)

                // 4. Remove Restrictions
                mDpm.clearUserRestriction(adminName, UserManager.DISALLOW_SAFE_BOOT)
                
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
