package takagi.ru.fleur.ui.screens.contacts.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * 添加联系人 FAB 组件
 * 支持滚动时自动隐藏/显示
 * 
 * @param onClick 点击事件
 * @param listState 列表滚动状态（用于控制显示/隐藏）
 * @param modifier Modifier
 */
@Composable
fun AddContactFAB(
    onClick: () -> Unit,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    // 当滚动到顶部附近时显示 FAB
    val fabVisible by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex < 5
        }
    }
    
    androidx.compose.animation.AnimatedVisibility(
        visible = fabVisible,
        enter = scaleIn(
            animationSpec = tween(
                durationMillis = 200,
                easing = FastOutSlowInEasing
            )
        ),
        exit = scaleOut(
            animationSpec = tween(
                durationMillis = 200,
                easing = FastOutSlowInEasing
            )
        ),
        modifier = modifier
    ) {
        FloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .size(56.dp)
                .semantics {
                    contentDescription = "添加联系人"
                }
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
