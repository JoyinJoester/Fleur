package takagi.ru.fleur.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import takagi.ru.fleur.data.mapper.MessageMapper
import takagi.ru.fleur.domain.model.SearchFilters
import takagi.ru.fleur.domain.repository.EmailRepository
import takagi.ru.fleur.ui.model.MessageUiModel
import javax.inject.Inject

/**
 * 搜索消息用例
 * 
 * 在指定对话中搜索消息
 * 支持全文搜索，匹配消息内容
 */
class SearchMessagesUseCase @Inject constructor(
    private val emailRepository: EmailRepository
) {
    /**
     * 执行用例
     * 
     * @param query 搜索关键词
     * @param threadId 线程ID（对话ID），用于限制搜索范围
     * @param currentUserEmail 当前用户邮箱地址
     * @return Flow<Result<List<MessageUiModel>>> 搜索结果流
     */
    operator fun invoke(
        query: String,
        threadId: String,
        currentUserEmail: String
    ): Flow<Result<List<MessageUiModel>>> {
        // 验证搜索关键词
        if (query.isBlank()) {
            return kotlinx.coroutines.flow.flowOf(Result.success(emptyList()))
        }
        
        // 创建搜索过滤器，限制在指定线程内搜索
        val filters = SearchFilters(
            threadId = threadId
        )
        
        return emailRepository.searchEmails(
            query = query,
            filters = filters
        ).map { result ->
            result.mapCatching { emails ->
                // 将搜索结果转换为消息列表
                val messages = MessageMapper.fromEmailList(
                    emails = emails,
                    currentUserEmail = currentUserEmail
                )
                
                // 按时间升序排序
                messages.sortedBy { it.timestamp }
            }
        }
    }
    
    /**
     * 在消息列表中本地搜索
     * 
     * 用于在已加载的消息中进行快速搜索，无需访问数据库
     * 
     * @param messages 消息列表
     * @param query 搜索关键词
     * @return 匹配的消息列表
     */
    fun searchInMessages(
        messages: List<MessageUiModel>,
        query: String
    ): List<MessageUiModel> {
        if (query.isBlank()) {
            return emptyList()
        }
        
        val lowerQuery = query.lowercase()
        
        return messages.filter { message ->
            // 在消息内容中搜索
            message.content.lowercase().contains(lowerQuery) ||
            // 在发件人名称中搜索
            message.senderName.lowercase().contains(lowerQuery)
        }
    }
    
    /**
     * 高亮搜索关键词
     * 
     * 在文本中标记匹配的关键词位置
     * 
     * @param text 原始文本
     * @param query 搜索关键词
     * @return 匹配位置的索引列表（起始位置）
     */
    fun highlightMatches(
        text: String,
        query: String
    ): List<Int> {
        if (query.isBlank()) {
            return emptyList()
        }
        
        val lowerText = text.lowercase()
        val lowerQuery = query.lowercase()
        val matches = mutableListOf<Int>()
        
        var startIndex = 0
        while (startIndex < lowerText.length) {
            val index = lowerText.indexOf(lowerQuery, startIndex)
            if (index == -1) break
            
            matches.add(index)
            startIndex = index + lowerQuery.length
        }
        
        return matches
    }
}
