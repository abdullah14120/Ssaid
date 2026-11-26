package com.android.ssaid.abdullah.core.shizuku

import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import com.android.ssaid.abdullah.BuildConfig
import com.android.ssaid.abdullah.IShellService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import rikka.shizuku.Shizuku

object ShizukuManager {

    const val SHIZUKU_PERMISSION_REQUEST_CODE = 1412

    private val _isBinderAlive = MutableStateFlow(false)
    val isBinderAlive: StateFlow<Boolean> = _isBinderAlive.asStateFlow()

    private val _isPermissionGranted = MutableStateFlow(false)
    val isPermissionGranted: StateFlow<Boolean> = _isPermissionGranted.asStateFlow()

    private val _isServiceBound = MutableStateFlow(false)
    val isServiceBound: StateFlow<Boolean> = _isServiceBound.asStateFlow()

    @Volatile
    private var shellService: IShellService? = null

    private var onStateChanged: (() -> Unit)? = null

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        refreshStatus()
        if (isGranted()) {
            bindService()
        }
        onStateChanged?.invoke()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        shellService = null
        _isBinderAlive.value = false
        _isServiceBound.value = false
        onStateChanged?.invoke()
    }

    private val permissionResultListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == SHIZUKU_PERMISSION_REQUEST_CODE) {
            val granted = grantResult == PackageManager.PERMISSION_GRANTED
            _isPermissionGranted.value = granted
            if (granted) {
                bindService()
            }
            onStateChanged?.invoke()
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            shellService = IShellService.Stub.asInterface(service)
            _isServiceBound.value = shellService != null
            onStateChanged?.invoke()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            shellService = null
            _isServiceBound.value = false
            onStateChanged?.invoke()
        }
    }

    private val userServiceArgs: Shizuku.UserServiceArgs by lazy {
        Shizuku.UserServiceArgs(
            ComponentName(BuildConfig.APPLICATION_ID, ShellUserService::class.java.name)
        )
            .daemon(false)
            .processNameSuffix("ssaid_shell")
            .debuggable(BuildConfig.DEBUG)
            .version(BuildConfig.VERSION_CODE)
    }

    fun registerListeners(callback: () -> Unit) {
        onStateChanged = callback
        try {
            Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
            Shizuku.addRequestPermissionResultListener(permissionResultListener)
        } catch (_: Throwable) {
        }
        refreshStatus()
        if (isGranted()) {
            bindService()
        }
    }

    fun unregisterListeners() {
        onStateChanged = null
        try {
            Shizuku.removeBinderReceivedListener(binderReceivedListener)
            Shizuku.removeBinderDeadListener(binderDeadListener)
            Shizuku.removeRequestPermissionResultListener(permissionResultListener)
        } catch (_: Throwable) {
        }
    }

    fun refreshStatus() {
        val alive = try {
            Shizuku.pingBinder()
        } catch (_: Throwable) {
            false
        }
        _isBinderAlive.value = alive
        _isPermissionGranted.value = alive && isGranted()
    }

    fun isGranted(): Boolean {
        return try {
            if (!Shizuku.pingBinder()) return false
            if (Shizuku.isPreV11()) false
            else Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (_: Throwable) {
            false
        }
    }

    fun requestPermission() {
        try {
            if (Shizuku.pingBinder() && !Shizuku.isPreV11()) {
                Shizuku.requestPermission(SHIZUKU_PERMISSION_REQUEST_CODE)
            }
        } catch (_: Throwable) {
        }
    }

    fun bindService() {
        if (!isGranted()) return
        try {
            Shizuku.bindUserService(userServiceArgs, serviceConnection)
        } catch (_: Throwable) {
        }
    }

    fun unbindService() {
        try {
            Shizuku.unbindUserService(userServiceArgs, serviceConnection, true)
        } catch (_: Throwable) {
        }
        shellService = null
        _isServiceBound.value = false
    }

    fun grantWriteSecureSettings(packageName: String): Boolean {
        return try {
            shellService?.grantWriteSecureSettings(packageName) ?: false
        } catch (_: Throwable) {
            false
        }
    }

    fun getGlobalAndroidId(): String? {
        return try {
            shellService?.getGlobalAndroidId()?.takeIf { it.isNotBlank() && it != "null" }
        } catch (_: Throwable) {
            null
        }
    }

    fun setGlobalAndroidId(newId: String): Boolean {
        return try {
            shellService?.setGlobalAndroidId(newId) ?: false
        } catch (_: Throwable) {
            false
        }
    }
}
