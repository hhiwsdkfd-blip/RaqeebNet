package com.example.raqeeb.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.raqeeb.data.local.entity.ConnectionProfileEntity
import com.example.raqeeb.ui.components.CyberCard
import com.example.raqeeb.ui.navigation.Screen

@Composable
fun SettingsScreen(
    currentThemeMode: String,
    alertsEnabled: Boolean,
    activeProfile: ConnectionProfileEntity?,
    onThemeChanged: (String) -> Unit,
    onAlertsChanged: (Boolean) -> Unit,
    onNavigate: (Screen) -> Unit,
    onClearAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("settings_screen")
    ) {
        // Top Header
        Text(
            text = "الإعدادات",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "تخصيص المظهر، إدارة الأمان ومسح البيانات",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 1. Connection Profile Management Card
        CyberCard(
            cornerRadius = 16.dp,
            contentPadding = 14.dp,
            onClick = { onNavigate(Screen.ConnectionSetup) }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Router,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "إدارة بيانات الاتصال والمودم",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (activeProfile != null) "الاتصال النشط: ${activeProfile.connectionName}" else "لم يتم تحديد اتصال نشط",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Filled.ChevronLeft,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 2. Appearance Section
        Text(
            text = "المظهر ونمط العرض",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(10.dp))

        CyberCard(
            cornerRadius = 16.dp,
            contentPadding = 14.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val themes = listOf(
                    Triple("DARK", "الوضع الداكن", Icons.Filled.DarkMode),
                    Triple("LIGHT", "الوضع الفاتح", Icons.Filled.LightMode),
                    Triple("SYSTEM", "حسب النظام", Icons.Filled.SettingsBrightness)
                )

                themes.forEach { (mode, title, icon) ->
                    val isSelected = currentThemeMode == mode
                    FilterChip(
                        selected = isSelected,
                        onClick = { onThemeChanged(mode) },
                        leadingIcon = {
                            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        label = { Text(title, fontSize = 12.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("theme_chip_$mode")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 3. Alerts & Monitoring Section
        Text(
            text = "التنبيهات والمراقبة",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(10.dp))

        CyberCard(
            cornerRadius = 16.dp,
            contentPadding = 14.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "تنبيهات استهلاك الباقة",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "إشعار عند اقتراب انتهاء حجم باقة الإنترنت",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = alertsEnabled,
                    onCheckedChange = onAlertsChanged,
                    modifier = Modifier.testTag("alerts_toggle_switch")
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 4. Data Management & Clear Cache
        Text(
            text = "إدارة البيانات المحلية",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(10.dp))

        CyberCard(
            cornerRadius = 16.dp,
            contentPadding = 14.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "مسح قاعدة البيانات المحلية",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "حذف سجلات الأجهزة المكتشفة وتقارير الاستهلاك المخزنة على الهاتف",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showClearConfirmDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("clear_data_button")
                ) {
                    Icon(imageVector = Icons.Filled.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "مسح", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 5. Security Architecture & About Box
        CyberCard(
            cornerRadius = 16.dp,
            contentPadding = 16.dp,
            borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "الأمان والخصوصية في رقيب نت",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "• تشفير كلمات المرور داخل مستودع المفاتيح الآمن Android KeyStore (AES-256 GCM).\n" +
                       "• استهلاك الهاتف يتم احتسابه من العدادات الرسمية TrafficStats للنظام.\n" +
                       "• خصوصية عناوين MAC خاضعة لضوابط أندرويد لحماية المستخدمين.\n" +
                       "• لا يتم إرسال أي كلمات مرور أو سجلات إلى خوادم خارجية.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "رقيب نت RaqeebNet",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "الإصدار 1.0.0 (المرحلة الأولى)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }

    // Confirmation Dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text(text = "تأكيد مسح البيانات المحلية") },
            text = {
                Text(text = "هل أنت متأكد من رغبتك في حذف كافة سجلات الأجهزة المكتشفة، تقارير الاستهلاك والاتصالات المحفوظة؟ لا يمكن التراجع عن هذا الإجراء.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllData()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("نعم، مسح البيانات")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
