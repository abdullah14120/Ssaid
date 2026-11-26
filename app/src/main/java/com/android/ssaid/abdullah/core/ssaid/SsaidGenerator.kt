package com.android.ssaid.abdullah.core.ssaid

import java.security.SecureRandom

object SsaidGenerator {
    private val random = SecureRandom()
    private val hexChars = "0123456789abcdef".toCharArray()
    private val ssaidRegex = Regex("^[0-9a-f]{16}$")

    fun generate(): String {
        val chars = CharArray(16) { hexChars[random.nextInt(16)] }
        return String(chars)
    }

    fun normalize(input: String): String = input.trim().lowercase()

    fun isValid(input: String): Boolean = ssaidRegex.matches(normalize(input))
}
