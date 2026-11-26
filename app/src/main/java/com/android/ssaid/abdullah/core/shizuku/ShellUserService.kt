package com.android.ssaid.abdullah.core.shizuku

import androidx.annotation.Keep
import com.android.ssaid.abdullah.IShellService
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.system.exitProcess

@Keep
class ShellUserService : IShellService.Stub() {

    override fun destroy() {
        exitProcess(0)
    }

    override fun runCommand(command: String?): String {
        if (command.isNullOrBlank()) return ""
        return try {
            val process = ProcessBuilder("sh", "-c", command)
                .redirectErrorStream(true)
                .start()
            val output = BufferedReader(InputStreamReader(process.inputStream)).use { it.readText() }
            process.waitFor()
            output.trim()
        } catch (e: Throwable) {
            "ERR: ${e.message}"
        }
    }

    override fun grantWriteSecureSettings(packageName: String?): Boolean {
        if (packageName.isNullOrBlank()) return false
        return try {
            val process = ProcessBuilder(
                "pm", "grant", packageName, "android.permission.WRITE_SECURE_SETTINGS"
            ).redirectErrorStream(true).start()
            process.waitFor() == 0
        } catch (_: Throwable) {
            false
        }
    }

    override fun getGlobalAndroidId(): String {
        return runCommand("settings get secure android_id")
    }

    override fun setGlobalAndroidId(newId: String?): Boolean {
        if (newId.isNullOrBlank()) return false
        return try {
            val process = ProcessBuilder(
                "settings", "put", "secure", "android_id", newId
            ).redirectErrorStream(true).start()
            process.waitFor() == 0
        } catch (_: Throwable) {
            false
        }
    }
}
