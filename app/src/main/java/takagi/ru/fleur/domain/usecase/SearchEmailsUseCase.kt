package takagi.ru.fleur.domain.usecase

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import takagi.ru.fleur.domain.model.Email
import takagi.ru.fleur.domain.model.SearchFilters
import takagi.ru.fleur.domain.repository.EmailRepository
import javax.inject.Inject

/**
 * 搜索邮件用例
 * 实现300ms防抖
 */
class SearchEmailsUseCase @Inject constructor(
    private val emailRepository: EmailRepository
) {
    companion object {
        private const val DEBOUNCE_TIMEOUT_MS = 300L
    }
    
    /**
     * 执行用例
     * @param query 搜索关键词
     * @param filters 搜索过滤器
     */
    @OptIn(FlowPreview::class)
    operator fun invoke(
        query: String,
        filters: SearchFilters = SearchFilters()
    ): Flow<Result<List<Email>>> {
        return emailRepository.searchEmails(query, filters)
            .debounce(DEBOUNCE_TIMEOUT_MS)
    }
}
