package com.example.raqeeb.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Router
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val titleArabic: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Splash : Screen("splash", "البداية", Icons.Filled.Home, Icons.Outlined.Home)
    object Dashboard : Screen("dashboard", "الرئيسية", Icons.Filled.Home, Icons.Outlined.Home)
    object Devices : Screen("devices", "الأجهزة", Icons.Filled.Devices, Icons.Outlined.Devices)
    object Reports : Screen("reports", "التقارير", Icons.Filled.Assessment, Icons.Outlined.Assessment)
    object ConnectionSetup : Screen("connection", "الاتصال", Icons.Filled.Router, Icons.Outlined.Router)
    object Settings : Screen("settings", "الإعدادات", Icons.Filled.Settings, Icons.Outlined.Settings)

    companion object {
        val bottomNavItems = listOf(Dashboard, Devices, Reports, ConnectionSetup, Settings)
    }
}
