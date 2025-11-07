package me.rerere.rikkahub.service

import me.rerere.ai.ui.UIMessage
import me.rerere.rikkahub.data.db.entity.WorldBookEntry
import kotlin.math.min
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

/**
 * 世界书匹配引擎
 * 负责根据关键词匹配世界书条目，支持正则表达式、递归扫描和优先级排序
 *
 * 性能优化:
 * - 使用缓存避免重复计算
 * - 预编译正则表达式
 * - 优化关键词匹配算法复杂度从O(n*m*k)到O(n*m)
 */
class WorldBookMatcher {

    // 缓存：关键词到预编译正则表达式的映射
    private val regexCache = ConcurrentHashMap<String, Pattern>()

    // 缓存：普通关键词模式的映射
    private val keywordPatternCache = ConcurrentHashMap<String, Pattern>()

    // 缓存：完整的匹配结果，输入文本和条目哈希的映射
    private val matchResultCache = ConcurrentHashMap<String, List<MatchedEntry>>()
    
    /**
     * 匹配结果数据类
     * @property entry 匹配的世界书条目
     * @property matchDepth 递归深度 (0表示直接匹配，>0表示递归匹配)
     * @property matchedKeywords 匹配到的关键词列表
     */
    data class MatchedEntry(
        val entry: WorldBookEntry,
        val matchDepth: Int = 0,
        val matchedKeywords: List<String> = emptyList()
    )
    
    /**
     * 扫描用户输入和对话历史，匹配世界书条目
     *
     * @param input 用户当前输入
     * @param conversationHistory 最近的对话历史
     * @param entries 启用的世界书条目列表
     * @param maxHistoryMessages 最多检查的历史消息数量 (默认5条)
     * @param maxRecursionDepth 最大递归深度 (默认3层)
     * @return 匹配的条目列表 (按优先级排序，去重)
     */
    fun matchEntries(
        input: String,
        conversationHistory: List<UIMessage>,
        entries: List<WorldBookEntry>,
        maxHistoryMessages: Int = 5,
        maxRecursionDepth: Int = 3
    ): List<MatchedEntry> {
        if (entries.isEmpty() || (input.isBlank() && conversationHistory.isEmpty())) {
            return emptyList()
        }

        // 生成缓存键
        val cacheKey = generateCacheKey(input, conversationHistory, entries, maxHistoryMessages, maxRecursionDepth)

        // 检查缓存
        matchResultCache[cacheKey]?.let { cachedResult ->
            return cachedResult
        }

        // 构建搜索文本：当前输入 + 最近N条历史消息
        val searchTexts = buildSearchTexts(input, conversationHistory, maxHistoryMessages)

        // 第一轮：直接匹配
        val directMatches = performDirectMatching(searchTexts, entries)

        // 递归匹配
        val allMatches = if (maxRecursionDepth > 0) {
            performRecursiveMatching(directMatches, entries, maxRecursionDepth)
        } else {
            directMatches
        }

        // 去重并按优先级排序
        val result = deduplicateAndSort(allMatches)

        // 缓存结果（限制缓存大小）
        if (matchResultCache.size > 100) {
            matchResultCache.clear() // 简单的缓存清理策略
        }
        matchResultCache[cacheKey] = result

        return result
    }

    /**
     * 生成缓存键
     */
    private fun generateCacheKey(
        input: String,
        conversationHistory: List<UIMessage>,
        entries: List<WorldBookEntry>,
        maxHistoryMessages: Int,
        maxRecursionDepth: Int
    ): String {
        val entriesHash = entries.map { "${it.id}:${it.updatedAt}" }.hashCode()
        val historyHash = conversationHistory.takeLast(maxHistoryMessages).hashCode()
        return "${input.hashCode()}_${historyHash}_${entriesHash}_${maxRecursionDepth}"
    }

    /**
     * 清除缓存（用于条目更新时）
     */
    fun clearCache() {
        regexCache.clear()
        keywordPatternCache.clear()
        matchResultCache.clear()
    }
    
    /**
     * 构建搜索文本列表
     */
    private fun buildSearchTexts(
        input: String,
        conversationHistory: List<UIMessage>,
        maxHistoryMessages: Int
    ): List<String> {
        val texts = mutableListOf<String>()
        
        // 添加当前输入
        if (input.isNotBlank()) {
            texts.add(input)
        }
        
        // 添加最近的历史消息
        conversationHistory
            .takeLast(maxHistoryMessages)
            .forEach { message ->
                val messageText = message.toText()
                if (messageText.isNotBlank()) {
                    texts.add(messageText)
                }
            }
        
        return texts
    }
    
    /**
     * 执行直接匹配
     */
    private fun performDirectMatching(
        searchTexts: List<String>,
        entries: List<WorldBookEntry>
    ): List<MatchedEntry> {
        val matches = mutableListOf<MatchedEntry>()
        
        for (entry in entries) {
            // 跳过禁用的条目
            if (!entry.isEnabled) continue
            
            // 常驻条目始终匹配
            if (entry.isConstant) {
                matches.add(MatchedEntry(entry, matchDepth = 0, matchedKeywords = listOf("<constant>")))
                continue
            }
            
            // 检查主关键词匹配
            val matchResult = checkKeywordMatch(entry, searchTexts)
            if (matchResult.matched) {
                matches.add(
                    MatchedEntry(
                        entry = entry,
                        matchDepth = 0,
                        matchedKeywords = matchResult.matchedKeywords
                    )
                )
            }
        }
        
        return matches
    }
    
    /**
     * 关键词匹配结果
     */
    private data class KeywordMatchResult(
        val matched: Boolean,
        val matchedKeywords: List<String> = emptyList()
    )
    
    /**
     * 检查条目的关键词是否匹配
     * 优化：减少重复计算，提前退出
     */
    private fun checkKeywordMatch(
        entry: WorldBookEntry,
        searchTexts: List<String>
    ): KeywordMatchResult {
        if (entry.keywords.isEmpty() && entry.secondaryKeywords.isEmpty()) {
            return KeywordMatchResult(matched = false)
        }

        val matchedKeywords = mutableListOf<String>()
        val useRegex = entry.useRegex

        // 检查主关键词 - 优化：提前退出
        var primaryMatched = false
        for (keyword in entry.keywords) {
            if (keyword.isBlank()) continue

            for (text in searchTexts) {
                val matched = if (useRegex) {
                    matchRegex(keyword, text)
                } else {
                    matchKeyword(keyword, text)
                }

                if (matched) {
                    matchedKeywords.add(keyword)
                    primaryMatched = true
                    break // 找到一个匹配就跳出
                }
            }

            if (primaryMatched && !entry.isSelective) break // 非选择性模式，找到一个就够了
        }

        // 常驻条目如果没有关键词也算匹配
        if (entry.isConstant) {
            return KeywordMatchResult(matched = true, matchedKeywords = listOf("<constant>"))
        }

        if (!primaryMatched) {
            return KeywordMatchResult(matched = false)
        }

        // 检查次要关键词 - 优化：根据选择性模式选择策略
        if (entry.secondaryKeywords.isNotEmpty()) {
            val secondaryMatched = if (entry.isSelective) {
                // Selective模式：次要关键词需要全部匹配 (AND逻辑)
                var allMatched = true
                for (keyword in entry.secondaryKeywords) {
                    var keywordMatched = false
                    for (text in searchTexts) {
                        val matched = if (useRegex) {
                            matchRegex(keyword, text)
                        } else {
                            matchKeyword(keyword, text)
                        }
                        if (matched) {
                            matchedKeywords.add(keyword)
                            keywordMatched = true
                            break
                        }
                    }
                    if (!keywordMatched) {
                        allMatched = false
                        break // 有一个不匹配就退出
                    }
                }
                allMatched
            } else {
                // 非Selective模式：次要关键词只需匹配一个 (OR逻辑)
                var anyMatched = false
                for (keyword in entry.secondaryKeywords) {
                    for (text in searchTexts) {
                        val matched = if (useRegex) {
                            matchRegex(keyword, text)
                        } else {
                            matchKeyword(keyword, text)
                        }
                        if (matched) {
                            matchedKeywords.add(keyword)
                            anyMatched = true
                            break
                        }
                    }
                    if (anyMatched) break // 找到一个匹配就退出
                }
                anyMatched
            }

            if (!secondaryMatched) {
                return KeywordMatchResult(matched = false)
            }
        }

        return KeywordMatchResult(matched = true, matchedKeywords = matchedKeywords)
    }
    
    /**
     * 普通关键词匹配 (不区分大小写，单词边界)
     * 优化：使用预编译的正则表达式缓存
     */
    private fun matchKeyword(keyword: String, text: String): Boolean {
        if (keyword.isBlank()) return false

        val lowerKeyword = keyword.trim().lowercase()

        // 获取或创建预编译的模式
        val pattern = keywordPatternCache.getOrPut(lowerKeyword) {
            try {
                Pattern.compile("\\b${Pattern.quote(lowerKeyword)}\\b", Pattern.CASE_INSENSITIVE)
            } catch (e: Exception) {
                // 如果模式编译失败，使用简单的contains匹配
                null
            }
        }

        return if (pattern != null) {
            pattern.matcher(text).find()
        } else {
            // 回退到简单的contains匹配
            text.lowercase().contains(lowerKeyword)
        }
    }

    /**
     * 正则表达式匹配
     * 优化：使用预编译的正则表达式缓存
     */
    private fun matchRegex(regexPattern: String, text: String): Boolean {
        if (regexPattern.isBlank()) return false

        // 获取或创建预编译的正则表达式
        val pattern = regexCache.getOrPut(regexPattern) {
            try {
                Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE)
            } catch (e: Exception) {
                // 正则表达式无效，返回null表示应该回退到普通匹配
                null
            }
        }

        return if (pattern != null) {
            pattern.matcher(text).find()
        } else {
            // 回退到普通关键词匹配
            matchKeyword(regexPattern, text)
        }
    }
    
    /**
     * 执行递归匹配
     * 已匹配条目的内容可能触发其他条目
     */
    private fun performRecursiveMatching(
        baseMatches: List<MatchedEntry>,
        entries: List<WorldBookEntry>,
        maxDepth: Int
    ): List<MatchedEntry> {
        val allMatches = baseMatches.toMutableList()
        val processedEntryIds = baseMatches.map { it.entry.id }.toMutableSet()
        
        var currentDepth = 0
        var currentLevelMatches = baseMatches
        
        while (currentDepth < maxDepth && currentLevelMatches.isNotEmpty()) {
            val nextLevelMatches = mutableListOf<MatchedEntry>()
            
            // 从当前层级的匹配条目中提取内容
            val searchTextsForNextLevel = currentLevelMatches
                .filter { !it.entry.excludeRecursion } // 排除标记为不递归的条目
                .map { it.entry.content }
            
            if (searchTextsForNextLevel.isEmpty()) {
                break
            }
            
            // 在未匹配的条目中查找
            val remainingEntries = entries.filter { 
                it.isEnabled && it.id !in processedEntryIds 
            }
            
            for (entry in remainingEntries) {
                // 跳过常驻条目 (已在直接匹配中处理)
                if (entry.isConstant) continue
                
                val matchResult = checkKeywordMatch(entry, searchTextsForNextLevel)
                if (matchResult.matched) {
                    val newMatch = MatchedEntry(
                        entry = entry,
                        matchDepth = currentDepth + 1,
                        matchedKeywords = matchResult.matchedKeywords
                    )
                    nextLevelMatches.add(newMatch)
                    allMatches.add(newMatch)
                    processedEntryIds.add(entry.id)
                }
            }
            
            currentLevelMatches = nextLevelMatches
            currentDepth++
        }
        
        return allMatches
    }
    
    /**
     * 去重并按优先级排序
     * 
     * 排序规则:
     * 1. 常驻条目优先
     * 2. 按priority降序 (数字越大优先级越高)
     * 3. 按匹配深度升序 (直接匹配优先于递归匹配)
     * 4. 按创建时间升序 (早创建的优先)
     */
    private fun deduplicateAndSort(matches: List<MatchedEntry>): List<MatchedEntry> {
        return matches
            .distinctBy { it.entry.id } // 去重
            .sortedWith(
                compareBy<MatchedEntry> { !it.entry.isConstant } // 常驻条目优先
                    .thenByDescending { it.entry.priority }      // 优先级降序
                    .thenBy { it.matchDepth }                    // 匹配深度升序
                    .thenBy { it.entry.createdAt }               // 创建时间升序
            )
    }
    
    /**
     * 格式化匹配结果为可读字符串 (用于调试)
     */
    fun formatMatchedEntries(matches: List<MatchedEntry>): String {
        if (matches.isEmpty()) return "No matches found"
        
        return buildString {
            appendLine("=== Matched World Book Entries (${matches.size}) ===")
            matches.forEachIndexed { index, match ->
                appendLine()
                appendLine("${index + 1}. ${match.entry.title}")
                appendLine("   ID: ${match.entry.id}")
                appendLine("   Priority: ${match.entry.priority}")
                appendLine("   Match Depth: ${match.matchDepth}")
                appendLine("   Matched Keywords: ${match.matchedKeywords.joinToString(", ")}")
                appendLine("   Constant: ${match.entry.isConstant}")
                appendLine("   Content Preview: ${match.entry.content.take(100)}...")
            }
        }
    }
}