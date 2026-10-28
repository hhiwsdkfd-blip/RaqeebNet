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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.raqeeb.data.local.entity.ConnectionProfileEntity
import com.example.raqeeb.data.model.ConnectionType
import com.example.raqeeb.ui.components.CyberCard

@Composable
fun ConnectionSetupScreen(
    savedProfiles: List<ConnectionProfileEntity>,
    activeProfile: ConnectionProfileEntity?,
    isTestingGateway: Boolean,
    gatewayTestResult: String?,
    onSaveProfile: (
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
    ) -> Unit,
    onSetActiveProfile: (Long) -> Unit,
    onDeleteProfile: (ConnectionProfileEntity) -> Unit,
    onTestGateway: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var selectedType by remember { mutableStateOf(ConnectionType.WIFI_ROUTER) }
    var connectionName by remember { mutableStateOf("راوتر المنزل الرئيسي") }
    var providerName by remember { mutableStateOf("") }
    var subscriptionNumber by remember { mutableStateOf("") }
    var gatewayIp by remember { mutableStateOf("192.168.1.1") }
    var username by remember { mutableStateOf("admin") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var totalQuotaGbText by remember { mutableStateOf("") }
    var renewalDayText by remember { mutableStateOf("1") }
    var setAsActive by remember { mutableStateOf(true) }

    var validationError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("connection_setup_screen")
    ) {
        // Header
        Text(
            text = "إعداد الاتصال والخدمة",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "اختر نوع الخدمة وأدخل البيانات اللازمة لمراقبة الاتصال",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Service Type Selector Tabs
        Text(
            text = "نوع الاتصال أو الخدمة",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ConnectionType.values().forEach { type ->
                val isSelected = selectedType == type
                val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                val bgColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor)
                        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                        .clickable { selectedType = type }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val icon = when (type) {
                        ConnectionType.WIFI_ROUTER -> Icons.Filled.Router
                        ConnectionType.FIBER_ISP -> Icons.Filled.Wifi
                        ConnectionType.CELLULAR_SIM -> Icons.Filled.CellTower
                        ConnectionType.HOTSPOT -> Icons.Filled.WifiTethering
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = type.titleArabic,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = type.subtitleArabic,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Dynamic Form Fields
        CyberCard(
            cornerRadius = 16.dp,
            contentPadding = 16.dp
        ) {
            Text(
                text = "بيانات الاتصال",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Connection Name (Always visible)
            OutlinedTextField(
                value = connectionName,
                onValueChange = {
                    connectionName = it
                    validationError = null
                },
                label = { Text("اسم الاتصال أو الشبكة *") },
                placeholder = { Text("مثال: راوتر الصالة، ألياف المنزل، شريحة 5G") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_connection_name")
            )

            // 2. Telecom Provider (For Fiber & Cellular SIM)
            if (selectedType == ConnectionType.FIBER_ISP || selectedType == ConnectionType.CELLULAR_SIM) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = providerName,
                    onValueChange = { providerName = it },
                    label = { Text("شركة الاتصالات / مزود الخدمة") },
                    placeholder = { Text("مثال: STC, Mobily, Zain, WE, Vodafone...") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_provider_name")
                )

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = subscriptionNumber,
                    onValueChange = { subscriptionNumber = it },
                    label = { Text("رقم الاشتراك / رقم الحساب / الشريحة") },
                    placeholder = { Text("رقم العقد أو الحساب لدى المشغل") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_subscription_number")
                )
            }

            // 3. Gateway IP (For Router & Hotspot)
            if (selectedType == ConnectionType.WIFI_ROUTER || selectedType == ConnectionType.HOTSPOT) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = gatewayIp,
                        onValueChange = { gatewayIp = it },
                        label = { Text("عنوان بوابة المودم Gateway IP *") },
                        placeholder = { Text("192.168.1.1") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_gateway_ip")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = { onTestGateway(gatewayIp) },
                        enabled = !isTestingGateway && gatewayIp.isNotBlank(),
                        modifier = Modifier.testTag("test_gateway_button")
                    ) {
                        if (isTestingGateway) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(imageVector = Icons.Filled.NetworkCheck, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                if (gatewayTestResult != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = gatewayTestResult,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (gatewayTestResult.contains("بنجاح")) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                    )
                }
            }

            // 4. Username (For Router admin & Fiber portal)
            if (selectedType == ConnectionType.WIFI_ROUTER || selectedType == ConnectionType.FIBER_ISP) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("اسم المستخدم (اختياري)") },
                    placeholder = { Text("مثال: admin") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_username")
                )

                Spacer(modifier = Modifier.height(10.dp))
                // Password with Show/Hide toggle
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("كلمة المرور (مشفرة داخل KeyStore)") },
                    placeholder = { Text("كلمة مرور لوحة التحكم أو الحساب") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(
                            onClick = { passwordVisible = !passwordVisible },
                            modifier = Modifier.testTag("toggle_password_visibility")
                        ) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (passwordVisible) "إخفاء كلمة المرور" else "إظهار كلمة المرور"
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_password")
                )
            }

            // 5. Total Quota & Renewal Day
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = totalQuotaGbText,
                    onValueChange = { totalQuotaGbText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("حجم الباقة (GB)") },
                    placeholder = { Text("مثال: 100") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_quota_gb")
                )

                Spacer(modifier = Modifier.width(10.dp))

                OutlinedTextField(
                    value = renewalDayText,
                    onValueChange = { renewalDayText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("يوم التجديد") },
                    placeholder = { Text("1..31") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_renewal_day")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Set as active switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تعيين كالاتصال النشط حالياً",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Switch(
                    checked = setAsActive,
                    onCheckedChange = { setAsActive = it }
                )
            }

            // Validation error display
            if (validationError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = validationError ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = {
                    if (connectionName.isBlank()) {
                        validationError = "يرجى كتابة اسم للاتصال"
                        return@Button
                    }
                    val quota = totalQuotaGbText.toDoubleOrNull() ?: 0.0
                    val day = renewalDayText.toIntOrNull()?.coerceIn(1, 31) ?: 1

                    onSaveProfile(
                        connectionName.trim(),
                        selectedType,
                        providerName.trim(),
                        subscriptionNumber.trim(),
                        gatewayIp.trim(),
                        username.trim(),
                        password,
                        quota,
                        day,
                        setAsActive
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_profile_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "حفظ إعدادات الاتصال بصورة آمنة")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Saved Profiles Section
        if (savedProfiles.isNotEmpty()) {
            Text(
                text = "الاتصالات المحفوظة (${savedProfiles.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(10.dp))

            savedProfiles.forEach { profile ->
                val isActive = activeProfile?.id == profile.id
                CyberCard(
                    modifier = Modifier.padding(bottom = 8.dp),
                    cornerRadius = 14.dp,
                    borderColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    contentPadding = 12.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = profile.connectionName,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (isActive) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "نشط",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${profile.connectionType.titleArabic} • ${profile.modemGatewayIp}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!isActive) {
                                OutlinedButton(
                                    onClick = { onSetActiveProfile(profile.id) },
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Text(text = "تفعيل", fontSize = 12.sp)
                                }
                            }
                            IconButton(onClick = { onDeleteProfile(profile) }) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "حذف الاتصال",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
