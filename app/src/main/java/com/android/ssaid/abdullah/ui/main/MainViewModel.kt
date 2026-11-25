package com.android.ssaid.abdullah.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.ssaid.abdullah.core.shizuku.ShizukuManager
import com.android.ssaid.abdullah.data.model.AppSsaidInfo
import com.android.ssaid.abdullah.data.model.WorkingMode
import com.android.ssaid.abdullah.data.repository.SsaidRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SsaidRepository(application)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var initialModeSelected = false

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            val env = repository.inspectEnvironment()
            val ownId = repository.readOwnDeviceId()
            val apps = repository.loadInstalledAppsWithSsaid(env)

            val mode = if (!initialModeSelected) {
                initialModeSelected = true
                repository.selector.selectBestMode()
            } else {
                _uiState.value.selectedMode
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    ownDeviceId = ownId,
                    selectedMode = mode,
                    envStatus = env,
                    allApps = apps
                )
            }
        }
    }

    fun selectMode(mode: WorkingMode) {
        _uiState.update { it.copy(selectedMode = mode) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun selectFilter(filter: AppFilter) {
        _uiState.update { it.copy(appFilter = filter) }
    }

    fun openEditDialog(app: AppSsaidInfo) {
        _uiState.update { it.copy(editingApp = app) }
    }

    fun openQuickDeviceIdEditor() {
        val currentPkg = getApplication<Application>().packageName
        val ownApp = _uiState.value.allApps.firstOrNull { it.packageName == currentPkg }
            ?: AppSsaidInfo(
                packageName = currentPkg,
                appName = "رقم الجهاز الحالي",
                uid = 0,
                isSystemApp = false,
                icon = null,
                systemSsaid = _uiState.value.ownDeviceId,
                hookOverrideSsaid = null
            )
        _uiState.update { it.copy(editingApp = ownApp) }
    }

    fun dismissEditDialog() {
        _uiState.update { it.copy(editingApp = null) }
    }

    fun setAdbGuideVisible(visible: Boolean) {
        _uiState.update { it.copy(showAdbGuideDialog = visible) }
    }

    fun dismissRebootPrompt() {
        _uiState.update { it.copy(showRebootPrompt = false) }
    }

    fun showMessage(message: String) {
        _uiState.update { it.copy(statusMessage = message) }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    fun saveSsaid(packageName: String, newSsaid: String) {
        val mode = _uiState.value.selectedMode
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.updateSsaid(mode, packageName, newSsaid)
            _uiState.update {
                it.copy(
                    editingApp = if (result.success) null else it.editingApp,
                    statusMessage = result.message,
                    showRebootPrompt = result.requiresReboot
                )
            }
            if (result.success) {
                refreshAll()
            }
        }
    }

    fun removeHookOverride(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.removeHookOverride(packageName)
            _uiState.update {
                it.copy(
                    editingApp = null,
                    statusMessage = result.message
                )
            }
            refreshAll()
        }
    }

    fun requestShizukuOrGrant() {
        val env = _uiState.value.envStatus
        if (!env.isShizukuAlive) {
            _uiState.update {
                it.copy(statusMessage = "تطبيق Shizuku غير قيد التشغيل. افتح Shizuku وابدأ الخدمة أولاً.")
            }
            return
        }
        if (!env.isShizukuGranted) {
            ShizukuManager.requestPermission()
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.grantSecureSettingsViaShizuku()
            _uiState.update { it.copy(statusMessage = result.message) }
            refreshAll()
        }
    }

    fun restoreRootBackup() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.restoreRootBackup()
            _uiState.update {
                it.copy(
                    statusMessage = result.message,
                    showRebootPrompt = result.requiresReboot
                )
            }
            refreshAll()
        }
    }

    fun rebootDevice() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.rebootDevice()
            _uiState.update {
                it.copy(
                    showRebootPrompt = false,
                    statusMessage = result.message
                )
            }
        }
    }
}
