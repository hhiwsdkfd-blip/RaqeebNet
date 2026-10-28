package com.example.raqeeb.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.TrafficStats
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import com.example.raqeeb.data.model.ConnectionMedium
import com.example.raqeeb.data.model.NetworkStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.Inet4Address
import java.net.NetworkInterface

class NetworkMonitorService(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

    private val _networkStatus = MutableStateFlow(NetworkStatus())
    val networkStatus: StateFlow<NetworkStatus> = _networkStatus.asStateFlow()

    private var speedMeasurementJob: Job? = null
    private var lastRxBytes = TrafficStats.getTotalRxBytes().coerceAtLeast(0)
    private var lastTxBytes = TrafficStats.getTotalTxBytes().coerceAtLeast(0)
    private var lastSampleTime = System.currentTimeMillis()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            refreshNetworkStatus()
        }

        override fun onLost(network: Network) {
            refreshNetworkStatus()
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            refreshNetworkStatus()
        }
    }

    fun startMonitoring() {
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager.registerNetworkCallback(request, networkCallback)
        } catch (e: Exception) {
            // Fallback for restricted environments
        }

        // Periodic ticker for bandwidth speed & total traffic
        speedMeasurementJob?.cancel()
        speedMeasurementJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                measureTrafficDelta()
                delay(2000)
            }
        }
        refreshNetworkStatus()
    }

    fun stopMonitoring() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (_: Exception) {}
        speedMeasurementJob?.cancel()
    }

    fun refreshNetworkStatus() {
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)

        val isConnected = capabilities != null &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

        var medium = ConnectionMedium.NONE
        var networkName = "غير متصل"
        var linkSpeed = 0
        var frequency = 0

        if (capabilities != null) {
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    medium = ConnectionMedium.WIFI
                    networkName = getWifiSsid(capabilities)
                    val wifiInfo = getWifiInfo(capabilities)
                    linkSpeed = wifiInfo?.linkSpeed ?: 0
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        frequency = wifiInfo?.frequency ?: 0
                    }
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    medium = ConnectionMedium.CELLULAR
                    networkName = "شبكة بيانات الجوال"
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    medium = ConnectionMedium.ETHERNET
                    networkName = "إيثرنت سلكي"
                }
            }
        }

        val localIp = getLocalIpAddress()
        val gatewayIp = getGatewayIpAddress()

        val rx = TrafficStats.getTotalRxBytes().coerceAtLeast(0)
        val tx = TrafficStats.getTotalTxBytes().coerceAtLeast(0)

        _networkStatus.value = _networkStatus.value.copy(
            medium = medium,
            isConnected = isConnected,
            networkName = networkName,
            localIpAddress = localIp,
            gatewayIp = gatewayIp,
            linkSpeedMbps = linkSpeed,
            frequencyMhz = frequency,
            totalDeviceRxBytes = rx,
            totalDeviceTxBytes = tx,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
    }

    private fun measureTrafficDelta() {
        val now = System.currentTimeMillis()
        val elapsedSec = ((now - lastSampleTime) / 1000.0).coerceAtLeast(0.5)

        val currentRx = TrafficStats.getTotalRxBytes().coerceAtLeast(0)
        val currentTx = TrafficStats.getTotalTxBytes().coerceAtLeast(0)

        val deltaRx = (currentRx - lastRxBytes).coerceAtLeast(0)
        val deltaTx = (currentTx - lastTxBytes).coerceAtLeast(0)

        val rxSpeed = (deltaRx / elapsedSec).toLong()
        val txSpeed = (deltaTx / elapsedSec).toLong()

        lastRxBytes = currentRx
        lastTxBytes = currentTx
        lastSampleTime = now

        _networkStatus.value = _networkStatus.value.copy(
            totalDeviceRxBytes = currentRx,
            totalDeviceTxBytes = currentTx,
            currentRxSpeedBps = rxSpeed,
            currentTxSpeedBps = txSpeed,
            lastUpdatedTimestamp = now
        )
    }

    private fun getWifiSsid(capabilities: NetworkCapabilities): String {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val wifiInfo = capabilities.transportInfo as? WifiInfo
                val ssid = wifiInfo?.ssid?.replace("\"", "")
                if (!ssid.isNullOrBlank() && ssid != "<unknown ssid>") {
                    return ssid
                }
            }
            @Suppress("DEPRECATION")
            val ssid = wifiManager?.connectionInfo?.ssid?.replace("\"", "")
            if (!ssid.isNullOrBlank() && ssid != "<unknown ssid>") {
                return ssid
            }
        } catch (_: Exception) {}
        return "شبكة Wi-Fi النشطة"
    }

    private fun getWifiInfo(capabilities: NetworkCapabilities): WifiInfo? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                capabilities.transportInfo as? WifiInfo
            } else {
                @Suppress("DEPRECATION")
                wifiManager?.connectionInfo
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun getLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return "127.0.0.1"
            for (intf in interfaces) {
                if (intf.isLoopback || !intf.isUp) continue
                val addrs = intf.inetAddresses
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress ?: "غير متاح"
                    }
                }
            }
        } catch (_: Exception) {}
        return "غير متاح"
    }

    private fun getGatewayIpAddress(): String {
        try {
            val activeNetwork = connectivityManager.activeNetwork ?: return "192.168.1.1"
            val linkProps = connectivityManager.getLinkProperties(activeNetwork) ?: return "192.168.1.1"
            for (route in linkProps.routes) {
                if (route.isDefaultRoute && route.gateway is Inet4Address) {
                    return route.gateway?.hostAddress ?: "192.168.1.1"
                }
            }
        } catch (_: Exception) {}

        // Fallback: local IP with .1
        val local = getLocalIpAddress()
        if (local.contains(".") && !local.startsWith("127.")) {
            val parts = local.split(".")
            if (parts.size == 4) {
                return "${parts[0]}.${parts[1]}.${parts[2]}.1"
            }
        }
        return "192.168.1.1"
    }
}
