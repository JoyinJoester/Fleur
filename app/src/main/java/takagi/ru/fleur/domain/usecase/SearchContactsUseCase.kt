package takagi.ru.fleur.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import takagi.ru.fleur.domain.repository.ContactRepository
import takagi.ru.fleur.ui.model.ContactUiModel
import javax.inject.Inject

/**
 * 搜索已保存联系人用例
 * 支持按姓名、邮箱地址和组织搜索联系人
 */
class SearchContactsUseCase @Inject constructor(
    private val contactRepository: ContactRepository
) {
    /**
     * 执行用例
     * @param query 搜索关键词
     * @return Flow<Result<List<ContactUiModel>>> 搜索结果流
     */
    operator fun invoke(query: String): Flow<Result<List<ContactUiModel>>> {
        if (query.isBlank()) {
            return kotlinx.coroutines.flow.flowOf(Result.success(emptyList()))
        }
        
        return contactRepository.searchContacts(query).map { contacts ->
            Result.success(
                contacts.map { contact ->
                    ContactUiModel(
                        id = contact.id,
                        name = contact.name,
                        email = contact.email,
                        phoneNumber = contact.phoneNumber,
                        avatarUrl = contact.avatarUrl,
                        isFavorite = contact.isFavorite
                    )
                }
            )
        }
    }
}

