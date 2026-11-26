package com.android.ssaid.abdullah.core.backend

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import com.android.ssaid.abdullah.core.ssaid.SsaidGenerator
import com.android.ssaid.abdullah.data.model.WorkingMode

class SecureSettingsBackend(private val context: Context) : SsaidBackend {

    override val mode: WorkingMode = WorkingMode.SECURE_SETTINGS

    override fun isAvailable(): Boolean {
        return context.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) ==
            PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("HardwareIds")
    fun readOwnSsaid(): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ).orEmpty()
    }

    override fun readAllSsaids(): Map<String, String> {
        val own = readOwnSsaid()
        return if (own.isNotBlank()) mapOf(context.packageName to own) else emptyMap()
    }

    override fun updateSsaid(packageName: String, newSsaid: String): OperationResult {
        val normalized = SsaidGenerator.normalize(newSsaid)
        if (!SsaidGenerator.isValid(normalized)) {
            return OperationResult(false, "المعرّف غير صالح، يجب أن يكون 16 خانة (0-9 و a-f).")
        }
        if (!isAvailable()) {
            return OperationResult(
                false,
                "إذن WRITE_SECURE_SETTINGS غير ممنوح. امنحه عبر Shizuku أو أمر ADB."
            )
        }
        return try {
            val ok = Settings.Secure.putString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID,
                normalized
            )
            if (ok) {
                val note = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    "تم تحديث settings_secure بنجاح. ملاحظة: في أندرويد 8+ تستخدم التطبيقات الأخرى ملف SSAID مستقل، يُفضّل استخدام وضع LSPatch أو Root للتطبيقات الأخرى."
                } else {
                    "تم تعديل معرّف الجهاز بنجاح."
                }
                OperationResult(true, note)
            } else {
                OperationResult(false, "رفض النظام كتابة القيمة الجديدة في Settings.Secure.")
            }
        } catch (e: SecurityException) {
            OperationResult(false, "خطأ صلاحيات: ${e.message}")
        } catch (e: Throwable) {
            OperationResult(false, "فشل التعديل: ${e.message}")
        }
    }
}
