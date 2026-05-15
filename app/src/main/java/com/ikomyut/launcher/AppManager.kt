package com.ikomyut.launcher

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

class AppManager(private val context: Context) {
    private val PREFS_NAME = "KioskPrefs"
    private val EXTRA_APPS_KEY = "extra_apps"
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val pm: PackageManager = context.packageManager

    fun getExtraApps(): MutableSet<String> {
        return prefs.getStringSet(EXTRA_APPS_KEY, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
    }

    fun addApp(packageName: String) {
        val apps = getExtraApps()
        apps.add(packageName)
        prefs.edit().putStringSet(EXTRA_APPS_KEY, apps).apply()
    }

    fun removeApp(packageName: String) {
        val apps = getExtraApps()
        apps.remove(packageName)
        prefs.edit().putStringSet(EXTRA_APPS_KEY, apps).apply()
    }

    fun getAppLabel(packageName: String): String {
        return try {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    fun getAppIcon(packageName: String): Drawable? {
        return try {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationIcon(appInfo)
        } catch (e: Exception) {
            null
        }
    }

    fun getInstalledApps(): List<ApplicationInfo> {
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        return apps.filter { appInfo ->
            pm.getLaunchIntentForPackage(appInfo.packageName) != null && 
            appInfo.packageName != context.packageName
        }.sortedBy { pm.getApplicationLabel(it).toString().lowercase() }
    }
}
