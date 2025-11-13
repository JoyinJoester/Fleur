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
 * 只显示实际存在的首字母，不显示全部A-Z
 * 数字和特殊字符统一显示为"#"
 * 
 * @param currentLetter 当前选中的字母
 * @param availableLetters 实际存在的字母列表
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
    // 将可用字母转换为排序列表，确保字母在前，#在后
    val sortedLetters = availableLetters.sortedWith(compareBy { letter ->
        when (letter) {
            "#" -> "ZZZ" // 确保#排在最后
            else -> letter
        }
    })
    
    Column(
        modifier = modifier.padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // 只显示实际存在的首字母
        sortedLetters.forEach { letter ->
            val isCurrent = letter == currentLetter
            
            Text(
                text = letter,
                fontSize = 10.sp,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                color = if (isCurrent) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier
                    .clickable { onLetterClick(letter) }
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}
