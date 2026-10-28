package com.example.raqeeb.data.model

enum class ConnectionMedium {
    WIFI,
    CELLULAR,
    ETHERNET,
    NONE
}

data class NetworkStatus(
    val medium: ConnectionMedium = ConnectionMedium.NONE,
    val isConnected: Boolean = false,
    val networkName: String = "غير متصل",
    val localIpAddress: String = "غير متاح",
    val gatewayIp: String = "192.168.1.1",
    val subnetMask: String = "255.255.255.0",
    val linkSpeedMbps: Int = 0,
    val frequencyMhz: Int = 0,
    val totalDeviceRxBytes: Long = 0,
    val totalDeviceTxBytes: Long = 0,
    val currentRxSpeedBps: Long = 0,
    val currentTxSpeedBps: Long = 0,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
) {
    fun formattedRx(): String = formatBytes(totalDeviceRxBytes)
    fun formattedTx(): String = formatBytes(totalDeviceTxBytes)
    fun formattedRxSpeed(): String = "${formatSpeed(currentRxSpeedBps)}/s"
    fun formattedTxSpeed(): String = "${formatSpeed(currentTxSpeedBps)}/s"

    companion object {
        fun formatBytes(bytes: Long): String {
            if (bytes <= 0) return "0 ميجابايت"
            val kb = bytes / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> String.format(java.util.Locale.US, "%.2f جيجابايت", gb)
                mb >= 1.0 -> String.format(java.util.Locale.US, "%.1f ميجابايت", mb)
                else -> String.format(java.util.Locale.US, "%.0f كيلوبايت", kb)
            }
        }

        fun formatSpeed(bytesPerSec: Long): String {
            if (bytesPerSec <= 0) return "0 ك.ب"
            val kb = bytesPerSec / 1024.0
            val mb = kb / 1024.0
            return when {
                mb >= 1.0 -> String.format(java.util.Locale.US, "%.1f م.ب", mb)
                else -> String.format(java.util.Locale.US, "%.0f ك.ب", kb)
            }
        }
    }
}
