package com.android.ssaid.abdullah.core.ssaid

import com.android.ssaid.abdullah.core.root.RootManager
import java.io.File

object AbxConverter {

    private val ABX_MAGIC = byteArrayOf(0x41, 0x42, 0x58, 0x00) // "ABX\0"

    fun isAbxFile(file: File): Boolean {
        if (!file.exists() || file.length() < 4) return false
        return try {
            val header = ByteArray(4)
            file.inputStream().use { it.read(header) }
            header.contentEquals(ABX_MAGIC)
        } catch (_: Throwable) {
            false
        }
    }

    fun decodeAbxToXml(inputPath: String, outputPath: String): Boolean {
        val result = RootManager.exec("abx2xml \"$inputPath\" \"$outputPath\"")
        return result.isSuccess
    }

    fun encodeXmlToAbx(inputPath: String, outputPath: String): Boolean {
        val result = RootManager.exec("xml2abx \"$inputPath\" \"$outputPath\"")
        return result.isSuccess
    }
}
