package com.android.ssaid.abdullah.core.backend

import android.content.Context
import android.os.Build
import com.android.ssaid.abdullah.core.root.RootManager
import com.android.ssaid.abdullah.core.ssaid.AbxConverter
import com.android.ssaid.abdullah.core.ssaid.SsaidGenerator
import com.android.ssaid.abdullah.core.ssaid.SsaidXmlParser
import com.android.ssaid.abdullah.data.model.WorkingMode
import java.io.File

class RootBackend(private val context: Context) : SsaidBackend {

    override val mode: WorkingMode = WorkingMode.ROOT

    private val systemSsaidPath = "/data/system/users/0/settings_ssaid.xml"
    private val backupSsaidPath = "/data/system/users/0/settings_ssaid.xml.abdullah.bak"

    override fun isAvailable(): Boolean = RootManager.isRootAvailable()

    fun hasBackup(): Boolean {
        val res = RootManager.exec("[ -f \"$backupSsaidPath\" ] && echo yes || echo no")
        return res.isSuccess && res.output == "yes"
    }

    override fun readAllSsaids(): Map<String, String> {
        if (!isAvailable()) return emptyMap()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            val res = RootManager.exec("settings get secure android_id")
            return if (res.isSuccess && res.output.isNotBlank()) {
                mapOf(context.packageName to res.output)
            } else {
                emptyMap()
            }
        }

        val xmlText = readSystemXmlText() ?: return emptyMap()
        return SsaidXmlParser.parse(xmlText).mapValues { it.value.ssaid }
    }

    override fun updateSsaid(packageName: String, newSsaid: String): OperationResult {
        val normalized = SsaidGenerator.normalize(newSsaid)
        if (!SsaidGenerator.isValid(normalized)) {
            return OperationResult(false, "المعرّف غير صالح، يجب أن يكون 16 خانة (0-9 و a-f).")
        }
        if (!isAvailable()) {
            return OperationResult(false, "صلاحية الروت غير متوفرة على هذا الجهاز.")
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            val res = RootManager.exec("settings put secure android_id $normalized")
            return if (res.isSuccess) {
                OperationResult(true, "تم تعديل معرّف الجهاز بالروت بنجاح.")
            } else {
                OperationResult(false, "فشل التعديل: ${res.error}")
            }
        }

        val workDir = File(context.cacheDir, "ssaid_work").apply { mkdirs() }
        val rawCopy = File(workDir, "raw_ssaid.xml")
        val plainXml = File(workDir, "plain_ssaid.xml")
        val modifiedXml = File(workDir, "modified_ssaid.xml")
        val finalOutput = File(workDir, "final_ssaid.xml")

        try {
            if (!hasBackup()) {
                RootManager.exec("cp \"$systemSsaidPath\" \"$backupSsaidPath\"")
            }

            val copyRes = RootManager.exec(
                "cp \"$systemSsaidPath\" \"${rawCopy.absolutePath}\" && chmod 644 \"${rawCopy.absolutePath}\""
            )
            if (!copyRes.isSuccess || !rawCopy.exists()) {
                return OperationResult(false, "تعذّر قراءة ملف $systemSsaidPath: ${copyRes.error}")
            }

            val wasAbx = AbxConverter.isAbxFile(rawCopy)
            val xmlContent = if (wasAbx) {
                if (!AbxConverter.decodeAbxToXml(rawCopy.absolutePath, plainXml.absolutePath)) {
                    return OperationResult(false, "فشل فك ترميز ملف ABX عبر abx2xml.")
                }
                RootManager.exec("chmod 644 \"${plainXml.absolutePath}\"")
                plainXml.readText()
            } else {
                rawCopy.readText()
            }

            val updatedXml = SsaidXmlParser.replacePackageSsaid(xmlContent, packageName, normalized)
                ?: return OperationResult(
                    false,
                    "لم يتم العثور على سجل SSAID للحزمة $packageName داخل settings_ssaid.xml (شغّل التطبيق المستهدف مرة واحدة على الأقل ثم حاول مجدداً)."
                )

            modifiedXml.writeText(updatedXml)

            val readyPath = if (wasAbx) {
                if (!AbxConverter.encodeXmlToAbx(modifiedXml.absolutePath, finalOutput.absolutePath)) {
                    return OperationResult(false, "فشل إعادة ترميز XML إلى ABX عبر xml2abx.")
                }
                finalOutput.absolutePath
            } else {
                modifiedXml.absolutePath
            }

            val writeCmd = listOf(
                "cp \"$readyPath\" \"$systemSsaidPath\"",
                "chown system:system \"$systemSsaidPath\"",
                "chmod 600 \"$systemSsaidPath\"",
                "restorecon \"$systemSsaidPath\""
            ).joinToString(" && ")

            val writeRes = RootManager.exec(writeCmd)
            return if (writeRes.isSuccess) {
                OperationResult(
                    success = true,
                    message = "تم تعديل SSAID لـ $packageName في ملف النظام بنجاح. يُنصح بإعادة تشغيل الجهاز لتفعيل القيمة في الذاكرة.",
                    requiresReboot = true
                )
            } else {
                OperationResult(false, "فشل حفظ الملف المعدّل في النظام: ${writeRes.error}")
            }
        } catch (e: Throwable) {
            return OperationResult(false, "حدث خطأ أثناء معالجة ملف SSAID: ${e.message}")
        } finally {
            rawCopy.delete()
            plainXml.delete()
            modifiedXml.delete()
            finalOutput.delete()
        }
    }

    fun restoreBackup(): OperationResult {
        if (!isAvailable()) return OperationResult(false, "صلاحية الروت غير متوفرة.")
        if (!hasBackup()) return OperationResult(false, "لا توجد نسخة احتياطية محفوظة.")
        val cmd = listOf(
            "cp \"$backupSsaidPath\" \"$systemSsaidPath\"",
            "chown system:system \"$systemSsaidPath\"",
            "chmod 600 \"$systemSsaidPath\"",
            "restorecon \"$systemSsaidPath\""
        ).joinToString(" && ")
        val res = RootManager.exec(cmd)
        return if (res.isSuccess) {
            OperationResult(true, "تمت استعادة النسخة الاحتياطية بنجاح. أعد تشغيل الجهاز.", requiresReboot = true)
        } else {
            OperationResult(false, "فشلت الاستعادة: ${res.error}")
        }
    }

    private fun readSystemXmlText(): String? {
        val workDir = File(context.cacheDir, "ssaid_read").apply { mkdirs() }
        val rawCopy = File(workDir, "raw.xml")
        val plainXml = File(workDir, "plain.xml")
        return try {
            val copyRes = RootManager.exec(
                "cp \"$systemSsaidPath\" \"${rawCopy.absolutePath}\" && chmod 644 \"${rawCopy.absolutePath}\""
            )
            if (!copyRes.isSuccess || !rawCopy.exists()) return null

            if (AbxConverter.isAbxFile(rawCopy)) {
                if (!AbxConverter.decodeAbxToXml(rawCopy.absolutePath, plainXml.absolutePath)) {
                    return null
                }
                RootManager.exec("chmod 644 \"${plainXml.absolutePath}\"")
                plainXml.readText()
            } else {
                rawCopy.readText()
            }
        } catch (_: Throwable) {
            null
        } finally {
            rawCopy.delete()
            plainXml.delete()
        }
    }
}
