package takagi.ru.fleur.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 同步状态指示器组件（本地优先架构）
 * 
 * 显示同步状态信息：
 * - 待同步数量
 * - 同步进度动画
 * - 最后同步时间
 * 
 * @param isSyncing 是否正在同步
 * @param pendingSyncCount 待同步操作数量
 * @param lastSyncTime 最后同步时间（可选）
 * @param modifier 修饰符
 */
@Composable
fun SyncStatusIndicator(
    isSyncing: Boolean,
    pendingSyncCount: Int = 0,
    lastSyncTime: String? = null,
    modifier: Modifier = Modifier
) {
    // 只在有待同步操作或正在同步时显示
    AnimatedVisibility(
        visible = isSyncing || pendingSyncCount > 0,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(
                    if (isSyncing) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    }
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 同步图标或进度指示器
            if (isSyncing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    imageVector = if (pendingSyncCount > 0) {
                        Icons.Default.CloudQueue
                    } else {
                        Icons.Default.CloudDone
                    },
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (pendingSyncCount > 0) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // 状态文本
            Text(
                text = when {
                    isSyncing -> "正在同步..."
                    pendingSyncCount > 0 -> "待同步: $pendingSyncCount 项操作"
                    else -> "已同步"
                },
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    fontWeight = if (isSyncing) FontWeight.Medium else FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // 最后同步时间（如果提供）
            lastSyncTime?.let { time ->
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "• $time",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * 离线指示器组件（紧凑版）
 * 
 * 在离线状态下显示一个小巧的标签，不占用太多空间
 * 
 * @param isOffline 是否离线
 * @param pendingOperationCount 待处理操作数量
 * @param modifier 修饰符
 */
@Composable
fun OfflineIndicator(
    isOffline: Boolean,
    pendingOperationCount: Int = 0,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isOffline,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f),
            tonalElevation = 2.dp,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = "离线",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                
                Text(
                    text = if (pendingOperationCount > 0) {
                        "离线 · $pendingOperationCount"
                    } else {
                        "离线"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}
