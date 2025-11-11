package takagi.ru.fleur.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import takagi.ru.fleur.domain.model.Account
import takagi.ru.fleur.ui.theme.DrawerAnimations
import takagi.ru.fleur.ui.theme.DrawerDimens

/**
 * 折叠状态的账户视图
 * 显示当前账户的头像、名称、邮箱和展开图标
 * 
 * @param account 当前账户
 * @param isExpanded 是否处于展开状态
 * @param onToggleExpand 切换展开/收起的回调
 */
@Composable
fun CollapsedAccountView(
    account: Account?,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 展开图标的旋转角度动画
    val iconRotation by animateFloatAsState(
        targetValue = if (isExpanded) {
            DrawerAnimations.ExpandedRotation
        } else {
            DrawerAnimations.CollapsedRotation
        },
        animationSpec = DrawerAnimations.iconRotationSpring(),
        label = "icon_rotation"
    )
    
    // 语义标签，用于无障碍支持
    val expandStateDescription = if (isExpanded) "已展开" else "已折叠"
    val contentDesc = "账户卡片，${account?.displayName ?: "未登录"}，$expandStateDescription"
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = contentDesc
                role = Role.Button
            }
            .clickable(onClick = onToggleExpand),
        shape = RoundedCornerShape(DrawerDimens.AccountCardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = DrawerDimens.AccountCardElevation
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DrawerDimens.AccountCardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 左侧：头像和账户信息
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // 圆形头像
                Box(
                    modifier = Modifier
                        .size(DrawerDimens.AccountCardAvatarSize)
                        .clip(CircleShape)
                        .background(account?.color ?: MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = account?.displayName?.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.width(DrawerDimens.AccountCardAvatarTextSpacing))
                
                // 账户名称和邮箱
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = account?.displayName ?: "未登录",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = account?.email ?: "添加账户",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            
            // 右侧：展开图标
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = if (isExpanded) "收起账户列表" else "展开账户列表",
                modifier = Modifier
                    .size(DrawerDimens.AccountCardExpandIconSize)
                    .rotate(iconRotation),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}


/**
 * 展开状态的账户列表
 * 显示所有账户，支持滚动和选择
 * 
 * @param accounts 账户列表
 * @param currentAccountId 当前选中的账户ID
 * @param onAccountSelected 账户选择回调
 */
@Composable
fun ExpandedAccountList(
    accounts: List<Account>,
    currentAccountId: String?,
    onAccountSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = DrawerDimens.AccountCardExpandedMaxHeight),
        verticalArrangement = Arrangement.spacedBy(DrawerDimens.ItemSpacing)
    ) {
        items(
            items = accounts,
            key = { account -> account.id }
        ) { account ->
            ExpandedAccountItem(
                account = account,
                isSelected = account.id == currentAccountId,
                onClick = { onAccountSelected(account.id) }
            )
        }
    }
}

/**
 * 展开列表中的单个账户项
 * 显示头像、名称、邮箱和默认账户标记
 * 
 * @param account 账户信息
 * @param isSelected 是否为当前选中账户
 * @param onClick 点击回调
 */
@Composable
private fun ExpandedAccountItem(
    account: Account,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // 根据选中状态设置背景色
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        Color.Transparent
    }
    
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(DrawerDimens.AccountItemHeight)
            .clip(RoundedCornerShape(DrawerDimens.AccountItemCornerRadius))
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Button
                this.contentDescription = "${account.displayName}, ${account.email}" +
                    if (account.isDefault) ", 默认账户" else ""
            },
        color = backgroundColor,
        shape = RoundedCornerShape(DrawerDimens.AccountItemCornerRadius)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(DrawerDimens.AccountItemPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 左侧：头像和账户信息
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // 圆形头像
                Box(
                    modifier = Modifier
                        .size(DrawerDimens.AccountItemAvatarSize)
                        .clip(CircleShape)
                        .background(account.color),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = account.displayName.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.width(DrawerDimens.AccountItemAvatarTextSpacing))
                
                // 账户名称和邮箱
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = account.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = contentColor
                    )
                    Text(
                        text = account.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }
            }
            
            // 右侧：默认账户星标
            if (account.isDefault) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "默认账户",
                    modifier = Modifier.size(DrawerDimens.AccountItemStarIconSize),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}


/**
 * 可展开的账户卡片主组件
 * 整合折叠和展开状态，管理展开/收起逻辑和动画
 * 处理空账户列表、单个账户和多个账户的不同情况
 * 
 * @param currentAccount 当前账户
 * @param accounts 所有账户列表
 * @param onAccountSelected 账户选择回调
 * @param onNavigateToAccountManagement 导航到账户管理的回调
 */
@Composable
fun ExpandableAccountCard(
    currentAccount: Account?,
    accounts: List<Account>,
    onAccountSelected: (String) -> Unit,
    onNavigateToAccountManagement: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 展开状态管理
    var isExpanded by remember { mutableStateOf(false) }
    
    // 判断是否应该显示展开功能
    // 只有多个账户时才允许展开
    val canExpand = accounts.size > 1
    
    // 处理空账户列表的情况
    if (accounts.isEmpty() && currentAccount == null) {
        // 显示"添加账户"占位符
        EmptyAccountPlaceholder(
            onNavigateToAccountManagement = onNavigateToAccountManagement,
            modifier = modifier
        )
        return
    }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DrawerDimens.ItemSpacing)
    ) {
        // 折叠状态视图（始终显示）
        CollapsedAccountView(
            account = currentAccount,
            isExpanded = isExpanded && canExpand,
            onToggleExpand = {
                if (canExpand) {
                    // 多个账户时切换展开状态
                    isExpanded = !isExpanded
                } else {
                    // 单个或无账户时直接导航到账户管理
                    onNavigateToAccountManagement()
                }
            }
        )
        
        // 展开状态视图（带动画）
        // 只在有多个账户时显示
        AnimatedVisibility(
            visible = isExpanded && canExpand,
            enter = DrawerAnimations.accountCardExpandAnimation(),
            exit = DrawerAnimations.accountCardCollapseAnimation()
        ) {
            // 使用 AnimatedContent 实现账户切换时的过渡效果
            AnimatedContent(
                targetState = currentAccount?.id,
                transitionSpec = {
                    fadeIn(animationSpec = DrawerAnimations.fadeInSpec()) togetherWith
                        fadeOut(animationSpec = DrawerAnimations.fadeOutSpec())
                },
                label = "account_switch"
            ) { _ ->
                ExpandedAccountList(
                    accounts = accounts,
                    currentAccountId = currentAccount?.id,
                    onAccountSelected = { accountId ->
                        onAccountSelected(accountId)
                        // 选择账户后自动收起
                        isExpanded = false
                    }
                )
            }
        }
    }
}

/**
 * 空账户占位符
 * 当没有账户时显示，引导用户添加账户
 * 
 * @param onNavigateToAccountManagement 导航到账户管理的回调
 */
@Composable
private fun EmptyAccountPlaceholder(
    onNavigateToAccountManagement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onNavigateToAccountManagement)
            .semantics {
                contentDescription = "添加账户"
                role = Role.Button
            },
        shape = RoundedCornerShape(DrawerDimens.AccountCardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = DrawerDimens.AccountCardElevation
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DrawerDimens.AccountCardPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 占位符图标
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(DrawerDimens.AccountCardAvatarSize),
                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
            )
            
            // 提示文本
            Text(
                text = "未登录",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Text(
                text = "点击添加账户",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}
