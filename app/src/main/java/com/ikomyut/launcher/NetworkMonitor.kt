package com.ikomyut.launcher

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Handler
import android.os.Looper

class NetworkMonitor(
    private val context: Context,
    private val onNetworkChanged: (isWifi: Boolean, isData: Boolean) -> Unit
) {
    private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val mainHandler = Handler(Looper.getMainLooper())
    
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            triggerUpdate()
        }

        override fun onLost(network: Network) {
            triggerUpdate()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            triggerUpdate()
        }
    }

    fun start() {
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            cm.registerNetworkCallback(request, networkCallback)
            // Trigger initial state
            triggerUpdate()
        } catch (e: Exception) {
            // Fallback for security exceptions or configuration limits
            triggerUpdate()
        }
    }

    fun stop() {
        try {
            cm.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun triggerUpdate() {
        mainHandler.post {
            var isWifi = false
            var isData = false
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    val activeNet = cm.activeNetwork
                    val caps = cm.getNetworkCapabilities(activeNet)
                    if (caps != null) {
                        isWifi = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        isData = caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val info = cm.activeNetworkInfo
                    if (info != null && info.isConnected) {
                        @Suppress("DEPRECATION")
                        isWifi = info.type == ConnectivityManager.TYPE_WIFI
                        @Suppress("DEPRECATION")
                        isData = info.type == ConnectivityManager.TYPE_MOBILE
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
            onNetworkChanged(isWifi, isData)
        }
    }
}
