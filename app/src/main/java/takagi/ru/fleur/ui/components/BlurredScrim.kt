package takagi.ru.fleur.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import takagi.ru.fleur.ui.theme.ScrimDark
import takagi.ru.fleur.ui.theme.ScrimLight

/**
 * 模糊遮罩层组件
 * 用于覆盖层（Drawer, Bottom Sheet, Modal）的背景
 * 
 * @param visible 是否可见
 * @param onDismiss 点击遮罩层时的回调
 */
@Composable
fun BlurredScrim(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (visible) {
        val isDark = isSystemInDarkTheme()
        val scrimColor = if (isDark) ScrimDark else ScrimLight
        
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(scrimColor)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
        )
    }
}

/**
 * 轻量级遮罩层
 * 用于较轻的覆盖场景
 * 
 * @param visible 是否可见
 * @param onDismiss 点击遮罩层时的回调
 * @param alpha 不透明度 (0.0 - 1.0)
 */
@Composable
fun LightScrim(
    visible: Boolean,
    onDismiss: () -> Unit,
    alpha: Float = 0.3f,
    modifier: Modifier = Modifier
) {
    if (visible) {
        val isDark = isSystemInDarkTheme()
        val scrimColor = if (isDark) {
            ScrimDark.copy(alpha = alpha)
        } else {
            ScrimLight.copy(alpha = alpha)
        }
        
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(scrimColor)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
        )
    }
}
