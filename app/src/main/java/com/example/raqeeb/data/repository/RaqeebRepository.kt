package com.example.raqeeb.data.repository

import com.example.raqeeb.data.local.DataStoreManager
import com.example.raqeeb.data.local.EncryptedCredentialsManager
import com.example.raqeeb.data.local.dao.ConnectionProfileDao
import com.example.raqeeb.data.local.dao.NetworkDeviceDao
import com.example.raqeeb.data.local.dao.TrafficSnapshotDao
import com.example.raqeeb.data.local.entity.ConnectionProfileEntity
import com.example.raqeeb.data.local.entity.NetworkDeviceEntity
import com.example.raqeeb.data.local.entity.TrafficSnapshotEntity
import com.example.raqeeb.data.model.NetworkStatus
import com.example.raqeeb.data.model.QuotaInfo
import com.example.raqeeb.data.model.ReportPeriod
import com.example.raqeeb.data.model.TrafficDataPoint
import com.example.raqeeb.data.model.TrafficReport
import com.example.raqeeb.data.network.NetworkMonitorService
import com.example.raqeeb.data.network.ScanProgress
import com.example.raqeeb.data.network.SubnetScanner
import com.example.raqeeb.data.network.provider.ProviderConnectorRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RaqeebRepository(
    private val profileDao: ConnectionProfileDao,
    private val deviceDao: NetworkDeviceDao,
    private val snapshotDao: TrafficSnapshotDao,
    private val monitorService: NetworkMonitorService,
    private val scanner: SubnetScanner,
    private val credentialsManager: EncryptedCredentialsManager,
    private val dataStoreManager: DataStoreManager,
    private val connectorRegistry: ProviderConnectorRegistry,
    private val appScope: CoroutineScope
) {
    val networkStatus: StateFlow<NetworkStatus> = monitorService.networkStatus
    val allProfiles: Flow<List<ConnectionProfileEntity>> = profileDao.getAllProfiles()
    val activeProfile: Flow<ConnectionProfileEntity?> = profileDao.getActiveProfile()
    val allDevices: Flow<List<NetworkDeviceEntity>> = deviceDao.getAllDevices()
    val onlineCount: Flow<Int> = deviceDao.getOnlineCount()

    init {
        monitorService.startMonitoring()
        startPeriodicTrafficSnapshot()
    }

    private fun startPeriodicTrafficSnapshot() {
        appScope.launch(Dispatchers.IO) {
            var lastRecordedRx = 0L
            var lastRecordedTx = 0L

            while (true) {
                kotlinx.coroutines.delay(10_000)
                val currentStatus = monitorService.networkStatus.value
                if (currentStatus.isConnected) {
                    val rxDelta = if (lastRecordedRx > 0) (currentStatus.totalDeviceRxBytes - lastRecordedRx).coerceAtLeast(0) else 0
                    val txDelta = if (lastRecordedTx > 0) (currentStatus.totalDeviceTxBytes - lastRecordedTx).coerceAtLeast(0) else 0

                    lastRecordedRx = currentStatus.totalDeviceRxBytes
                    lastRecordedTx = currentStatus.totalDeviceTxBytes

                    snapshotDao.insertSnapshot(
                        TrafficSnapshotEntity(
                            timestamp = System.currentTimeMillis(),
                            rxBytes = currentStatus.totalDeviceRxBytes,
                            txBytes = currentStatus.totalDeviceTxBytes,
                            rxSpeedBps = currentStatus.currentRxSpeedBps,
                            txSpeedBps = currentStatus.currentTxSpeedBps,
                            networkType = currentStatus.medium.name
                        )
                    )
                }
            }
        }
    }

    fun startSubnetScan(): Flow<ScanProgress> = flow {
        val current = networkStatus.value
        scanner.scanSubnet(current.localIpAddress, current.gatewayIp).collect { progress ->
            emit(progress)
            if (!progress.isScanning && progress.discoveredDevices.isNotEmpty()) {
                // Save discovered devices to database
                deviceDao.insertOrUpdateAll(progress.discoveredDevices)
                dataStoreManager.setLastScanTimestamp(System.currentTimeMillis())
            }
        }
    }.flowOn(Dispatchers.IO)

    suspend fun saveProfile(
        name: String,
        type: com.example.raqeeb.data.model.ConnectionType,
        provider: String,
        subscriptionNumber: String,
        gatewayIp: String,
        username: String,
        rawPassword: String,
        totalQuotaGb: Double,
        renewalDay: Int,
        setAsActive: Boolean
    ): Long = withContext(Dispatchers.IO) {
        val encryptedPassword = if (rawPassword.isNotBlank()) {
            credentialsManager.encrypt(rawPassword)
        } else ""

        if (setAsActive) {
            profileDao.clearActiveProfiles()
        }

        val profile = ConnectionProfileEntity(
            connectionName = name,
            connectionType = type,
            telecomProvider = provider,
            subscriptionNumber = subscriptionNumber,
            modemGatewayIp = gatewayIp.ifBlank { "192.168.1.1" },
            username = username,
            encryptedPassword = encryptedPassword,
            totalQuotaGb = totalQuotaGb,
            renewalDayOfMonth = renewalDay,
            isActive = setAsActive,
            createdAt = System.currentTimeMillis()
        )
        profileDao.insertProfile(profile)
    }

    suspend fun setActiveProfile(id: Long) = withContext(Dispatchers.IO) {
        profileDao.clearActiveProfiles()
        profileDao.setActiveProfile(id)
    }

    suspend fun deleteProfile(profile: ConnectionProfileEntity) = withContext(Dispatchers.IO) {
        profileDao.deleteProfile(profile)
    }

    suspend fun updateDeviceDetails(ip: String, customName: String, deviceType: String) =
        withContext(Dispatchers.IO) {
            deviceDao.updateCustomInfo(ip, customName, deviceType)
        }

    fun getQuotaInfo(): Flow<QuotaInfo> = activeProfile.combine(networkStatus) { profile, _ ->
        if (profile == null) {
            QuotaInfo(
                hasConfiguredProfile = false,
                hasQueriedData = false,
                providerName = "لم يتم إعداد اتصال",
                statusMessage = "يرجى إضافة إعدادات الاتصال أو المودم أولاً"
            )
        } else {
            val connector = connectorRegistry.getTelecomConnector(profile.telecomProvider)
            val result = connector.fetchAccountQuota(profile)
            val quota = result.getOrNull() ?: QuotaInfo(
                hasConfiguredProfile = true,
                hasQueriedData = false,
                providerName = profile.telecomProvider,
                statusMessage = "لم يتم جلب البيانات بعد"
            )

            // Calculate billing cycle remaining days if configured
            val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            val daysInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
            val renewalDay = profile.renewalDayOfMonth.coerceIn(1, daysInMonth)
            val remainingDays = if (renewalDay >= today) {
                renewalDay - today
            } else {
                (daysInMonth - today) + renewalDay
            }

            quota.copy(
                hasConfiguredProfile = true,
                providerName = profile.telecomProvider.ifBlank { profile.connectionName },
                totalQuotaGb = if (profile.totalQuotaGb > 0) profile.totalQuotaGb else null,
                daysRemainingInCycle = remainingDays
            )
        }
    }

    fun getTrafficReport(period: ReportPeriod): Flow<TrafficReport> = flow {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        val sinceTimestamp = when (period) {
            ReportPeriod.DAILY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.timeInMillis
            }
            ReportPeriod.WEEKLY -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                calendar.timeInMillis
            }
            ReportPeriod.MONTHLY -> {
                calendar.add(Calendar.DAY_OF_YEAR, -30)
                calendar.timeInMillis
            }
        }

        snapshotDao.getSnapshotsSince(sinceTimestamp).collect { snapshots ->
            val totalRx = snapshots.sumOf { it.rxSpeedBps * 10 } // Estimated integrated bytes
            val totalTx = snapshots.sumOf { it.txSpeedBps * 10 }
            val peakSpeed = snapshots.maxOfOrNull { maxOf(it.rxSpeedBps, it.txSpeedBps) } ?: 0L

            val dataPoints = generateReportDataPoints(snapshots, period)

            emit(
                TrafficReport(
                    period = period,
                    totalRxBytes = if (totalRx > 0) totalRx else monitorService.networkStatus.value.totalDeviceRxBytes,
                    totalTxBytes = if (totalTx > 0) totalTx else monitorService.networkStatus.value.totalDeviceTxBytes,
                    peakSpeedBps = peakSpeed,
                    dataPoints = dataPoints,
                    generatedAt = now
                )
            )
        }
    }.flowOn(Dispatchers.IO)

    private fun generateReportDataPoints(
        snapshots: List<TrafficSnapshotEntity>,
        period: ReportPeriod
    ): List<TrafficDataPoint> {
        val points = mutableListOf<TrafficDataPoint>()
        val timeFormat = SimpleDateFormat(
            when (period) {
                ReportPeriod.DAILY -> "HH:00"
                ReportPeriod.WEEKLY -> "EEE"
                ReportPeriod.MONTHLY -> "dd MMM"
            },
            Locale("ar")
        )

        if (snapshots.isEmpty()) {
            // Provide sensible time buckets with 0 or initial readings
            val calendar = Calendar.getInstance()
            when (period) {
                ReportPeriod.DAILY -> {
                    for (i in 0..5) {
                        calendar.set(Calendar.HOUR_OF_DAY, i * 4)
                        val label = "${i * 4}:00"
                        points.add(TrafficDataPoint(label, 0, 0))
                    }
                }
                ReportPeriod.WEEKLY -> {
                    val days = listOf("السبت", "الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة")
                    days.forEach { day ->
                        points.add(TrafficDataPoint(day, 0, 0))
                    }
                }
                ReportPeriod.MONTHLY -> {
                    for (week in 1..4) {
                        points.add(TrafficDataPoint("أسبوع $week", 0, 0))
                    }
                }
            }
        } else {
            // Group by bucket
            val grouped: Map<String, List<TrafficSnapshotEntity>> = snapshots.groupBy { timeFormat.format(Date(it.timestamp)) }
            val entriesList = grouped.entries.toList()
            for (entry in entriesList.takeLast(7)) {
                val label = entry.key
                val bucket = entry.value
                val rx = bucket.sumOf { it.rxSpeedBps * 10 }
                val tx = bucket.sumOf { it.txSpeedBps * 10 }
                points.add(TrafficDataPoint(label, rx, tx))
            }
        }
        return points
    }

    suspend fun clearLocalData() = withContext(Dispatchers.IO) {
        deviceDao.deleteAllDevices()
        snapshotDao.deleteAllSnapshots()
        profileDao.deleteAllProfiles()
        dataStoreManager.clearAllPreferences()
    }

    fun getDataStoreManager() = dataStoreManager
}
