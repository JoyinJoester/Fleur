package takagi.ru.fleur.ui.screens.contacts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 在线状态指示器
 * 显示联系人的在线/离线状态
 * 
 * @param isOnline 是否在线
 * @param modifier Modifier
 */
@Composable
fun OnlineIndicator(
    isOnline: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isOnline) {
        Color(0xFF4CAF50) // 在线：绿色
    } else {
        MaterialTheme.colorScheme.outlineVariant // 离线：灰色
    }
    
    Box(
        modifier = modifier
            .size(14.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(
                width = 2.dp,
                color = Color.White,
                shape = CircleShape
            )
    )
}
