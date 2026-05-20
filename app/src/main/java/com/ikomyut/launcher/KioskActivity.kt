package com.ikomyut.launcher

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.text.TextUtils
import android.view.Gravity
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class KioskActivity : Activity() {

    private lateinit var kioskManager: KioskManager
    private lateinit var appManager: AppManager
    private lateinit var appGrid: GridLayout

    private lateinit var clockTicker: ClockTicker
    private lateinit var batteryMonitor: BatteryMonitor
    private lateinit var networkMonitor: NetworkMonitor
    
    private val exitPassword = "ipick" 
    private val colorPrimary = "#004D25" // Dark Green

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_main)

            kioskManager = KioskManager(this)
            appManager = AppManager(this)
            appGrid = findViewById(R.id.app_grid)

            setupObservers()
            refreshAppGrid()
            
            kioskManager.setKioskLockedState(true)
            kioskManager.lockKiosk(appManager.getExtraApps().toList())
            startLockTask()

        } catch (e: Exception) {
            android.util.Log.e("KioskCrash", "Fatal error in onCreate", e)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        kioskManager.setKioskLockedState(true)
        kioskManager.lockKiosk(appManager.getExtraApps().toList())
        startLockTask()
    }

    private fun setupObservers() {
        val tvClock = findViewById<TextView>(R.id.tv_clock)
        val tvDate = findViewById<TextView>(R.id.tv_date)
        clockTicker = ClockTicker { time, date ->
            tvClock?.text = time
            tvDate?.text = date
        }

        val tvBattery = findViewById<TextView>(R.id.tv_battery)
        batteryMonitor = BatteryMonitor(this) { pct ->
            tvBattery?.text = "$pct%"
        }

        val tvWifi = findViewById<TextView>(R.id.tv_wifi)
        val tvData = findViewById<TextView>(R.id.tv_data)
        networkMonitor = NetworkMonitor(this) { isWifi, isData ->
            tvWifi?.text = if (isWifi) "WiFi: ON" else "WiFi: OFF"
            tvWifi?.alpha = if (isWifi) 1.0f else 0.5f
            
            tvData?.text = if (isData) "Data: ON" else "Data: OFF"
            tvData?.alpha = if (isData) 1.0f else 0.5f
        }
    }

    override fun onStart() {
        super.onStart()
        clockTicker.start()
        batteryMonitor.start()
        networkMonitor.start()
    }

    override fun onStop() {
        super.onStop()
        clockTicker.stop()
        batteryMonitor.stop()
        networkMonitor.stop()
    }

    private fun refreshAppGrid() {
        appGrid.removeAllViews()
        
        addSettingsCard()

        val extraApps = appManager.getExtraApps()
        for (pkg in extraApps) {
            addAppCard(appManager.getAppLabel(pkg), pkg, appManager.getAppIcon(pkg))
        }
    }

    private fun addSettingsCard() {
        val displayMetrics = resources.displayMetrics
        val itemWidth = (displayMetrics.widthPixels - dpToPx(32)) / 4

        val container = LinearLayout(this).apply {
            layoutParams = GridLayout.LayoutParams().apply {
                width = itemWidth
                height = dpToPx(100)
                setMargins(dpToPx(4), dpToPx(8), dpToPx(4), dpToPx(8))
            }
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setOnClickListener { showPasswordPrompt { showAdminMenu() } }
        }

        val iconView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(dpToPx(50), dpToPx(50))
            setImageResource(R.drawable.ic_settings)
            scaleType = ImageView.ScaleType.FIT_CENTER
            setColorFilter(android.graphics.Color.WHITE)
        }

        val nameView = TextView(this).apply {
            text = "Settings"
            setTextColor(android.graphics.Color.WHITE)
            textSize = 11f
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            gravity = Gravity.CENTER
            setPadding(0, dpToPx(4), 0, 0)
        }

        container.addView(iconView)
        container.addView(nameView)
        appGrid.addView(container)
    }

    private fun addAppCard(name: String, pkg: String, icon: Drawable?) {
        val displayMetrics = resources.displayMetrics
        val itemWidth = (displayMetrics.widthPixels - dpToPx(32)) / 4

        val container = LinearLayout(this).apply {
            layoutParams = GridLayout.LayoutParams().apply {
                width = itemWidth
                height = dpToPx(100)
                setMargins(dpToPx(4), dpToPx(8), dpToPx(4), dpToPx(8))
            }
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setOnClickListener { launchApp(pkg) }
        }

        val iconView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(dpToPx(50), dpToPx(50))
            if (icon != null) setImageDrawable(icon) else setImageResource(android.R.drawable.sym_def_app_icon)
            scaleType = ImageView.ScaleType.FIT_CENTER
        }

        val nameView = TextView(this).apply {
            text = name
            setTextColor(android.graphics.Color.WHITE)
            textSize = 11f
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            gravity = Gravity.CENTER
            setPadding(0, dpToPx(4), 0, 0)
        }

        container.addView(iconView)
        container.addView(nameView)
        appGrid.addView(container)
    }

    private fun launchApp(packageName: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) startActivity(intent)
        else Toast.makeText(this, "App not found", Toast.LENGTH_SHORT).show()
    }

    private fun showPasswordPrompt(onSuccess: () -> Unit) {
        val input = EditText(this).apply { 
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD 
        }
        AlertDialog.Builder(this).setTitle("Admin Access").setView(input)
            .setPositiveButton("Verify") { _, _ ->
                if (input.text.toString() == exitPassword) onSuccess()
                else Toast.makeText(this, "Wrong Password", Toast.LENGTH_SHORT).show()
            }.setNegativeButton("Cancel", null).show()
    }

    private fun showAdminMenu() {
        val options = arrayOf(
            "Add Application", 
            "Remove Application", 
            "Unlock Kiosk Mode"
        )
        AlertDialog.Builder(this)
            .setTitle("Admin Settings")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showAddAppDialog()
                    1 -> showRemoveAppDialog()
                    2 -> {
                        kioskManager.unlockKiosk()
                        stopLockTask()
                        Toast.makeText(this, "Kiosk Unlocked", Toast.LENGTH_SHORT).show()
                        
                        Handler(Looper.getMainLooper()).postDelayed({
                            val intent = Intent(Intent.ACTION_MAIN).apply {
                                addCategory(Intent.CATEGORY_HOME)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            startActivity(intent)
                            finishAffinity()
                        }, 500)
                    }
                }
            }
            .show()
    }

    private fun showAddAppDialog() {
        val apps = appManager.getInstalledApps()
        val appNames = apps.map { packageManager.getApplicationLabel(it).toString() }.toTypedArray()
        AlertDialog.Builder(this).setTitle("Select App").setItems(appNames) { _, which ->
            appManager.addApp(apps[which].packageName)
            refreshAppGrid()
            kioskManager.lockKiosk(appManager.getExtraApps().toList())
        }.show()
    }

    private fun showRemoveAppDialog() {
        val apps = appManager.getExtraApps().toList()
        val appNames = apps.map { appManager.getAppLabel(it) }.toTypedArray()
        AlertDialog.Builder(this).setTitle("Remove App").setItems(appNames) { _, which ->
            appManager.removeApp(apps[which])
            refreshAppGrid()
            kioskManager.lockKiosk(appManager.getExtraApps().toList())
        }.show()
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()
}
