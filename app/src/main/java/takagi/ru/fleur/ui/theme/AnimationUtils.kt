package takagi.ru.fleur.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch

/**
 * 涟漪效果修饰符
 * 点击时产生缩放动画
 * 
 * @param onClick 点击回调
 */
fun Modifier.rippleEffect(
    onClick: () -> Unit
): Modifier = composed {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    
    this
        .scale(scale.value)
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    scope.launch {
                        scale.animateTo(
                            targetValue = 0.95f,
                            animationSpec = tween(
                                durationMillis = FleurAnimation.MICRO_DURATION / 2
                            )
                        )
                        scale.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(
                                durationMillis = FleurAnimation.MICRO_DURATION / 2
                            )
                        )
                    }
                    tryAwaitRelease()
                },
                onTap = {
                    onClick()
                }
            )
        }
}

/**
 * 悬停缩放效果修饰符
 * 悬停时轻微放大
 * 
 * @param isHovered 是否悬停
 * @param scale 缩放比例
 */
@Composable
fun Modifier.hoverScale(
    isHovered: Boolean,
    scale: Float = 1.02f
): Modifier {
    val animatedScale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    
    scope.launch {
        animatedScale.animateTo(
            targetValue = if (isHovered) scale else 1f,
            animationSpec = tween(
                durationMillis = FleurAnimation.MICRO_DURATION,
                easing = FleurAnimation.FastOutSlowIn
            )
        )
    }
    
    return this.scale(animatedScale.value)
}

/**
 * 创建动画浮点值
 * 用于自定义动画
 */
@Composable
fun rememberAnimatedFloat(
    initialValue: Float = 0f
): Animatable<Float, AnimationVector1D> {
    return remember { Animatable(initialValue) }
}

/**
 * 弹性动画
 * 用于强调效果
 */
suspend fun Animatable<Float, AnimationVector1D>.bounceAnimation(
    targetValue: Float = 1f,
    bounceScale: Float = 1.1f
) {
    // 先放大
    animateTo(
        targetValue = bounceScale,
        animationSpec = tween(
            durationMillis = FleurAnimation.MICRO_DURATION,
            easing = FleurAnimation.FastOutSlowIn
        )
    )
    // 再回到目标值
    animateTo(
        targetValue = targetValue,
        animationSpec = tween(
            durationMillis = FleurAnimation.MICRO_DURATION,
            easing = FleurAnimation.FastOutSlowIn
        )
    )
}

/**
 * 脉冲动画
 * 用于吸引注意力
 */
suspend fun Animatable<Float, AnimationVector1D>.pulseAnimation(
    minScale: Float = 0.95f,
    maxScale: Float = 1.05f,
    repeat: Int = 2
) {
    repeat(repeat) {
        animateTo(
            targetValue = maxScale,
            animationSpec = tween(
                durationMillis = FleurAnimation.FAST_DURATION,
                easing = FleurAnimation.FastOutSlowIn
            )
        )
        animateTo(
            targetValue = minScale,
            animationSpec = tween(
                durationMillis = FleurAnimation.FAST_DURATION,
                easing = FleurAnimation.FastOutSlowIn
            )
        )
    }
    animateTo(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = FleurAnimation.FAST_DURATION,
            easing = FleurAnimation.FastOutSlowIn
        )
    )
}
