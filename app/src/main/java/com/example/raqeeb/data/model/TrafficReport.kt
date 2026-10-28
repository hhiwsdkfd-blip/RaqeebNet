package com.example.raqeeb.data.model

enum class ReportPeriod(val titleArabic: String) {
    DAILY("اليومي"),
    WEEKLY("الأسبوعي"),
    MONTHLY("الشهري")
}

data class TrafficDataPoint(
    val label: String,       // e.g. "00:00", "04:00" or "الأحد", "الاثنين" or "الأسبوع 1"
    val rxBytes: Long,
    val txBytes: Long
)

data class TrafficReport(
    val period: ReportPeriod = ReportPeriod.DAILY,
    val totalRxBytes: Long = 0,
    val totalTxBytes: Long = 0,
    val peakSpeedBps: Long = 0,
    val dataPoints: List<TrafficDataPoint> = emptyList(),
    val generatedAt: Long = System.currentTimeMillis()
) {
    val totalBytes: Long get() = totalRxBytes + totalTxBytes

    val rxPercentage: Float
        get() = if (totalBytes > 0) (totalRxBytes.toFloat() / totalBytes) * 100f else 50f

    val txPercentage: Float
        get() = if (totalBytes > 0) (totalTxBytes.toFloat() / totalBytes) * 100f else 50f
}
