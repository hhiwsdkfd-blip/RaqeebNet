package com.example

import android.app.Application
import com.example.raqeeb.data.local.DataStoreManager
import com.example.raqeeb.data.local.EncryptedCredentialsManager
import com.example.raqeeb.data.local.RaqeebDatabase
import com.example.raqeeb.data.network.NetworkMonitorService
import com.example.raqeeb.data.network.SubnetScanner
import com.example.raqeeb.data.network.provider.ProviderConnectorRegistry
import com.example.raqeeb.data.repository.RaqeebRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class RaqeebApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var database: RaqeebDatabase
        private set

    lateinit var credentialsManager: EncryptedCredentialsManager
        private set

    lateinit var dataStoreManager: DataStoreManager
        private set

    lateinit var monitorService: NetworkMonitorService
        private set

    lateinit var repository: RaqeebRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = RaqeebDatabase.getInstance(this)
        credentialsManager = EncryptedCredentialsManager()
        dataStoreManager = DataStoreManager(this)
        monitorService = NetworkMonitorService(this, applicationScope)

        val scanner = SubnetScanner()
        val registry = ProviderConnectorRegistry()

        repository = RaqeebRepository(
            profileDao = database.connectionProfileDao(),
            deviceDao = database.networkDeviceDao(),
            snapshotDao = database.trafficSnapshotDao(),
            monitorService = monitorService,
            scanner = scanner,
            credentialsManager = credentialsManager,
            dataStoreManager = dataStoreManager,
            connectorRegistry = registry,
            appScope = applicationScope
        )
    }

    companion object {
        lateinit var instance: RaqeebApplication
            private set
    }
}
