package me.rerere.rikkahub.data.ai.memory

import kotlinx.coroutines.flow.first
// import me.rerere.rikkahub.data.ai.prompts.PromptManager // TODO: PromptManager暂未实现
import me.rerere.rikkahub.data.db.dao.MemorySheetDao
import me.rerere.rikkahub.data.db.entity.SheetType
import me.rerere.rikkahub.utils.JsonInstant
import java.util.*
import kotlin.Lazy

/**
 * 记忆注入器
 * 负责将记忆表格数据注入到AI请求的上下文中
 * 基于st-memory-enhancement插件的注入机制
 */
class MemoryInjector(
    private val memoryEnhancementService: Lazy<MemoryEnhancementService>,
    private val memorySheetDao: MemorySheetDao
    // TODO: private val promptManager: PromptManager - 暂时移除依赖
) {

    /**
     * 注入记忆到AI请求中
     * @param assistantId 助手ID
     * @param originalPrompt 原始提示词
     * @param maxMemoryTokens 最大记忆token数
     * @return 增强后的提示词
     */
    suspend fun injectMemory(
        assistantId: String,
        originalPrompt: String,
        maxMemoryTokens: Int = 2000
    ): MemoryInjectionResult {
        return try {
            // 获取启用的记忆表格
            val enabledSheets = memoryEnhancementService.value.getEnabledSheetsByAssistant(assistantId)

            if (enabledSheets.isEmpty()) {
                return MemoryInjectionResult(
                    enhancedPrompt = originalPrompt,
                    injectedSheets = emptyList(),
                    usedTokens = 0,
                    status = InjectionStatus.NO_MEMORY_AVAILABLE
                )
            }

            // 按优先级排序
            val sortedSheets = enabledSheets.sortedBy { it.order }

            // 生成记忆注入内容
            val memoryContent = buildMemoryContent(sortedSheets, maxMemoryTokens)

            if (memoryContent.content.isBlank()) {
                return MemoryInjectionResult(
                    enhancedPrompt = originalPrompt,
                    injectedSheets = emptyList(),
                    usedTokens = 0,
                    status = InjectionStatus.EMPTY_MEMORY
                )
            }

            // 构建增强后的提示词
            val enhancedPrompt = buildEnhancedPrompt(originalPrompt, memoryContent.content)

            MemoryInjectionResult(
                enhancedPrompt = enhancedPrompt,
                injectedSheets = memoryContent.injectedSheets,
                usedTokens = memoryContent.usedTokens,
                status = InjectionStatus.SUCCESS
            )
        } catch (e: Exception) {
            MemoryInjectionResult(
                enhancedPrompt = originalPrompt,
                injectedSheets = emptyList(),
                usedTokens = 0,
                status = InjectionStatus.ERROR,
                error = e.message
            )
        }
    }

    /**
     * 为聊天会话注入记忆
     * @param assistantId 助手ID
     * @param conversationHistory 聊天历史
     * @param maxMemoryTokens 最大记忆token数
     * @return 增强后的聊天上下文
     */
    suspend fun injectMemoryForChat(
        assistantId: String,
        conversationHistory: List<String>,
        maxMemoryTokens: Int = 2000
    ): ChatMemoryInjectionResult {
        return try {
            // 获取关键记忆表格（优先BASE和DERIVED类型）
            val keySheets = memoryEnhancementService.value.getEnabledSheetsByAssistant(assistantId)
                .filter { it.sheetType == SheetType.BASE || it.sheetType == SheetType.DERIVED }
                .sortedBy { it.order }

            if (keySheets.isEmpty()) {
                return ChatMemoryInjectionResult(
                    enhancedHistory = conversationHistory,
                    injectedSheets = emptyList(),
                    usedTokens = 0,
                    status = InjectionStatus.NO_MEMORY_AVAILABLE
                )
            }

            // 生成上下文相关的记忆内容
            val memoryContent = buildContextualMemory(keySheets, conversationHistory, maxMemoryTokens)

            // 将记忆内容插入到聊天历史中
            val enhancedHistory = insertMemoryIntoHistory(conversationHistory, memoryContent.content)

            ChatMemoryInjectionResult(
                enhancedHistory = enhancedHistory,
                injectedSheets = memoryContent.injectedSheets,
                usedTokens = memoryContent.usedTokens,
                status = InjectionStatus.SUCCESS
            )
        } catch (e: Exception) {
            ChatMemoryInjectionResult(
                enhancedHistory = conversationHistory,
                injectedSheets = emptyList(),
                usedTokens = 0,
                status = InjectionStatus.ERROR,
                error = e.message
            )
        }
    }

    /**
     * 构建记忆内容
     */
    private suspend fun buildMemoryContent(
        sheets: List<me.rerere.rikkahub.data.db.entity.MemorySheet>,
        maxTokens: Int
    ): MemoryContent {
        val contentBuilder = StringBuilder()
        val injectedSheets = mutableListOf<InjectedSheetInfo>()
        var usedTokens = 0

        contentBuilder.appendLine("--- 记忆表格数据 ---")
        contentBuilder.appendLine()

        for (sheet in sheets) {
            if (usedTokens >= maxTokens) break

            val sheetWithData = memoryEnhancementService.value.getSheetWithData(sheet.id) ?: continue
            val sheetContent = formatSheetForInjection(sheetWithData)
            val estimatedTokens = estimateTokenCount(sheetContent)

            if (usedTokens + estimatedTokens > maxTokens) {
                // 尝试截断当前表格内容
                val remainingTokens = maxTokens - usedTokens
                if (remainingTokens > 100) { // 至少留100个token给表格结构
                    val truncatedContent = truncateContent(sheetContent, remainingTokens)
                    contentBuilder.appendLine(truncatedContent)
                    contentBuilder.appendLine("(内容已截断)")
                    injectedSheets.add(
                        InjectedSheetInfo(
                            sheetId = sheet.id,
                            sheetName = sheet.name,
                            isTruncated = true,
                            estimatedTokens = remainingTokens
                        )
                    )
                    usedTokens += remainingTokens
                }
                break
            }

            contentBuilder.appendLine("## ${sheet.name}")
            if (sheet.description.isNotBlank()) {
                contentBuilder.appendLine(sheet.description)
                contentBuilder.appendLine()
            }
            contentBuilder.appendLine(sheetContent)
            contentBuilder.appendLine()

            injectedSheets.add(
                InjectedSheetInfo(
                    sheetId = sheet.id,
                    sheetName = sheet.name,
                    isTruncated = false,
                    estimatedTokens = estimatedTokens
                )
            )
            usedTokens += estimatedTokens
        }

        contentBuilder.appendLine("--- 记忆表格数据结束 ---")

        return MemoryContent(
            content = contentBuilder.toString(),
            injectedSheets = injectedSheets,
            usedTokens = usedTokens
        )
    }

    /**
     * 构建上下文相关的记忆内容
     */
    private suspend fun buildContextualMemory(
        sheets: List<me.rerere.rikkahub.data.db.entity.MemorySheet>,
        conversationHistory: List<String>,
        maxTokens: Int
    ): MemoryContent {
        // TODO: 实现基于聊天历史的智能记忆筛选
        // 暂时使用简单的构建方式
        return buildMemoryContent(sheets, maxTokens)
    }

    /**
     * 格式化表格数据用于注入
     */
    private suspend fun formatSheetForInjection(
        sheetWithData: me.rerere.rikkahub.data.ai.memory.MemorySheetWithData
    ): String {
        if (sheetWithData.cells.isEmpty()) {
            return "(表格为空)"
        }

        val styleConfig = try {
            JsonInstant.decodeFromString<MemorySheetStyle>(sheetWithData.sheet.styleConfig)
        } catch (e: Exception) {
            MemorySheetStyle()
        }

        val rowsMap = sheetWithData.cells.groupBy { it.rowIndex }

        return when (styleConfig.formatType) {
            MemoryFormatType.TABLE -> formatAsTable(sheetWithData, rowsMap, styleConfig)
            MemoryFormatType.LIST -> formatAsList(sheetWithData, rowsMap, styleConfig)
            MemoryFormatType.JSON -> formatAsJson(sheetWithData, rowsMap, styleConfig)
            MemoryFormatType.CUSTOM -> formatAsCustom(sheetWithData, rowsMap, styleConfig)
        }
    }

    /**
     * 格式化为表格
     */
    private fun formatAsTable(
        sheetWithData: me.rerere.rikkahub.data.ai.memory.MemorySheetWithData,
        rowsMap: Map<Int, List<me.rerere.rikkahub.data.db.entity.MemoryCell>>,
        styleConfig: MemorySheetStyle
    ): String {
        val tableBuilder = StringBuilder()
        val headers = sheetWithData.columns.map { it.name }

        // 限制行数
        val sortedRows = rowsMap.toSortedMap().toList().take(styleConfig.maxRows).toMap()

        // 表头
        if (styleConfig.showHeaders) {
            tableBuilder.appendLine("| ${headers.joinToString(" | ")} |")
            tableBuilder.appendLine("|${headers.map { "---" }.joinToString("|")}|")
        }

        // 数据行
        sortedRows.forEach { (rowIndex, cells) ->
            val rowData: List<String> = headers.map { header ->
                var content = cells.find { it.columnName == header }?.content ?: ""
                if (styleConfig.truncateContent && content.length > 100) {
                    content = content.take(97) + "..."
                }
                content
            }
            tableBuilder.appendLine("| ${rowData.joinToString(" | ")} |")
        }

        if (rowsMap.size > styleConfig.maxRows) {
            tableBuilder.appendLine("... (还有 ${rowsMap.size - styleConfig.maxRows} 行未显示)")
        }

        return tableBuilder.toString()
    }

    /**
     * 格式化为列表
     */
    private fun formatAsList(
        sheetWithData: me.rerere.rikkahub.data.ai.memory.MemorySheetWithData,
        rowsMap: Map<Int, List<me.rerere.rikkahub.data.db.entity.MemoryCell>>,
        styleConfig: MemorySheetStyle
    ): String {
        val listBuilder = StringBuilder()
        val sortedRows = rowsMap.toSortedMap().toList().take(styleConfig.maxRows).toMap()

        sortedRows.forEach { (rowIndex, cells) ->
            listBuilder.appendLine("行 ${rowIndex + 1}:")
            cells.forEach { cell ->
                var content = cell.content
                if (styleConfig.truncateContent && content.length > 200) {
                    content = content.take(197) + "..."
                }
                listBuilder.appendLine("  ${cell.columnName}: ${content}")
            }
            listBuilder.appendLine()
        }

        if (rowsMap.size > styleConfig.maxRows) {
            listBuilder.appendLine("... (还有 ${rowsMap.size - styleConfig.maxRows} 行未显示)")
        }

        return listBuilder.toString()
    }

    /**
     * 格式化为JSON
     */
    private fun formatAsJson(
        sheetWithData: me.rerere.rikkahub.data.ai.memory.MemorySheetWithData,
        rowsMap: Map<Int, List<me.rerere.rikkahub.data.db.entity.MemoryCell>>,
        styleConfig: MemorySheetStyle
    ): String {
        val sortedRows = rowsMap.toSortedMap().toList().take(styleConfig.maxRows).toMap()
        val rowsData = sortedRows.map { (rowIndex, cells) ->
            val rowData = mutableMapOf<String, String>()
            cells.forEach { cell ->
                var content = cell.content
                if (styleConfig.truncateContent && content.length > 500) {
                    content = content.take(497) + "..."
                }
                rowData[cell.columnName] = content
            }
            rowData
        }

        val jsonData = mapOf(
                "sheet" to sheetWithData.sheet.name,
                "description" to sheetWithData.sheet.description,
                "rows" to rowsData,
                "totalRows" to rowsMap.size,
                "isTruncated" to (rowsMap.size > styleConfig.maxRows)
            )
        return JsonInstant.encodeToString(jsonData)
    }

    /**
     * 格式化为自定义模板
     */
    private fun formatAsCustom(
        sheetWithData: me.rerere.rikkahub.data.ai.memory.MemorySheetWithData,
        rowsMap: Map<Int, List<me.rerere.rikkahub.data.db.entity.MemoryCell>>,
        styleConfig: MemorySheetStyle
    ): String {
        if (styleConfig.customTemplate.isBlank()) {
            return formatAsTable(sheetWithData, rowsMap, styleConfig)
        }

        // TODO: 实现自定义模板渲染
        // 暂时返回表格格式
        return formatAsTable(sheetWithData, rowsMap, styleConfig)
    }

    /**
     * 构建增强后的提示词
     */
    private fun buildEnhancedPrompt(originalPrompt: String, memoryContent: String): String {
        return """
            $memoryContent

            基于以上记忆表格数据，请回答以下问题：

            $originalPrompt
        """.trimIndent()
    }

    /**
     * 将记忆内容插入到聊天历史中
     */
    private fun insertMemoryIntoHistory(
        history: List<String>,
        memoryContent: String
    ): List<String> {
        if (history.isEmpty()) return listOf(memoryContent)

        val result = mutableListOf<String>()

        // 在对话开头插入记忆内容
        result.add(memoryContent)
        result.addAll(history)

        return result
    }

    /**
     * 估算token数量（粗略估算：1 token ≈ 4 字符）
     */
    private fun estimateTokenCount(text: String): Int {
        return (text.length + 3) / 4
    }

    /**
     * 截断内容到指定token数
     */
    private fun truncateContent(content: String, maxTokens: Int): String {
        val maxChars = maxTokens * 4
        return if (content.length <= maxChars) {
            content
        } else {
            content.take(maxChars - 20) + "...(已截断)"
        }
    }

    /**
     * 检查是否需要注入记忆
     */
    suspend fun shouldInjectMemory(assistantId: String): Boolean {
        val enabledSheets = memoryEnhancementService.value.getEnabledSheetsByAssistant(assistantId)
        return enabledSheets.isNotEmpty()
    }

    /**
     * 获取记忆注入统计信息
     */
    suspend fun getMemoryStatistics(assistantId: String): MemoryStatistics {
        val sheets = memoryEnhancementService.value.getEnabledSheetsByAssistant(assistantId)
        val totalSheets = sheets.size
        var totalCells = 0
        var totalRows = 0

        for (sheet in sheets) {
            val stats = memoryEnhancementService.value.getSheetStatistics(sheet.id)
            totalCells += stats.cellCount
            totalRows += stats.rowCount
        }

        return MemoryStatistics(
            totalSheets = totalSheets,
            totalCells = totalCells,
            totalRows = totalRows,
            estimatedTokens = estimateTokenCount(
                memoryEnhancementService.value.generateMemoryInjection(assistantId)
            )
        )
    }
}

/**
 * 记忆注入结果
 */
data class MemoryInjectionResult(
    val enhancedPrompt: String,
    val injectedSheets: List<InjectedSheetInfo>,
    val usedTokens: Int,
    val status: InjectionStatus,
    val error: String? = null
)

/**
 * 聊天记忆注入结果
 */
data class ChatMemoryInjectionResult(
    val enhancedHistory: List<String>,
    val injectedSheets: List<InjectedSheetInfo>,
    val usedTokens: Int,
    val status: InjectionStatus,
    val error: String? = null
)

/**
 * 注入的表格信息
 */
data class InjectedSheetInfo(
    val sheetId: String,
    val sheetName: String,
    val isTruncated: Boolean,
    val estimatedTokens: Int
)

/**
 * 记忆内容
 */
private data class MemoryContent(
    val content: String,
    val injectedSheets: List<InjectedSheetInfo>,
    val usedTokens: Int
)

/**
 * 注入状态
 */
enum class InjectionStatus {
    SUCCESS,              // 成功
    NO_MEMORY_AVAILABLE,  // 没有可用的记忆
    EMPTY_MEMORY,         // 记忆为空
    ERROR                 // 发生错误
}

/**
 * 记忆统计信息
 */
data class MemoryStatistics(
    val totalSheets: Int,
    val totalCells: Int,
    val totalRows: Int,
    val estimatedTokens: Int
)