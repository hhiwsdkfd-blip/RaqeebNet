package com.example.raqeeb.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.raqeeb.data.model.NetworkStatus
import com.example.raqeeb.data.model.ReportPeriod
import com.example.raqeeb.data.model.TrafficReport
import com.example.raqeeb.ui.components.CanvasNetworkChart
import com.example.raqeeb.ui.components.CyberCard
import com.example.raqeeb.ui.components.StatMetricBadge

@Composable
fun ReportsScreen(
    trafficReport: TrafficReport,
    selectedPeriod: ReportPeriod,
    onPeriodChanged: (ReportPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("reports_screen")
    ) {
        // Header
        Text(
            text = "تقارير الاستهلاك",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "تحليل حركة المرور، التنزيل والرفع عبر فترات زمنية",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Period Selection Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ReportPeriod.values().forEach { period ->
                FilterChip(
                    selected = selectedPeriod == period,
                    onClick = { onPeriodChanged(period) },
                    label = { Text("التقرير ${period.titleArabic}") },
                    modifier = Modifier.testTag("filter_period_${period.name}")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chart Card
        CyberCard(
            cornerRadius = 20.dp,
            contentPadding = 18.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "منحنى حركة البيانات (${selectedPeriod.titleArabic})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Icon(
                    imageVector = Icons.Filled.BarChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            CanvasNetworkChart(
                dataPoints = trafficReport.dataPoints,
                downloadColor = MaterialTheme.colorScheme.primary,
                uploadColor = MaterialTheme.colorScheme.secondary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Summary Badges
        Text(
            text = "ملخص حركة المرور",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            StatMetricBadge(
                modifier = Modifier.weight(1f),
                title = "إجمالي التنزيل",
                value = NetworkStatus.formatBytes(trafficReport.totalRxBytes),
                subtitle = "حركة الاستقبال Rx",
                icon = Icons.Filled.ArrowDownward,
                iconTint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(10.dp))

            StatMetricBadge(
                modifier = Modifier.weight(1f),
                title = "إجمالي الرفع",
                value = NetworkStatus.formatBytes(trafficReport.totalTxBytes),
                subtitle = "حركة الإرسال Tx",
                icon = Icons.Filled.ArrowUpward,
                iconTint = MaterialTheme.colorScheme.secondary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        StatMetricBadge(
            title = "أعلى سرعة مسجلة",
            value = "${NetworkStatus.formatSpeed(trafficReport.peakSpeedBps)}/s",
            subtitle = "أقصى ذروة تدفق خلال هذه الفترة",
            icon = Icons.Filled.Speed,
            iconTint = MaterialTheme.colorScheme.tertiary
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Download vs Upload Distribution Bar
        CyberCard(
            cornerRadius = 16.dp,
            contentPadding = 16.dp
        ) {
            Text(
                text = "توزيع حركة البيانات (تنزيل مقابل رفع)",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(CircleShape)
            ) {
                Box(
                    modifier = Modifier
                        .weight(trafficReport.rxPercentage.coerceAtLeast(1f))
                        .background(MaterialTheme.colorScheme.primary)
                )
                Box(
                    modifier = Modifier
                        .weight(trafficReport.txPercentage.coerceAtLeast(1f))
                        .background(MaterialTheme.colorScheme.secondary)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "تنزيل: ${String.format(java.util.Locale.US, "%.1f", trafficReport.rxPercentage)}%",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "رفع: ${String.format(java.util.Locale.US, "%.1f", trafficReport.txPercentage)}%",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
