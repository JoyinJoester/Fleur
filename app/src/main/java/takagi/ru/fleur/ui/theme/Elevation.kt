package takagi.ru.fleur.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Fleur 阴影系统
 * 定义柔和阴影的 elevation 级别
 */
object FleurElevation {
    /**
     * Level 0: 无阴影
     */
    val Level0: Dp = 0.dp
    
    /**
     * Level 1: 轻微阴影
     * 模糊半径: 4px, 不透明度: 0.06
     * 用于: 悬浮按钮、卡片默认状态
     */
    val Level1: Dp = 1.dp
    
    /**
     * Level 2: 柔和阴影
     * 模糊半径: 8px, 不透明度: 0.08
     * 用于: 卡片、按钮、输入框
     */
    val Level2: Dp = 2.dp
    
    /**
     * Level 3: 中等阴影
     * 模糊半径: 10px, 不透明度: 0.10
     * 用于: 悬停状态的卡片
     */
    val Level3: Dp = 3.dp
    
    /**
     * Level 4: 明显阴影
     * 模糊半径: 12px, 不透明度: 0.12
     * 用于: 弹出菜单、对话框
     */
    val Level4: Dp = 4.dp
    
    /**
     * Level 5: 强阴影
     * 模糊半径: 14px, 不透明度: 0.14
     * 用于: 模态对话框
     */
    val Level5: Dp = 5.dp
    
    /**
     * Level 6: 最强阴影
     * 模糊半径: 16px, 不透明度: 0.16
     * 用于: 悬停时的卡片、FAB
     */
    val Level6: Dp = 6.dp
    
    /**
     * Level 8: 超强阴影
     * 模糊半径: 20px, 不透明度: 0.18
     * 用于: M3E 悬停状态的卡片
     */
    val Level8: Dp = 8.dp
}

/**
 * 阴影配置
 * @property elevation 高度
 * @property blurRadius 模糊半径（像素）
 * @property opacity 不透明度 (0.0 - 1.0)
 */
data class ShadowConfig(
    val elevation: Dp,
    val blurRadius: Float,
    val opacity: Float
)

/**
 * 获取阴影配置
 */
fun getShadowConfig(elevation: Dp): ShadowConfig {
    return when (elevation) {
        FleurElevation.Level0 -> ShadowConfig(elevation, 0f, 0f)
        FleurElevation.Level1 -> ShadowConfig(elevation, 4f, 0.06f)
        FleurElevation.Level2 -> ShadowConfig(elevation, 8f, 0.08f)
        FleurElevation.Level3 -> ShadowConfig(elevation, 10f, 0.10f)
        FleurElevation.Level4 -> ShadowConfig(elevation, 12f, 0.12f)
        FleurElevation.Level5 -> ShadowConfig(elevation, 14f, 0.14f)
        FleurElevation.Level6 -> ShadowConfig(elevation, 16f, 0.16f)
        FleurElevation.Level8 -> ShadowConfig(elevation, 20f, 0.18f)
        else -> ShadowConfig(elevation, 8f, 0.08f)
    }
}
