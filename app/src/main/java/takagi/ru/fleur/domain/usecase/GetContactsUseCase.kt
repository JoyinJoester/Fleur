package takagi.ru.fleur.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import takagi.ru.fleur.domain.repository.ContactRepository
import takagi.ru.fleur.ui.model.ContactUiModel
import javax.inject.Inject

/**
 * 获取已保存联系人用例
 * 从数据库中获取用户保存的联系人列表
 */
class GetContactsUseCase @Inject constructor(
    private val contactRepository: ContactRepository
) {
    /**
     * 执行用例
     * @return Flow<Result<List<ContactUiModel>>> 已保存联系人列表流
     */
    operator fun invoke(): Flow<Result<List<ContactUiModel>>> {
        return contactRepository.getAllContacts().map { contacts ->
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

