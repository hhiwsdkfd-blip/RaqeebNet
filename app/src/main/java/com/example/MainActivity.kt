package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.raqeeb.ui.RaqeebViewModel
import com.example.raqeeb.ui.components.RaqeebBottomNavBar
import com.example.raqeeb.ui.navigation.Screen
import com.example.raqeeb.ui.screens.ConnectionSetupScreen
import com.example.raqeeb.ui.screens.DashboardScreen
import com.example.raqeeb.ui.screens.DevicesScreen
import com.example.raqeeb.ui.screens.ReportsScreen
import com.example.raqeeb.ui.screens.SettingsScreen
import com.example.raqeeb.ui.screens.SplashScreen
import com.example.ui.theme.RaqeebNetTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val app = application as RaqeebApplication
            val viewModel: RaqeebViewModel = viewModel(
                factory = RaqeebViewModel.provideFactory(app.repository)
            )

            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val isDarkTheme = when (themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            RaqeebNetTheme(darkTheme = isDarkTheme) {
                // Full RTL Layout support for Arabic
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    RaqeebAppContent(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun RaqeebAppContent(viewModel: RaqeebViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val networkStatus by viewModel.networkStatus.collectAsStateWithLifecycle()
    val quotaInfo by viewModel.quotaInfo.collectAsStateWithLifecycle()
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val allProfiles by viewModel.allProfiles.collectAsStateWithLifecycle()
    val allDevices by viewModel.allDevices.collectAsStateWithLifecycle()
    val onlineCount by viewModel.onlineCount.collectAsStateWithLifecycle()
    val scanProgress by viewModel.scanProgress.collectAsStateWithLifecycle()
    val trafficReport by viewModel.trafficReport.collectAsStateWithLifecycle()
    val selectedPeriod by viewModel.selectedReportPeriod.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val alertsEnabled by viewModel.alertsEnabled.collectAsStateWithLifecycle()
    val isTestingGateway by viewModel.isTestingGateway.collectAsStateWithLifecycle()
    val gatewayTestResult by viewModel.gatewayTestResult.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Request permissions for Wi-Fi SSID
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        // Refresh network state once permissions resolved
        viewModel.networkStatus.value
    }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    // Handle Back Press to return to Dashboard if on other tabs
    if (currentScreen != Screen.Dashboard && currentScreen != Screen.Splash) {
        BackHandler {
            viewModel.navigateTo(Screen.Dashboard)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (currentScreen != Screen.Splash) {
                RaqeebBottomNavBar(
                    currentScreen = currentScreen,
                    onNavigate = { screen -> viewModel.navigateTo(screen) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.Splash -> {
                    SplashScreen(
                        onSplashFinished = { viewModel.navigateTo(Screen.Dashboard) }
                    )
                }

                Screen.Dashboard -> {
                    DashboardScreen(
                        networkStatus = networkStatus,
                        quotaInfo = quotaInfo,
                        onlineDevicesCount = onlineCount,
                        onNavigate = { screen -> viewModel.navigateTo(screen) },
                        onTriggerScan = { viewModel.triggerSubnetScan() }
                    )
                }

                Screen.Devices -> {
                    DevicesScreen(
                        devices = allDevices,
                        scanProgress = scanProgress,
                        onTriggerScan = { viewModel.triggerSubnetScan() },
                        onUpdateDevice = { ip, customName, deviceType ->
                            viewModel.updateDeviceCustomDetails(ip, customName, deviceType)
                        }
                    )
                }

                Screen.Reports -> {
                    ReportsScreen(
                        trafficReport = trafficReport,
                        selectedPeriod = selectedPeriod,
                        onPeriodChanged = { period -> viewModel.setReportPeriod(period) }
                    )
                }

                Screen.ConnectionSetup -> {
                    ConnectionSetupScreen(
                        savedProfiles = allProfiles,
                        activeProfile = activeProfile,
                        isTestingGateway = isTestingGateway,
                        gatewayTestResult = gatewayTestResult,
                        onSaveProfile = { name, type, provider, subNum, gwIp, user, pass, quota, day, active ->
                            viewModel.saveConnectionProfile(name, type, provider, subNum, gwIp, user, pass, quota, day, active)
                        },
                        onSetActiveProfile = { id -> viewModel.setActiveProfile(id) },
                        onDeleteProfile = { profile -> viewModel.deleteProfile(profile) },
                        onTestGateway = { ip -> viewModel.testGatewayConnection(ip) }
                    )
                }

                Screen.Settings -> {
                    SettingsScreen(
                        currentThemeMode = themeMode,
                        alertsEnabled = alertsEnabled,
                        activeProfile = activeProfile,
                        onThemeChanged = { mode -> viewModel.setThemeMode(mode) },
                        onAlertsChanged = { enabled -> viewModel.setAlertsEnabled(enabled) },
                        onNavigate = { screen -> viewModel.navigateTo(screen) },
                        onClearAllData = { viewModel.clearAllData() }
                    )
                }
            }
        }
    }
}
