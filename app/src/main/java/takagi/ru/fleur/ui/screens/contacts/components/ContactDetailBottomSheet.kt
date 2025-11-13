package takagi.ru.fleur.ui.screens.contacts.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import takagi.ru.fleur.ui.model.ContactUiModel

/**
 * 联系人详情底部面板
 * 显示联系人的详细信息和操作按钮
 * 
 * @param contact 联系人数据
 * @param onDismiss 关闭回调
 * @param onChatClick 聊天按钮点击回调
 * @param onEmailClick 邮件按钮点击回调
 * @param onEditClick 编辑按钮点击回调
 * @param onDeleteClick 删除按钮点击回调
 * @param modifier Modifier
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailBottomSheet(
    contact: ContactUiModel,
    onDismiss: () -> Unit,
    onChatClick: () -> Unit,
    onEmailClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState()
    var contentVisible by remember { mutableStateOf(false) }
    
    // 内容淡入动画
    LaunchedEffect(Unit) {
        contentVisible = true
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn(tween(200)) + expandVertically(tween(200))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 头像和基本信息
                ContactAvatar(
                    name = contact.name,
                    avatarUrl = contact.avatarUrl,
                    size = 80.dp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = contact.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 操作按钮
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ActionButton(
                        icon = Icons.Outlined.Chat,
                        label = "聊天",
                        onClick = onChatClick
                    )
                    
                    ActionButton(
                        icon = Icons.Outlined.Email,
                        label = "邮件",
                        onClick = onEmailClick
                    )
                    
                    ActionButton(
                        icon = Icons.Outlined.Edit,
                        label = "编辑",
                        onClick = onEditClick
                    )
                    
                    ActionButton(
                        icon = Icons.Outlined.Delete,
                        label = "删除",
                        onClick = onDeleteClick
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                HorizontalDivider()
                
                // 详细信息
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    // 电话号码
                    if (contact.phoneNumber != null) {
                        DetailItem(
                            icon = Icons.Outlined.Phone,
                            label = "电话",
                            value = contact.phoneNumber
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // 地址
                    if (contact.address != null) {
                        DetailItem(
                            icon = Icons.Outlined.Home,
                            label = "地址",
                            value = contact.address
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // 备注
                    if (contact.notes != null) {
                        DetailItem(
                            icon = Icons.Outlined.Notes,
                            label = "备注",
                            value = contact.notes
                        )
                    }
                }
            }
        }
    }
}

/**
 * 操作按钮组件
 */
@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * 详细信息项组件
 */
@Composable
private fun DetailItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
