package me.rerere.rikkahub.service

import me.rerere.ai.ui.UIMessage
import me.rerere.rikkahub.data.db.entity.WorldBookEntry
import kotlin.math.min

/**
 * 世界书匹配引擎
 * 负责根据关键词匹配世界书条目，支持正则表达式、递归扫描和优先级排序
 */
class WorldBookMatcher {
    
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
        return deduplicateAndSort(allMatches)
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
     */
    private fun checkKeywordMatch(
        entry: WorldBookEntry,
        searchTexts: List<String>
    ): KeywordMatchResult {
        val matchedKeywords = mutableListOf<String>()
        
        // 检查主关键词
        val primaryMatched = entry.keywords.any { keyword ->
            searchTexts.any { text ->
                val matched = if (entry.useRegex) {
                    matchRegex(keyword, text)
                } else {
                    matchKeyword(keyword, text)
                }
                if (matched) matchedKeywords.add(keyword)
                matched
            }
        }
        
        if (!primaryMatched) {
            return KeywordMatchResult(matched = false)
        }
        
        // 如果有次要关键词，检查次要关键词
        if (entry.secondaryKeywords.isNotEmpty()) {
            val secondaryMatched = if (entry.isSelective) {
                // Selective模式：次要关键词需要全部匹配 (AND逻辑)
                entry.secondaryKeywords.all { keyword ->
                    searchTexts.any { text ->
                        val matched = if (entry.useRegex) {
                            matchRegex(keyword, text)
                        } else {
                            matchKeyword(keyword, text)
                        }
                        if (matched) matchedKeywords.add(keyword)
                        matched
                    }
                }
            } else {
                // 非Selective模式：次要关键词只需匹配一个 (OR逻辑)
                entry.secondaryKeywords.any { keyword ->
                    searchTexts.any { text ->
                        val matched = if (entry.useRegex) {
                            matchRegex(keyword, text)
                        } else {
                            matchKeyword(keyword, text)
                        }
                        if (matched) matchedKeywords.add(keyword)
                        matched
                    }
                }
            }
            
            if (!secondaryMatched) {
                return KeywordMatchResult(matched = false)
            }
        }
        
        return KeywordMatchResult(matched = true, matchedKeywords = matchedKeywords)
    }
    
    /**
     * 普通关键词匹配 (不区分大小写，单词边界)
     */
    private fun matchKeyword(keyword: String, text: String): Boolean {
        if (keyword.isBlank()) return false
        
        // 不区分大小写
        val lowerKeyword = keyword.trim().lowercase()
        val lowerText = text.lowercase()
        
        // 检查是否包含关键词
        if (!lowerText.contains(lowerKeyword)) {
            return false
        }
        
        // 检查单词边界 (避免部分匹配，如 "cat" 不应该匹配 "category")
        val pattern = "\\b${Regex.escape(lowerKeyword)}\\b".toRegex()
        return pattern.containsMatchIn(lowerText)
    }
    
    /**
     * 正则表达式匹配
     */
    private fun matchRegex(pattern: String, text: String): Boolean {
        return try {
            val regex = pattern.toRegex(RegexOption.IGNORE_CASE)
            regex.containsMatchIn(text)
        } catch (e: Exception) {
            // 正则表达式无效，回退到普通匹配
            matchKeyword(pattern, text)
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