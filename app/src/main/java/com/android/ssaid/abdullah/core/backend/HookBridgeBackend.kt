package com.android.ssaid.abdullah.core.backend

import android.content.Context
import com.android.ssaid.abdullah.core.hook.HookConfigStore
import com.android.ssaid.abdullah.core.ssaid.SsaidGenerator
import com.android.ssaid.abdullah.data.model.WorkingMode

class HookBridgeBackend(context: Context) : SsaidBackend {

    private val store = HookConfigStore(context)

    override val mode: WorkingMode = WorkingMode.LSPATCH_HOOK

    override fun isAvailable(): Boolean = true

    override fun readAllSsaids(): Map<String, String> = store.getAllOverrides()

    override fun updateSsaid(packageName: String, newSsaid: String): OperationResult {
        val normalized = SsaidGenerator.normalize(newSsaid)
        if (!SsaidGenerator.isValid(normalized)) {
            return OperationResult(false, "المعرّف غير صالح، يجب أن يكون 16 خانة (0-9 و a-f).")
        }
        val saved = store.setOverride(packageName, normalized)
        return if (saved) {
            OperationResult(
                success = true,
                message = "تم حفظ SSAID المخصص لـ $packageName. أعد تشغيل التطبيق المستهدف (المحقون بـ LSPatch/LSPosed) لتفعيل الرقم فوراً."
            )
        } else {
            OperationResult(false, "تعذّر حفظ المعرّف المخصص.")
        }
    }

    fun removeOverride(packageName: String): OperationResult {
        val removed = store.removeOverride(packageName)
        return if (removed) {
            OperationResult(true, "تمت إزالة التخصيص لـ $packageName.")
        } else {
            OperationResult(false, "تعذّر إزالة التخصيص.")
        }
    }
}
