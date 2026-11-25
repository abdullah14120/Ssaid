package com.android.ssaid.abdullah.data.model

import android.graphics.drawable.Drawable

data class AppSsaidInfo(
    val packageName: String,
    val appName: String,
    val uid: Int,
    val isSystemApp: Boolean,
    val icon: Drawable?,
    val systemSsaid: String?,
    val hookOverrideSsaid: String?
) {
    val displayedSsaid: String?
        get() = hookOverrideSsaid ?: systemSsaid

    val hasHookOverride: Boolean
        get() = !hookOverrideSsaid.isNullOrBlank()
}
