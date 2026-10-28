package com.example.raqeeb.data.model

enum class ConnectionType(val titleArabic: String, val subtitleArabic: String) {
    WIFI_ROUTER(
        titleArabic = "راوتر / مودم Wi-Fi",
        subtitleArabic = "التحكم ومراقبة المودم المحلي والأجهزة المتصلة"
    ),
    FIBER_ISP(
        titleArabic = "اشتراك ألياف / إنترنت منزلي",
        subtitleArabic = "متابعة باقة المشغل ورصيد البيانات الشهري"
    ),
    CELLULAR_SIM(
        titleArabic = "باقة بيانات شريحة SIM",
        subtitleArabic = "متابعة استهلاك الشريحة وباقة الجوال"
    ),
    HOTSPOT(
        titleArabic = "نقطة اتصال متنقلة Hotspot",
        subtitleArabic = "مراقبة الأجهزة المتصلة ببث الهاتف"
    )
}
