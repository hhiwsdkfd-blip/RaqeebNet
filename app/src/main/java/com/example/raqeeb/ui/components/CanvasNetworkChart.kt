package com.example.raqeeb.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.raqeeb.data.model.TrafficDataPoint

@Composable
fun CanvasNetworkChart(
    modifier: Modifier = Modifier,
    dataPoints: List<TrafficDataPoint>,
    downloadColor: Color = MaterialTheme.colorScheme.primary,
    uploadColor: Color = MaterialTheme.colorScheme.secondary
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(downloadColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "تنزيل (Rx)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(uploadColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "رفع (Tx)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Chart Canvas
        val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        val maxBytes = dataPoints.maxOfOrNull { maxOf(it.rxBytes, it.txBytes) }?.coerceAtLeast(1024 * 1024) ?: (1024 * 1024)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                val chartWidth = size.width
                val chartHeight = size.height - 24.dp.toPx()
                val bottomY = chartHeight

                // Draw 4 horizontal grid lines
                val gridSteps = 4
                for (i in 0..gridSteps) {
                    val y = (chartHeight / gridSteps) * i
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(chartWidth, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                    )
                }

                if (dataPoints.isNotEmpty()) {
                    val count = dataPoints.size
                    val slotWidth = chartWidth / count
                    val barWidth = (slotWidth * 0.25f).coerceAtMost(16.dp.toPx())

                    dataPoints.forEachIndexed { index, point ->
                        val centerX = slotWidth * index + (slotWidth / 2)

                        // Calculate bar heights
                        val rxHeight = ((point.rxBytes.toFloat() / maxBytes) * chartHeight).coerceIn(4f, chartHeight)
                        val txHeight = ((point.txBytes.toFloat() / maxBytes) * chartHeight).coerceIn(4f, chartHeight)

                        // Draw Rx Bar (Download)
                        val rxLeft = centerX - barWidth - 2.dp.toPx()
                        drawRoundRect(
                            color = downloadColor,
                            topLeft = Offset(rxLeft, bottomY - rxHeight),
                            size = Size(barWidth, rxHeight),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )

                        // Draw Tx Bar (Upload)
                        val txLeft = centerX + 2.dp.toPx()
                        drawRoundRect(
                            color = uploadColor,
                            topLeft = Offset(txLeft, bottomY - txHeight),
                            size = Size(barWidth, txHeight),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }
                }
            }
        }

        // X-Axis Labels
        if (dataPoints.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dataPoints.forEach { point ->
                    Text(
                        text = point.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
