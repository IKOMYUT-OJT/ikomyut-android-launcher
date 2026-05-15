package com.ikomyut.launcher

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import java.text.SimpleDateFormat
import java.util.*

class KioskActivity : Activity() {

    private lateinit var kioskManager: KioskManager
    private lateinit var appManager: AppManager
    private lateinit var appGrid: GridLayout
    private var batteryReceiver: android.content.BroadcastReceiver? = null
    
    private val exitPassword = "ipick" 
    
    // Theme Colors (Logo based)
    private val colorPrimary = "#004D25" // Dark Green

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_main)

            kioskManager = KioskManager(this)
            appManager = AppManager(this)
            appGrid = findViewById(R.id.app_grid)

            // --- FORCE LOCK ON EVERY START ---
            kioskManager.setKioskLockedState(true)

            startClock()
            startBatteryMonitor()
            startNetworkMonitor()
            refreshAppGrid()
            
            // --- LOCKING LOGIC ---
            if (kioskManager.isLocked()) {
                kioskManager.lockKiosk(appManager.getExtraApps().toList())
                startLockTask()
            }

        } catch (e: Exception) {
            android.util.Log.e("KioskCrash", "Fatal error in onCreate", e)
        }
    }

    private fun startClock() {
        val tvClock = findViewById<TextView>(R.id.tv_clock) ?: return
        val tvDate = findViewById<TextView>(R.id.tv_date) ?: return
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                val timeSdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                val dateSdf = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
                tvClock.text = timeSdf.format(Date())
                tvDate.text = dateSdf.format(Date())
                handler.postDelayed(this, 10000)
            }
        }
        handler.post(runnable)
    }

    private fun startBatteryMonitor() {
        val tvBattery = findViewById<TextView>(R.id.tv_battery) ?: return
        batteryReceiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                if (level != -1 && scale != -1) {
                    val pct = (level * 100 / scale.toFloat()).toInt()
                    tvBattery.text = "$pct%"
                }
            }
        }
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    private fun startNetworkMonitor() {
        val tvWifi = findViewById<TextView>(R.id.tv_wifi)
        val tvData = findViewById<TextView>(R.id.tv_data)
        val handler = Handler(Looper.getMainLooper())
        
        val runnable = object : Runnable {
            override fun run() {
                val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                var isWifi = false
                var isData = false

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    val network = cm.activeNetwork
                    val capabilities = cm.getNetworkCapabilities(network)
                    isWifi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
                    isData = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
                } else {
                    @Suppress("DEPRECATION")
                    val info = cm.activeNetworkInfo
                    @Suppress("DEPRECATION")
                    isWifi = info?.type == ConnectivityManager.TYPE_WIFI
                    @Suppress("DEPRECATION")
                    isData = info?.type == ConnectivityManager.TYPE_MOBILE
                }
                
                tvWifi?.text = if (isWifi) "WiFi: ON" else "WiFi: OFF"
                tvWifi?.alpha = if (isWifi) 1.0f else 0.5f
                
                tvData?.text = if (isData) "Data: ON" else "Data: OFF"
                tvData?.alpha = if (isData) 1.0f else 0.5f
                
                handler.postDelayed(this, 3000)
            }
        }
        handler.post(runnable)
    }

    private fun refreshAppGrid() {
        appGrid.removeAllViews()
        
        // --- ADD SETTINGS AS THE FIRST ITEM ---
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
            // Make settings icon white or colored to stand out on dark background
            setColorFilter(android.graphics.Color.WHITE)
        }

        val nameView = TextView(this).apply {
            text = "Settings"
            setTextColor(android.graphics.Color.WHITE)
            textSize = 11f
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
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
            ellipsize = android.text.TextUtils.TruncateAt.END
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
        val isLocked = kioskManager.isLocked()
        val options = arrayOf(
            "Add Application", 
            "Remove Application", 
            if (isLocked) "Unlock Kiosk Mode" else "Lock Kiosk Mode"
        )
        AlertDialog.Builder(this)
            .setTitle("Admin Settings")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showAddAppDialog()
                    1 -> showRemoveAppDialog()
                    2 -> {
                        if (isLocked) {
                            kioskManager.unlockKiosk()
                            stopLockTask()
                            Toast.makeText(this, "Kiosk Unlocked", Toast.LENGTH_SHORT).show()
                            val intent = Intent(Intent.ACTION_MAIN).apply {
                                addCategory(Intent.CATEGORY_HOME)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            startActivity(intent)
                            finish()
                        } else {
                            kioskManager.lockKiosk(appManager.getExtraApps().toList())
                            startLockTask()
                            Toast.makeText(this, "Kiosk Locked", Toast.LENGTH_SHORT).show()
                        }
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
            if (kioskManager.isLocked()) {
                kioskManager.lockKiosk(appManager.getExtraApps().toList())
            }
        }.show()
    }

    private fun showRemoveAppDialog() {
        val apps = appManager.getExtraApps().toList()
        val appNames = apps.map { appManager.getAppLabel(it) }.toTypedArray()
        AlertDialog.Builder(this).setTitle("Remove App").setItems(appNames) { _, which ->
            appManager.removeApp(apps[which])
            refreshAppGrid()
            if (kioskManager.isLocked()) {
                kioskManager.lockKiosk(appManager.getExtraApps().toList())
            }
        }.show()
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        super.onDestroy()
        batteryReceiver?.let { try { unregisterReceiver(it) } catch (e: Exception) {} }
    }
}
