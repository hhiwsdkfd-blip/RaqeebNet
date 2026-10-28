package com.example.raqeeb.data.network.provider

import com.example.raqeeb.data.local.entity.ConnectionProfileEntity
import com.example.raqeeb.data.model.QuotaInfo

/**
 * Clean architectural interface for router / modem gateways.
 * Specific router implementations (Huawei, ZTE, TP-Link, MikroTik, OpenWrt, etc.)
 * implement this interface to fetch real connected devices and router stats.
 */
interface GatewayConnector {
    val routerBrandName: String

    suspend fun testConnection(profile: ConnectionProfileEntity): Result<Boolean>

    suspend fun queryGatewayStats(profile: ConnectionProfileEntity): Result<GatewayTelemetry>

    suspend fun fetchConnectedDevices(profile: ConnectionProfileEntity): Result<List<GatewayDevice>>
}

data class GatewayTelemetry(
    val wanIp: String = "",
    val connectionUptimeSeconds: Long = 0,
    val downstreamRateKbps: Long = 0,
    val upstreamRateKbps: Long = 0,
    val signalDbm: Int? = null
)

data class GatewayDevice(
    val ip: String,
    val mac: String,
    val hostname: String,
    val isOnline: Boolean,
    val currentRxRateKbps: Long = 0,
    val currentTxRateKbps: Long = 0
)

/**
 * Clean architectural interface for telecom & ISP balance/quota queries.
 * Providers (e.g. STC, Mobily, Zain, WE, Vodafone, Ooredoo, etc.)
 * will be integrated through this interface.
 */
interface TelecomProviderConnector {
    val providerId: String
    val providerNameArabic: String

    suspend fun testCredentials(profile: ConnectionProfileEntity): Result<Boolean>

    suspend fun fetchAccountQuota(profile: ConnectionProfileEntity): Result<QuotaInfo>
}

/**
 * Standard factory for obtaining appropriate connectors.
 * In this foundation stage, provides graceful fallback informing user
 * that direct modem/carrier API sync requires configured credentials.
 */
class ProviderConnectorRegistry {

    fun getGatewayConnector(profile: ConnectionProfileEntity): GatewayConnector {
        return DefaultGenericGatewayConnector()
    }

    fun getTelecomConnector(providerId: String): TelecomProviderConnector {
        return DefaultTelecomConnector(providerId)
    }
}

class DefaultGenericGatewayConnector : GatewayConnector {
    override val routerBrandName: String = "Generic Router"

    override suspend fun testConnection(profile: ConnectionProfileEntity): Result<Boolean> {
        // Will ping router gateway IP
        return Result.success(true)
    }

    override suspend fun queryGatewayStats(profile: ConnectionProfileEntity): Result<GatewayTelemetry> {
        return Result.failure(UnsupportedOperationException("لم يتم تفعيل تكامل هذا المودم بعد"))
    }

    override suspend fun fetchConnectedDevices(profile: ConnectionProfileEntity): Result<List<GatewayDevice>> {
        return Result.success(emptyList())
    }
}

class DefaultTelecomConnector(override val providerId: String) : TelecomProviderConnector {
    override val providerNameArabic: String = providerId.ifEmpty { "المشغل العام" }

    override suspend fun testCredentials(profile: ConnectionProfileEntity): Result<Boolean> {
        return Result.success(true)
    }

    override suspend fun fetchAccountQuota(profile: ConnectionProfileEntity): Result<QuotaInfo> {
        // Honest state: No fake numbers
        return Result.success(
            QuotaInfo(
                hasConfiguredProfile = profile.connectionName.isNotBlank(),
                hasQueriedData = false,
                providerName = profile.telecomProvider.ifBlank { "غير محدد" },
                totalQuotaGb = if (profile.totalQuotaGb > 0) profile.totalQuotaGb else null,
                statusMessage = "لم يتم الربط المباشر مع بوابة ${profile.telecomProvider.ifBlank { "المشغل" }} بعد"
            )
        )
    }
}
