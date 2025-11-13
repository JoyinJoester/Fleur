package takagi.ru.fleur.ui.screens.contacts.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import takagi.ru.fleur.ui.model.ContactUiModel

/**
 * 联系人列表组件
 * 按首字母分组显示联系人，支持字母索引快速跳转
 * 
 * @param contacts 联系人列表
 * @param onContactClick 联系人点击事件
 * @param onChatClick 聊天按钮点击事件
 * @param onEmailClick 邮件按钮点击事件
 * @param modifier Modifier
 */
@Composable
fun ContactsList(
    contacts: List<ContactUiModel>,
    onContactClick: (ContactUiModel) -> Unit,
    onChatClick: (ContactUiModel) -> Unit,
    onEmailClick: (ContactUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // 按首字母分组
    val groupedContacts by remember(contacts) {
        derivedStateOf {
            contacts
                .groupBy { contact ->
                    val firstChar = contact.name.firstOrNull()?.uppercaseChar()
                    when {
                        firstChar == null -> "#"
                        firstChar in 'A'..'Z' -> firstChar.toString()
                        firstChar in '\u4E00'..'\u9FFF' -> "#" // 中文字符
                        else -> "#"
                    }
                }
                .toSortedMap()
        }
    }
    
    // 获取可用的字母列表
    val availableLetters by remember(groupedContacts) {
        derivedStateOf {
            groupedContacts.keys.toSet()
        }
    }
    
    // 获取当前显示的字母
    val currentLetter by remember {
        derivedStateOf {
            val firstVisibleIndex = listState.firstVisibleItemIndex
            var itemCount = 0
            var currentGroup: String? = null
            
            for ((letter, contactsInGroup) in groupedContacts) {
                // 每组有一个 header + contacts
                val groupSize = 1 + contactsInGroup.size
                if (firstVisibleIndex < itemCount + groupSize) {
                    currentGroup = letter
                    break
                }
                itemCount += groupSize
            }
            
            currentGroup
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            groupedContacts.forEach { (letter, contactsInGroup) ->
                // Sticky header
                @OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
                stickyHeader(key = "header_$letter") {
                    ContactGroupHeader(letter = letter)
                }
                
                // 联系人列表
                items(
                    items = contactsInGroup,
                    key = { contact -> contact.id }
                ) { contact ->
                    ContactItem(
                        contact = contact,
                        onClick = { onContactClick(contact) },
                        onChatClick = { onChatClick(contact) },
                        onEmailClick = { onEmailClick(contact) }
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 88.dp)
                    )
                }
            }
        }
        
        // 字母索引（右侧）
        AlphabetIndex(
            currentLetter = currentLetter,
            availableLetters = availableLetters,
            onLetterClick = { letter ->
                // 计算目标字母的索引位置
                var targetIndex = 0
                for ((groupLetter, contactsInGroup) in groupedContacts) {
                    if (groupLetter == letter) {
                        break
                    }
                    // 每组有一个 header + contacts
                    targetIndex += 1 + contactsInGroup.size
                }
                
                // 滚动到目标位置
                coroutineScope.launch {
                    listState.animateScrollToItem(targetIndex)
                }
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp)
        )
    }
}
