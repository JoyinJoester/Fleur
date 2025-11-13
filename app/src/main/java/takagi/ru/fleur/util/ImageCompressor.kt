package takagi.ru.fleur.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.min

/**
 * 图片压缩工具类
 * 
 * 提供图片压缩功能，用于在上传前减小图片大小
 */
object ImageCompressor {
    
    // 最大文件大小（5MB）
    private const val MAX_FILE_SIZE = 5 * 1024 * 1024
    
    // 最大图片尺寸
    private const val MAX_WIDTH = 1920
    private const val MAX_HEIGHT = 1920
    
    /**
     * 压缩图片
     * 
     * 如果图片大于 5MB，则进行压缩
     * 
     * @param context 上下文
     * @param uri 图片 URI
     * @return 压缩后的文件，如果不需要压缩则返回 null
     */
    suspend fun compressImage(
        context: Context,
        uri: Uri
    ): Result<File?> = withContext(Dispatchers.IO) {
        try {
            // 获取文件大小
            val fileSize = getFileSize(context, uri)
            
            // 如果文件小于 5MB，不需要压缩
            if (fileSize < MAX_FILE_SIZE) {
                return@withContext Result.success(null)
            }
            
            // 读取图片
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(IOException("无法打开图片"))
            
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            
            if (bitmap == null) {
                return@withContext Result.failure(IOException("无法解码图片"))
            }
            
            // 修正图片方向
            val rotatedBitmap = rotateImageIfRequired(context, uri, bitmap)
            
            // 计算缩放比例
            val scale = calculateScale(rotatedBitmap.width, rotatedBitmap.height)
            
            // 缩放图片
            val scaledBitmap = if (scale < 1.0f) {
                Bitmap.createScaledBitmap(
                    rotatedBitmap,
                    (rotatedBitmap.width * scale).toInt(),
                    (rotatedBitmap.height * scale).toInt(),
                    true
                )
            } else {
                rotatedBitmap
            }
            
            // 创建临时文件
            val compressedFile = File(
                context.cacheDir,
                "compressed_${System.currentTimeMillis()}.jpg"
            )
            
            // 压缩并保存
            var quality = 90
            var outputStream = FileOutputStream(compressedFile)
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.close()
            
            // 如果文件仍然太大，降低质量
            while (compressedFile.length() > MAX_FILE_SIZE && quality > 50) {
                quality -= 10
                outputStream = FileOutputStream(compressedFile)
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                outputStream.close()
            }
            
            // 释放 Bitmap
            if (rotatedBitmap != bitmap) {
                rotatedBitmap.recycle()
            }
            if (scaledBitmap != rotatedBitmap) {
                scaledBitmap.recycle()
            }
            bitmap.recycle()
            
            Result.success(compressedFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取文件大小
     * 
     * @param context 上下文
     * @param uri 文件 URI
     * @return 文件大小（字节）
     */
    private fun getFileSize(context: Context, uri: Uri): Long {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.available().toLong()
            } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * 计算缩放比例
     * 
     * @param width 原始宽度
     * @param height 原始高度
     * @return 缩放比例
     */
    private fun calculateScale(width: Int, height: Int): Float {
        if (width <= MAX_WIDTH && height <= MAX_HEIGHT) {
            return 1.0f
        }
        
        val widthScale = MAX_WIDTH.toFloat() / width
        val heightScale = MAX_HEIGHT.toFloat() / height
        
        return min(widthScale, heightScale)
    }
    
    /**
     * 修正图片方向
     * 
     * 根据 EXIF 信息旋转图片
     * 
     * @param context 上下文
     * @param uri 图片 URI
     * @param bitmap 原始 Bitmap
     * @return 修正方向后的 Bitmap
     */
    private fun rotateImageIfRequired(
        context: Context,
        uri: Uri,
        bitmap: Bitmap
    ): Bitmap {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return bitmap
            
            val exif = ExifInterface(inputStream)
            inputStream.close()
            
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                else -> bitmap
            }
        } catch (e: Exception) {
            bitmap
        }
    }
    
    /**
     * 旋转 Bitmap
     * 
     * @param bitmap 原始 Bitmap
     * @param degrees 旋转角度
     * @return 旋转后的 Bitmap
     */
    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        
        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }
    
    /**
     * 格式化文件大小
     * 
     * @param bytes 字节数
     * @return 格式化后的字符串
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }
}
