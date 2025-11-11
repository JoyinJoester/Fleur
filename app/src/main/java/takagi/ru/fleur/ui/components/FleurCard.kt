package takagi.ru.fleur.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import takagi.ru.fleur.ui.theme.FleurAnimation
import takagi.ru.fleur.ui.theme.FleurElevation
import takagi.ru.fleur.ui.theme.glassmorphism

/**
 * Fleur 卡片组件
 * 支持悬停和选中状态，带有流畅的动画效果
 * 
 * @param modifier 修饰符
 * @param isHovered 是否悬停状态
 * @param isSelected 是否选中状态
 * @param shape 形状
 * @param onClick 点击回调（可选）
 * @param content 内容
 */
@Composable
fun FleurCard(
    modifier: Modifier = Modifier,
    isHovered: Boolean = false,
    isSelected: Boolean = false,
    shape: Shape = RoundedCornerShape(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    
    // 动画：elevation 提升 (M3E优化: 4dp → 8dp, 150ms)
    val elevation by animateDpAsState(
        targetValue = if (isHovered) FleurElevation.Level8 else FleurElevation.Level4,
        animationSpec = tween(
            durationMillis = FleurAnimation.MICRO_DURATION,
            easing = FleurAnimation.FastOutSlowIn
        ),
        label = "elevation"
    )
    
    // 动画：缩放效果 (1.0 → 1.02, 150ms)
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.02f else 1f,
        animationSpec = tween(
            durationMillis = FleurAnimation.MICRO_DURATION,
            easing = FleurAnimation.FastOutSlowIn
        ),
        label = "scale"
    )
    
    // 动画：选中状态边框宽度 (0dp → 2dp)
    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 2.dp else 0.dp,
        animationSpec = tween(
            durationMillis = FleurAnimation.MICRO_DURATION,
            easing = FleurAnimation.FastOutSlowIn
        ),
        label = "borderWidth"
    )
    
    // 卡片容器颜色
    val containerColor = if (isDark) {
        MaterialTheme.colorScheme.surface
    } else {
        Color.Transparent // 浅色模式使用玻璃拟态
    }
    
    // 自定义涟漪效果，使用150ms duration
    val interactionSource = remember { MutableInteractionSource() }
    val ripple = rememberRipple(
        bounded = true,
        color = MaterialTheme.colorScheme.primary
    )
    
    Box(
        modifier = modifier.scale(scale)
    ) {
        Row {
            // 选中状态：左侧蓝色竖条
            if (isSelected) {
                Spacer(
                    modifier = Modifier
                        .width(4.dp)
                        .padding(vertical = 8.dp)
                )
            }
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (!isDark) {
                            // 浅色模式应用玻璃拟态
                            Modifier.glassmorphism(shape = shape)
                        } else {
                            Modifier
                        }
                    ),
                shape = shape,
                colors = CardDefaults.cardColors(
                    containerColor = containerColor
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = elevation
                ),
                // 选中状态边框动画
                border = if (borderWidth > 0.dp) {
                    BorderStroke(borderWidth, MaterialTheme.colorScheme.primary)
                } else null,
                onClick = onClick ?: {},
                interactionSource = interactionSource
            ) {
                content()
            }
        }
    }
}

/**
 * 简化版 FleurCard
 * 用于不需要交互状态的场景
 */
@Composable
fun SimpleFleurCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    elevation: Dp = FleurElevation.Level4,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    
    val containerColor = if (isDark) {
        MaterialTheme.colorScheme.surface
    } else {
        Color.Transparent
    }
    
    Card(
        modifier = modifier.then(
            if (!isDark) {
                Modifier.glassmorphism(shape = shape)
            } else {
                Modifier
            }
        ),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation
        )
    ) {
        content()
    }
}
