package takagi.ru.fleur.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import takagi.ru.fleur.utils.MarkdownUtils
import takagi.ru.fleur.utils.MarkdownUtils.markdownToHtml
import takagi.ru.fleur.utils.MarkdownUtils.stripMarkdown
import kotlin.math.max

/**
 * 撰写邮件底部弹窗
 * 使用 ModalBottomSheet 实现的邮件撰写界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onSend: (
        to: String,
        subject: String,
        bodyPlain: String,
        bodyMarkdown: String?,
        bodyHtml: String?,
        contentType: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var fromAccount by remember { mutableStateOf("user@example.com") }
    var toList by remember { mutableStateOf(listOf<String>()) }
    var toInputText by remember { mutableStateOf("") }
    var ccList by remember { mutableStateOf(listOf<String>()) }
    var ccInputText by remember { mutableStateOf("") }
    var bccList by remember { mutableStateOf(listOf<String>()) }
    var bccInputText by remember { mutableStateOf("") }
    var subjectText by remember { mutableStateOf("") }
    var bodyText by remember { mutableStateOf("") }
    var showCc by remember { mutableStateOf(false) }
    var showBcc by remember { mutableStateOf(false) }
    var priority by remember { mutableStateOf(EmailPriority.NORMAL) }
    var showAccountPicker by remember { mutableStateOf(false) }
    var showPriorityMenu by remember { mutableStateOf(false) }
    var showFullscreenEditor by remember { mutableStateOf(false) }
    var enableMarkdown by remember { mutableStateOf(false) }  // Markdown 开关状态
    
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    
    if (visible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            modifier = modifier.fillMaxHeight(),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = null,
            windowInsets = WindowInsets(0)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .statusBarsPadding()
            ) {
                ComposeBottomSheetTopBar(
                    onClose = onDismiss,
                    onSend = {
                        if (toList.isNotEmpty() && subjectText.isNotBlank()) {
                            // 根据 Markdown 开关生成对应的内容格式
                            val bodyPlain: String
                            val bodyMarkdown: String?
                            val bodyHtml: String?
                            val contentType: String
                            
                            if (enableMarkdown && bodyText.isNotBlank()) {
                                // Markdown 模式：保存 Markdown 和转换后的 HTML
                                bodyPlain = bodyText.stripMarkdown()
                                bodyMarkdown = bodyText
                                bodyHtml = bodyText.markdownToHtml()
                                contentType = "markdown"
                            } else {
                                // 纯文本模式：只保存纯文本
                                bodyPlain = bodyText
                                bodyMarkdown = null
                                bodyHtml = null
                                contentType = "text"
                            }
                            
                            onSend(
                                toList.joinToString(", "),
                                subjectText,
                                bodyPlain,
                                bodyMarkdown,
                                bodyHtml,
                                contentType
                            )
                            onDismiss()
                        }
                    },
                    canSend = toList.isNotEmpty() && subjectText.isNotBlank()
                )
                
                HorizontalDivider()
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    ComposeFromField(
                        fromAccount = fromAccount,
                        onAccountClick = { showAccountPicker = true }
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    
                    ComposeMultipleRecipientField(
                        label = "收件人",
                        recipients = toList,
                        inputText = toInputText,
                        onInputTextChange = { toInputText = it },
                        onAddRecipient = { email ->
                            Log.d("ComposeBottomSheet", "尝试添加收件人: '$email', 是否空白: ${email.isBlank()}, 包含@: ${email.contains("@")}")
                            if (email.isNotBlank()) {
                                val trimmedEmail = email.trim()
                                if (trimmedEmail.isNotEmpty() && !toList.contains(trimmedEmail)) {
                                    toList = toList + trimmedEmail
                                    toInputText = ""
                                    Log.d("ComposeBottomSheet", "成功添加收件人: $trimmedEmail, 当前列表: $toList")
                                } else {
                                    Log.d("ComposeBottomSheet", "收件人已存在或为空")
                                }
                            }
                        },
                        onRemoveRecipient = { email ->
                            Log.d("ComposeBottomSheet", "移除收件人: $email")
                            toList = toList - email
                        },
                        placeholder = "输入收件人邮箱",
                        trailingIcon = {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (!showCc) {
                                    TextButton(
                                        onClick = { showCc = true },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("抄送", style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                                if (!showBcc) {
                                    TextButton(
                                        onClick = { showBcc = true },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("密送", style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    )
                    
                    if (showCc) {
                        ComposeMultipleRecipientField(
                            label = "抄送",
                            recipients = ccList,
                            inputText = ccInputText,
                            onInputTextChange = { ccInputText = it },
                            onAddRecipient = { email ->
                                if (email.isNotBlank()) {
                                    val trimmedEmail = email.trim()
                                    if (trimmedEmail.isNotEmpty() && !ccList.contains(trimmedEmail)) {
                                        ccList = ccList + trimmedEmail
                                        ccInputText = ""
                                    }
                                }
                            },
                            onRemoveRecipient = { email ->
                                ccList = ccList - email
                            },
                            placeholder = "输入抄送邮箱",
                            trailingIcon = {
                                IconButton(onClick = { 
                                    showCc = false
                                    ccList = emptyList()
                                    ccInputText = ""
                                }) {
                                    Icon(
                                        Icons.Default.Close, 
                                        "移除抄送",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        )
                    }
                    
                    if (showBcc) {
                        ComposeMultipleRecipientField(
                            label = "密送",
                            recipients = bccList,
                            inputText = bccInputText,
                            onInputTextChange = { bccInputText = it },
                            onAddRecipient = { email ->
                                if (email.isNotBlank()) {
                                    val trimmedEmail = email.trim()
                                    if (trimmedEmail.isNotEmpty() && !bccList.contains(trimmedEmail)) {
                                        bccList = bccList + trimmedEmail
                                        bccInputText = ""
                                    }
                                }
                            },
                            onRemoveRecipient = { email ->
                                bccList = bccList - email
                            },
                            placeholder = "输入密送邮箱",
                            trailingIcon = {
                                IconButton(onClick = { 
                                    showBcc = false
                                    bccList = emptyList()
                                    bccInputText = ""
                                }) {
                                    Icon(
                                        Icons.Default.Close, 
                                        "移除密送",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        )
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                    
                    ComposeTextField(
                        value = subjectText,
                        onValueChange = { subjectText = it },
                        label = "主题",
                        placeholder = "输入邮件主题",
                        singleLine = true,
                        trailingIcon = if (priority != EmailPriority.NORMAL) {
                            {
                                Icon(
                                    imageVector = priority.icon,
                                    contentDescription = "优先级: ${priority.label}",
                                    tint = priority.color,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else null
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                    
                    // 正文预览（支持 Markdown 开关）
                    MarkdownPreviewCard(
                        markdown = bodyText,
                        enableMarkdown = enableMarkdown,
                        onMarkdownToggle = { enableMarkdown = it },
                        onClick = { showFullscreenEditor = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(80.dp))
                }
                
                ComposeBottomToolbar(
                    onPriorityClick = { showPriorityMenu = true },
                    currentPriority = priority
                )
            }
            
            if (showAccountPicker) {
                AccountPickerDialog(
                    currentAccount = fromAccount,
                    onAccountSelected = { 
                        fromAccount = it
                        showAccountPicker = false
                    },
                    onDismiss = { showAccountPicker = false }
                )
            }
            
            if (showPriorityMenu) {
                PriorityPickerDialog(
                    currentPriority = priority,
                    onPrioritySelected = { 
                        priority = it
                        showPriorityMenu = false
                    },
                    onDismiss = { showPriorityMenu = false }
                )
            }
            
            if (showFullscreenEditor) {
                FullscreenBodyEditor(
                    bodyText = bodyText,
                    onBodyTextChange = { bodyText = it },
                    onDismiss = { showFullscreenEditor = false }
                )
            }
        }
    }
}

@Composable
private fun ComposeBottomSheetTopBar(
    onClose: () -> Unit,
    onSend: () -> Unit,
    canSend: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "撰写邮件",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Button(
                onClick = onSend,
                enabled = canSend,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("发送", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun ComposeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    singleLine: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(60.dp)
        )
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { 
                Text(
                    placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            },
            singleLine = singleLine,
            trailingIcon = trailingIcon,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent
            ),
            textStyle = MaterialTheme.typography.bodyMedium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComposeMultipleRecipientField(
    label: String,
    recipients: List<String>,
    inputText: String,
    onInputTextChange: (String) -> Unit,
    onAddRecipient: (String) -> Unit,
    onRemoveRecipient: (String) -> Unit,
    placeholder: String,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .width(60.dp)
                    .padding(top = 12.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                if (recipients.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalSpacing = 6.dp,
                        verticalSpacing = 6.dp
                    ) {
                        recipients.forEach { email ->
                            AssistChip(
                                onClick = { onRemoveRecipient(email) },
                                label = { 
                                    Text(
                                        email,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1
                                    ) 
                                },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Cancel,
                                        contentDescription = "移除",
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    leadingIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                border = null
                            )
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = onInputTextChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { 
                            Text(
                                if (recipients.isEmpty()) placeholder else "添加更多...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                onAddRecipient(inputText)
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                    
                    if (inputText.isNotBlank()) {
                        IconButton(
                            onClick = { onAddRecipient(inputText) },
                            modifier = Modifier
                                .size(40.dp)
                                .padding(start = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "添加收件人",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    trailingIcon?.invoke()
                }
            }
        }
    }
}

@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    verticalSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints) }
        
        var xPos = 0
        var yPos = 0
        var maxHeight = 0
        val rows = mutableListOf<MutableList<Placeable>>()
        var currentRow = mutableListOf<Placeable>()
        val hSpacing = horizontalSpacing.roundToPx()
        val vSpacing = verticalSpacing.roundToPx()
        
        placeables.forEach { placeable ->
            if (xPos + placeable.width > constraints.maxWidth && currentRow.isNotEmpty()) {
                rows.add(currentRow)
                currentRow = mutableListOf()
                xPos = 0
                yPos += maxHeight + vSpacing
                maxHeight = 0
            }
            
            currentRow.add(placeable)
            xPos += placeable.width + hSpacing
            maxHeight = max(maxHeight, placeable.height)
        }
        
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
        }
        
        val totalHeight = rows.sumOf { row ->
            row.maxOfOrNull { it.height } ?: 0
        } + (rows.size - 1) * vSpacing
        
        layout(constraints.maxWidth, totalHeight) {
            var y = 0
            rows.forEach { row ->
                var x = 0
                val rowHeight = row.maxOfOrNull { it.height } ?: 0
                row.forEach { placeable ->
                    placeable.place(x, y)
                    x += placeable.width + hSpacing
                }
                y += rowHeight + vSpacing
            }
        }
    }
}

@Composable
private fun ComposeBottomToolbar(
    onPriorityClick: () -> Unit = {},
    currentPriority: EmailPriority = EmailPriority.NORMAL
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Outlined.AttachFile,
                    contentDescription = "添加附件",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Outlined.FormatBold,
                    contentDescription = "格式化",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = onPriorityClick) {
                Icon(
                    imageVector = if (currentPriority != EmailPriority.NORMAL) 
                        currentPriority.icon else Icons.Outlined.Flag,
                    contentDescription = "优先级",
                    tint = if (currentPriority != EmailPriority.NORMAL)
                        currentPriority.color else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = "定时发送",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            TextButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Outlined.Save,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("草稿", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun ComposeFromField(
    fromAccount: String,
    onAccountClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onAccountClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "发件人",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(60.dp)
        )
        
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = fromAccount,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "选择账号",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

enum class EmailPriority(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
) {
    HIGH("高优先级", Icons.Filled.PriorityHigh, Color(0xFFD32F2F)),
    NORMAL("普通", Icons.Outlined.Flag, Color.Gray),
    LOW("低优先级", Icons.Filled.KeyboardArrowDown, Color(0xFF1976D2))
}

@Composable
private fun AccountPickerDialog(
    currentAccount: String,
    onAccountSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val accounts = listOf(
        "user@example.com",
        "work@company.com",
        "personal@gmail.com"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择发件账号") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                accounts.forEach { account ->
                    Surface(
                        onClick = { onAccountSelected(account) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = if (account == currentAccount) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = if (account == currentAccount)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = account,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (account == currentAccount)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (account == currentAccount) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "已选中",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun PriorityPickerDialog(
    currentPriority: EmailPriority,
    onPrioritySelected: (EmailPriority) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("设置优先级") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EmailPriority.values().forEach { priority ->
                    Surface(
                        onClick = { onPrioritySelected(priority) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = if (priority == currentPriority)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = priority.icon,
                                    contentDescription = null,
                                    tint = priority.color
                                )
                                Text(
                                    text = priority.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (priority == currentPriority)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (priority == currentPriority) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "已选中",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 全屏正文编辑器
 */
@OptIn(ExperimentalMaterial3Api::class)
/**
 * 全屏正文编辑器
 */
@Composable
internal fun FullscreenBodyEditor(
    bodyText: String,
    onBodyTextChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var showFormatMenu by remember { mutableStateOf(false) }
    var showInsertMenu by remember { mutableStateOf(false) }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    
    // 使用 TextFieldValue 来支持文本选择
    var textFieldValue by remember(bodyText) { 
        mutableStateOf(TextFieldValue(
            text = bodyText,
            selection = TextRange(bodyText.length)
        ))
    }
    
    // 同步到父组件
    LaunchedEffect(textFieldValue.text) {
        if (textFieldValue.text != bodyText) {
            onBodyTextChange(textFieldValue.text)
        }
    }
    
    // 显示提示消息
    fun showMessage(message: String) {
        snackbarMessage = message
        showSnackbar = true
    }
    
    // 格式化选中的文本或在光标位置插入
    fun formatText(prefix: String, suffix: String = "", placeholder: String = "文本") {
        val selection = textFieldValue.selection
        val currentText = textFieldValue.text
        
        if (selection.start == selection.end) {
            // 没有选中文本，插入模板
            val newText = currentText.substring(0, selection.start) +
                    prefix + placeholder + suffix +
                    currentText.substring(selection.end)
            val newPosition = selection.start + prefix.length
            textFieldValue = TextFieldValue(
                text = newText,
                selection = TextRange(newPosition, newPosition + placeholder.length)
            )
        } else {
            // 有选中文本，格式化选中部分
            val selectedText = currentText.substring(selection.start, selection.end)
            val newText = currentText.substring(0, selection.start) +
                    prefix + selectedText + suffix +
                    currentText.substring(selection.end)
            val newPosition = selection.start + prefix.length + selectedText.length + suffix.length
            textFieldValue = TextFieldValue(
                text = newText,
                selection = TextRange(newPosition)
            )
        }
    }
    
    // 在光标位置插入文本
    fun insertText(text: String) {
        val selection = textFieldValue.selection
        val currentText = textFieldValue.text
        val newText = currentText.substring(0, selection.start) +
                text +
                currentText.substring(selection.end)
        val newPosition = selection.start + text.length
        textFieldValue = TextFieldValue(
            text = newText,
            selection = TextRange(newPosition)
        )
    }
    
    LaunchedEffect(showSnackbar) {
        if (showSnackbar) {
            snackbarHostState.showSnackbar(snackbarMessage)
            showSnackbar = false
        }
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Scaffold(
            topBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp
                ) {
                    Column {
                        // 标题栏
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .height(56.dp)
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "返回"
                                )
                            }
                            
                            Text(
                                "编辑正文",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Medium
                            )
                            
                            TextButton(onClick = onDismiss) {
                                Text("完成", fontWeight = FontWeight.SemiBold)
                            }
                        }
                        
                        // 编辑工具栏
                        EditorToolbar(
                            onBoldClick = { 
                                formatText("**", "**", "加粗文本")
                                showMessage("✓ 加粗")
                            },
                            onItalicClick = { 
                                formatText("*", "*", "斜体文本")
                                showMessage("✓ 斜体")
                            },
                            onUnderlineClick = { 
                                formatText("<u>", "</u>", "下划线文本")
                                showMessage("✓ 下划线")
                            },
                            onLinkClick = { 
                                formatText("[", "](https://example.com)", "链接文字")
                                showMessage("✓ 插入链接")
                            },
                            onListClick = { 
                                insertText("\n• ")
                                showMessage("✓ 列表")
                            },
                            onImageClick = { 
                                insertText("\n![图片描述](图片URL)")
                                showMessage("✓ 插入图片")
                            },
                            onAttachClick = { 
                                showMessage("附件功能开发中...")
                            },
                            onFormatClick = { showFormatMenu = !showFormatMenu },
                            onInsertClick = { showInsertMenu = !showInsertMenu }
                        )
                        
                        // 格式化菜单（展开时显示）
                        if (showFormatMenu) {
                            FormatMenu(
                                onFontSizeClick = { 
                                    insertText("\n# ")
                                    showMessage("✓ 标题")
                                    showFormatMenu = false
                                },
                                onColorClick = { 
                                    formatText("<span style='color: #FF0000'>", "</span>", "彩色文本")
                                    showMessage("✓ 颜色")
                                    showFormatMenu = false
                                },
                                onAlignClick = { 
                                    formatText("<center>", "</center>", "居中文本")
                                    showMessage("✓ 居中")
                                    showFormatMenu = false
                                },
                                onQuoteClick = { 
                                    insertText("\n> ")
                                    showMessage("✓ 引用")
                                    showFormatMenu = false
                                }
                            )
                        }
                        
                        // 插入菜单（展开时显示）
                        if (showInsertMenu) {
                            InsertMenu(
                                onTableClick = { 
                                    insertText("\n| 列1 | 列2 | 列3 |\n|-----|-----|-----|\n| 内容 | 内容 | 内容 |\n")
                                    showMessage("✓ 表格")
                                    showInsertMenu = false
                                },
                                onCodeClick = { 
                                    formatText("\n```\n", "\n```\n", "代码内容")
                                    showMessage("✓ 代码块")
                                    showInsertMenu = false
                                },
                                onDividerClick = { 
                                    insertText("\n---\n")
                                    showMessage("✓ 分割线")
                                    showInsertMenu = false
                                },
                                onEmojiClick = { 
                                    insertText("😊 ")
                                    showMessage("✓ 表情")
                                    showInsertMenu = false
                                }
                            )
                        }
                        
                        Divider()
                    }
                }
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            bottomBar = {
                // 底部状态栏
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    tonalElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${textFieldValue.text.length} 字符",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(onClick = { 
                                showMessage("预览功能：将显示渲染后的邮件效果")
                            }) {
                                Icon(
                                    Icons.Default.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("预览", style = MaterialTheme.typography.labelSmall)
                            }
                            
                            TextButton(onClick = { 
                                showMessage("草稿已自动保存 ✓")
                            }) {
                                Icon(
                                    Icons.Default.Save,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("保存草稿", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        ) { paddingValues ->
            OutlinedTextField(
                value = textFieldValue,
                onValueChange = { textFieldValue = it },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                placeholder = { 
                    Text(
                        "在此编写邮件正文...\n\n💡 提示：选中文本后点击工具栏按钮可以格式化选中内容",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    ) 
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                ),
                textStyle = MaterialTheme.typography.bodyLarge,
                minLines = 20
            )
        }
    }
}

/**
 * 编辑器工具栏
 */
@Composable
private fun EditorToolbar(
    onBoldClick: () -> Unit,
    onItalicClick: () -> Unit,
    onUnderlineClick: () -> Unit,
    onLinkClick: () -> Unit,
    onListClick: () -> Unit,
    onImageClick: () -> Unit,
    onAttachClick: () -> Unit,
    onFormatClick: () -> Unit,
    onInsertClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 基础格式化按钮
            IconButton(onClick = onBoldClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Default.FormatBold,
                    contentDescription = "加粗",
                    modifier = Modifier.size(20.dp)
                )
            }
            
            IconButton(onClick = onItalicClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Default.FormatItalic,
                    contentDescription = "斜体",
                    modifier = Modifier.size(20.dp)
                )
            }
            
            IconButton(onClick = onUnderlineClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Default.FormatUnderlined,
                    contentDescription = "下划线",
                    modifier = Modifier.size(20.dp)
                )
            }
            
            VerticalDivider(
                modifier = Modifier
                    .height(24.dp)
                    .padding(horizontal = 4.dp)
            )
            
            // 插入功能
            IconButton(onClick = onLinkClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Default.Link,
                    contentDescription = "插入链接",
                    modifier = Modifier.size(20.dp)
                )
            }
            
            IconButton(onClick = onListClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Default.FormatListBulleted,
                    contentDescription = "列表",
                    modifier = Modifier.size(20.dp)
                )
            }
            
            IconButton(onClick = onImageClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = "插入图片",
                    modifier = Modifier.size(20.dp)
                )
            }
            
            IconButton(onClick = onAttachClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Default.AttachFile,
                    contentDescription = "附件",
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(Modifier.weight(1f))
            
            // 更多功能
            IconButton(onClick = onFormatClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Default.FormatSize,
                    contentDescription = "格式化",
                    modifier = Modifier.size(20.dp)
                )
            }
            
            IconButton(onClick = onInsertClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Default.AddCircleOutline,
                    contentDescription = "插入",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * 格式化菜单
 */
@Composable
private fun FormatMenu(
    onFontSizeClick: () -> Unit,
    onColorClick: () -> Unit,
    onAlignClick: () -> Unit,
    onQuoteClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssistChip(
                onClick = onFontSizeClick,
                label = { Text("字号", style = MaterialTheme.typography.labelSmall) },
                leadingIcon = {
                    Icon(
                        Icons.Default.TextFields,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
            
            AssistChip(
                onClick = onColorClick,
                label = { Text("颜色", style = MaterialTheme.typography.labelSmall) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Palette,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
            
            AssistChip(
                onClick = onAlignClick,
                label = { Text("对齐", style = MaterialTheme.typography.labelSmall) },
                leadingIcon = {
                    Icon(
                        Icons.Default.FormatAlignLeft,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
            
            AssistChip(
                onClick = onQuoteClick,
                label = { Text("引用", style = MaterialTheme.typography.labelSmall) },
                leadingIcon = {
                    Icon(
                        Icons.Default.FormatQuote,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
    }
}

/**
 * 插入菜单
 */
@Composable
private fun InsertMenu(
    onTableClick: () -> Unit,
    onCodeClick: () -> Unit,
    onDividerClick: () -> Unit,
    onEmojiClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssistChip(
                onClick = onTableClick,
                label = { Text("表格", style = MaterialTheme.typography.labelSmall) },
                leadingIcon = {
                    Icon(
                        Icons.Default.TableChart,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
            
            AssistChip(
                onClick = onCodeClick,
                label = { Text("代码", style = MaterialTheme.typography.labelSmall) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Code,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
            
            AssistChip(
                onClick = onDividerClick,
                label = { Text("分割线", style = MaterialTheme.typography.labelSmall) },
                leadingIcon = {
                    Icon(
                        Icons.Default.HorizontalRule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
            
            AssistChip(
                onClick = onEmojiClick,
                label = { Text("表情", style = MaterialTheme.typography.labelSmall) },
                leadingIcon = {
                    Icon(
                        Icons.Default.EmojiEmotions,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
    }
}
