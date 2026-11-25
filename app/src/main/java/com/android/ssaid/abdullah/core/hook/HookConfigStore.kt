package com.android.ssaid.abdullah.core.hook

import android.content.Context
import com.android.ssaid.abdullah.core.ssaid.SsaidGenerator

class HookConfigStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getAllOverrides(): Map<String, String> {
        val result = mutableMapOf<String, String>()
        prefs.all.forEach { (key, value) ->
            if (value is String && SsaidGenerator.isValid(value)) {
                result[key] = value
            }
        }
        return result
    }

    fun getOverride(packageName: String): String? {
        return prefs.getString(packageName, null)?.takeIf { SsaidGenerator.isValid(it) }
    }

    fun setOverride(packageName: String, ssaid: String): Boolean {
        val normalized = SsaidGenerator.normalize(ssaid)
        if (!SsaidGenerator.isValid(normalized)) return false
        return prefs.edit().putString(packageName, normalized).commit()
    }

    fun removeOverride(packageName: String): Boolean {
        return prefs.edit().remove(packageName).commit()
    }

    fun clearAll(): Boolean {
        return prefs.edit().clear().commit()
    }

    companion object {
        private const val PREFS_NAME = "ssaid_hook_overrides"
    }
}
