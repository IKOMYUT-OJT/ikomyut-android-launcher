package com.example.poskiosk

import android.app.Activity
import android.app.AlertDialog
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import java.text.SimpleDateFormat
import java.util.*

class KioskActivity : Activity() {

    private lateinit var mDpm: DevicePolicyManager
    private lateinit var prefs: SharedPreferences
    private lateinit var appGrid: GridLayout
    
    private val EXIT_PASSWORD = "ipick" 
    private val PREFS_NAME = "KioskPrefs"
    private val EXTRA_APPS_KEY = "extra_apps"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_main)

            mDpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            appGrid = findViewById(R.id.app_grid)

            // Settings Button
            findViewById<ImageButton>(R.id.btn_settings)?.setOnClickListener { 
                showPasswordPrompt { showAdminMenu() }
            }

            startClock()
            refreshAppGrid()
            setupKioskMode()
        } catch (e: Exception) {
            android.util.Log.e("KioskCrash", "Fatal error in onCreate", e)
            Toast.makeText(this, "CRASH: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun startClock() {
        try {
            val tvClock = findViewById<TextView>(R.id.tv_clock) ?: return
            val handler = Handler(Looper.getMainLooper())
            val runnable = object : Runnable {
                override fun run() {
                    val sdf = SimpleDateFormat("EEE, MMM d - hh:mm a", Locale.getDefault())
                    tvClock.text = sdf.format(Date())
                    handler.postDelayed(this, 10000)
                }
            }
            handler.post(runnable)
        } catch (e: Exception) {}
    }

    private fun getExtraApps(): MutableSet<String> {
        return prefs.getStringSet(EXTRA_APPS_KEY, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
    }

    private fun refreshAppGrid() {
        try {
            appGrid.removeAllViews()
            
            // Add Default Apps
            addAppCard("POS SYSTEM", "com.your.pos.app", R.drawable.ic_pos, "#1E293B")
            addAppCard("PRINTER", "com.telpo.printer", R.drawable.ic_printer, "#1E293B")
            
            // Add Extra Apps
            val extraApps = getExtraApps()
            val pm = packageManager
            for (pkg in extraApps) {
                try {
                    val appInfo = pm.getApplicationInfo(pkg, 0)
                    val label = pm.getApplicationLabel(appInfo).toString()
                    addAppCard(label, pkg, null, "#1E293B") 
                } catch (e: Exception) {}
            }
        } catch (e: Exception) {}
    }

    private fun addAppCard(name: String, pkg: String, iconRes: Int?, color: String) {
        val card = CardView(this).apply {
            layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = dpToPx(180)
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
            }
            radius = dpToPx(16).toFloat()
            elevation = dpToPx(4).toFloat()
            setCardBackgroundColor(android.graphics.Color.parseColor(color))
            isClickable = true
            isFocusable = true
            
            // Safe way to get ripple effect
            val outValue = TypedValue()
            theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            foreground = getDrawable(outValue.resourceId)
            
            setOnClickListener { launchApp(pkg) }
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
        }

        val iconView = if (iconRes != null) {
            ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(dpToPx(48), dpToPx(48))
                setImageResource(iconRes)
            }
        } else {
            TextView(this).apply {
                text = "📱"
                textSize = 32f
            }
        }

        val nameView = TextView(this).apply {
            text = name
            setTextColor(android.graphics.Color.WHITE)
            textSize = 16f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(0, dpToPx(12), 0, 0)
        }

        layout.addView(iconView)
        layout.addView(nameView)
        card.addView(layout)
        appGrid.addView(card)
    }

    private fun launchApp(packageName: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "App not found: $packageName", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPasswordPrompt(onSuccess: () -> Unit) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Admin Access")
        val input = EditText(this)
        input.hint = "Enter Password"
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        builder.setView(input)

        builder.setPositiveButton("Verify") { _, _ ->
            if (input.text.toString() == EXIT_PASSWORD) {
                onSuccess()
            } else {
                Toast.makeText(this, "Wrong Password", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun showAdminMenu() {
        val options = arrayOf("Add Application", "Exit Kiosk Mode")
        AlertDialog.Builder(this)
            .setTitle("Admin Settings")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showAddAppDialog()
                    1 -> showPasswordPrompt { exitKiosk() }
                }
            }
            .show()
    }

    private fun showAddAppDialog() {
        val pm = packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val filteredApps = apps.filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
        
        val appNames = filteredApps.map { pm.getApplicationLabel(it).toString() }.toTypedArray()
        val appPackages = filteredApps.map { it.packageName }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Select App to Add")
            .setItems(appNames) { _, which ->
                val pkg = appPackages[which]
                val extraApps = getExtraApps()
                extraApps.add(pkg)
                prefs.edit().putStringSet(EXTRA_APPS_KEY, extraApps).apply()
                
                refreshAppGrid()
                setupKioskMode() 
                Toast.makeText(this, "App Added", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun exitKiosk() {
        try {
            stopLockTask()
            val admin = ComponentName("com.example.poskiosk", "com.example.poskiosk.DeviceAdminReceiver")
            mDpm.setStatusBarDisabled(admin, false)
            mDpm.setKeyguardDisabled(admin, false)
            finish()
        } catch (e: Exception) {
            finish()
        }
    }

    private fun setupKioskMode() {
        try {
            val admin = ComponentName("com.example.poskiosk", "com.example.poskiosk.DeviceAdminReceiver")
            if (mDpm.isDeviceOwnerApp(packageName)) {
                val whitelist = mutableListOf(packageName, "com.your.pos.app", "com.telpo.printer")
                whitelist.addAll(getExtraApps())
                
                mDpm.setLockTaskPackages(admin, whitelist.toTypedArray())
                mDpm.setKeyguardDisabled(admin, true)
                mDpm.setStatusBarDisabled(admin, true)
                
                // Force this app to be the default Home/Launcher automatically
                val filter = android.content.IntentFilter(Intent.ACTION_MAIN)
                filter.addCategory(Intent.CATEGORY_HOME)
                filter.addCategory(Intent.CATEGORY_DEFAULT)
                mDpm.addPersistentPreferredActivity(admin, filter, ComponentName(packageName, KioskActivity::class.java.name))

                // Force hide the system launcher so it doesn't show in the 'Complete action using' list
                try {
                    mDpm.setApplicationHidden(admin, "com.android.launcher3", true)
                    mDpm.setApplicationHidden(admin, "com.android.launcher", true)
                } catch (e: Exception) { }

                // 3. Start Lockdown
                startLockTask()
            } else {
                // If not owner, try to at least request Admin status
                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin)
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Activate Admin to enable Kiosk Mode")
                startActivity(intent)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Sync Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }
}
