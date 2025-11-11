package takagi.ru.fleur.ui.theme

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 玻璃拟态效果修饰符
 * 用于普通卡片和组件
 * 
 * @param blurRadius 模糊半径
 * @param backgroundColor 背景颜色（带透明度）
 * @param borderColor 边框颜色（可选）
 * @param borderWidth 边框宽度
 * @param shape 形状
 */
@Composable
fun Modifier.glassmorphism(
    blurRadius: Dp = 20.dp,
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    borderWidth: Dp = 1.dp,
    shape: Shape = RoundedCornerShape(12.dp)
): Modifier {
    val isDark = isSystemInDarkTheme()
    
    val bgColor = backgroundColor ?: if (isDark) {
        GlassSurfaceDark
    } else {
        GlassSurfaceLight
    }
    
    val border = borderColor ?: if (isDark) {
        GlassBorderDark
    } else {
        GlassBorderLight
    }
    
    return this
        .clip(shape)
        .then(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+ 使用原生模糊效果
                Modifier.blurEffect(blurRadius)
            } else {
                // 旧版本使用半透明背景模拟
                Modifier
            }
        )
        .background(bgColor, shape)
        .border(borderWidth, border, shape)
}

/**
 * 模糊效果修饰符
 * 用于覆盖层组件（Navigation Drawer, Bottom Sheet, Modal）
 * 
 * @param radius 模糊半径（8-12dp）
 */
@Composable
fun Modifier.blurEffect(radius: Dp = 10.dp): Modifier {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Android 12+ 使用原生模糊
        this.blur(radius)
    } else {
        // 旧版本降级处理
        this
    }
}

/**
 * 覆盖层玻璃拟态效果
 * 用于 Navigation Drawer, Bottom Sheet, Modal Dialog
 * 
 * @param blurRadius 模糊半径
 * @param opacity 不透明度 (0.0 - 1.0)
 * @param shape 形状
 */
@Composable
fun Modifier.overlayGlassmorphism(
    blurRadius: Dp = 10.dp,
    opacity: Float = 0.85f,
    shape: Shape = RectangleShape
): Modifier {
    val isDark = isSystemInDarkTheme()
    
    val backgroundColor = if (isDark) {
        DarkSurface.copy(alpha = opacity)
    } else {
        Color.White.copy(alpha = opacity)
    }
    
    val borderColor = if (isDark) {
        Color.White.copy(alpha = 0.2f)
    } else {
        Color.White.copy(alpha = 0.3f)
    }
    
    return this
        .clip(shape)
        .blurEffect(blurRadius)
        .background(backgroundColor, shape)
        .border(1.dp, borderColor, shape)
}

/**
 * Navigation Drawer 玻璃拟态效果
 * 10dp blur + 85% opacity
 */
@Composable
fun Modifier.drawerGlassmorphism(): Modifier {
    return overlayGlassmorphism(
        blurRadius = 10.dp,
        opacity = 0.85f,
        shape = RectangleShape
    )
}

/**
 * Bottom Sheet 玻璃拟态效果
 * 12dp blur + 90% opacity
 */
@Composable
fun Modifier.bottomSheetGlassmorphism(): Modifier {
    return overlayGlassmorphism(
        blurRadius = 12.dp,
        opacity = 0.90f,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    )
}

/**
 * Modal Dialog 玻璃拟态效果
 * 8dp blur + 80% opacity
 */
@Composable
fun Modifier.modalGlassmorphism(): Modifier {
    return overlayGlassmorphism(
        blurRadius = 8.dp,
        opacity = 0.80f,
        shape = RoundedCornerShape(16.dp)
    )
}
