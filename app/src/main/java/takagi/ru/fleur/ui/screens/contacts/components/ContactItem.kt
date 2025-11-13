package takagi.ru.fleur.ui.screens.contacts.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import takagi.ru.fleur.ui.model.ContactUiModel

/**
 * 联系人列表项组件
 * 
 * @param contact 联系人数据
 * @param onClick 点击事件
 * @param onChatClick 聊天按钮点击事件
 * @param onEmailClick 邮件按钮点击事件
 * @param modifier Modifier
 */
@Composable
fun ContactItem(
    contact: ContactUiModel,
    onClick: () -> Unit,
    onChatClick: () -> Unit,
    onEmailClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(100),
        label = "contactItemScale"
    )
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .scale(scale)
            .clickable(
                onClick = onClick,
                onClickLabel = "查看联系人详情"
            )
            .semantics {
                contentDescription = "联系人 ${contact.name}, 邮箱 ${contact.email}"
                role = Role.Button
            }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧：头像和在线状态
        Box {
            ContactAvatar(
                name = contact.name,
                avatarUrl = contact.avatarUrl,
                size = 56.dp
            )
            
            // 在线状态指示器（右下角）
            OnlineIndicator(
                isOnline = contact.isOnline,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .semantics {
                        contentDescription = if (contact.isOnline) "在线" else "离线"
                    }
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // 中间：姓名和邮箱
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = contact.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // 右侧：快速操作按钮
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 聊天按钮
            IconButton(
                onClick = onChatClick,
                modifier = Modifier
                    .size(48.dp)
                    .semantics {
                        contentDescription = "与 ${contact.name} 发起聊天"
                    }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Chat,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // 邮件按钮
            IconButton(
                onClick = onEmailClick,
                modifier = Modifier
                    .size(48.dp)
                    .semantics {
                        contentDescription = "给 ${contact.name} 发送邮件"
                    }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Email,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
