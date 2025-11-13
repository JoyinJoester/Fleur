package takagi.ru.fleur.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * 权限处理工具
 * 
 * 提供相机和存储权限的请求和检查功能
 */
object PermissionHandler {
    
    /**
     * 检查相机权限
     * 
     * @param context 上下文
     * @return 是否已授权
     */
    fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * 检查存储权限
     * 
     * @param context 上下文
     * @return 是否已授权
     */
    fun hasStoragePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ 使用新的媒体权限
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 12 及以下使用旧的存储权限
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * 获取需要请求的存储权限
     * 
     * @return 权限数组
     */
    fun getStoragePermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
}

/**
 * 相机权限请求 Composable
 * 
 * @param onPermissionGranted 权限授予回调
 * @param onPermissionDenied 权限拒绝回调
 * @param content 内容，接收一个请求权限的函数
 */
@Composable
fun CameraPermissionRequest(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit = {},
    content: @Composable (requestPermission: () -> Unit) -> Unit
) {
    val context = LocalContext.current
    var showRationale by remember { mutableStateOf(false) }
    
    // 权限请求启动器
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onPermissionGranted()
        } else {
            onPermissionDenied()
        }
    }
    
    // 请求权限函数
    val requestPermission = {
        if (PermissionHandler.hasCameraPermission(context)) {
            onPermissionGranted()
        } else {
            showRationale = true
        }
    }
    
    // 权限说明对话框
    if (showRationale) {
        AlertDialog(
            onDismissRequest = {
                showRationale = false
                onPermissionDenied()
            },
            title = { Text("需要相机权限") },
            text = { Text("为了拍摄照片，需要访问您的相机。请在下一步中授予相机权限。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRationale = false
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                ) {
                    Text("授予权限")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRationale = false
                        onPermissionDenied()
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }
    
    content(requestPermission)
}

/**
 * 存储权限请求 Composable
 * 
 * @param onPermissionGranted 权限授予回调
 * @param onPermissionDenied 权限拒绝回调
 * @param content 内容，接收一个请求权限的函数
 */
@Composable
fun StoragePermissionRequest(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit = {},
    content: @Composable (requestPermission: () -> Unit) -> Unit
) {
    val context = LocalContext.current
    var showRationale by remember { mutableStateOf(false) }
    
    // 权限请求启动器
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            onPermissionGranted()
        } else {
            onPermissionDenied()
        }
    }
    
    // 请求权限函数
    val requestPermission = {
        if (PermissionHandler.hasStoragePermission(context)) {
            onPermissionGranted()
        } else {
            showRationale = true
        }
    }
    
    // 权限说明对话框
    if (showRationale) {
        AlertDialog(
            onDismissRequest = {
                showRationale = false
                onPermissionDenied()
            },
            title = { Text("需要存储权限") },
            text = { Text("为了选择图片和文件，需要访问您的存储空间。请在下一步中授予存储权限。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRationale = false
                        permissionLauncher.launch(PermissionHandler.getStoragePermissions())
                    }
                ) {
                    Text("授予权限")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRationale = false
                        onPermissionDenied()
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }
    
    content(requestPermission)
}
