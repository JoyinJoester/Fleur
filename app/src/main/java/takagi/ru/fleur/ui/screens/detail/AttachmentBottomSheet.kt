package takagi.ru.fleur.ui.screens.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import takagi.ru.fleur.domain.model.Attachment

/**
 * 附件操作底部弹窗
 * 提供预览、下载、分享选项
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentBottomSheet(
    attachment: Attachment,
    onDismiss: () -> Unit,
    onPreview: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = attachment.fileName,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // 预览选项
            if (attachment.isImage()) {
                ListItem(
                    headlineContent = { Text("预览") },
                    leadingContent = {
                        Icon(Icons.Default.Visibility, null)
                    },
                    modifier = Modifier.clickable {
                        onPreview()
                        onDismiss()
                    }
                )
            }
            
            // 下载选项
            ListItem(
                headlineContent = { Text("下载") },
                leadingContent = {
                    Icon(Icons.Default.Download, null)
                },
                modifier = Modifier.clickable {
                    onDownload()
                    onDismiss()
                }
            )
            
            // 分享选项
            ListItem(
                headlineContent = { Text("分享") },
                leadingContent = {
                    Icon(Icons.Default.Share, null)
                },
                modifier = Modifier.clickable {
                    onShare()
                    onDismiss()
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
