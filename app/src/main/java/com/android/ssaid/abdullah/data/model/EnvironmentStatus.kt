package com.android.ssaid.abdullah.data.model

data class EnvironmentStatus(
    val hasWriteSecureSettings: Boolean = false,
    val isShizukuAlive: Boolean = false,
    val isShizukuGranted: Boolean = false,
    val isShizukuServiceBound: Boolean = false,
    val isRootAvailable: Boolean = false,
    val hasRootBackup: Boolean = false,
    val androidVersionLabel: String = ""
)
