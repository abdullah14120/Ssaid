package com.android.ssaid.abdullah.core.root

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

object RootManager {

    fun isRootAvailable(): Boolean {
        return try {
            val res = exec("id")
            res.isSuccess && res.output.contains("uid=0")
        } catch (_: Throwable) {
            false
        }
    }

    fun exec(command: String): ShellResult {
        var process: Process? = null
        return try {
            process = ProcessBuilder("su").start()
            DataOutputStream(process.outputStream).use { os ->
                os.writeBytes("$command\n")
                os.writeBytes("exit\n")
                os.flush()
            }
            val out = BufferedReader(InputStreamReader(process.inputStream)).use { it.readText() }
            val err = BufferedReader(InputStreamReader(process.errorStream)).use { it.readText() }
            val code = process.waitFor()
            ShellResult(code, out.trim(), err.trim())
        } catch (e: Throwable) {
            ShellResult(-1, "", e.message ?: "Root execution failed")
        } finally {
            process?.destroy()
        }
    }

    fun rebootDevice(): ShellResult = exec("svc power reboot || reboot")
}
