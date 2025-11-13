package takagi.ru.fleur.ui.theme

import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.pow

/**
 * 无障碍性工具类
 * 
 * 提供符合 WCAG 标准的无障碍性支持
 */
object AccessibilityUtils {
    
    /**
     * 最小触摸目标大小（符合无障碍规范）
     * 
     * 根据 Material Design 和 WCAG 指南，
     * 所有可交互元素的最小触摸目标应为 48dp × 48dp
     */
    val MinTouchTargetSize: Dp = 48.dp
    
    /**
     * 推荐的触摸目标大小
     * 
     * 对于重要操作，建议使用更大的触摸目标
     */
    val RecommendedTouchTargetSize: Dp = 56.dp
    
    /**
     * 为 Modifier 添加最小触摸目标大小
     * 
     * 确保元素符合无障碍规范
     * 
     * @param size 触摸目标大小，默认为 48dp
     * @return 应用了最小尺寸的 Modifier
     */
    fun Modifier.minimumTouchTarget(size: Dp = MinTouchTargetSize): Modifier {
        return this.size(size)
    }
    
    /**
     * 计算颜色对比度
     * 
     * 根据 WCAG 2.1 标准计算两个颜色之间的对比度
     * 
     * @param foreground 前景色
     * @param background 背景色
     * @return 对比度值（1-21）
     */
    fun calculateContrastRatio(foreground: Color, background: Color): Float {
        val fgLuminance = calculateRelativeLuminance(foreground)
        val bgLuminance = calculateRelativeLuminance(background)
        
        val lighter = maxOf(fgLuminance, bgLuminance)
        val darker = minOf(fgLuminance, bgLuminance)
        
        return (lighter + 0.05f) / (darker + 0.05f)
    }
    
    /**
     * 计算相对亮度
     * 
     * 根据 WCAG 2.1 标准计算颜色的相对亮度
     * 
     * @param color 颜色
     * @return 相对亮度值（0-1）
     */
    private fun calculateRelativeLuminance(color: Color): Float {
        val r = linearizeColorComponent(color.red)
        val g = linearizeColorComponent(color.green)
        val b = linearizeColorComponent(color.blue)
        
        return 0.2126f * r + 0.7152f * g + 0.0722f * b
    }
    
    /**
     * 线性化颜色分量
     * 
     * 将 sRGB 颜色分量转换为线性值
     * 
     * @param component 颜色分量（0-1）
     * @return 线性化后的值
     */
    private fun linearizeColorComponent(component: Float): Float {
        return if (component <= 0.03928f) {
            component / 12.92f
        } else {
            ((component + 0.055f) / 1.055f).pow(2.4f)
        }
    }
    
    /**
     * 检查对比度是否符合 WCAG AA 标准
     * 
     * WCAG AA 标准要求：
     * - 普通文本：对比度至少 4.5:1
     * - 大文本（18pt 或 14pt 粗体）：对比度至少 3:1
     * 
     * @param foreground 前景色
     * @param background 背景色
     * @param isLargeText 是否为大文本
     * @return 是否符合标准
     */
    fun meetsWCAGAA(
        foreground: Color,
        background: Color,
        isLargeText: Boolean = false
    ): Boolean {
        val ratio = calculateContrastRatio(foreground, background)
        val minimumRatio = if (isLargeText) 3.0f else 4.5f
        return ratio >= minimumRatio
    }
    
    /**
     * 检查对比度是否符合 WCAG AAA 标准
     * 
     * WCAG AAA 标准要求：
     * - 普通文本：对比度至少 7:1
     * - 大文本（18pt 或 14pt 粗体）：对比度至少 4.5:1
     * 
     * @param foreground 前景色
     * @param background 背景色
     * @param isLargeText 是否为大文本
     * @return 是否符合标准
     */
    fun meetsWCAGAAA(
        foreground: Color,
        background: Color,
        isLargeText: Boolean = false
    ): Boolean {
        val ratio = calculateContrastRatio(foreground, background)
        val minimumRatio = if (isLargeText) 4.5f else 7.0f
        return ratio >= minimumRatio
    }
    
    /**
     * 获取对比度等级描述
     * 
     * @param foreground 前景色
     * @param background 背景色
     * @param isLargeText 是否为大文本
     * @return 对比度等级描述
     */
    fun getContrastRatingDescription(
        foreground: Color,
        background: Color,
        isLargeText: Boolean = false
    ): String {
        val ratio = calculateContrastRatio(foreground, background)
        
        return when {
            meetsWCAGAAA(foreground, background, isLargeText) -> 
                "优秀 (AAA) - 对比度: ${String.format("%.2f", ratio)}:1"
            meetsWCAGAA(foreground, background, isLargeText) -> 
                "良好 (AA) - 对比度: ${String.format("%.2f", ratio)}:1"
            else -> 
                "不合格 - 对比度: ${String.format("%.2f", ratio)}:1"
        }
    }
}

/**
 * Modifier 扩展：应用最小触摸目标大小
 * 
 * 使用示例：
 * ```kotlin
 * IconButton(
 *     onClick = { },
 *     modifier = Modifier.minimumTouchTarget()
 * ) {
 *     Icon(...)
 * }
 * ```
 */
fun Modifier.minimumTouchTarget(size: Dp = AccessibilityUtils.MinTouchTargetSize): Modifier {
    return this.size(size)
}
