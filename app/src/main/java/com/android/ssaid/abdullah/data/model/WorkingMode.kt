package com.android.ssaid.abdullah.data.model

enum class WorkingMode(
    val titleAr: String,
    val descriptionAr: String
) {
    SECURE_SETTINGS(
        titleAr = "بدون روت (ADB)",
        descriptionAr = "تعديل ANDROID_ID عبر إذن WRITE_SECURE_SETTINGS مباشرة"
    ),
    SHIZUKU(
        titleAr = "Shizuku",
        descriptionAr = "تنفيذ أوامر settings ومنح الإذن تلقائياً من الهاتف بدون حاسوب"
    ),
    LSPATCH_HOOK(
        titleAr = "LSPatch / Hook",
        descriptionAr = "تخصيص SSAID مستقل لكل تطبيق بدون روت (أندرويد 8 إلى 15+)"
    ),
    ROOT(
        titleAr = "روت (النظام)",
        descriptionAr = "قراءة وتعديل ملف settings_ssaid.xml (يدعم ABX لأندرويد 12+)"
    )
}
