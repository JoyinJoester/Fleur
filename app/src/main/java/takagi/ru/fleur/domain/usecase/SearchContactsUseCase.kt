package takagi.ru.fleur.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import takagi.ru.fleur.data.mapper.ContactMapper
import takagi.ru.fleur.domain.repository.EmailRepository
import takagi.ru.fleur.ui.model.ContactUiModel
import javax.inject.Inject

/**
 * 搜索联系人用例
 * 支持按姓名和邮箱地址搜索联系人
 */
class SearchContactsUseCase @Inject constructor(
    private val emailRepository: EmailRepository
) {
    /**
     * 执行用例
     * @param query 搜索关键词
     * @param accountId 账户ID，null表示所有账户
     * @return Flow<Result<List<ContactUiModel>>> 搜索结果流
     */
    operator fun invoke(
        query: String,
        accountId: String? = null
    ): Flow<Result<List<ContactUiModel>>> {
        if (query.isBlank()) {
            return kotlinx.coroutines.flow.flowOf(Result.success(emptyList()))
        }
        
        // 获取所有邮件
        return emailRepository.getEmails(
            accountId = accountId,
            page = 0,
            pageSize = 500
        ).map { result ->
            result.map { emails ->
                // 从邮件中提取联系人
                val allContacts = ContactMapper.extractContactsFromEmails(emails)
                
                // 过滤匹配的联系人
                allContacts.filter { contact ->
                    contact.name.contains(query, ignoreCase = true) ||
                    contact.email.contains(query, ignoreCase = true)
                }
            }
        }
    }
}
