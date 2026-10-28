package com.example.raqeeb.data.network

import com.example.raqeeb.data.local.entity.NetworkDeviceEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.FileReader
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.Locale

data class ScanProgress(
    val isScanning: Boolean,
    val percent: Int,
    val currentIp: String,
    val discoveredDevices: List<NetworkDeviceEntity>
)

class SubnetScanner {

    private val commonPorts = intArrayOf(80, 443, 53, 8080, 22, 445, 139)

    fun scanSubnet(
        localIp: String,
        gatewayIp: String
    ): Flow<ScanProgress> = flow {
        if (!localIp.contains(".") || localIp.startsWith("127.")) {
            emit(ScanProgress(isScanning = false, percent = 0, currentIp = "", discoveredDevices = emptyList()))
            return@flow
        }

        val parts = localIp.split(".")
        if (parts.size != 4) {
            emit(ScanProgress(isScanning = false, percent = 0, currentIp = "", discoveredDevices = emptyList()))
            return@flow
        }

        val subnetPrefix = "${parts[0]}.${parts[1]}.${parts[2]}."
        val foundDevices = mutableListOf<NetworkDeviceEntity>()
        val arpMap = readArpTable()

        emit(ScanProgress(isScanning = true, percent = 0, currentIp = "${subnetPrefix}1", discoveredDevices = emptyList()))

        // Process in chunks of 25 IPs to prevent socket exhaustion and throttle gracefully
        val totalHosts = 254
        val chunkSize = 25

        for (chunkStart in 1..totalHosts step chunkSize) {
            val chunkEnd = (chunkStart + chunkSize - 1).coerceAtMost(totalHosts)
            val chunkIps = (chunkStart..chunkEnd).map { "$subnetPrefix$it" }

            val scannedResults = withContext(Dispatchers.IO) {
                chunkIps.map { ip ->
                    async {
                        checkHost(ip, gatewayIp, localIp, arpMap[ip])
                    }
                }.awaitAll()
            }

            scannedResults.filterNotNull().forEach { device ->
                if (foundDevices.none { it.ipAddress == device.ipAddress }) {
                    foundDevices.add(device)
                }
            }

            val currentPercent = ((chunkEnd.toFloat() / totalHosts) * 100).toInt()
            emit(
                ScanProgress(
                    isScanning = chunkEnd < totalHosts,
                    percent = currentPercent,
                    currentIp = "$subnetPrefix$chunkEnd",
                    discoveredDevices = foundDevices.toList()
                )
            )
        }
    }

    private suspend fun checkHost(
        ip: String,
        gatewayIp: String,
        localIp: String,
        arpMac: String?
    ): NetworkDeviceEntity? = withContext(Dispatchers.IO) {
        val isLocalPhone = (ip == localIp)
        val isGateway = (ip == gatewayIp || ip.endsWith(".1"))

        var reachable = false
        var pingMs = -1L
        val openPorts = mutableListOf<Int>()

        if (isLocalPhone) {
            reachable = true
            pingMs = 1
        } else {
            val startTime = System.currentTimeMillis()
            // Quick socket connection check on common ports
            for (port in commonPorts) {
                if (isPortOpen(ip, port, timeoutMs = 160)) {
                    reachable = true
                    openPorts.add(port)
                    if (pingMs == -1L) {
                        pingMs = (System.currentTimeMillis() - startTime).coerceAtLeast(1)
                    }
                }
            }

            // Fallback ICMP / echo check
            if (!reachable) {
                try {
                    val address = InetAddress.getByName(ip)
                    if (address.isReachable(150)) {
                        reachable = true
                        pingMs = (System.currentTimeMillis() - startTime).coerceAtLeast(1)
                    }
                } catch (_: Exception) {}
            }
        }

        if (!reachable && !isGateway) {
            return@withContext null
        }

        // Hostname resolution
        var hostname = ""
        try {
            val address = InetAddress.getByName(ip)
            val resolved = address.canonicalHostName
            if (resolved != ip && !resolved.isBlank()) {
                hostname = resolved
            }
        } catch (_: Exception) {}

        // Resolve device type
        val deviceType = when {
            isGateway -> "ROUTER"
            isLocalPhone -> "PHONE"
            hostname.contains("iphone", ignoreCase = true) ||
            hostname.contains("android", ignoreCase = true) ||
            hostname.contains("galaxy", ignoreCase = true) -> "PHONE"
            hostname.contains("macbook", ignoreCase = true) ||
            hostname.contains("laptop", ignoreCase = true) ||
            hostname.contains("desktop", ignoreCase = true) ||
            openPorts.contains(445) || openPorts.contains(139) -> "LAPTOP"
            hostname.contains("tv", ignoreCase = true) ||
            hostname.contains("roku", ignoreCase = true) ||
            hostname.contains("chromecast", ignoreCase = true) -> "TV"
            openPorts.contains(53) -> "ROUTER"
            openPorts.isNotEmpty() -> "IOT"
            else -> "GENERIC"
        }

        val vendor = resolveVendor(hostname, deviceType, isGateway, isLocalPhone)
        val finalMac = arpMac ?: if (isLocalPhone) "هذا الجهاز" else "محمي بسياسة النظام"

        NetworkDeviceEntity(
            ipAddress = ip,
            macAddress = finalMac,
            hostname = hostname,
            vendor = vendor,
            customName = when {
                isLocalPhone -> "هاتفي الحالي (مراقب)"
                isGateway -> "المودم الرئيسي (البوابة)"
                else -> ""
            },
            deviceType = deviceType,
            isOnline = true,
            pingMs = pingMs,
            openPorts = openPorts.joinToString(","),
            dataUsageMb = 0.0, // Specific device usage requires gateway SNMP/API, keeping honest
            firstSeenAt = System.currentTimeMillis(),
            lastSeenAt = System.currentTimeMillis()
        )
    }

    private fun isPortOpen(ip: String, port: Int, timeoutMs: Int): Boolean {
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(ip, port), timeoutMs)
                true
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun resolveVendor(hostname: String, type: String, isGateway: Boolean, isSelf: Boolean): String {
        val h = hostname.lowercase(Locale.ROOT)
        return when {
            isSelf -> "هذا الجهاز"
            isGateway -> "بوابة المودم الرئيسية"
            h.contains("apple") || h.contains("iphone") || h.contains("ipad") || h.contains("mac") -> "Apple Inc."
            h.contains("samsung") || h.contains("galaxy") -> "Samsung Electronics"
            h.contains("huawei") -> "Huawei Technologies"
            h.contains("tplink") || h.contains("tp-link") -> "TP-Link"
            h.contains("zte") -> "ZTE Corporation"
            h.contains("xiaomi") || h.contains("redmi") -> "Xiaomi"
            h.contains("google") || h.contains("nest") -> "Google"
            h.contains("amazon") || h.contains("echo") -> "Amazon"
            type == "TV" -> "شاشة ذكية Smart TV"
            type == "ROUTER" -> "موزع شبكة / راوتر"
            else -> "جهاز شبكة محلي"
        }
    }

    private fun readArpTable(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        try {
            BufferedReader(FileReader("/proc/net/arp")).use { reader ->
                reader.forEachLine { line ->
                    val tokens = line.split("\\s+".toRegex())
                    if (tokens.size >= 4 && tokens[0].contains(".")) {
                        val ip = tokens[0]
                        val mac = tokens[3]
                        if (mac != "00:00:00:00:00:00" && !mac.contains("00:00:00")) {
                            map[ip] = mac.uppercase(Locale.ROOT)
                        }
                    }
                }
            }
        } catch (_: Exception) {}
        return map
    }
}
