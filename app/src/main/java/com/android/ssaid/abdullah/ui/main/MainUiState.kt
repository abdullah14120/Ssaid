package com.android.ssaid.abdullah.ui.main

import com.android.ssaid.abdullah.data.model.AppSsaidInfo
import com.android.ssaid.abdullah.data.model.EnvironmentStatus
import com.android.ssaid.abdullah.data.model.WorkingMode

enum class AppFilter(val labelAr: String) {
    USER("تطبيقات المستخدم"),
    ALL("الكل"),
    MODIFIED("المخصّصة فقط")
}

data class MainUiState(
    val isLoading: Boolean = true,
    val ownDeviceId: String = "",
    val selectedMode: WorkingMode = WorkingMode.LSPATCH_HOOK,
    val envStatus: EnvironmentStatus = EnvironmentStatus(),
    val allApps: List<AppSsaidInfo> = emptyList(),
    val searchQuery: String = "",
    val appFilter: AppFilter = AppFilter.USER,
    val editingApp: AppSsaidInfo? = null,
    val showAdbGuideDialog: Boolean = false,
    val showRebootPrompt: Boolean = false,
    val statusMessage: String? = null
) {
    val filteredApps: List<AppSsaidInfo>
        get() = allApps.filter { app ->
            val matchesFilter = when (appFilter) {
                AppFilter.USER -> !app.isSystemApp || app.hasHookOverride
                AppFilter.ALL -> true
                AppFilter.MODIFIED -> app.hasHookOverride
            }
            val q = searchQuery.trim().lowercase()
            val matchesQuery = q.isEmpty() ||
                app.appName.lowercase().contains(q) ||
                app.packageName.lowercase().contains(q) ||
                (app.displayedSsaid?.lowercase()?.contains(q) == true)
            matchesFilter && matchesQuery
        }
}
