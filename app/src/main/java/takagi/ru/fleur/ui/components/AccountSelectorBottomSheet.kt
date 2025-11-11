package takagi.ru.fleur.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import takagi.ru.fleur.domain.model.Account
import takagi.ru.fleur.ui.theme.bottomSheetGlassmorphism

/**
 * 账户选择器 Bottom Sheet
 * 
 * 用于选择发件账户，显示所有可用账户列表
 * 
 * 特性:
 * - 毛玻璃效果：12dp blur + 90% opacity
 * - 滑入动画：300ms
 * - 支持手势拖拽关闭
 * - 显示账户颜色指示器
 * - 高亮当前选中账户
 * 
 * @param accounts 账户列表
 * @param selectedAccountId 当前选中的账户 ID
 * @param onDismiss 关闭回调
 * @param onAccountSelected 账户选择回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSelectorBottomSheet(
    accounts: List<Account>,
    selectedAccountId: String?,
    onDismiss: () -> Unit,
    onAccountSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier.bottomSheetGlassmorphism(),
        containerColor = Color.Transparent,
        dragHandle = {
            // 自定义拖拽手柄
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .width(32.dp)
                        .height(4.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                ) {}
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            // 标题
            Text(
                text = "选择发件账户",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 账户列表
            accounts.forEach { account ->
                AccountSelectorItem(
                    account = account,
                    isSelected = account.id == selectedAccountId,
                    onClick = {
                        onAccountSelected(account.id)
                        onDismiss()
                    }
                )
            }
        }
    }
}

/**
 * 账户选择器项
 * 
 * @param account 账户信息
 * @param isSelected 是否选中
 * @param onClick 点击回调
 */
@Composable
private fun AccountSelectorItem(
    account: Account,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) {
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        } else {
            Color.Transparent
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 账户颜色指示器和头像
            Box(
                modifier = Modifier
                    .size(40.dp)
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
            
            // 账户信息
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = account.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = account.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 选中指示器
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "已选中",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
