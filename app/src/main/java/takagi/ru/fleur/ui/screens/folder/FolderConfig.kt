package takagi.ru.fleur.ui.screens.folder

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 文件夹配置
 * 定义每个文件夹页面的特定行为和外观
 * 
 * @property folderType 文件夹类型
 * @property title 页面标题
 * @property emptyStateConfig 空状态配置
 * @property swipeActions 滑动操作配置
 * @property showFab 是否显示浮动操作按钮
 * @property fabIcon FAB 图标
 * @property fabAction FAB 点击回调
 * @property topBarActions 顶部应用栏操作按钮列表
 */
data class FolderConfig(
    val folderType: FolderType,
    val title: String,
    val emptyStateConfig: EmptyStateConfig,
    val swipeActions: SwipeActionsConfig,
    val showFab: Boolean = false,
    val fabIcon: ImageVector? = null,
    val fabAction: (() -> Unit)? = null,
    val topBarActions: List<TopBarAction> = emptyList()
)

/**
 * 文件夹类型枚举
 */
enum class FolderType {
    /**
     * 已发送
     */
    SENT,
    
    /**
     * 草稿箱
     */
    DRAFTS,
    
    /**
     * 星标邮件
     */
    STARRED,
    
    /**
     * 归档
     */
    ARCHIVE,
    
    /**
     * 垃圾箱
     */
    TRASH
}

/**
 * 滑动操作配置
 * 
 * @property leftSwipe 左滑操作
 * @property rightSwipe 右滑操作
 */
data class SwipeActionsConfig(
    val leftSwipe: SwipeAction?,
    val rightSwipe: SwipeAction?
)

/**
 * 滑动操作
 * 
 * @property icon 操作图标
 * @property backgroundColor 背景颜色
 * @property action 操作类型
 */
data class SwipeAction(
    val icon: ImageVector,
    val backgroundColor: Color,
    val action: EmailAction
)

/**
 * 空状态配置
 * 
 * @property icon 显示的图标
 * @property title 标题文本
 * @property description 描述文本
 * @property actionButton 可选的操作按钮
 */
data class EmptyStateConfig(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val actionButton: ActionButton? = null
)

/**
 * 操作按钮
 * 
 * @property text 按钮文本
 * @property onClick 点击回调
 */
data class ActionButton(
    val text: String,
    val onClick: () -> Unit
)

/**
 * 顶部应用栏操作
 * 
 * @property icon 操作图标
 * @property contentDescription 无障碍描述
 * @property onClick 点击回调
 */
data class TopBarAction(
    val icon: ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit
)
