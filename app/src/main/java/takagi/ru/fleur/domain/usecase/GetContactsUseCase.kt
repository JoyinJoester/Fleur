package takagi.ru.fleur.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import takagi.ru.fleur.data.mapper.ContactMapper
import takagi.ru.fleur.domain.repository.EmailRepository
import takagi.ru.fleur.ui.model.ContactUiModel
import javax.inject.Inject

/**
 * 获取联系人列表用例
 * 从邮件中提取所有联系人，去重并按姓名排序
 */
class GetContactsUseCase @Inject constructor(
    private val emailRepository: EmailRepository
) {
    /**
     * 执行用例
     * @param accountId 账户ID，null表示所有账户
     * @return Flow<Result<List<ContactUiModel>>> 联系人列表流
     */
    operator fun invoke(
        accountId: String? = null
    ): Flow<Result<List<ContactUiModel>>> {
        // 获取所有邮件（使用较大的页面大小以获取足够的联系人）
        return emailRepository.getEmails(
            accountId = accountId,
            page = 0,
            pageSize = 500
        ).map { result ->
            result.map { emails ->
                // 从邮件中提取联系人
                ContactMapper.extractContactsFromEmails(emails)
            }
        }
    }
}
