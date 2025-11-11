package takagi.ru.fleur.ui.theme

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically

/**
 * Navigation Drawer 动画配置
 * 定义侧边栏所有动画的参数和规格
 */
object DrawerAnimations {
    
    // ========== 动画时长 ==========
    
    /**
     * 账户卡片展开/收起动画时长
     */
    const val AccountCardExpandDuration = 300
    
    /**
     * 图标旋转动画时长
     */
    const val IconRotationDuration = 250
    
    /**
     * 账户切换淡入淡出时长
     */
    const val AccountSwitchFadeDuration = 200
    
    /**
     * 悬停效果动画时长
     */
    const val HoverEffectDuration = 150
    
    /**
     * Badge 出现/消失动画时长
     */
    const val BadgeFadeDuration = 200
    
    /**
     * 列表项点击缩放动画时长
     */
    const val ItemScaleDuration = 100
    
    // ========== Spring 动画配置 ==========
    
    /**
     * 展开动画的 Spring 配置
     * 使用中等弹性和低刚度，产生柔和的弹跳效果
     */
    fun <T> expandSpring(): FiniteAnimationSpec<T> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    /**
     * 收起动画的 Spring 配置
     * 使用无弹性和中等刚度，产生快速收起效果
     */
    fun <T> collapseSpring(): FiniteAnimationSpec<T> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    /**
     * 图标旋转动画的 Spring 配置
     * 使用中等弹性和中等刚度，产生流畅的旋转效果
     */
    fun <T> iconRotationSpring(): FiniteAnimationSpec<T> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    /**
     * 悬停效果的 Spring 配置
     * 使用低弹性和高刚度，产生快速响应
     */
    fun <T> hoverSpring(): FiniteAnimationSpec<T> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessHigh
    )
    
    /**
     * 图标缩放动画的 Spring 配置
     * 使用中等弹性和高刚度，产生微妙的弹跳效果
     */
    fun <T> iconScaleSpring(): FiniteAnimationSpec<T> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessHigh
    )
    
    // ========== Tween 动画配置 ==========
    
    /**
     * 淡入动画规格
     */
    fun <T> fadeInSpec(): FiniteAnimationSpec<T> = tween(
        durationMillis = AccountSwitchFadeDuration,
        easing = FleurAnimation.FastOutSlowIn
    )
    
    /**
     * 淡出动画规格
     */
    fun <T> fadeOutSpec(): FiniteAnimationSpec<T> = tween(
        durationMillis = AccountSwitchFadeDuration,
        easing = FleurAnimation.AccelerateEasing
    )
    
    /**
     * Badge 淡入动画规格
     */
    fun <T> badgeFadeInSpec(): FiniteAnimationSpec<T> = tween(
        durationMillis = BadgeFadeDuration,
        easing = FleurAnimation.FastOutSlowIn
    )
    
    /**
     * Badge 淡出动画规格
     */
    fun <T> badgeFadeOutSpec(): FiniteAnimationSpec<T> = tween(
        durationMillis = BadgeFadeDuration,
        easing = FleurAnimation.AccelerateEasing
    )
    
    // ========== 组合动画 ==========
    
    /**
     * 账户卡片展开动画
     * 垂直展开 + 淡入
     */
    fun accountCardExpandAnimation() = expandVertically(
        animationSpec = expandSpring()
    ) + fadeIn(
        animationSpec = fadeInSpec()
    )
    
    /**
     * 账户卡片收起动画
     * 垂直收起 + 淡出
     */
    fun accountCardCollapseAnimation() = shrinkVertically(
        animationSpec = collapseSpring()
    ) + fadeOut(
        animationSpec = fadeOutSpec()
    )
    
    /**
     * 账户项进入动画
     * 向上滑入 + 淡入
     */
    fun accountItemEnterAnimation() = slideInVertically(
        animationSpec = expandSpring(),
        initialOffsetY = { it / 2 }
    ) + fadeIn(
        animationSpec = fadeInSpec()
    )
    
    /**
     * 账户项退出动画
     * 向下滑出 + 淡出
     */
    fun accountItemExitAnimation() = slideOutVertically(
        animationSpec = collapseSpring(),
        targetOffsetY = { it / 2 }
    ) + fadeOut(
        animationSpec = fadeOutSpec()
    )
    
    /**
     * Badge 出现动画
     */
    fun badgeEnterAnimation() = fadeIn(
        animationSpec = badgeFadeInSpec()
    )
    
    /**
     * Badge 消失动画
     */
    fun badgeExitAnimation() = fadeOut(
        animationSpec = badgeFadeOutSpec()
    )
    
    // ========== 缩放值 ==========
    
    /**
     * 悬停时的图标缩放比例
     */
    const val HoverIconScale = 1.1f
    
    /**
     * 点击时的项目缩放比例
     */
    const val PressedItemScale = 0.98f
    
    /**
     * 正常状态的缩放比例
     */
    const val NormalScale = 1.0f
    
    // ========== 旋转角度 ==========
    
    /**
     * 展开图标折叠状态的旋转角度
     */
    const val CollapsedRotation = 0f
    
    /**
     * 展开图标展开状态的旋转角度
     */
    const val ExpandedRotation = 180f
}
