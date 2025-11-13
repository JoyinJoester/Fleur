package takagi.ru.fleur.ui.screens.contacts.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 字母索引组件
 * 显示 A-Z 字母列表，支持点击跳转到对应分组
 * 
 * @param currentLetter 当前选中的字母
 * @param availableLetters 可用的字母列表
 * @param onLetterClick 字母点击事件
 * @param modifier Modifier
 */
@Composable
fun AlphabetIndex(
    currentLetter: String?,
    availableLetters: Set<String>,
    onLetterClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val alphabet = ('A'..'Z').map { it.toString() } + "#"
    
    Column(
        modifier = modifier.padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        alphabet.forEach { letter ->
            val isAvailable = availableLetters.contains(letter)
            val isCurrent = letter == currentLetter
            
            Text(
                text = letter,
                fontSize = 10.sp,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isCurrent -> MaterialTheme.colorScheme.primary
                    isAvailable -> MaterialTheme.colorScheme.onSurface
                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                },
                modifier = Modifier
                    .clickable(enabled = isAvailable) {
                        onLetterClick(letter)
                    }
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}
