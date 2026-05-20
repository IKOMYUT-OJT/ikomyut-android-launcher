package com.ikomyut.launcher

import android.os.Handler
import android.os.Looper
import java.text.SimpleDateFormat
import java.util.*

class ClockTicker(
    private val onTick: (time: String, date: String) -> Unit
) {
    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null

    fun start() {
        if (runnable != null) return
        runnable = object : Runnable {
            override fun run() {
                val timeSdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                val dateSdf = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
                onTick(timeSdf.format(Date()), dateSdf.format(Date()))
                handler.postDelayed(this, 10000)
            }
        }
        handler.post(runnable!!)
    }

    fun stop() {
        runnable?.let {
            handler.removeCallbacks(it)
            runnable = null
        }
    }
}
