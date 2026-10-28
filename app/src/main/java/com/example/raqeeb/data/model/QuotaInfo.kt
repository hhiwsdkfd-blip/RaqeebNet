package com.example.raqeeb.data.model

data class QuotaInfo(
    val hasConfiguredProfile: Boolean = false,
    val hasQueriedData: Boolean = false,
    val providerName: String = "",
    val totalQuotaGb: Double? = null,
    val consumedQuotaGb: Double? = null,
    val remainingQuotaGb: Double? = null,
    val daysRemainingInCycle: Int? = null,
    val statusMessage: String = "لم يتم جلب البيانات بعد",
    val lastSyncTimestamp: Long? = null
) {
    val consumptionPercentage: Float
        get() {
            val total = totalQuotaGb ?: return 0f
            val consumed = consumedQuotaGb ?: return 0f
            if (total <= 0.0) return 0f
            return ((consumed / total) * 100).toFloat().coerceIn(0f, 100f)
        }
}
