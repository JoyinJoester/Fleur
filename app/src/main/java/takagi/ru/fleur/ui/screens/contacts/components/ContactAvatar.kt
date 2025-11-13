package takagi.ru.fleur.ui.screens.contacts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest

/**
 * 联系人头像组件
 * 支持从 URL 加载头像，或显示首字母缩写头像
 * 
 * @param name 联系人姓名
 * @param avatarUrl 头像 URL（可选）
 * @param size 头像尺寸
 * @param modifier Modifier
 */
@Composable
fun ContactAvatar(
    name: String,
    avatarUrl: String? = null,
    size: Dp = 56.dp,
    modifier: Modifier = Modifier
) {
    if (avatarUrl != null) {
        // 从 URL 加载头像
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(avatarUrl)
                .memoryCacheKey(name)
                .diskCacheKey(name)
                .crossfade(true)
                .size(size.value.toInt())
                .build(),
            contentDescription = "$name 的头像",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size)
                .clip(CircleShape)
        )
    } else {
        // 显示首字母缩写头像
        InitialsAvatar(
            name = name,
            size = size,
            modifier = modifier
        )
    }
}

/**
 * 首字母缩写头像
 * 
 * @param name 联系人姓名
 * @param size 头像尺寸
 * @param modifier Modifier
 */
@Composable
private fun InitialsAvatar(
    name: String,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val initials = getInitials(name)
    val backgroundColor = getColorForName(name)
    
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = Color.White,
            fontSize = (size.value * 0.4).sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 提取姓名首字母
 * 支持中文和英文姓名
 * 
 * @param name 姓名
 * @return 首字母（1-2个字符）
 */
private fun getInitials(name: String): String {
    if (name.isBlank()) return "?"
    
    val trimmedName = name.trim()
    
    // 检查是否包含中文字符
    val hasChinese = trimmedName.any { it.code in 0x4E00..0x9FFF }
    
    return if (hasChinese) {
        // 中文姓名：取前两个字符
        trimmedName.take(2)
    } else {
        // 英文姓名：取每个单词的首字母（最多2个）
        trimmedName
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercaseChar() }
            .joinToString("")
    }
}

/**
 * 根据姓名生成颜色
 * 使用哈希算法确保相同姓名总是生成相同颜色
 * 
 * @param name 姓名
 * @return 颜色
 */
private fun getColorForName(name: String): Color {
    // 预定义的颜色列表（Material Design 色板）
    val colors = listOf(
        Color(0xFFE57373), // Red 300
        Color(0xFFF06292), // Pink 300
        Color(0xFFBA68C8), // Purple 300
        Color(0xFF9575CD), // Deep Purple 300
        Color(0xFF7986CB), // Indigo 300
        Color(0xFF64B5F6), // Blue 300
        Color(0xFF4FC3F7), // Light Blue 300
        Color(0xFF4DD0E1), // Cyan 300
        Color(0xFF4DB6AC), // Teal 300
        Color(0xFF81C784), // Green 300
        Color(0xFFAED581), // Light Green 300
        Color(0xFFFFD54F), // Amber 300
        Color(0xFFFFB74D), // Orange 300
        Color(0xFFFF8A65), // Deep Orange 300
        Color(0xFFA1887F), // Brown 300
        Color(0xFF90A4AE)  // Blue Grey 300
    )
    
    // 使用姓名的哈希值选择颜色
    val hash = name.hashCode()
    val index = (hash and 0x7FFFFFFF) % colors.size
    
    return colors[index]
}
