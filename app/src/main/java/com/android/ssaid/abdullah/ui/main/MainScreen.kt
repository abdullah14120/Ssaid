package com.android.ssaid.abdullah.ui.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.android.ssaid.abdullah.core.ssaid.SsaidGenerator
import com.android.ssaid.abdullah.data.model.AppSsaidInfo
import com.android.ssaid.abdullah.data.model.EnvironmentStatus
import com.android.ssaid.abdullah.data.model.WorkingMode
import com.android.ssaid.abdullah.ui.theme.AccentAmber
import com.android.ssaid.abdullah.ui.theme.AccentEmerald

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.statusMessage) {
        val msg = state.statusMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "مدير رقم الجهاز SSAID",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = state.envStatus.androidVersionLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    if (state.envStatus.hasRootBackup) {
                        IconButton(onClick = { viewModel.restoreRootBackup() }) {
                            Icon(Icons.Rounded.Restore, contentDescription = "استعادة النسخة الاحتياطية")
                        }
                    }
                    IconButton(onClick = { viewModel.setAdbGuideVisible(true) }) {
                        Icon(Icons.Rounded.Terminal, contentDescription = "أمر ADB والمساعدة")
                    }
                    IconButton(onClick = { viewModel.refreshAll() }) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "تحديث")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                DeviceIdHeroCard(
                    deviceId = state.ownDeviceId,
                    selectedMode = state.selectedMode,
                    onCopy = {
                        copyToClipboard(context, "رقم الجهاز", state.ownDeviceId)
                        viewModel.showMessage("تم نسخ رقم الجهاز إلى الحافظة")
                    },
                    onEdit = { viewModel.openQuickDeviceIdEditor() }
                )
            }

            item {
                EnvironmentStatusCard(
                    status = state.envStatus,
                    onShizukuAction = { viewModel.requestShizukuOrGrant() },
                    onOpenAdbGuide = { viewModel.setAdbGuideVisible(true) }
                )
            }

            item {
                ModeSelectorSection(
                    selectedMode = state.selectedMode,
                    onSelectMode = { viewModel.selectMode(it) }
                )
            }

            item {
                SearchAndFilterBar(
                    query = state.searchQuery,
                    onQueryChange = { viewModel.updateSearchQuery(it) },
                    selectedFilter = state.appFilter,
                    onSelectFilter = { viewModel.selectFilter(it) }
                )
            }

            if (state.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (state.filteredApps.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد تطبيقات مطابقة للبحث",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(
                    items = state.filteredApps,
                    key = { it.packageName }
                ) { app ->
                    AppSsaidRow(
                        app = app,
                        onCopy = { id ->
                            copyToClipboard(context, app.appName, id)
                            viewModel.showMessage("تم نسخ SSAID الخاص بـ ${app.appName}")
                        },
                        onClick = { viewModel.openEditDialog(app) }
                    )
                }
            }
        }
    }

    state.editingApp?.let { targetApp ->
        EditSsaidDialog(
            app = targetApp,
            mode = state.selectedMode,
            onDismiss = { viewModel.dismissEditDialog() },
            onSave = { newId -> viewModel.saveSsaid(targetApp.packageName, newId) },
            onClearOverride = { viewModel.removeHookOverride(targetApp.packageName) }
        )
    }

    if (state.showAdbGuideDialog) {
        AdbAndNoRootGuideDialog(
            packageName = context.packageName,
            onCopyCommand = { cmd ->
                copyToClipboard(context, "ADB Command", cmd)
                viewModel.showMessage("تم نسخ أمر ADB إلى الحافظة")
            },
            onDismiss = { viewModel.setAdbGuideVisible(false) }
        )
    }

    if (state.showRebootPrompt) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissRebootPrompt() },
            icon = { Icon(Icons.Rounded.RestartAlt, contentDescription = null) },
            title = { Text("إعادة تشغيل الجهاز؟") },
            text = {
                Text("تم تعديل ملف settings_ssaid.xml في النظام. لتفعيل القيمة الجديدة داخل ذاكرة SettingsProvider يُنصح بإعادة التشغيل الآن.")
            },
            confirmButton = {
                Button(onClick = { viewModel.rebootDevice() }) {
                    Text("إعادة التشغيل الآن")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissRebootPrompt() }) {
                    Text("لاحقاً")
                }
            }
        )
    }
}

@Composable
private fun DeviceIdHeroCard(
    deviceId: String,
    selectedMode: WorkingMode,
    onCopy: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF0F172A), Color(0xFF1E3A8A))
                    )
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Smartphone,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "رقم الجهاز (ANDROID_ID / SSAID)",
                            color = Color(0xFFCBD5E1),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    Surface(
                        color = Color(0x3338BDF8),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = selectedMode.titleAr,
                            color = Color(0xFF7DD3FC),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = deviceId.ifBlank { "غير متوفر" },
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.2.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onCopy,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("نسخ الرقم")
                    }

                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Edit,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("تعديل الرقم", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun EnvironmentStatusCard(
    status: EnvironmentStatus,
    onShizukuAction: () -> Unit,
    onOpenAdbGuide: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "حالة الصلاحيات المتاحة",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusBadge(
                    label = "WRITE_SECURE_SETTINGS",
                    active = status.hasWriteSecureSettings,
                    onClick = onOpenAdbGuide
                )
                StatusBadge(
                    label = when {
                        status.isShizukuServiceBound -> "Shizuku متصل"
                        status.isShizukuAlive -> "Shizuku يحتاج إذن"
                        else -> "Shizuku غير نشط"
                    },
                    active = status.isShizukuServiceBound,
                    onClick = onShizukuAction
                )
                StatusBadge(
                    label = "LSPatch Hook جاهز",
                    active = true,
                    onClick = onOpenAdbGuide
                )
                StatusBadge(
                    label = if (status.isRootAvailable) "Root متاح" else "بدون Root",
                    active = status.isRootAvailable,
                    onClick = {}
                )
            }

            if (!status.hasWriteSecureSettings && status.isShizukuAlive) {
                AssistChip(
                    onClick = onShizukuAction,
                    label = {
                        Text(
                            if (status.isShizukuGranted)
                                "منح إذن WRITE_SECURE_SETTINGS تلقائياً عبر Shizuku"
                            else
                                "طلب صلاحية Shizuku الآن"
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Rounded.Shield, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (active) AccentEmerald.copy(alpha = 0.14f) else AccentAmber.copy(alpha = 0.14f)
    val contentColor = if (active) AccentEmerald else AccentAmber

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (active) Icons.Rounded.CheckCircle else Icons.Rounded.Warning,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(15.dp)
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = label,
                color = contentColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ModeSelectorSection(
    selectedMode: WorkingMode,
    onSelectMode: (WorkingMode) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "وضع التعديل النشط",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WorkingMode.entries.forEach { mode ->
                FilterChip(
                    selected = mode == selectedMode,
                    onClick = { onSelectMode(mode) },
                    label = { Text(mode.titleAr) }
                )
            }
        }
        Text(
            text = selectedMode.descriptionAr,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SearchAndFilterBar(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedFilter: AppFilter,
    onSelectFilter: (AppFilter) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("ابحث باسم التطبيق أو الحزمة أو SSAID...") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Rounded.Close, contentDescription = "مسح")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppFilter.entries.forEach { filter ->
                FilterChip(
                    selected = filter == selectedFilter,
                    onClick = { onSelectFilter(filter) },
                    label = { Text(filter.labelAr) }
                )
            }
        }
    }
}

@Composable
private fun AppSsaidRow(
    app: AppSsaidInfo,
    onCopy: (String) -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIconView(drawable = app.icon)

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (app.hasHookOverride) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            color = AccentEmerald.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "مخصّص",
                                color = AccentEmerald,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = app.displayedSsaid ?: "انقر لتعيين SSAID مخصص",
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = if (app.displayedSsaid != null) FontFamily.Monospace else FontFamily.Default,
                    color = if (app.displayedSsaid != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!app.displayedSsaid.isNullOrBlank()) {
                IconButton(onClick = { onCopy(app.displayedSsaid!!) }) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = "نسخ",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "تعديل",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun AppIconView(drawable: Drawable?) {
    val bitmap = remember(drawable) {
        try {
            drawable?.toBitmap(width = 96, height = 96)?.asImageBitmap()
        } catch (_: Throwable) {
            null
        }
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
        )
    } else {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Apps, contentDescription = null)
        }
    }
}

@Composable
private fun EditSsaidDialog(
    app: AppSsaidInfo,
    mode: WorkingMode,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onClearOverride: () -> Unit
) {
    var ssaidInput by remember(app) {
        mutableStateOf(app.displayedSsaid ?: SsaidGenerator.generate())
    }
    val isValid = SsaidGenerator.isValid(ssaidInput)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(text = app.appName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = "${app.packageName} • ${mode.titleAr}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = ssaidInput,
                    onValueChange = { if (it.length <= 16) ssaidInput = it.lowercase() },
                    label = { Text("معرّف SSAID (16 خانة Hex)") },
                    singleLine = true,
                    isError = !isValid,
                    supportingText = {
                        Text(
                            if (isValid) "صيغة صالحة (${ssaidInput.length}/16)"
                            else "يجب إدخال 16 حرفاً من 0-9 و a-f"
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { ssaidInput = SsaidGenerator.generate() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Rounded.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("توليد عشوائي")
                    }

                    if (app.hasHookOverride && mode == WorkingMode.LSPATCH_HOOK) {
                        OutlinedButton(
                            onClick = onClearOverride
                        ) {
                            Icon(Icons.Rounded.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(ssaidInput) },
                enabled = isValid
            ) {
                Text("حفظ التعديل")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
private fun AdbAndNoRootGuideDialog(
    packageName: String,
    onCopyCommand: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val adbCommand = "adb shell pm grant $packageName android.permission.WRITE_SECURE_SETTINGS"
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.Info, contentDescription = null) },
        title = { Text("طرق التعديل بدون روت") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "1. عبر ADB (لمرة واحدة):",
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = adbCommand,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                OutlinedButton(
                    onClick = { onCopyCommand(adbCommand) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("نسخ أمر ADB")
                }

                Text(
                    text = "2. عبر Shizuku: شغّل Shizuku بالتصحيح اللاسلكي واضغط «منح الإذن تلقائياً» من الشاشة الرئيسية دون حاسوب.",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "3. عبر LSPatch (الأقوى لأندرويد 8–15+ بدون روت): احقن التطبيق المستهدف بوحدة «مدير رقم الجهاز SSAID» ثم حدّد له رقماً مخصصاً من وضع LSPatch Hook داخل التطبيق.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("فهمت")
            }
        }
    )
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
    cm.setPrimaryClip(ClipData.newPlainText(label, text))
}
