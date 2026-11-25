package com.android.ssaid.abdullah.core.hook

import android.content.ContentResolver
import android.net.Uri
import android.provider.Settings
import androidx.annotation.Keep
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

@Keep
class SsaidHookEntry : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        val targetPkg = lpparam.packageName ?: return
        if (targetPkg == "com.android.ssaid.abdullah") return

        val hookCallback = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                try {
                    val key = param.args.getOrNull(1) as? String ?: return
                    if (key != Settings.Secure.ANDROID_ID) return
                    val resolver = param.args.getOrNull(0) as? ContentResolver ?: return
                    val spoofed = querySpoofedSsaid(resolver, targetPkg)
                    if (!spoofed.isNullOrBlank()) {
                        param.setResult(spoofed)
                    }
                } catch (t: Throwable) {
                    XposedBridge.log("SSAIDHook error ($targetPkg): ${t.message}")
                }
            }
        }

        try {
            XposedHelpers.findAndHookMethod(
                Settings.Secure::class.java,
                "getString",
                ContentResolver::class.java,
                String::class.java,
                hookCallback
            )
        } catch (t: Throwable) {
            XposedBridge.log("SSAIDHook getString hook failed: ${t.message}")
        }

        try {
            XposedHelpers.findAndHookMethod(
                Settings.Secure::class.java,
                "getStringForUser",
                ContentResolver::class.java,
                String::class.java,
                java.lang.Integer.TYPE,
                hookCallback
            )
        } catch (_: Throwable) {
        }
    }

    private fun querySpoofedSsaid(resolver: ContentResolver, packageName: String): String? {
        val uri = Uri.parse("content://${HookConfigProvider.AUTHORITY}/$packageName")
        return resolver.query(uri, arrayOf(HookConfigProvider.COLUMN_SSAID), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getString(0)
            } else {
                null
            }
        }
    }
}
