package com.android.ssaid.abdullah.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.android.ssaid.abdullah.core.shizuku.ShizukuManager
import com.android.ssaid.abdullah.ui.theme.SSAIDManagerTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ShizukuManager.registerListeners {
            viewModel.refreshAll()
        }

        setContent {
            SSAIDManagerTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onDestroy() {
        ShizukuManager.unregisterListeners()
        if (isFinishing) {
            ShizukuManager.unbindService()
        }
        super.onDestroy()
    }
}
