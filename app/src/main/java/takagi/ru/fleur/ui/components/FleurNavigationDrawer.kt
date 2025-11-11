package takagi.ru.fleur.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import takagi.ru.fleur.domain.model.Account
import takagi.ru.fleur.ui.theme.DrawerDimens

/**
 * Fleur 导航抽屉
 * Material 3 风格的模态导航抽屉
 * 
 * @param visible 是否可见
 * @param currentAccount 当前账户
 * @param accounts 账户列表
 * @param unreadCounts 各文件夹未读数（inbox, sent, drafts, starred, archive, trash）
 * @param onDismiss 关闭回调
 * @param onNavigateToInbox 导航到收件箱
 * @param onNavigateToSent 导航到已发送
 * @param onNavigateToDrafts 导航到草稿箱
 * @param onNavigateToStarred 导航到星标邮件
 * @param onNavigateToArchive 导航到归档
 * @param onNavigateToTrash 导航到垃圾箱
 * @param onNavigateToSettings 导航到设置
 * @param onNavigateToAccountManagement 导航到账户管理
 * @param onSwitchAccount 切换账户
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FleurNavigationDrawer(
    visible: Boolean,
    currentAccount: Account?,
    accounts: List<Account>,
    unreadCounts: Map<String, Int> = emptyMap(),
    onDismiss: () -> Unit,
    onNavigateToInbox: () -> Unit,
    onNavigateToSent: () -> Unit,
    onNavigateToDrafts: () -> Unit,
    onNavigateToStarred: () -> Unit,
    onNavigateToArchive: () -> Unit,
    onNavigateToTrash: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAccountManagement: () -> Unit,
    onSwitchAccount: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    
    // 当 visible 改变时，控制抽屉的打开/关闭
    LaunchedEffect(visible) {
        if (visible) {
            drawerState.open()
        } else {
            drawerState.close()
        }
    }
    
    // 当抽屉关闭时，通知外部
    LaunchedEffect(drawerState.isClosed) {
        if (drawerState.isClosed && visible) {
            onDismiss()
        }
    }
    
    if (visible) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = true,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.width(320.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(
                                horizontal = DrawerDimens.DrawerContentPadding,
                                vertical = DrawerDimens.DrawerTopPadding
                            )
                    ) {
                        // 可展开的账户卡片（替换原有的 UserInfoSection）
                        ExpandableAccountCard(
                            currentAccount = currentAccount,
                            accounts = accounts,
                            onAccountSelected = { accountId ->
                                onSwitchAccount(accountId)
                                onDismiss()
                            },
                            onNavigateToAccountManagement = {
                                onNavigateToAccountManagement()
                                onDismiss()
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(DrawerDimens.SectionSpacing))
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = DrawerDimens.DividerVerticalMargin),
                            thickness = DrawerDimens.DividerThickness
                        )
                        
                        Spacer(modifier = Modifier.height(DrawerDimens.SectionSpacing))
                        
                        // 文件夹列表（使用增强版组件）
                        EnhancedFolderSection(
                            unreadCounts = unreadCounts,
                            onNavigateToInbox = {
                                onNavigateToInbox()
                                onDismiss()
                            },
                            onNavigateToSent = {
                                onNavigateToSent()
                                onDismiss()
                            },
                            onNavigateToDrafts = {
                                onNavigateToDrafts()
                                onDismiss()
                            },
                            onNavigateToStarred = {
                                onNavigateToStarred()
                                onDismiss()
                            },
                            onNavigateToArchive = {
                                onNavigateToArchive()
                                onDismiss()
                            },
                            onNavigateToTrash = {
                                onNavigateToTrash()
                                onDismiss()
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(DrawerDimens.SectionSpacing))
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = DrawerDimens.DividerVerticalMargin),
                            thickness = DrawerDimens.DividerThickness
                        )
                        
                        Spacer(modifier = Modifier.height(DrawerDimens.SectionSpacing))
                        
                        // 设置入口
                        SettingsSection(
                            onNavigateToSettings = {
                                onNavigateToSettings()
                                onDismiss()
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(DrawerDimens.DrawerBottomPadding))
                    }
                }
            },
            modifier = modifier
        ) {
            // 透明的内容区域，点击会关闭抽屉
            Box(modifier = Modifier.fillMaxSize())
        }
    }
}

/**
 * 增强版文件夹区域
 * 使用 EnhancedDrawerItem 显示邮件文件夹列表，包含未读数 Badge
 */
@Composable
private fun EnhancedFolderSection(
    unreadCounts: Map<String, Int>,
    onNavigateToInbox: () -> Unit,
    onNavigateToSent: () -> Unit,
    onNavigateToDrafts: () -> Unit,
    onNavigateToStarred: () -> Unit,
    onNavigateToArchive: () -> Unit,
    onNavigateToTrash: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(DrawerDimens.ItemSpacing)
    ) {
        EnhancedDrawerItem(
            icon = Icons.Default.Inbox,
            label = "收件箱",
            badge = unreadCounts["inbox"]?.takeIf { it > 0 },
            isSelected = false, // TODO: 根据当前路由设置选中状态
            onClick = onNavigateToInbox
        )
        
        EnhancedDrawerItem(
            icon = Icons.Default.Send,
            label = "已发送",
            isSelected = false,
            onClick = onNavigateToSent
        )
        
        EnhancedDrawerItem(
            icon = Icons.Default.Edit,
            label = "草稿箱",
            badge = unreadCounts["drafts"]?.takeIf { it > 0 },
            isSelected = false,
            onClick = onNavigateToDrafts
        )
        
        EnhancedDrawerItem(
            icon = Icons.Default.Star,
            label = "星标邮件",
            isSelected = false,
            onClick = onNavigateToStarred
        )
        
        EnhancedDrawerItem(
            icon = Icons.Default.Archive,
            label = "归档",
            isSelected = false,
            onClick = onNavigateToArchive
        )
        
        EnhancedDrawerItem(
            icon = Icons.Default.Delete,
            label = "垃圾箱",
            isSelected = false,
            onClick = onNavigateToTrash
        )
    }
}

/**
 * 设置区域
 * 显示设置和关于入口
 */
@Composable
private fun SettingsSection(
    onNavigateToSettings: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(DrawerDimens.ItemSpacing)
    ) {
        EnhancedDrawerItem(
            icon = Icons.Default.Settings,
            label = "设置",
            isSelected = false,
            onClick = onNavigateToSettings
        )
        
        EnhancedDrawerItem(
            icon = Icons.Default.Info,
            label = "关于",
            isSelected = false,
            onClick = { /* TODO: 实现关于页面 */ }
        )
    }
}
