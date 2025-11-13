package takagi.ru.fleur.util

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import takagi.ru.fleur.domain.model.Attachment
import java.io.File
import java.util.UUID

/**
 * 附件上传工具类
 * 
 * 处理附件的压缩和上传
 */
class AttachmentUploader(
    private val context: Context
) {
    
    /**
     * 上传进度
     * 
     * @property attachmentId 附件ID
     * @property progress 进度（0-1）
     * @property status 状态
     */
    data class UploadProgress(
        val attachmentId: String,
        val progress: Float,
        val status: UploadStatus
    )
    
    /**
     * 上传状态
     */
    enum class UploadStatus {
        COMPRESSING,  // 压缩中
        UPLOADING,    // 上传中
        SUCCESS,      // 成功
        FAILED        // 失败
    }
    
    /**
     * 上传图片附件
     * 
     * 自动处理图片压缩
     * 
     * @param uri 图片 URI
     * @param fileName 文件名
     * @return 上传进度流
     */
    fun uploadImage(
        uri: Uri,
        fileName: String
    ): Flow<UploadProgress> = flow {
        val attachmentId = UUID.randomUUID().toString()
        
        try {
            // 压缩图片
            emit(UploadProgress(attachmentId, 0f, UploadStatus.COMPRESSING))
            
            val compressResult = ImageCompressor.compressImage(context, uri)
            
            if (compressResult.isFailure) {
                emit(UploadProgress(attachmentId, 0f, UploadStatus.FAILED))
                return@flow
            }
            
            val compressedFile = compressResult.getOrNull()
            val fileToUpload = compressedFile ?: run {
                // 如果不需要压缩，使用原始文件
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw Exception("无法打开文件")
                
                val tempFile = File(context.cacheDir, "temp_$fileName")
                tempFile.outputStream().use { output ->
                    inputStream.copyTo(output)
                }
                inputStream.close()
                tempFile
            }
            
            // 上传文件
            emit(UploadProgress(attachmentId, 0.3f, UploadStatus.UPLOADING))
            
            // TODO: 实现实际的上传逻辑
            // 这里需要调用邮件服务器的附件上传 API
            // 暂时模拟上传过程
            
            // 模拟上传进度
            emit(UploadProgress(attachmentId, 0.5f, UploadStatus.UPLOADING))
            kotlinx.coroutines.delay(500)
            
            emit(UploadProgress(attachmentId, 0.8f, UploadStatus.UPLOADING))
            kotlinx.coroutines.delay(500)
            
            // 上传成功
            emit(UploadProgress(attachmentId, 1f, UploadStatus.SUCCESS))
            
            // 清理临时文件
            if (compressedFile != null) {
                compressedFile.delete()
            }
            
        } catch (e: Exception) {
            emit(UploadProgress(attachmentId, 0f, UploadStatus.FAILED))
        }
    }
    
    /**
     * 上传文件附件
     * 
     * @param uri 文件 URI
     * @param fileName 文件名
     * @return 上传进度流
     */
    fun uploadFile(
        uri: Uri,
        fileName: String
    ): Flow<UploadProgress> = flow {
        val attachmentId = UUID.randomUUID().toString()
        
        try {
            // 检查文件大小
            val fileSize = getFileSize(uri)
            
            if (fileSize > 25 * 1024 * 1024) {
                // 文件大于 25MB
                emit(UploadProgress(attachmentId, 0f, UploadStatus.FAILED))
                return@flow
            }
            
            // 上传文件
            emit(UploadProgress(attachmentId, 0f, UploadStatus.UPLOADING))
            
            // TODO: 实现实际的上传逻辑
            // 这里需要调用邮件服务器的附件上传 API
            
            // 模拟上传进度
            emit(UploadProgress(attachmentId, 0.3f, UploadStatus.UPLOADING))
            kotlinx.coroutines.delay(500)
            
            emit(UploadProgress(attachmentId, 0.6f, UploadStatus.UPLOADING))
            kotlinx.coroutines.delay(500)
            
            emit(UploadProgress(attachmentId, 0.9f, UploadStatus.UPLOADING))
            kotlinx.coroutines.delay(500)
            
            // 上传成功
            emit(UploadProgress(attachmentId, 1f, UploadStatus.SUCCESS))
            
        } catch (e: Exception) {
            emit(UploadProgress(attachmentId, 0f, UploadStatus.FAILED))
        }
    }
    
    /**
     * 获取文件大小
     * 
     * @param uri 文件 URI
     * @return 文件大小（字节）
     */
    private fun getFileSize(uri: Uri): Long {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.available().toLong()
            } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * 获取文件信息
     * 
     * @param uri 文件 URI
     * @return 文件名和大小
     */
    suspend fun getFileInfo(uri: Uri): Pair<String, Long> = withContext(Dispatchers.IO) {
        val fileName = getFileName(uri)
        val fileSize = getFileSize(uri)
        Pair(fileName, fileSize)
    }
    
    /**
     * 获取文件名
     * 
     * @param uri 文件 URI
     * @return 文件名
     */
    private fun getFileName(uri: Uri): String {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            } ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }
}
