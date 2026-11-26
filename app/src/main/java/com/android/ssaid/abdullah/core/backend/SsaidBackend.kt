package com.android.ssaid.abdullah.core.backend

import com.android.ssaid.abdullah.data.model.WorkingMode

data class OperationResult(
    val success: Boolean,
    val message: String,
    val requiresReboot: Boolean = false
)

interface SsaidBackend {
    val mode: WorkingMode
    fun isAvailable(): Boolean
    fun readAllSsaids(): Map<String, String>
    fun updateSsaid(packageName: String, newSsaid: String): OperationResult
}
