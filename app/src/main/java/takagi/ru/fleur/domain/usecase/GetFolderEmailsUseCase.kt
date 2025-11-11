package takagi.ru.fleur.domain.usecase

import kotlinx.coroutines.flow.Flow
import takagi.ru.fleur.domain.model.Email
import takagi.ru.fleur.domain.repository.EmailRepository
import takagi.ru.fleur.ui.screens.folder.FolderType
import javax.inject.Inject

/**
 * 获取文件夹邮件用例
 * 根据文件夹类型获取对应的邮件列表
 */
class GetFolderEmailsUseCase @Inject constructor(
    private val emailRepository: EmailRepository
) {
    /**
     * 根据文件夹类型获取邮件列表
     * @param folderType 文件夹类型
     * @param accountId 账户ID
     * @param page 页码（从0开始）
     * @param pageSize 每页数量
     * @return Flow<Result<List<Email>>> 邮件列表流
     */
    operator fun invoke(
        folderType: FolderType,
        accountId: String,
        page: Int = 0,
        pageSize: Int = 50
    ): Flow<Result<List<Email>>> {
        return when (folderType) {
            FolderType.SENT -> emailRepository.getSentEmails(accountId, page, pageSize)
            FolderType.DRAFTS -> emailRepository.getDraftEmails(accountId, page, pageSize)
            FolderType.STARRED -> emailRepository.getStarredEmails(accountId, page, pageSize)
            FolderType.ARCHIVE -> emailRepository.getArchivedEmails(accountId, page, pageSize)
            FolderType.TRASH -> emailRepository.getTrashedEmails(accountId, page, pageSize)
        }
    }
}
