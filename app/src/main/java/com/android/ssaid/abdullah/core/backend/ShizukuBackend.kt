package com.android.ssaid.abdullah.core.backend

import android.content.Context
import android.os.Build
import com.android.ssaid.abdullah.core.shizuku.ShizukuManager
import com.android.ssaid.abdullah.core.ssaid.SsaidGenerator
import com.android.ssaid.abdullah.data.model.WorkingMode

class ShizukuBackend(private val context: Context) : SsaidBackend {

    override val mode: WorkingMode = WorkingMode.SHIZUKU

    override fun isAvailable(): Boolean {
        return ShizukuManager.isBinderAlive.value &&
            ShizukuManager.isPermissionGranted.value &&
            ShizukuManager.isServiceBound.value
    }

    override fun readAllSsaids(): Map<String, String> {
        if (!isAvailable()) return emptyMap()
        val id = ShizukuManager.getGlobalAndroidId()
        return if (!id.isNullOrBlank()) mapOf(context.packageName to id) else emptyMap()
    }

    override fun updateSsaid(packageName: String, newSsaid: String): OperationResult {
        val normalized = SsaidGenerator.normalize(newSsaid)
        if (!SsaidGenerator.isValid(normalized)) {
            return OperationResult(false, "المعرّف غير صالح، يجب أن يكون 16 خانة (0-9 و a-f).")
        }
        if (!isAvailable()) {
            return OperationResult(false, "خدمة Shizuku غير متصلة أو لم يتم منح الصلاحية بعد.")
        }

        val ok = ShizukuManager.setGlobalAndroidId(normalized)
        return if (ok) {
            val extra = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                " (تم التحديث عبر Shell؛ لتغيير SSAID لتطبيق محدد على أندرويد 8+ استخدم وضع LSPatch أو Root)."
            } else {
                ""
            }
            OperationResult(true, "تم تحديث معرّف الجهاز عبر Shizuku بنجاح$extra")
        } else {
            OperationResult(false, "فشل تنفيذ أمر settings put عبر Shizuku.")
        }
    }
}
