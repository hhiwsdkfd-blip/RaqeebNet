package com.example.raqeeb.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "network_devices")
data class NetworkDeviceEntity(
    @PrimaryKey
    val ipAddress: String,
    val macAddress: String = "غير متاح (قيود النظام)",
    val hostname: String = "",
    val vendor: String = "جهاز غير محدد",
    val customName: String = "",
    val deviceType: String = "GENERIC",   // ROUTER, PHONE, LAPTOP, TV, IOT, GENERIC
    val isOnline: Boolean = true,
    val pingMs: Long = -1,
    val openPorts: String = "",           // e.g. "80,443"
    val dataUsageMb: Double = 0.0,
    val firstSeenAt: Long = System.currentTimeMillis(),
    val lastSeenAt: Long = System.currentTimeMillis()
)
