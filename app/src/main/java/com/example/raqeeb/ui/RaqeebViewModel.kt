package com.example.raqeeb.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.raqeeb.data.local.entity.ConnectionProfileEntity
import com.example.raqeeb.data.local.entity.NetworkDeviceEntity
import com.example.raqeeb.data.model.ConnectionType
import com.example.raqeeb.data.model.NetworkStatus
import com.example.raqeeb.data.model.QuotaInfo
import com.example.raqeeb.data.model.ReportPeriod
import com.example.raqeeb.data.model.TrafficReport
import com.example.raqeeb.data.network.ScanProgress
import com.example.raqeeb.data.repository.RaqeebRepository
import com.example.raqeeb.ui.navigation.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

class RaqeebViewModel(
    private val repository: RaqeebRepository
) : ViewModel() {

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    val networkStatus: StateFlow<NetworkStatus> = repository.networkStatus

    val activeProfile: StateFlow<ConnectionProfileEntity?> = repository.activeProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val allProfiles: StateFlow<List<ConnectionProfileEntity>> = repository.allProfiles.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allDevices: StateFlow<List<NetworkDeviceEntity>> = repository.allDevices.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val onlineCount: StateFlow<Int> = repository.onlineCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val quotaInfo: StateFlow<QuotaInfo> = repository.getQuotaInfo().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = QuotaInfo()
    )

    private val _scanProgress = MutableStateFlow(
        ScanProgress(isScanning = false, percent = 0, currentIp = "", discoveredDevices = emptyList())
    )
    val scanProgress: StateFlow<ScanProgress> = _scanProgress.asStateFlow()

    private val _selectedReportPeriod = MutableStateFlow(ReportPeriod.DAILY)
    val selectedReportPeriod: StateFlow<ReportPeriod> = _selectedReportPeriod.asStateFlow()

    private val _trafficReport = MutableStateFlow(TrafficReport())
    val trafficReport: StateFlow<TrafficReport> = _trafficReport.asStateFlow()

    val themeMode: StateFlow<String> = repository.getDataStoreManager().themeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "DARK"
    )

    val alertsEnabled: StateFlow<Boolean> = repository.getDataStoreManager().alertsEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    private val _isTestingGateway = MutableStateFlow(false)
    val isTestingGateway: StateFlow<Boolean> = _isTestingGateway.asStateFlow()

    private val _gatewayTestResult = MutableStateFlow<String?>(null)
    val gatewayTestResult: StateFlow<String?> = _gatewayTestResult.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private var scanJob: Job? = null

    init {
        loadTrafficReport(ReportPeriod.DAILY)
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun triggerSubnetScan() {
        if (_scanProgress.value.isScanning) return
        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            _scanProgress.value = _scanProgress.value.copy(isScanning = true, percent = 0)
            repository.startSubnetScan().collect { progress ->
                _scanProgress.value = progress
            }
            _snackbarMessage.value = "اكتمل فحص الشبكة المحلية"
        }
    }

    fun saveConnectionProfile(
        name: String,
        type: ConnectionType,
        provider: String,
        subscriptionNumber: String,
        gatewayIp: String,
        username: String,
        rawPassword: String,
        totalQuotaGb: Double,
        renewalDay: Int,
        setAsActive: Boolean
    ) {
        viewModelScope.launch {
            if (name.isBlank()) {
                _snackbarMessage.value = "يرجى كتابة اسم للاتصال"
                return@launch
            }
            repository.saveProfile(
                name = name,
                type = type,
                provider = provider,
                subscriptionNumber = subscriptionNumber,
                gatewayIp = gatewayIp,
                username = username,
                rawPassword = rawPassword,
                totalQuotaGb = totalQuotaGb,
                renewalDay = renewalDay,
                setAsActive = setAsActive
            )
            _snackbarMessage.value = "تم حفظ إعدادات الاتصال بنجاح"
            _currentScreen.value = Screen.Dashboard
        }
    }

    fun setActiveProfile(id: Long) {
        viewModelScope.launch {
            repository.setActiveProfile(id)
            _snackbarMessage.value = "تم تفعيل الاتصال المختار"
        }
    }

    fun deleteProfile(profile: ConnectionProfileEntity) {
        viewModelScope.launch {
            repository.deleteProfile(profile)
            _snackbarMessage.value = "تم حذف الاتصال"
        }
    }

    fun testGatewayConnection(gatewayIp: String) {
        viewModelScope.launch {
            _isTestingGateway.value = true
            _gatewayTestResult.value = null

            val success = withContext(Dispatchers.IO) {
                try {
                    val socket = Socket()
                    socket.connect(InetSocketAddress(gatewayIp, 80), 1500)
                    socket.close()
                    true
                } catch (_: Exception) {
                    try {
                        val socket = Socket()
                        socket.connect(InetSocketAddress(gatewayIp, 443), 1500)
                        socket.close()
                        true
                    } catch (_: Exception) {
                        false
                    }
                }
            }

            _isTestingGateway.value = false
            _gatewayTestResult.value = if (success) {
                "تم الاتصال ببوابة المودم ($gatewayIp) بنجاح"
            } else {
                "تعذر الوصول إلى $gatewayIp (تأكد من عنوان IP واتصالك بالواي فاي)"
            }
        }
    }

    fun updateDeviceCustomDetails(ip: String, customName: String, deviceType: String) {
        viewModelScope.launch {
            repository.updateDeviceDetails(ip, customName, deviceType)
            _snackbarMessage.value = "تم تحديث بيانات الجهاز"
        }
    }

    fun setReportPeriod(period: ReportPeriod) {
        _selectedReportPeriod.value = period
        loadTrafficReport(period)
    }

    private fun loadTrafficReport(period: ReportPeriod) {
        viewModelScope.launch {
            repository.getTrafficReport(period).collect { report ->
                _trafficReport.value = report
            }
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            repository.getDataStoreManager().setThemeMode(mode)
        }
    }

    fun setAlertsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.getDataStoreManager().setAlertsEnabled(enabled)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearLocalData()
            _snackbarMessage.value = "تم مسح جميع البيانات المحلية بنجاح"
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    companion object {
        fun provideFactory(repository: RaqeebRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return RaqeebViewModel(repository) as T
                }
            }
    }
}
