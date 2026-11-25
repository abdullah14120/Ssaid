package com.android.ssaid.abdullah.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.android.ssaid.abdullah.core.backend.BackendSelector
import com.android.ssaid.abdullah.core.backend.OperationResult
import com.android.ssaid.abdullah.core.root.RootManager
import com.android.ssaid.abdullah.core.shizuku.ShizukuManager
import com.android.ssaid.abdullah.data.model.AppSsaidInfo
import com.android.ssaid.abdullah.data.model.EnvironmentStatus
import com.android.ssaid.abdullah.data.model.WorkingMode

class SsaidRepository(private val context: Context) {

    val selector = BackendSelector(context)

    fun readOwnDeviceId(): String {
        return selector.secureSettingsBackend.readOwnSsaid()
    }

    fun inspectEnvironment(): EnvironmentStatus {
        ShizukuManager.refreshStatus()
        val hasRoot = selector.rootBackend.isAvailable()
        return EnvironmentStatus(
            hasWriteSecureSettings = selector.secureSettingsBackend.isAvailable(),
            isShizukuAlive = ShizukuManager.isBinderAlive.value,
            isShizukuGranted = ShizukuManager.isPermissionGranted.value,
            isShizukuServiceBound = ShizukuManager.isServiceBound.value,
            isRootAvailable = hasRoot,
            hasRootBackup = if (hasRoot) selector.rootBackend.hasBackup() else false,
            androidVersionLabel = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
        )
    }

    fun loadInstalledAppsWithSsaid(env: EnvironmentStatus): List<AppSsaidInfo> {
        val pm = context.packageManager
        val systemSsaidMap = mutableMapOf<String, String>()

        if (env.isRootAvailable) {
            systemSsaidMap.putAll(selector.rootBackend.readAllSsaids())
        }
        val ownId = readOwnDeviceId()
        if (ownId.isNotBlank() && !systemSsaidMap.containsKey(context.packageName)) {
            systemSsaidMap[context.packageName] = ownId
        }

        val hookOverrides = selector.hookBridgeBackend.readAllSsaids()

        val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledApplications(0)
        }

        return packages.map { appInfo ->
            val pkg = appInfo.packageName
            val label = try {
                pm.getApplicationLabel(appInfo).toString()
            } catch (_: Throwable) {
                pkg
            }
            val icon = try {
                pm.getApplicationIcon(appInfo)
            } catch (_: Throwable) {
                null
            }
            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            AppSsaidInfo(
                packageName = pkg,
                appName = label,
                uid = appInfo.uid,
                isSystemApp = isSystem,
                icon = icon,
                systemSsaid = systemSsaidMap[pkg],
                hookOverrideSsaid = hookOverrides[pkg]
            )
        }.sortedWith(
            compareByDescending<AppSsaidInfo> { it.packageName == context.packageName }
                .thenByDescending { it.hasHookOverride }
                .thenBy { it.isSystemApp }
                .thenBy { it.appName.lowercase() }
        )
    }

    fun updateSsaid(mode: WorkingMode, packageName: String, newSsaid: String): OperationResult {
        return selector.getBackend(mode).updateSsaid(packageName, newSsaid)
    }

    fun removeHookOverride(packageName: String): OperationResult {
        return selector.hookBridgeBackend.removeOverride(packageName)
    }

    fun grantSecureSettingsViaShizuku(): OperationResult {
        if (!ShizukuManager.isServiceBound.value) {
            return OperationResult(false, "يرجى تشغيل Shizuku ومنح الصلاحية أولاً.")
        }
        val ok = ShizukuManager.grantWriteSecureSettings(context.packageName)
        return if (ok) {
            OperationResult(true, "تم منح إذن WRITE_SECURE_SETTINGS تلقائياً عبر Shizuku!")
        } else {
            OperationResult(false, "فشل منح الإذن عبر Shizuku.")
        }
    }

    fun restoreRootBackup(): OperationResult = selector.rootBackend.restoreBackup()

    fun rebootDevice(): OperationResult {
        val res = RootManager.rebootDevice()
        return if (res.isSuccess) {
            OperationResult(true, "جارٍ إعادة تشغيل الجهاز...")
        } else {
            OperationResult(false, "فشل إعادة التشغيل: ${res.error}")
        }
    }
}
