package takagi.ru.fleur.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import takagi.ru.fleur.domain.model.FleurError

/**
 * 错误显示组件
 * 根据错误类型显示不同的 UI
 */
@Composable
fun ErrorDisplay(
    error: FleurError?,
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)? = null,
    onReLogin: (() -> Unit)? = null,
    onClearCache: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    when (error) {
        is FleurError.NetworkError -> {
            NetworkErrorDialog(
                error = error,
                onDismiss = onDismiss,
                onRetry = onRetry
            )
        }
        is FleurError.AuthenticationError -> {
            AuthenticationErrorDialog(
                error = error,
                onDismiss = onDismiss,
                onReLogin = onReLogin
            )
        }
        is FleurError.StorageError -> {
            StorageErrorDialog(
                error = error,
                onDismiss = onDismiss,
                onClearCache = onClearCache
            )
        }
        is FleurError.ValidationError -> {
            // 验证错误通常在输入字段显示，这里不处理
        }
        else -> {
            // 其他错误显示通用对话框
            if (error != null) {
                GenericErrorDialog(
                    error = error,
                    onDismiss = onDismiss,
                    onRetry = onRetry
                )
            }
        }
    }
}

/**
 * 网络错误对话框
 */
@Composable
private fun NetworkErrorDialog(
    error: FleurError.NetworkError,
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)?
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "网络错误",
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text("网络连接失败")
        },
        text = {
            Column {
                Text(error.getUserMessage())
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "请检查您的网络连接后重试",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            if (onRetry != null) {
                Button(onClick = {
                    onDismiss()
                    onRetry()
                }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text("重试")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

/**
 * 认证错误对话框
 */
@Composable
private fun AuthenticationErrorDialog(
    error: FleurError.AuthenticationError,
    onDismiss: () -> Unit,
    onReLogin: (() -> Unit)?
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "认证错误",
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text("认证失败")
        },
        text = {
            Column {
                Text(error.getUserMessage())
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "您的登录凭证可能已过期，请重新登录",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            if (onReLogin != null) {
                Button(onClick = {
                    onDismiss()
                    onReLogin()
                }) {
                    Text("重新登录")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("稍后")
            }
        }
    )
}

/**
 * 存储错误对话框
 */
@Composable
private fun StorageErrorDialog(
    error: FleurError.StorageError,
    onDismiss: () -> Unit,
    onClearCache: (() -> Unit)?
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "存储错误",
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text("存储空间不足")
        },
        text = {
            Column {
                Text(error.getUserMessage())
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "您可以尝试清理缓存以释放空间",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            if (onClearCache != null) {
                Button(onClick = {
                    onDismiss()
                    onClearCache()
                }) {
                    Text("清理缓存")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 通用错误对话框
 */
@Composable
private fun GenericErrorDialog(
    error: FleurError,
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)?
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "错误",
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text("操作失败")
        },
        text = {
            Text(error.getUserMessage())
        },
        confirmButton = {
            if (onRetry != null) {
                Button(onClick = {
                    onDismiss()
                    onRetry()
                }) {
                    Text("重试")
                }
            } else {
                Button(onClick = onDismiss) {
                    Text("确定")
                }
            }
        },
        dismissButton = {
            if (onRetry != null) {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        }
    )
}

/**
 * 错误 Snackbar
 * 用于显示轻量级错误提示
 */
@Composable
fun ErrorSnackbar(
    snackbarHostState: SnackbarHostState,
    error: FleurError?,
    onRetry: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    LaunchedEffect(error) {
        error?.let {
            val result = snackbarHostState.showSnackbar(
                message = it.getUserMessage(),
                actionLabel = if (onRetry != null) "重试" else null,
                duration = SnackbarDuration.Long
            )
            
            when (result) {
                SnackbarResult.ActionPerformed -> {
                    onRetry?.invoke()
                }
                SnackbarResult.Dismissed -> {
                    onDismiss()
                }
            }
        }
    }
}

/**
 * 空状态显示
 * 用于显示错误后的空状态
 */
@Composable
fun ErrorEmptyState(
    error: FleurError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = error.getUserMessage(),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "请稍后重试",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedButton(onClick = onRetry) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null
            )
            Spacer(modifier = Modifier.padding(4.dp))
            Text("重试")
        }
    }
}
