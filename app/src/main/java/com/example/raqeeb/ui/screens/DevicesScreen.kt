package com.example.raqeeb.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DevicesOther
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.raqeeb.data.local.entity.NetworkDeviceEntity
import com.example.raqeeb.data.network.ScanProgress
import com.example.raqeeb.ui.components.CyberCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DevicesScreen(
    devices: List<NetworkDeviceEntity>,
    scanProgress: ScanProgress,
    onTriggerScan: () -> Unit,
    onUpdateDevice: (ip: String, customName: String, deviceType: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, ONLINE, ROUTER, PHONE, LAPTOP, IOT
    var selectedDeviceForEdit by remember { mutableStateOf<NetworkDeviceEntity?>(null) }

    val filteredDevices = remember(devices, searchQuery, selectedFilter) {
        devices.filter { device ->
            val matchesSearch = searchQuery.isBlank() ||
                    device.ipAddress.contains(searchQuery, ignoreCase = true) ||
                    device.hostname.contains(searchQuery, ignoreCase = true) ||
                    device.customName.contains(searchQuery, ignoreCase = true) ||
                    device.vendor.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "ONLINE" -> device.isOnline
                "ROUTER" -> device.deviceType == "ROUTER"
                "PHONE" -> device.deviceType == "PHONE"
                "LAPTOP" -> device.deviceType == "LAPTOP"
                "IOT" -> device.deviceType == "IOT" || device.deviceType == "TV"
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("devices_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "الأجهزة المتصلة",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "اكتشاف وتحليل الأجهزة على الشبكة المحلية",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onTriggerScan,
                enabled = !scanProgress.isScanning,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("scan_network_button")
            ) {
                if (scanProgress.isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "${scanProgress.percent}%", fontSize = 12.sp)
                } else {
                    Icon(imageVector = Icons.Filled.Radar, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "فحص الشبكة", fontSize = 13.sp)
                }
            }
        }

        // Live Scanning Progress Bar
        if (scanProgress.isScanning) {
            Spacer(modifier = Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "جاري مسح المدى المحلي: ${scanProgress.currentIp}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${scanProgress.percent}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { scanProgress.percent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surface
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("بحث بالاسم، عنوان IP، أو الماك...") },
            leadingIcon = {
                Icon(imageVector = Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(imageVector = Icons.Filled.Clear, contentDescription = "مسح")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_devices_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedFilter == "ALL",
                    onClick = { selectedFilter = "ALL" },
                    label = { Text("الكل (${devices.size})") }
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == "ONLINE",
                    onClick = { selectedFilter = "ONLINE" },
                    label = { Text("متصل فقط") }
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == "ROUTER",
                    onClick = { selectedFilter = "ROUTER" },
                    label = { Text("المودم والراوتر") }
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == "PHONE",
                    onClick = { selectedFilter = "PHONE" },
                    label = { Text("هواتف") }
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == "LAPTOP",
                    onClick = { selectedFilter = "LAPTOP" },
                    label = { Text("أجهزة كمبيوتر") }
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == "IOT",
                    onClick = { selectedFilter = "IOT" },
                    label = { Text("أجهزة ذكية / IoT") }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Devices List
        if (filteredDevices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Radar,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (devices.isEmpty()) "لم يتم اكتشاف أجهزة بعد" else "لا توجد نتائج مطابقة للبحث",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "اضغط على زر (فحص الشبكة) للبدء باكتشاف الأجهزة المتصلة",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredDevices, key = { it.ipAddress }) { device ->
                    DeviceItemCard(
                        device = device,
                        onEditClicked = { selectedDeviceForEdit = device }
                    )
                }
            }
        }
    }

    // Edit Device Details Dialog
    selectedDeviceForEdit?.let { device ->
        EditDeviceDialog(
            device = device,
            onDismiss = { selectedDeviceForEdit = null },
            onSave = { newName, newType ->
                onUpdateDevice(device.ipAddress, newName, newType)
                selectedDeviceForEdit = null
            }
        )
    }
}

@Composable
fun DeviceItemCard(
    device: NetworkDeviceEntity,
    onEditClicked: () -> Unit
) {
    val icon: ImageVector = when (device.deviceType) {
        "ROUTER" -> Icons.Filled.Router
        "PHONE" -> Icons.Filled.Smartphone
        "LAPTOP" -> Icons.Filled.Computer
        "TV" -> Icons.Filled.Tv
        "IOT" -> Icons.Filled.Memory
        else -> Icons.Filled.DevicesOther
    }

    val dateFormat = remember { SimpleDateFormat("HH:mm", Locale("ar")) }

    CyberCard(
        cornerRadius = 16.dp,
        contentPadding = 14.dp,
        borderColor = if (device.deviceType == "ROUTER") MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        onClick = onEditClicked
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (device.deviceType == "ROUTER") MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (device.deviceType == "ROUTER") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Main Info
            Column(modifier = Modifier.weight(1f)) {
                val displayName = when {
                    device.customName.isNotBlank() -> device.customName
                    device.hostname.isNotBlank() -> device.hostname
                    else -> device.vendor
                }
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "IP: ${device.ipAddress}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "MAC: ${device.macAddress}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Status & Ping
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(
                                if (device.isOnline) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (device.isOnline) "متصل" else "غير نشط",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (device.isOnline) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                    )
                }

                if (device.pingMs > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${device.pingMs} ms",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateFormat.format(Date(device.lastSeenAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun EditDeviceDialog(
    device: NetworkDeviceEntity,
    onDismiss: () -> Unit,
    onSave: (newName: String, newType: String) -> Unit
) {
    var customName by remember { mutableStateOf(device.customName) }
    var deviceType by remember { mutableStateOf(device.deviceType) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "تخصيص بيانات الجهاز") },
        text = {
            Column {
                Text(text = "عنوان IP: ${device.ipAddress}", style = MaterialTheme.typography.bodySmall)
                Text(text = "الماك: ${device.macAddress}", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text("الاسم المخصص للجهاز") },
                    placeholder = { Text("مثال: هاتف أحمد، تلفزيون الصالة") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "نوع الجهاز:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))

                val types = listOf(
                    "PHONE" to "هاتف ذكي",
                    "LAPTOP" to "كمبيوتر محمول",
                    "TV" to "شاشة ذكية",
                    "ROUTER" to "مودم / راوتر",
                    "IOT" to "جهاز ذكي IoT",
                    "GENERIC" to "عام"
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(types) { (key, label) ->
                        FilterChip(
                            selected = deviceType == key,
                            onClick = { deviceType = key },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(customName, deviceType) }) {
                Text("حفظ التغييرات")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
