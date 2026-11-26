package com.android.ssaid.abdullah.core.root

data class ShellResult(
    val exitCode: Int,
    val output: String,
    val error: String
) {
    val isSuccess: Boolean get() = exitCode == 0
}
