package takagi.ru.fleur.util

import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination

/**
 * 拼音工具类
 * 支持中文转拼音、首字母提取等功能
 */
object PinyinUtils {
    
    private val format = HanyuPinyinOutputFormat().apply {
        caseType = HanyuPinyinCaseType.LOWERCASE
        toneType = HanyuPinyinToneType.WITHOUT_TONE
        vCharType = HanyuPinyinVCharType.WITH_V
    }
    
    /**
     * 获取字符串的拼音全拼(小写)
     * 例如: "张三" -> "zhangsan"
     */
    fun getPinyin(text: String): String {
        return try {
            text.map { char ->
                if (isChinese(char)) {
                    val pinyinArray = PinyinHelper.toHanyuPinyinStringArray(char, format)
                    pinyinArray?.firstOrNull() ?: char.toString()
                } else {
                    char.toString()
                }
            }.joinToString("")
        } catch (e: BadHanyuPinyinOutputFormatCombination) {
            text
        }
    }
    
    /**
     * 获取字符串的拼音首字母(小写)
     * 例如: "张三" -> "zs"
     */
    fun getPinyinFirstLetters(text: String): String {
        return try {
            text.map { char ->
                if (isChinese(char)) {
                    val pinyinArray = PinyinHelper.toHanyuPinyinStringArray(char, format)
                    pinyinArray?.firstOrNull()?.firstOrNull() ?: char
                } else {
                    char
                }
            }.joinToString("")
        } catch (e: BadHanyuPinyinOutputFormatCombination) {
            text
        }
    }
    
    /**
     * 判断字符是否为中文
     */
    private fun isChinese(char: Char): Boolean {
        return char.code in 0x4E00..0x9FA5
    }
    
    /**
     * 搜索匹配(支持拼音、首字母、原文)
     * @param text 要搜索的文本
     * @param query 搜索关键词
     * @return 是否匹配
     */
    fun matches(text: String, query: String): Boolean {
        if (query.isBlank()) return true
        
        val lowerQuery = query.lowercase()
        val lowerText = text.lowercase()
        
        // 1. 直接匹配原文
        if (lowerText.contains(lowerQuery)) {
            return true
        }
        
        // 2. 匹配拼音全拼
        val pinyin = getPinyin(text).lowercase()
        if (pinyin.contains(lowerQuery)) {
            return true
        }
        
        // 3. 匹配拼音首字母
        val firstLetters = getPinyinFirstLetters(text).lowercase()
        if (firstLetters.contains(lowerQuery)) {
            return true
        }
        
        return false
    }
}
