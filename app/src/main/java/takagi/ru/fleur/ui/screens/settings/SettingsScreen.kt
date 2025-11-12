package takagi.ru.fleur.ui.screens.settings

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import takagi.ru.fleur.domain.model.SwipeAction
import takagi.ru.fleur.domain.model.ThemeMode

/**
 * 设置页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // 外观设置
            SettingsSectionHeader("外观")
            
            ThemeModeSetting(
                currentMode = uiState.themeMode,
                onModeChange = { viewModel.setThemeMode(it) }
            )
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                SwitchSetting(
                    title = "动态配色",
                    subtitle = "使用系统壁纸颜色",
                    checked = uiState.useDynamicColor,
                    onCheckedChange = { viewModel.setDynamicColor(it) }
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // WebDAV 同步设置（本地优先架构）
            SettingsSectionHeader("WebDAV 同步")
            
            SwitchSetting(
                title = "启用 WebDAV 同步",
                subtitle = "将本地操作同步到 WebDAV 服务器",
                checked = uiState.webdavEnabled,
                onCheckedChange = { viewModel.setWebdavEnabled(it) }
            )
            
            if (uiState.webdavEnabled) {
                WebdavConfigSection(
                    webdavUrl = uiState.webdavUrl,
                    webdavUsername = uiState.webdavUsername,
                    isSyncing = uiState.isSyncing,
                    pendingSyncCount = uiState.pendingSyncCount,
                    lastSyncTime = uiState.lastSyncTime,
                    syncError = uiState.syncError,
                    onManualSync = { viewModel.triggerManualSync() },
                    onConfigureWebdav = { viewModel.showWebdavConfigDialog() }
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // 同步设置
            SettingsSectionHeader("同步")
            
            SyncIntervalSetting(
                currentInterval = uiState.syncInterval,
                onIntervalChange = { viewModel.setSyncInterval(it) }
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // 通知设置
            SettingsSectionHeader("通知")
            
            SwitchSetting(
                title = "启用通知",
                subtitle = "接收新邮件通知",
                checked = uiState.notificationsEnabled,
                onCheckedChange = { viewModel.setNotificationsEnabled(it) }
            )
            
            if (uiState.notificationsEnabled) {
                SwitchSetting(
                    title = "通知声音",
                    subtitle = null,
                    checked = uiState.notificationSound,
                    onCheckedChange = { viewModel.setNotificationSound(it) }
                )
                
                SwitchSetting(
                    title = "通知震动",
                    subtitle = null,
                    checked = uiState.notificationVibration,
                    onCheckedChange = { viewModel.setNotificationVibration(it) }
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // 手势设置
            SettingsSectionHeader("手势")
            
            SwipeActionSetting(
                title = "右滑操作",
                currentAction = uiState.swipeRightAction,
                onActionChange = { viewModel.setSwipeRightAction(it) }
            )
            
            SwipeActionSetting(
                title = "左滑操作",
                currentAction = uiState.swipeLeftAction,
                onActionChange = { viewModel.setSwipeLeftAction(it) }
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // 其他设置
            SettingsSectionHeader("其他")
            
            ListItem(
                headlineContent = { Text("重置为默认设置") },
                modifier = Modifier.clickable { showResetDialog = true }
            )
        }
    }
    
    // 重置确认对话框
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("重置设置") },
            text = { Text("确定要将所有设置重置为默认值吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetToDefaults()
                        showResetDialog = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 设置分组标题
 */
@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

/**
 * 开关设置项
 */
@Composable
private fun SwitchSetting(
    title: String,
    subtitle: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = subtitle?.let { { Text(it) } },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        },
        modifier = Modifier.clickable { onCheckedChange(!checked) }
    )
}

/**
 * 主题模式设置
 */
@Composable
private fun ThemeModeSetting(
    currentMode: ThemeMode,
    onModeChange: (ThemeMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    ListItem(
        headlineContent = { Text("主题") },
        supportingContent = { 
            Text(
                when (currentMode) {
                    ThemeMode.LIGHT -> "浅色"
                    ThemeMode.DARK -> "深色"
                    ThemeMode.SYSTEM -> "跟随系统"
                }
            )
        },
        modifier = Modifier.clickable { expanded = true }
    )
    
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        DropdownMenuItem(
            text = { Text("浅色") },
            onClick = {
                onModeChange(ThemeMode.LIGHT)
                expanded = false
            }
        )
        DropdownMenuItem(
            text = { Text("深色") },
            onClick = {
                onModeChange(ThemeMode.DARK)
                expanded = false
            }
        )
        DropdownMenuItem(
            text = { Text("跟随系统") },
            onClick = {
                onModeChange(ThemeMode.SYSTEM)
                expanded = false
            }
        )
    }
}

/**
 * 同步间隔设置
 */
@Composable
private fun SyncIntervalSetting(
    currentInterval: Int,
    onIntervalChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    ListItem(
        headlineContent = { Text("同步间隔") },
        supportingContent = { Text("$currentInterval 分钟") },
        modifier = Modifier.clickable { expanded = true }
    )
    
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        listOf(5, 15, 30, 60).forEach { interval ->
            DropdownMenuItem(
                text = { Text("$interval 分钟") },
                onClick = {
                    onIntervalChange(interval)
                    expanded = false
                }
            )
        }
    }
}

/**
 * 滑动操作设置
 */
@Composable
private fun SwipeActionSetting(
    title: String,
    currentAction: SwipeAction,
    onActionChange: (SwipeAction) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { 
            Text(
                when (currentAction) {
                    SwipeAction.ARCHIVE -> "归档"
                    SwipeAction.DELETE -> "删除"
                    SwipeAction.MARK_READ -> "标记已读"
                    SwipeAction.MARK_UNREAD -> "标记未读"
                    SwipeAction.STAR -> "星标"
                }
            )
        },
        modifier = Modifier.clickable { expanded = true }
    )
    
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        DropdownMenuItem(
            text = { Text("归档") },
            onClick = {
                onActionChange(SwipeAction.ARCHIVE)
                expanded = false
            }
        )
        DropdownMenuItem(
            text = { Text("删除") },
            onClick = {
                onActionChange(SwipeAction.DELETE)
                expanded = false
            }
        )
        DropdownMenuItem(
            text = { Text("标记已读") },
            onClick = {
                onActionChange(SwipeAction.MARK_READ)
                expanded = false
            }
        )
        DropdownMenuItem(
            text = { Text("标记未读") },
            onClick = {
                onActionChange(SwipeAction.MARK_UNREAD)
                expanded = false
            }
        )
        DropdownMenuItem(
            text = { Text("星标") },
            onClick = {
                onActionChange(SwipeAction.STAR)
                expanded = false
            }
        )
    }
}


/**
 * WebDAV 配置部分（本地优先架构）
 * 显示 WebDAV 配置信息、同步状态和手动同步按钮
 */
@Composable
private fun WebdavConfigSection(
    webdavUrl: String,
    webdavUsername: String,
    isSyncing: Boolean,
    pendingSyncCount: Int,
    lastSyncTime: String?,
    syncError: String?,
    onManualSync: () -> Unit,
    onConfigureWebdav: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        // WebDAV 服务器信息
        if (webdavUrl.isNotBlank()) {
            ListItem(
                headlineContent = { Text("服务器地址") },
                supportingContent = { Text(webdavUrl) },
                modifier = Modifier.clickable { onConfigureWebdav() }
            )
            
            if (webdavUsername.isNotBlank()) {
                ListItem(
                    headlineContent = { Text("用户名") },
                    supportingContent = { Text(webdavUsername) }
                )
            }
        } else {
            ListItem(
                headlineContent = { Text("配置 WebDAV") },
                supportingContent = { Text("点击配置 WebDAV 服务器") },
                modifier = Modifier.clickable { onConfigureWebdav() }
            )
        }
        
        // 同步状态
        ListItem(
            headlineContent = { Text("同步状态") },
            supportingContent = {
                Column {
                    Text(
                        when {
                            isSyncing -> "正在同步..."
                            pendingSyncCount > 0 -> "待同步: $pendingSyncCount 项操作"
                            else -> "已同步"
                        }
                    )
                    lastSyncTime?.let {
                        Text(
                            text = "最后同步: $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    syncError?.let {
                        Text(
                            text = "错误: $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        )
        
        // 手动同步按钮
        ListItem(
            headlineContent = { Text("手动同步") },
            supportingContent = { Text("立即同步到 WebDAV 服务器") },
            modifier = Modifier.clickable(enabled = !isSyncing) { onManualSync() }
        )
    }
}
