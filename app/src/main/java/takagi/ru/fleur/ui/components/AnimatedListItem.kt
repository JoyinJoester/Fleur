package takagi.ru.fleur.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import takagi.ru.fleur.ui.theme.listItemEnterAnimation
import takagi.ru.fleur.ui.theme.listItemExitAnimation

/**
 * 带动画的列表项容器
 * 封装列表项的进入和退出动画
 * 
 * @param index 列表项索引（用于 stagger 效果）
 * @param visible 是否可见
 * @param modifier 修饰符
 * @param content 内容
 */
@Composable
fun AnimatedListItem(
    index: Int = 0,
    visible: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val visibleState = remember(index) {
        MutableTransitionState(true).apply {
            targetState = visible
        }
    }
    
    AnimatedVisibility(
        visibleState = visibleState,
        modifier = modifier,
        enter = listItemEnterAnimation(index),
        exit = listItemExitAnimation()
    ) {
        content()
    }
}

/**
 * 简化版动画列表项
 * 不带 stagger 效果
 */
@Composable
fun SimpleAnimatedListItem(
    visible: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedListItem(
        index = 0,
        visible = visible,
        modifier = modifier,
        content = content
    )
}
