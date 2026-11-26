package com.android.ssaid.abdullah.core.backend

import android.content.Context
import com.android.ssaid.abdullah.data.model.WorkingMode

class BackendSelector(context: Context) {

    val secureSettingsBackend = SecureSettingsBackend(context)
    val shizukuBackend = ShizukuBackend(context)
    val hookBridgeBackend = HookBridgeBackend(context)
    val rootBackend = RootBackend(context)

    fun getBackend(mode: WorkingMode): SsaidBackend {
        return when (mode) {
            WorkingMode.SECURE_SETTINGS -> secureSettingsBackend
            WorkingMode.SHIZUKU -> shizukuBackend
            WorkingMode.LSPATCH_HOOK -> hookBridgeBackend
            WorkingMode.ROOT -> rootBackend
        }
    }

    fun selectBestMode(): WorkingMode {
        return when {
            rootBackend.isAvailable() -> WorkingMode.ROOT
            shizukuBackend.isAvailable() -> WorkingMode.SHIZUKU
            secureSettingsBackend.isAvailable() -> WorkingMode.SECURE_SETTINGS
            else -> WorkingMode.LSPATCH_HOOK
        }
    }
}
