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
     */
    data class InjectionConfig(
        val maxTokens: Int = 2000,
        val enableDeduplication: Boolean = true,
        val formatStyle: FormatStyle = FormatStyle.STRUCTURED
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
        MARKDOWN
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
        
        // 去重检查
        val entriesToInject = if (config.enableDeduplication) {
            deduplicateEntries(matchedEntries)
        } else {
            matchedEntries
        }
        
        if (entriesToInject.isEmpty()) {
            return messages
        }
        
        // 格式化世界书内容
        val worldBookMessage = formatWorldBookContent(
            entries = entriesToInject,
            style = config.formatStyle,
            maxTokens = config.maxTokens
        )
        
        // 根据注入位置插入消息
        return injectAtPosition(messages, worldBookMessage, entriesToInject)
    }
    
    /**
     * 去重：避免注入已经在对话历史中出现过的内容
     */
    private fun deduplicateEntries(
        entries: List<WorldBookMatcher.MatchedEntry>
    ): List<WorldBookMatcher.MatchedEntry> {
        // 简单去重：基于entry.id
        // 在实际应用中，可以检查消息历史中是否已包含相同内容
        return entries.distinctBy { it.entry.id }
    }
    
    /**
     * 格式化世界书内容为消息
     */
    private fun formatWorldBookContent(
        entries: List<WorldBookMatcher.MatchedEntry>,
        style: FormatStyle,
        maxTokens: Int
    ): UIMessage {
        val content = buildString {
            when (style) {
                FormatStyle.STRUCTURED -> formatStructured(entries)
                FormatStyle.MINIMAL -> formatMinimal(entries)
                FormatStyle.MARKDOWN -> formatMarkdown(entries)
            }
        }
        
        // 简单的token限制 (粗略估算: 1 token ≈ 4 characters)
        val estimatedTokens = content.length / 4
        val truncatedContent = if (estimatedTokens > maxTokens) {
            val maxChars = maxTokens * 4
            content.take(maxChars) + "\n\n[Content truncated due to length limit]"
        } else {
            content
        }
        
        return UIMessage(
            role = MessageRole.SYSTEM,
            parts = listOf(UIMessagePart.Text(truncatedContent))
        )
    }
    
    /**
     * 结构化格式
     */
    private fun StringBuilder.formatStructured(entries: List<WorldBookMatcher.MatchedEntry>) {
        appendLine("=== World Information ===")
        appendLine()
        appendLine("The following context is automatically provided based on the conversation:")
        appendLine()
        
        entries.forEachIndexed { index, match ->
            val entry = match.entry
            
            appendLine("--- Entry ${index + 1}: ${entry.title} ---")
            if (entry.priority > 0) {
                appendLine("[Priority: ${entry.priority}]")
            }
            if (match.matchDepth > 0) {
                appendLine("[Recursive Match Depth: ${match.matchDepth}]")
            }
            if (match.matchedKeywords.isNotEmpty()) {
                appendLine("[Matched Keywords: ${match.matchedKeywords.joinToString(", ")}]")
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
    private fun StringBuilder.formatMinimal(entries: List<WorldBookMatcher.MatchedEntry>) {
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
    private fun StringBuilder.formatMarkdown(entries: List<WorldBookMatcher.MatchedEntry>) {
        appendLine("## World Information")
        appendLine()
        appendLine("The following context is automatically provided:")
        appendLine()
        
        entries.forEach { match ->
            val entry = match.entry
            
            appendLine("### ${entry.title}")
            if (entry.priority > 0 || match.matchDepth > 0) {
                append("*")
                if (entry.priority > 0) append("Priority: ${entry.priority}")
                if (entry.priority > 0 && match.matchDepth > 0) append(", ")
                if (match.matchDepth > 0) append("Depth: ${match.matchDepth}")
                appendLine("*")
                appendLine()
            }
            appendLine(entry.content.trim())
            appendLine()
        }
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