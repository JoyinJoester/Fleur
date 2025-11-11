package takagi.ru.fleur.ui.theme

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically

/**
 * Fleur 动画系统
 * 定义所有动画的时长和缓动曲线
 */
object FleurAnimation {
    
    // ========== 动画时长 ==========
    
    /**
     * 微交互动画时长（涟漪、按钮）
     */
    const val MICRO_DURATION = 150
    
    /**
     * 快速动画时长（列表项出现）
     */
    const val FAST_DURATION = 200
    
    /**
     * 中等动画时长（Navigation Drawer）
     */
    const val MEDIUM_DURATION = 250
    
    /**
     * 标准动画时长（页面切换、视图切换）
     */
    const val STANDARD_DURATION = 300
    
    /**
     * 滑动操作动画时长
     */
    const val SWIPE_DURATION = 400
    
    /**
     * 主题切换动画时长
     */
    const val THEME_DURATION = 600
    
    /**
     * 列表项 stagger 延迟
     */
    const val STAGGER_DELAY = 50
    
    // ========== 缓动曲线 ==========
    
    /**
     * FastOutSlowIn 缓动曲线（默认）
     * 快速开始，缓慢结束，适合大多数动画
     */
    val FastOutSlowIn: Easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
    
    /**
     * DecelerateEasing 缓动曲线
     * 减速效果，适合滑动和滚动
     */
    val DecelerateEasing: Easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
    
    /**
     * AccelerateEasing 缓动曲线
     * 加速效果，适合退出动画
     */
    val AccelerateEasing: Easing = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)
    
    /**
     * LinearEasing 缓动曲线
     * 线性效果，适合持续动画
     */
    val LinearEasing: Easing = CubicBezierEasing(0.0f, 0.0f, 1.0f, 1.0f)
    
    // ========== 预定义动画规格 ==========
    
    /**
     * 微交互动画规格
     */
    fun <T> microSpec(): AnimationSpec<T> = tween(
        durationMillis = MICRO_DURATION,
        easing = FastOutSlowIn
    )
    
    /**
     * 快速动画规格
     */
    fun <T> fastSpec(): AnimationSpec<T> = tween(
        durationMillis = FAST_DURATION,
        easing = FastOutSlowIn
    )
    
    /**
     * 标准动画规格
     */
    fun <T> standardSpec(): AnimationSpec<T> = tween(
        durationMillis = STANDARD_DURATION,
        easing = FastOutSlowIn
    )
    
    /**
     * 滑动动画规格
     */
    fun <T> swipeSpec(): AnimationSpec<T> = tween(
        durationMillis = SWIPE_DURATION,
        easing = DecelerateEasing
    )
    
    /**
     * 带延迟的动画规格（用于 stagger 效果）
     */
    fun <T> staggerSpec(index: Int): AnimationSpec<T> = tween(
        durationMillis = FAST_DURATION,
        delayMillis = index * STAGGER_DELAY,
        easing = FastOutSlowIn
    )
}

/**
 * 列表项进入动画
 * 淡入 + 向上滑动
 */
fun listItemEnterAnimation(index: Int = 0) = fadeIn(
    animationSpec = tween(
        durationMillis = FleurAnimation.FAST_DURATION,
        delayMillis = index * FleurAnimation.STAGGER_DELAY,
        easing = FleurAnimation.FastOutSlowIn
    )
) + slideInVertically(
    animationSpec = tween(
        durationMillis = FleurAnimation.FAST_DURATION,
        delayMillis = index * FleurAnimation.STAGGER_DELAY,
        easing = FleurAnimation.FastOutSlowIn
    ),
    initialOffsetY = { it / 4 }
)

/**
 * 列表项退出动画
 * 淡出 + 向下滑动
 */
fun listItemExitAnimation() = fadeOut(
    animationSpec = tween(
        durationMillis = FleurAnimation.FAST_DURATION,
        easing = FleurAnimation.AccelerateEasing
    )
) + slideOutVertically(
    animationSpec = tween(
        durationMillis = FleurAnimation.FAST_DURATION,
        easing = FleurAnimation.AccelerateEasing
    ),
    targetOffsetY = { it / 4 }
)

/**
 * 页面切换进入动画（淡入）
 */
fun pageEnterAnimation() = fadeIn(
    animationSpec = tween(
        durationMillis = FleurAnimation.STANDARD_DURATION,
        easing = FleurAnimation.FastOutSlowIn
    )
)

/**
 * 页面切换退出动画（淡出）
 */
fun pageExitAnimation() = fadeOut(
    animationSpec = tween(
        durationMillis = FleurAnimation.STANDARD_DURATION,
        easing = FleurAnimation.FastOutSlowIn
    )
)

/**
 * 视图切换淡入动画
 */
fun viewSwitchEnterAnimation() = fadeIn(
    animationSpec = tween(
        durationMillis = FleurAnimation.STANDARD_DURATION,
        easing = FleurAnimation.FastOutSlowIn
    )
)

/**
 * 视图切换淡出动画
 */
fun viewSwitchExitAnimation() = fadeOut(
    animationSpec = tween(
        durationMillis = FleurAnimation.STANDARD_DURATION,
        easing = FleurAnimation.FastOutSlowIn
    )
)
