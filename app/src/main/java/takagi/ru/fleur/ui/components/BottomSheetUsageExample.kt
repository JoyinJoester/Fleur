package takagi.ru.fleur.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import takagi.ru.fleur.domain.model.Account
import takagi.ru.fleur.domain.model.WebDAVConfig

/**
 * Modal Bottom Sheet 使用示例
 * 
 * 此文件展示如何在应用中使用三种 Bottom Sheet 组件
 */

/**
 * 示例 1: 邮件操作 Bottom Sheet
 * 
 * 在邮件详情页面或列表项中使用
 */
@Composable
fun EmailActionsBottomSheetExample() {
    var showBottomSheet by remember { mutableStateOf(false) }
    var isStarred by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = { showBottomSheet = true }) {
            Text("显示邮件操作菜单")
        }
        
        if (showBottomSheet) {
            EmailActionsBottomSheet(
                onDismiss = { showBottomSheet = false },
                onReply = {
                    // 导航到回复页面
                    println("回复邮件")
                },
                onForward = {
                    // 导航到转发页面
                    println("转发邮件")
                },
                onArchive = {
                    // 归档邮件
                    println("归档邮件")
                },
                onToggleStar = {
                    // 切换星标状态
                    isStarred = !isStarred
                    println("切换星标: $isStarred")
                },
                onDelete = {
                    // 删除邮件
                    println("删除邮件")
                },
                isStarred = isStarred
            )
        }
    }
}

/**
 * 示例 2: 账户选择器 Bottom Sheet
 * 
 * 在撰写邮件页面选择发件账户时使用
 */
@Composable
fun AccountSelectorBottomSheetExample() {
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedAccountId by remember { mutableStateOf<String?>("1") }
    
    // 模拟账户数据
    val accounts = listOf(
        Account(
            id = "1",
            email = "personal@example.com",
            displayName = "个人邮箱",
            color = Color(0xFF1976D2),
            isDefault = true,
            webdavConfig = WebDAVConfig(
                serverUrl = "https://mail.example.com",
                port = 443,
                username = "personal@example.com",
                useSsl = true,
                calendarPath = null,
                contactsPath = null
            )
        ),
        Account(
            id = "2",
            email = "work@company.com",
            displayName = "工作邮箱",
            color = Color(0xFF4CAF50),
            isDefault = false,
            webdavConfig = WebDAVConfig(
                serverUrl = "https://mail.company.com",
                port = 443,
                username = "work@company.com",
                useSsl = true,
                calendarPath = null,
                contactsPath = null
            )
        ),
        Account(
            id = "3",
            email = "school@university.edu",
            displayName = "学校邮箱",
            color = Color(0xFFFF9800),
            isDefault = false,
            webdavConfig = WebDAVConfig(
                serverUrl = "https://mail.university.edu",
                port = 443,
                username = "school@university.edu",
                useSsl = true,
                calendarPath = null,
                contactsPath = null
            )
        )
    )
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = { showBottomSheet = true }) {
            Text("选择发件账户")
        }
        
        if (showBottomSheet) {
            AccountSelectorBottomSheet(
                accounts = accounts,
                selectedAccountId = selectedAccountId,
                onDismiss = { showBottomSheet = false },
                onAccountSelected = { accountId ->
                    selectedAccountId = accountId
                    println("选择账户: $accountId")
                }
            )
        }
    }
}

/**
 * 示例 3: 附件操作 Bottom Sheet
 * 
 * 在邮件详情页面点击附件时使用
 */
@Composable
fun AttachmentOptionsBottomSheetExample() {
    var showBottomSheet by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = { showBottomSheet = true }) {
            Text("显示附件操作菜单")
        }
        
        if (showBottomSheet) {
            AttachmentOptionsBottomSheet(
                attachmentName = "presentation.pdf",
                attachmentSize = "2.5 MB",
                isImage = false,
                onDismiss = { showBottomSheet = false },
                onOpen = {
                    // 打开附件
                    println("打开附件")
                },
                onDownload = {
                    // 下载附件
                    println("下载附件")
                },
                onShare = {
                    // 分享附件
                    println("分享附件")
                }
            )
        }
    }
}

/**
 * 示例 4: 图片附件操作 Bottom Sheet
 * 
 * 图片类型的附件会显示额外的"保存到相册"选项
 */
@Composable
fun ImageAttachmentOptionsBottomSheetExample() {
    var showBottomSheet by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = { showBottomSheet = true }) {
            Text("显示图片附件操作菜单")
        }
        
        if (showBottomSheet) {
            AttachmentOptionsBottomSheet(
                attachmentName = "photo.jpg",
                attachmentSize = "1.2 MB",
                isImage = true,
                onDismiss = { showBottomSheet = false },
                onOpen = {
                    // 预览图片
                    println("预览图片")
                },
                onDownload = {
                    // 下载图片
                    println("下载图片")
                },
                onShare = {
                    // 分享图片
                    println("分享图片")
                },
                onSaveToGallery = {
                    // 保存到相册
                    println("保存到相册")
                }
            )
        }
    }
}

/**
 * 关键特性说明:
 * 
 * 1. 毛玻璃效果
 *    - 使用 bottomSheetGlassmorphism() 修饰符
 *    - 12dp blur + 90% opacity
 *    - 自动适配浅色/深色模式
 * 
 * 2. 滑入动画
 *    - 300ms 动画时长
 *    - DecelerateEasing 缓动曲线
 *    - 从底部滑入
 * 
 * 3. 手势拖拽
 *    - 支持向下拖拽关闭
 *    - 自定义拖拽手柄
 *    - 流畅的交互体验
 * 
 * 4. 自适应内容
 *    - 根据内容自动调整高度
 *    - 支持滚动（内容过多时）
 *    - skipPartiallyExpanded = false 允许部分展开
 * 
 * 5. 操作反馈
 *    - 点击项目后自动关闭
 *    - 200ms 延迟提供视觉反馈
 *    - 涟漪效果
 * 
 * 6. 可访问性
 *    - 所有图标都有 contentDescription
 *    - 触摸目标符合最小尺寸要求
 *    - 清晰的视觉层次
 */
