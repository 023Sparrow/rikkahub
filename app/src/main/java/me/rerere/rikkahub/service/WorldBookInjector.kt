package me.rerere.rikkahub.service

import me.rerere.ai.core.MessageRole
import me.rerere.ai.ui.UIMessage
import me.rerere.ai.ui.UIMessagePart
import me.rerere.rikkahub.data.model.Assistant

/**
 * 世界书上下文注入器
 * 负责将匹配的世界书条目注入到对话消息列表中
 */
class WorldBookInjector {
    
    /**
     * 注入配置
     * @property maxTokens 最大token数限制
     * @property enableDeduplication 是否启用去重
     * @property formatStyle 格式化风格
     * @property enableSmartTruncation 是否启用智能截断（保留重要信息）
     * @property preservePriority 是否优先保留高优先级条目
     * @property includeMetadata 是否包含元数据（匹配深度、关键词等）
     * @property enableContextFiltering 是否启用上下文相关性过滤
     */
    data class InjectionConfig(
        val maxTokens: Int = 2000,
        val enableDeduplication: Boolean = true,
        val formatStyle: FormatStyle = FormatStyle.STRUCTURED,
        val enableSmartTruncation: Boolean = true,
        val preservePriority: Boolean = true,
        val includeMetadata: Boolean = true,
        val enableContextFiltering: Boolean = false
    )
    
    /**
     * 格式化风格
     */
    enum class FormatStyle {
        /**
         * 结构化格式，包含标题和元数据
         * 示例:
         * === World Info: Character Background ===
         * [Priority: 10]
         * Content here...
         */
        STRUCTURED,

        /**
         * 简洁格式，只包含内容
         * 示例:
         * Content here...
         */
        MINIMAL,

        /**
         * Markdown格式
         * 示例:
         * ## World Info: Character Background
         * Content here...
         */
        MARKDOWN,

        /**
         * JSON格式，适用于AI程序化处理
         * 示例:
         * {
         *   "world_info": [
         *     {
         *       "title": "Character Background",
         *       "priority": 10,
         *       "content": "..."
         *     }
         *   ]
         * }
         */
        JSON,

        /**
         * XML格式，适用于结构化数据交换
         * 示例:
         * <world_info>
         *   <entry title="Character Background" priority="10">
         *     <content>...</content>
         *   </entry>
         * </world_info>
         */
        XML,

        /**
         * 对话格式，模拟对话中的信息提供
         * 示例:
         * [System] As an AI assistant, I should know that:
         * Character Background: ...
         */
        CONVERSATIONAL
    }
    
    /**
     * 将匹配的世界书条目注入到对话消息列表
     *
     * @param messages 原始消息列表
     * @param matchedEntries 匹配的世界书条目 (已排序和去重)
     * @param assistant 当前助手配置
     * @param config 注入配置
     * @return 注入后的消息列表
     */
    fun injectWorldBook(
        messages: List<UIMessage>,
        matchedEntries: List<WorldBookMatcher.MatchedEntry>,
        assistant: Assistant,
        config: InjectionConfig = InjectionConfig()
    ): List<UIMessage> {
        if (matchedEntries.isEmpty()) {
            return messages
        }

        // 智能过滤和优化
        val processedEntries = processEntries(
            entries = matchedEntries,
            messages = messages,
            config = config
        )

        if (processedEntries.isEmpty()) {
            return messages
        }

        // 格式化世界书内容
        val worldBookMessage = formatWorldBookContent(
            entries = processedEntries,
            style = config.formatStyle,
            maxTokens = config.maxTokens,
            config = config
        )

        // 根据注入位置插入消息
        return injectAtPosition(messages, worldBookMessage, processedEntries)
    }

    /**
     * 处理条目：去重、过滤、排序
     */
    private fun processEntries(
        entries: List<WorldBookMatcher.MatchedEntry>,
        messages: List<UIMessage>,
        config: InjectionConfig
    ): List<WorldBookMatcher.MatchedEntry> {
        var processedEntries = entries

        // 去重检查
        if (config.enableDeduplication) {
            processedEntries = deduplicateEntries(processedEntries, messages)
        }

        // 上下文相关性过滤
        if (config.enableContextFiltering) {
            processedEntries = filterByContextRelevance(processedEntries, messages)
        }

        // 优先级保留策略
        if (config.preservePriority) {
            processedEntries = prioritizeEntries(processedEntries, config.maxTokens)
        }

        return processedEntries
    }

    /**
     * 改进的去重方法：检查消息历史中是否已包含相同内容
     */
    private fun deduplicateEntries(
        entries: List<WorldBookMatcher.MatchedEntry>,
        messages: List<UIMessage>
    ): List<WorldBookMatcher.MatchedEntry> {
        val messageTexts = messages.map { it.toText().lowercase() }.toSet()

        return entries.filter { match ->
            val entryContent = match.entry.content.lowercase()
            // 检查是否在消息历史中出现过相似内容
            !messageTexts.any { messageText ->
                messageText.contains(entryContent.take(50)) || entryContent.contains(messageText.take(50))
            }
        }.distinctBy { it.entry.id }
    }

    /**
     * 上下文相关性过滤
     */
    private fun filterByContextRelevance(
        entries: List<WorldBookMatcher.MatchedEntry>,
        messages: List<UIMessage>
    ): List<WorldBookMatcher.MatchedEntry> {
        if (messages.isEmpty()) return entries

        val recentTexts = messages.takeLast(3).map { it.toText() }.joinToString(" ").lowercase()

        return entries.filter { match ->
            val entryText = match.entry.content.lowercase()
            val keywords = match.matchedKeywords.map { it.lowercase() }

            // 计算相关性分数
            val relevanceScore = calculateRelevanceScore(entryText, keywords, recentTexts)
            relevanceScore > 0.1 // 设置最低相关性阈值
        }
    }

    /**
     * 计算相关性分数
     */
    private fun calculateRelevanceScore(
        entryText: String,
        keywords: List<String>,
        recentTexts: String
    ): Double {
        var score = 0.0

        // 关键词匹配分数
        keywords.forEach { keyword ->
            if (recentTexts.contains(keyword)) {
                score += 0.3
            }
        }

        // 内容相似度分数
        val entryWords = entryText.split("\\s+".toRegex()).toSet()
        val recentWords = recentTexts.split("\\s+").toSet()
        val commonWords = entryWords.intersect(recentWords)
        score += commonWords.size.toDouble() / entryWords.size.toDouble() * 0.7

        return score.coerceAtMost(1.0)
    }

    /**
     * 优先级保留策略：优先保留高优先级条目
     */
    private fun prioritizeEntries(
        entries: List<WorldBookMatcher.MatchedEntry>,
        maxTokens: Int
    ): List<WorldBookMatcher.MatchedEntry> {
        if (entries.size <= 5) return entries // 条目较少时不需要过滤

        // 按优先级排序（常驻条目 > 高优先级 > 低优先级）
        val sortedEntries = entries.sortedWith(compareBy<WorldBookMatcher.MatchedEntry> { !it.entry.isConstant }
            .thenByDescending { it.entry.priority }
            .thenBy { it.matchDepth })

        // 估算token使用量
        var currentTokens = 0
        val prioritizedEntries = mutableListOf<WorldBookMatcher.MatchedEntry>()

        for (entry in sortedEntries) {
            val entryTokens = estimateEntryTokens(entry)
            if (currentTokens + entryTokens <= maxTokens) {
                prioritizedEntries.add(entry)
                currentTokens += entryTokens
            } else {
                break
            }
        }

        return prioritizedEntries
    }

    /**
     * 估算单个条目的token数（更精确的估算）
     */
    private fun estimateEntryTokens(entry: WorldBookMatcher.MatchedEntry): Int {
        var tokens = entry.entry.content.length / 4 // 基础内容

        // 元数据token
        if (entry.matchedKeywords.isNotEmpty()) {
            tokens += entry.matchedKeywords.size * 2
        }
        if (entry.entry.title.isNotEmpty()) {
            tokens += entry.entry.title.length / 4
        }
        if (entry.entry.comment.isNotEmpty()) {
            tokens += entry.entry.comment.length / 4
        }

        return tokens
    }
    
        
    /**
     * 格式化世界书内容为消息
     */
    private fun formatWorldBookContent(
        entries: List<WorldBookMatcher.MatchedEntry>,
        style: FormatStyle,
        maxTokens: Int,
        config: InjectionConfig
    ): UIMessage {
        val content = buildString {
            when (style) {
                FormatStyle.STRUCTURED -> formatStructured(entries, config)
                FormatStyle.MINIMAL -> formatMinimal(entries, config)
                FormatStyle.MARKDOWN -> formatMarkdown(entries, config)
                FormatStyle.JSON -> formatJson(entries, config)
                FormatStyle.XML -> formatXml(entries, config)
                FormatStyle.CONVERSATIONAL -> formatConversational(entries, config)
            }
        }

        // 智能截断或普通截断
        val finalContent = if (config.enableSmartTruncation) {
            smartTruncate(content, maxTokens, entries)
        } else {
            simpleTruncate(content, maxTokens)
        }

        return UIMessage(
            role = MessageRole.SYSTEM,
            parts = listOf(UIMessagePart.Text(finalContent))
        )
    }

    /**
     * 智能截断：保留重要信息
     */
    private fun smartTruncate(
        content: String,
        maxTokens: Int,
        entries: List<WorldBookMatcher.MatchedEntry>
    ): String {
        val estimatedTokens = content.length / 4
        if (estimatedTokens <= maxTokens) {
            return content
        }

        val maxChars = maxTokens * 4
        if (content.length <= maxChars) {
            return content
        }

        // 按条目优先级智能截断
        val lines = content.split("\n").toMutableList()
        val truncatedLines = mutableListOf<String>()
        var currentChars = 0

        // 保留标题和重要标记
        for (line in lines) {
            if (currentChars + line.length > maxChars) break

            // 优先保留标题和重要信息
            if (line.startsWith("===") ||
                line.startsWith("---") ||
                line.startsWith("##") ||
                line.startsWith("###") ||
                line.startsWith("*") ||
                line.contains("[Priority:") ||
                line.contains("[Matched Keywords:")) {
                truncatedLines.add(line)
                currentChars += line.length + 1 // +1 for newline
            } else if (currentChars + line.length + 50 <= maxChars) {
                // 如果还有足够空间，添加内容行
                truncatedLines.add(line)
                currentChars += line.length + 1
            }
        }

        return truncatedLines.joinToString("\n") +
                "\n\n[Content intelligently truncated to fit token limit. " +
                "Important information preserved.]"
    }

    /**
     * 简单截断
     */
    private fun simpleTruncate(content: String, maxTokens: Int): String {
        val estimatedTokens = content.length / 4
        if (estimatedTokens <= maxTokens) {
            return content
        }

        val maxChars = maxTokens * 4
        return content.take(maxChars) + "\n\n[Content truncated due to length limit]"
    }
    
    /**
     * 结构化格式
     */
    private fun StringBuilder.formatStructured(
        entries: List<WorldBookMatcher.MatchedEntry>,
        config: InjectionConfig
    ) {
        appendLine("=== World Information ===")
        appendLine()
        appendLine("The following context is automatically provided based on the conversation:")
        appendLine()

        entries.forEachIndexed { index, match ->
            val entry = match.entry

            appendLine("--- Entry ${index + 1}: ${entry.title} ---")

            if (config.includeMetadata) {
                if (entry.priority > 0) {
                    appendLine("[Priority: ${entry.priority}]")
                }
                if (match.matchDepth > 0) {
                    appendLine("[Recursive Match Depth: ${match.matchDepth}]")
                }
                if (match.matchedKeywords.isNotEmpty()) {
                    appendLine("[Matched Keywords: ${match.matchedKeywords.joinToString(", ")}]")
                }
                if (entry.comment.isNotEmpty()) {
                    appendLine("[Note: ${entry.comment}]")
                }
            }
            appendLine()
            appendLine(entry.content.trim())
            appendLine()
        }

        appendLine("=== End of World Information ===")
    }

    /**
     * 简洁格式
     */
    private fun StringBuilder.formatMinimal(
        entries: List<WorldBookMatcher.MatchedEntry>,
        config: InjectionConfig
    ) {
        appendLine("Relevant context:")
        appendLine()

        entries.forEach { match ->
            appendLine(match.entry.content.trim())
            appendLine()
        }
    }
    
    /**
     * Markdown格式
     */
    private fun StringBuilder.formatMarkdown(
        entries: List<WorldBookMatcher.MatchedEntry>,
        config: InjectionConfig
    ) {
        appendLine("## World Information")
        appendLine()
        appendLine("The following context is automatically provided:")
        appendLine()

        entries.forEach { match ->
            val entry = match.entry

            appendLine("### ${entry.title}")
            if (config.includeMetadata) {
                if (entry.priority > 0 || match.matchDepth > 0) {
                    append("*")
                    if (entry.priority > 0) append("Priority: ${entry.priority}")
                    if (entry.priority > 0 && match.matchDepth > 0) append(", ")
                    if (match.matchDepth > 0) append("Depth: ${match.matchDepth}")
                    appendLine("*")
                    appendLine()
                }
                if (match.matchedKeywords.isNotEmpty()) {
                    appendLine("*Keywords: ${match.matchedKeywords.joinToString(", ")}*")
                    appendLine()
                }
                if (entry.comment.isNotEmpty()) {
                    appendLine("*${entry.comment}*")
                    appendLine()
                }
            }
            appendLine(entry.content.trim())
            appendLine()
        }
    }

    /**
     * JSON格式
     */
    private fun StringBuilder.formatJson(
        entries: List<WorldBookMatcher.MatchedEntry>,
        config: InjectionConfig
    ) {
        appendLine("{")
        appendLine("  \"world_info\": [")

        entries.forEachIndexed { index, match ->
            val entry = match.entry
            appendLine("    {")
            appendLine("      \"title\": \"${entry.title.replace("\"", "\\\"")}\",")
            appendLine("      \"priority\": ${entry.priority},")
            appendLine("      \"content\": \"${entry.content.trim().replace("\"", "\\\"")}\"")

            if (config.includeMetadata) {
                appendLine("      \"match_depth\": ${match.matchDepth},")
                appendLine("      \"matched_keywords\": [${match.matchedKeywords.joinToString(", ") { "\"$it\"" }}],")
                appendLine("      \"comment\": \"${entry.comment.replace("\"", "\\\"")}\",")
                appendLine("      \"is_constant\": ${entry.isConstant},")
                appendLine("      \"created_at\": ${entry.createdAt}")
            }
            appendLine("    }${if (index < entries.size - 1) "," else ""}")
        }

        appendLine("  ]")
        appendLine("}")
    }

    /**
     * XML格式
     */
    private fun StringBuilder.formatXml(
        entries: List<WorldBookMatcher.MatchedEntry>,
        config: InjectionConfig
    ) {
        appendLine("<world_info>")

        entries.forEach { match ->
            val entry = match.entry
            appendLine("  <entry title=\"${entry.title.replace("\"", "&quot;")}\" priority=\"${entry.priority}\">")
            appendLine("    <content>${entry.content.trim().replace("<", "&lt;").replace(">", "&gt;")}</content>")

            if (config.includeMetadata) {
                appendLine("    <match_depth>${match.matchDepth}</match_depth>")
                appendLine("    <matched_keywords>${match.matchedKeywords.joinToString(", ")}</matched_keywords>")
                appendLine("    <comment>${entry.comment.replace("\"", "&quot;")}</comment>")
                appendLine("    <is_constant>${entry.isConstant}</is_constant>")
                appendLine("    <created_at>${entry.createdAt}</created_at>")
            }
            appendLine("  </entry>")
        }

        appendLine("</world_info>")
    }

    /**
     * 对话格式
     */
    private fun StringBuilder.formatConversational(
        entries: List<WorldBookMatcher.MatchedEntry>,
        config: InjectionConfig
    ) {
        appendLine("[System] As an AI assistant, I should know that:")

        entries.forEachIndexed { index, match ->
            val entry = match.entry
            appendLine()
            appendLine("${index + 1}. ${entry.title}:")
            appendLine("   ${entry.content.trim()}")

            if (config.includeMetadata) {
                if (entry.priority > 0) {
                    appendLine("   (Priority: ${entry.priority})")
                }
                if (match.matchedKeywords.isNotEmpty()) {
                    appendLine("   (Keywords: ${match.matchedKeywords.joinToString(", ")})")
                }
            }
        }

        appendLine()
        appendLine("[System] I will use this information to provide more relevant responses.")
    }
    
    /**
     * 根据注入位置插入消息
     * 
     * 注入位置策略:
     * - Position 0 (开头): 在系统提示词之后，第一条用户消息之前
     * - Position 1 (结尾): 在最后一条用户消息之前
     * - 其他: 默认使用Position 0
     */
    private fun injectAtPosition(
        messages: List<UIMessage>,
        worldBookMessage: UIMessage,
        entries: List<WorldBookMatcher.MatchedEntry>
    ): List<UIMessage> {
        if (messages.isEmpty()) {
            return listOf(worldBookMessage)
        }
        
        // 确定注入位置（使用第一个条目的位置设置，因为已按优先级排序）
        val injectionPosition = entries.firstOrNull()?.entry?.injectionPosition ?: 0
        
        return when (injectionPosition) {
            0 -> injectAtBeginning(messages, worldBookMessage)
            1 -> injectAtEnd(messages, worldBookMessage)
            else -> injectAtBeginning(messages, worldBookMessage) // 默认开头
        }
    }
    
    /**
     * 在开头注入 (系统提示词之后)
     */
    private fun injectAtBeginning(
        messages: List<UIMessage>,
        worldBookMessage: UIMessage
    ): List<UIMessage> {
        // 找到第一条非系统消息的位置
        val firstNonSystemIndex = messages.indexOfFirst { it.role != MessageRole.SYSTEM }
        
        return if (firstNonSystemIndex == -1) {
            // 全是系统消息或为空，追加到末尾
            messages + worldBookMessage
        } else if (firstNonSystemIndex == 0) {
            // 没有系统消息，插入到开头
            listOf(worldBookMessage) + messages
        } else {
            // 插入到系统消息之后
            messages.subList(0, firstNonSystemIndex) + worldBookMessage + messages.subList(firstNonSystemIndex, messages.size)
        }
    }
    
    /**
     * 在结尾注入 (最后一条用户消息之前)
     */
    private fun injectAtEnd(
        messages: List<UIMessage>,
        worldBookMessage: UIMessage
    ): List<UIMessage> {
        // 找到最后一条用户消息的位置
        val lastUserMessageIndex = messages.indexOfLast { it.role == MessageRole.USER }
        
        return if (lastUserMessageIndex == -1) {
            // 没有用户消息，追加到末尾
            messages + worldBookMessage
        } else if (lastUserMessageIndex == messages.lastIndex) {
            // 最后一条就是用户消息，插入到它之前
            messages.subList(0, lastUserMessageIndex) + worldBookMessage + listOf(messages.last())
        } else {
            // 插入到最后一条用户消息之前
            messages.subList(0, lastUserMessageIndex) + worldBookMessage + messages.subList(lastUserMessageIndex, messages.size)
        }
    }
    
    /**
     * 估算消息的token数量 (粗略估算)
     */
    fun estimateTokens(message: UIMessage): Int {
        val text = message.toText()
        return text.length / 4 // 简单估算: 1 token ≈ 4 characters
    }
    
    /**
     * 估算所有条目的总token数
     */
    fun estimateTotalTokens(entries: List<WorldBookMatcher.MatchedEntry>): Int {
        return entries.sumOf { it.entry.content.length } / 4
    }
}