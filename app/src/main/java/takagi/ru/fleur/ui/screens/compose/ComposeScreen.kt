package takagi.ru.fleur.ui.screens.compose

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import takagi.ru.fleur.domain.model.Attachment

/**
 * 邮件撰写界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeScreen(
    onNavigateBack: () -> Unit,
    viewModel: ComposeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    
    // 文件选择器
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // 获取文件信息
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                    
                    val fileName = if (nameIndex >= 0) cursor.getString(nameIndex) else "unknown"
                    val fileSize = if (sizeIndex >= 0) cursor.getLong(sizeIndex) else 0L
                    val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
                    
                    viewModel.addAttachment(uri, fileName, mimeType, fileSize)
                }
            }
        }
    }
    
    // 显示错误消息
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error.getUserMessage())
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            ComposeTopAppBar(
                onClose = onNavigateBack,
                onSend = {
                    viewModel.sendEmail(onSuccess = onNavigateBack)
                },
                canSend = uiState.canSend(),
                isSending = uiState.isSending
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            ComposeBottomBar(
                onAttachFile = { filePickerLauncher.launch("*/*") },
                attachmentCount = uiState.attachments.size
            )
        }
    ) { paddingValues ->
        // 加载状态显示
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "正在加载邮件内容...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 发件人选择
                AccountSelector(
                selectedAccount = uiState.selectedAccount,
                onClick = { viewModel.toggleAccountSelector() }
            )
            
            Divider()
            
            // 收件人
            OutlinedTextField(
                value = uiState.toAddresses,
                onValueChange = { viewModel.updateToAddresses(it) },
                label = { Text("收件人") },
                placeholder = { Text("example@email.com") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 3
            )
            
            // 抄送/密送切换按钮
            if (!uiState.showCcBcc) {
                TextButton(onClick = { viewModel.toggleCcBcc() }) {
                    Text("添加抄送/密送")
                }
            }
            
            // 抄送
            if (uiState.showCcBcc) {
                OutlinedTextField(
                    value = uiState.ccAddresses,
                    onValueChange = { viewModel.updateCcAddresses(it) },
                    label = { Text("抄送") },
                    placeholder = { Text("example@email.com") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3
                )
            }
            
            // 密送
            if (uiState.showCcBcc) {
                OutlinedTextField(
                    value = uiState.bccAddresses,
                    onValueChange = { viewModel.updateBccAddresses(it) },
                    label = { Text("密送") },
                    placeholder = { Text("example@email.com") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3
                )
            }
            
            // 主题
            OutlinedTextField(
                value = uiState.subject,
                onValueChange = { viewModel.updateSubject(it) },
                label = { Text("主题") },
                placeholder = { Text("输入邮件主题") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Divider()
            
            // 富文本编辑工具栏
            RichTextToolbar()
            
            Divider()
            
            // 正文
            OutlinedTextField(
                value = uiState.body,
                onValueChange = { viewModel.updateBody(it) },
                placeholder = { Text("撰写邮件...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp),
                singleLine = false
            )
            
            // 附件列表
            if (uiState.attachments.isNotEmpty()) {
                Divider()
                AttachmentsList(
                    attachments = uiState.attachments,
                    onRemove = { viewModel.removeAttachment(it) }
                )
            }
            
            // 草稿保存状态
            if (uiState.isSavingDraft) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    Text(
                        text = "正在保存草稿...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (uiState.lastDraftSaveTime != null) {
                Text(
                    text = "草稿已保存",
                    style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
        
        // 账户选择器 Bottom Sheet
        if (uiState.showAccountSelector) {
            AccountSelectorBottomSheet(
                onDismiss = { viewModel.toggleAccountSelector() },
                onAccountSelected = { account ->
                    viewModel.selectAccount(account)
                }
            )
        }
    }
}

/**
 * 顶部应用栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComposeTopAppBar(
    onClose: () -> Unit,
    onSend: () -> Unit,
    canSend: Boolean,
    isSending: Boolean
) {
    TopAppBar(
        title = { Text("撰写邮件") },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, "关闭")
            }
        },
        actions = {
            if (isSending) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 16.dp)
                )
            } else {
                TextButton(
                    onClick = onSend,
                    enabled = canSend
                ) {
                    Text("发送")
                }
            }
        }
    )
}

/**
 * 底部工具栏
 */
@Composable
private fun ComposeBottomBar(
    onAttachFile: () -> Unit,
    attachmentCount: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onAttachFile) {
                Icon(Icons.Default.Attachment, "添加附件")
            }
            if (attachmentCount > 0) {
                Text(
                    text = "$attachmentCount 个附件",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 账户选择器
 */
@Composable
private fun AccountSelector(
    selectedAccount: takagi.ru.fleur.domain.model.Account?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "发件人",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = selectedAccount?.email ?: "选择账户",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = "选择账户"
        )
    }
}

/**
 * 富文本编辑工具栏
 */
@Composable
private fun RichTextToolbar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // 粗体
        IconButton(onClick = { /* TODO: 实现粗体 */ }) {
            Text(
                text = "B",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        // 斜体
        IconButton(onClick = { /* TODO: 实现斜体 */ }) {
            Text(
                text = "I",
                style = MaterialTheme.typography.titleMedium,
                fontStyle = FontStyle.Italic
            )
        }
        
        // 下划线
        IconButton(onClick = { /* TODO: 实现下划线 */ }) {
            Text(
                text = "U",
                style = MaterialTheme.typography.titleMedium,
                textDecoration = TextDecoration.Underline
            )
        }
        
        // 无序列表
        IconButton(onClick = { /* TODO: 实现无序列表 */ }) {
            Text(
                text = "•",
                style = MaterialTheme.typography.titleLarge
            )
        }
        
        // 有序列表
        IconButton(onClick = { /* TODO: 实现有序列表 */ }) {
            Text(
                text = "1.",
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

/**
 * 附件列表
 */
@Composable
private fun AttachmentsList(
    attachments: List<Attachment>,
    onRemove: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "附件 (${attachments.size})",
            style = MaterialTheme.typography.titleSmall
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(attachments) { attachment ->
                AttachmentItem(
                    attachment = attachment,
                    onRemove = { onRemove(attachment.id) }
                )
            }
        }
    }
}

/**
 * 附件项
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AttachmentItem(
    attachment: Attachment,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.width(140.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Attachment,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "删除",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Text(
                text = attachment.fileName,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2
            )
            
            Text(
                text = attachment.formattedSize(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 账户选择器 Bottom Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountSelectorBottomSheet(
    onDismiss: () -> Unit,
    onAccountSelected: (takagi.ru.fleur.domain.model.Account) -> Unit
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
                text = "选择发件账户",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // TODO: 从 AccountRepository 加载实际账户列表
            Text(
                text = "暂无账户",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 32.dp)
            )
        }
    }
}
