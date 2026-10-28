package com.example.raqeeb.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.raqeeb.data.model.ConnectionType

@Entity(tableName = "connection_profiles")
data class ConnectionProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val connectionName: String,
    val connectionType: ConnectionType,
    val telecomProvider: String = "",       // e.g. STC, Mobily, Zain, WE, Vodafone, etc.
    val subscriptionNumber: String = "",    // Account or SIM number
    val modemGatewayIp: String = "192.168.1.1",
    val username: String = "",
    val encryptedPassword: String = "",     // AES-GCM encrypted via EncryptedCredentialsManager
    val totalQuotaGb: Double = 0.0,         // Monthly quota in GB (0 if unlimited/unspecified)
    val renewalDayOfMonth: Int = 1,         // Billing cycle renewal day
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastSyncAt: Long? = null
)
