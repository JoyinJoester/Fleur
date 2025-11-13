package takagi.ru.fleur.util

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 文件下载管理器
 * 
 * 处理附件的下载功能
 */
class FileDownloadManager(
    private val context: Context
) {
    
    /**
     * 下载进度
     * 
     * @property attachmentId 附件ID
     * @property progress 进度（0-1）
     * @property status 状态
     * @property localPath 本地路径（下载成功后）
     */
    data class DownloadProgress(
        val attachmentId: String,
        val progress: Float,
        val status: DownloadStatus,
        val localPath: String? = null
    )
    
    /**
     * 下载状态
     */
    enum class DownloadStatus {
        DOWNLOADING,  // 下载中
        SUCCESS,      // 成功
        FAILED        // 失败
    }
    
    /**
     * 下载文件
     * 
     * @param attachmentId 附件ID
     * @param url 下载URL
     * @param fileName 文件名
     * @return 下载进度流
     */
    fun downloadFile(
        attachmentId: String,
        url: String,
        fileName: String
    ): Flow<DownloadProgress> = flow {
        try {
            // 开始下载
            emit(DownloadProgress(attachmentId, 0f, DownloadStatus.DOWNLOADING))
            
            // 使用 DownloadManager 下载文件
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle(fileName)
                .setDescription("正在下载附件")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
            
            val downloadId = downloadManager.enqueue(request)
            
            // 监听下载进度
            var downloading = true
            while (downloading) {
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = downloadManager.query(query)
                
                if (cursor.moveToFirst()) {
                    val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val status = cursor.getInt(statusIndex)
                    
                    when (status) {
                        DownloadManager.STATUS_RUNNING -> {
                            val bytesDownloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                            val bytesTotalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                            
                            val bytesDownloaded = cursor.getLong(bytesDownloadedIndex)
                            val bytesTotal = cursor.getLong(bytesTotalIndex)
                            
                            if (bytesTotal > 0) {
                                val progress = bytesDownloaded.toFloat() / bytesTotal.toFloat()
                                emit(DownloadProgress(attachmentId, progress, DownloadStatus.DOWNLOADING))
                            }
                        }
                        
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            val uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                            val localUri = cursor.getString(uriIndex)
                            
                            emit(DownloadProgress(
                                attachmentId,
                                1f,
                                DownloadStatus.SUCCESS,
                                localUri
                            ))
                            downloading = false
                        }
                        
                        DownloadManager.STATUS_FAILED -> {
                            emit(DownloadProgress(attachmentId, 0f, DownloadStatus.FAILED))
                            downloading = false
                        }
                    }
                }
                
                cursor.close()
                
                if (downloading) {
                    kotlinx.coroutines.delay(500)
                }
            }
            
        } catch (e: Exception) {
            emit(DownloadProgress(attachmentId, 0f, DownloadStatus.FAILED))
        }
    }
    
    /**
     * 打开文件
     * 
     * 使用系统默认应用打开文件
     * 
     * @param filePath 文件路径
     */
    suspend fun openFile(filePath: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                return@withContext Result.failure(Exception("文件不存在"))
            }
            
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                setDataAndType(uri, getMimeType(filePath))
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            context.startActivity(intent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取文件的 MIME 类型
     * 
     * @param filePath 文件路径
     * @return MIME 类型
     */
    private fun getMimeType(filePath: String): String {
        val extension = filePath.substringAfterLast('.', "")
        return when (extension.lowercase()) {
            "pdf" -> "application/pdf"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "ppt" -> "application/vnd.ms-powerpoint"
            "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            "txt" -> "text/plain"
            "zip" -> "application/zip"
            "rar" -> "application/x-rar-compressed"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            else -> "application/octet-stream"
        }
    }
    
    /**
     * 检查文件是否已下载
     * 
     * @param fileName 文件名
     * @return 是否已下载
     */
    fun isFileDownloaded(fileName: String): Boolean {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            fileName
        )
        return file.exists()
    }
    
    /**
     * 获取已下载文件的路径
     * 
     * @param fileName 文件名
     * @return 文件路径，如果不存在则返回 null
     */
    fun getDownloadedFilePath(fileName: String): String? {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            fileName
        )
        return if (file.exists()) file.absolutePath else null
    }
}
