package me.rerere.rikkahub.service

import kotlinx.coroutines.runBlocking
import me.rerere.ai.core.MessageRole
import me.rerere.ai.ui.UIMessage
import me.rerere.ai.ui.UIMessagePart
import me.rerere.rikkahub.data.db.entity.MemoryTable
import me.rerere.rikkahub.data.db.entity.MemoryTableRow
import me.rerere.rikkahub.data.db.entity.ColumnType
import me.rerere.rikkahub.data.repository.MemoryTableRepository

/**
 * 记忆表格上下文注入器
 * 负责将匹配的记忆表格条目注入到对话消息列表中，提供结构化的表格数据支持
 */
class MemoryTableInjector {

    /**
     * 注入配置
     * @property maxRows 最大行数限制
     * @property maxColumns 最大列数限制
     * @property formatStyle 格式化风格
     * @property includeHeaders 是否包含表头
     * @property includeMetadata 是否包含元数据（表格描述、行数等）
     * @property enableDataFiltering 是否启用数据过滤（空值处理）
     * @property smartColumnSelection 是否智能选择重要列
     */
    data class InjectionConfig(
        val maxRows: Int = 50,
        val maxColumns: Int = 10,
        val formatStyle: FormatStyle = FormatStyle.TABLE,
        val includeHeaders: Boolean = true,
        val includeMetadata: Boolean = true,
        val enableDataFiltering: Boolean = true,
        val smartColumnSelection: Boolean = false
    )

    /**
     * 格式化风格
     */
    enum class FormatStyle {
        /**
         * 表格格式，使用对齐的列展示
         * 示例:
         * | Name    | Age | Occupation |
         * |---------|-----|------------|
         * | Alice   | 28  | Engineer   |
         */
        TABLE,

        /**
         * 列表格式，每行数据作为列表项
         * 示例:
         * - Name: Alice, Age: 28, Occupation: Engineer
         */
        LIST,

        /**
         * Markdown格式
         * 示例:
         * ## Table: Employee Information
         * | Name | Age | Occupation |
         * |------|-----|------------|
         */
        MARKDOWN,

        /**
         * JSON格式，适用于AI程序化处理
         * 示例:
         * {
         *   "table_name": "Employee Information",
         *   "columns": ["Name", "Age", "Occupation"],
         *   "data": [...]
         * }
         */
        JSON,

        /**
         * 自然语言格式
         * 示例:
         * According to the employee information table:
         * Alice is 28 years old and works as an Engineer.
         */
        NATURAL_LANGUAGE,

        /**
         * 键值对格式
         * 示例:
         * Row 1:
         *   Name: Alice
         *   Age: 28
         *   Occupation: Engineer
         */
        KEY_VALUE
    }

    /**
     * 将匹配的记忆表格注入到对话消息列表中
     *
     * @param messages 原始消息列表
     * @param tables 匹配的记忆表格列表（包含行数据）
     * @param config 注入配置
     * @return 注入后的消息列表
     */
    fun injectMemoryTables(
        messages: List<UIMessage>,
        tables: List<TableWithContext>,
        config: InjectionConfig = InjectionConfig()
    ): List<UIMessage> {
        if (tables.isEmpty()) {
            return messages
        }

        // 处理表格数据
        val processedTables = processTables(tables, config)

        if (processedTables.isEmpty()) {
            return messages
        }

        // 格式化表格内容
        val tableMessage = formatTableContent(processedTables, config)

        // 插入到消息列表中
        return injectAtPosition(messages, tableMessage)
    }

    /**
     * 表格上下文数据类
     */
    data class TableWithContext(
        val table: MemoryTable,
        val rows: List<MemoryTableRow>,
        val relevanceScore: Double = 0.0,
        val matchedColumns: List<String> = emptyList()
    )

    /**
     * 处理表格数据：过滤、排序、限制
     */
    private fun processTables(
        tables: List<TableWithContext>,
        config: InjectionConfig
    ): List<TableWithContext> {
        return tables.map { tableWithContext ->
            val processedRows = processRows(tableWithContext.rows, tableWithContext.table, config)
            val processedColumns = processColumns(tableWithContext.table, config)

            tableWithContext.copy(
                rows = processedRows,
                table = tableWithContext.table.copy(columns = processedColumns)
            )
        }.filter { it.rows.isNotEmpty() && it.table.columns.isNotEmpty() }
    }

    /**
     * 处理行数据：限制数量、过滤空值
     */
    private fun processRows(
        rows: List<MemoryTableRow>,
        table: MemoryTable,
        config: InjectionConfig
    ): List<MemoryTableRow> {
        var processedRows = rows

        // 限制行数
        if (processedRows.size > config.maxRows) {
            processedRows = processedRows.take(config.maxRows)
        }

        // 数据过滤
        if (config.enableDataFiltering) {
            processedRows = processedRows.map { row ->
                val filteredRowData = row.rowData.filter { (key, value) ->
                    value.isNotBlank() && table.columns.any { it.name == key }
                }
                row.copy(rowData = filteredRowData)
            }.filter { it.rowData.isNotEmpty() }
        }

        return processedRows
    }

    /**
     * 处理列数据：限制数量、智能选择
     */
    private fun processColumns(
        table: MemoryTable,
        config: InjectionConfig
    ): List<me.rerere.rikkahub.data.db.entity.MemoryColumn> {
        var columns = table.columns

        // 限制列数
        if (columns.size > config.maxColumns) {
            columns = if (config.smartColumnSelection) {
                // 智能选择：优先选择有数据的列
                columns.sortedByDescending { column ->
                    // 这里需要传入实际的行数据，暂时使用固定值
                    1
                }.take(config.maxColumns)
            } else {
                columns.take(config.maxColumns)
            }
        }

        return columns
    }

    /**
     * 格式化表格内容为消息
     */
    private fun formatTableContent(
        tables: List<TableWithContext>,
        config: InjectionConfig
    ): UIMessage {
        val content = buildString {
            when (config.formatStyle) {
                FormatStyle.TABLE -> formatTable(tables, config)
                FormatStyle.LIST -> formatList(tables, config)
                FormatStyle.MARKDOWN -> formatMarkdown(tables, config)
                FormatStyle.JSON -> formatJson(tables, config)
                FormatStyle.NATURAL_LANGUAGE -> formatNaturalLanguage(tables, config)
                FormatStyle.KEY_VALUE -> formatKeyValue(tables, config)
            }
        }

        return UIMessage(
            role = MessageRole.SYSTEM,
            parts = listOf(UIMessagePart.Text(content))
        )
    }

    /**
     * 表格格式化
     */
    private fun StringBuilder.formatTable(
        tables: List<TableWithContext>,
        config: InjectionConfig
    ) {
        appendLine("=== Memory Tables ===")
        appendLine()

        tables.forEachIndexed { tableIndex, tableWithContext ->
            val table = tableWithContext.table
            val rows = tableWithContext.rows

            if (config.includeMetadata) {
                appendLine("Table ${tableIndex + 1}: ${table.name}")
                if (table.description.isNotEmpty()) {
                    appendLine("Description: ${table.description}")
                }
                appendLine("Rows: ${rows.size}, Columns: ${table.columns.size}")
                appendLine()
            }

            if (rows.isEmpty()) {
                appendLine("(No data available)")
                appendLine()
                return@forEachIndexed
            }

            val columns = table.columns.map { it.name }

            // 表头
            if (config.includeHeaders && columns.isNotEmpty()) {
                val headerLine = columns.joinToString(" | ")
                appendLine(headerLine)
                val separatorLine = columns.joinToString(" | ") { "-".repeat(it.length.coerceAtLeast(3)) }
                appendLine(separatorLine)
            }

            // 数据行
            rows.forEach { row ->
                val values = columns.map { column ->
                    row.rowData[column] ?: ""
                }
                appendLine(values.joinToString(" | "))
            }

            appendLine()
        }

        appendLine("=== End of Memory Tables ===")
    }

    /**
     * 列表格式化
     */
    private fun StringBuilder.formatList(
        tables: List<TableWithContext>,
        config: InjectionConfig
    ) {
        appendLine("Memory Table Data:")
        appendLine()

        tables.forEachIndexed { tableIndex, tableWithContext ->
            val table = tableWithContext.table
            val rows = tableWithContext.rows

            if (config.includeMetadata) {
                appendLine("${table.name}:")
                if (table.description.isNotEmpty()) {
                    appendLine("(${table.description})")
                }
                appendLine()
            }

            rows.forEachIndexed { rowIndex, row ->
                val values = table.columns.map { column ->
                    "${column.name}: ${row.rowData[column.name] ?: ""}"
                }
                appendLine("- ${values.joinToString(", ")}")
            }

            appendLine()
        }
    }

    /**
     * Markdown格式化
     */
    private fun StringBuilder.formatMarkdown(
        tables: List<TableWithContext>,
        config: InjectionConfig
    ) {
        appendLine("## Memory Tables")
        appendLine()

        tables.forEachIndexed { tableIndex, tableWithContext ->
            val table = tableWithContext.table
            val rows = tableWithContext.rows

            appendLine("### ${table.name}")

            if (config.includeMetadata && table.description.isNotEmpty()) {
                appendLine("*${table.description}*")
                appendLine()
            }

            if (rows.isEmpty()) {
                appendLine("*No data available*")
                appendLine()
                return@forEachIndexed
            }

            val columns = table.columns.map { it.name }

            if (columns.isNotEmpty()) {
                // Markdown表格语法
                if (config.includeHeaders) {
                    appendLine("| ${columns.joinToString(" | ")} |")
                    appendLine("| ${columns.joinToString(" | ") { "---" }} |")
                }

                rows.forEach { row ->
                    val values = columns.map { column ->
                        (row.rowData[column] ?: "").replace("|", "\\|")
                    }
                    appendLine("| ${values.joinToString(" | ")} |")
                }
            }

            appendLine()
        }
    }

    /**
     * JSON格式化
     */
    private fun StringBuilder.formatJson(
        tables: List<TableWithContext>,
        config: InjectionConfig
    ) {
        appendLine("{")
        appendLine("  \"memory_tables\": [")

        tables.forEachIndexed { tableIndex, tableWithContext ->
            val table = tableWithContext.table
            val rows = tableWithContext.rows

            appendLine("    {")
            appendLine("      \"name\": \"${table.name.replace("\"", "\\\"")}\",")

            if (config.includeMetadata) {
                appendLine("      \"description\": \"${table.description.replace("\"", "\\\"")}\",")
                appendLine("      \"row_count\": ${rows.size},")
                appendLine("      \"relevance_score\": ${tableWithContext.relevanceScore},")
            }

            appendLine("      \"columns\": [${table.columns.joinToString(", ") { "\"${it.name.replace("\"", "\\\"")}\"" }}],")
            appendLine("      \"data\": [")

            rows.forEachIndexed { rowIndex, row ->
                appendLine("        {")
                val values = table.columns.map<me.rerere.rikkahub.data.db.entity.MemoryColumn, String> { column ->
                    "\"${column.name}\": \"${(row.rowData[column.name] ?: "").replace("\"", "\\\"")}\""
                }
                appendLine("          ${values.joinToString(",\n          ")}")
                appendLine("        }${if (rowIndex < rows.size - 1) "," else ""}")
            }

            appendLine("      ]")
            appendLine("    }${if (tableIndex < tables.size - 1) "," else ""}")
        }

        appendLine("  ]")
        appendLine("}")
    }

    /**
     * 自然语言格式化
     */
    private fun StringBuilder.formatNaturalLanguage(
        tables: List<TableWithContext>,
        config: InjectionConfig
    ) {
        appendLine("Based on the available memory table data:")
        appendLine()

        tables.forEach { tableWithContext ->
            val table = tableWithContext.table
            val rows = tableWithContext.rows

            if (rows.isEmpty()) return@forEach

            appendLine("From the \"${table.name}\" table")
            if (table.description.isNotEmpty()) {
                appendLine(" (${table.description}):")
            } else {
                appendLine(":")
            }

            rows.take(3).forEach { row ->
                val values = table.columns.mapNotNull { column ->
                    val value = row.rowData[column.name]
                    if (!value.isNullOrBlank()) "${column.name}: $value" else null
                }
                if (values.isNotEmpty()) {
                    appendLine("- ${values.joinToString(", ")}")
                }
            }

            if (rows.size > 3) {
                appendLine("- ... and ${rows.size - 3} more rows")
            }

            appendLine()
        }
    }

    /**
     * 键值对格式化
     */
    private fun StringBuilder.formatKeyValue(
        tables: List<TableWithContext>,
        config: InjectionConfig
    ) {
        appendLine("Memory Table Information:")
        appendLine()

        tables.forEachIndexed { tableIndex, tableWithContext ->
            val table = tableWithContext.table
            val rows = tableWithContext.rows

            if (config.includeMetadata) {
                appendLine("=== ${table.name} ===")
                if (table.description.isNotEmpty()) {
                    appendLine("Description: ${table.description}")
                }
                appendLine()
            }

            rows.forEachIndexed { rowIndex, row ->
                appendLine("Row ${rowIndex + 1}:")
                table.columns.forEach { column ->
                    val value = row.rowData[column.name]
                    if (!value.isNullOrBlank()) {
                        appendLine("  ${column.name}: $value")
                    }
                }
                appendLine()
            }
        }
    }

    /**
     * 在开头注入表格数据 (系统提示词之后)
     */
    private fun injectAtPosition(
        messages: List<UIMessage>,
        tableMessage: UIMessage
    ): List<UIMessage> {
        if (messages.isEmpty()) {
            return listOf(tableMessage)
        }

        // 找到第一条非系统消息的位置
        val firstNonSystemIndex = messages.indexOfFirst { it.role != MessageRole.SYSTEM }

        return if (firstNonSystemIndex == -1) {
            // 全是系统消息或为空，追加到末尾
            messages + tableMessage
        } else if (firstNonSystemIndex == 0) {
            // 没有系统消息，插入到开头
            listOf(tableMessage) + messages
        } else {
            // 插入到系统消息之后
            messages.subList(0, firstNonSystemIndex) + tableMessage + messages.subList(firstNonSystemIndex, messages.size)
        }
    }

    /**
     * 估算表格消息的token数量 (粗略估算)
     */
    fun estimateTableTokens(tableMessage: UIMessage): Int {
        val text = tableMessage.toText()
        return text.length / 4 // 简单估算: 1 token ≈ 4 characters
    }

    /**
     * 搜索相关的表格数据
     * 基于关键词在表格中搜索相关内容
     */
    suspend fun searchRelevantTables(
        repository: MemoryTableRepository,
        assistantId: String,
        keywords: List<String>,
        maxTables: Int = 3
    ): List<TableWithContext> {
        if (keywords.isEmpty()) return emptyList()

        val allTables = runBlocking {
            val tables = mutableListOf<MemoryTable>()
            repository.getMemoryTablesByAssistant(assistantId).collect { tables.addAll(it) }
            tables
        }
        val relevantTables = mutableListOf<TableWithContext>()

        for (table in allTables.filter { it.isEnabled }) {
            // 在表格名称和描述中搜索
            val nameRelevance = calculateKeywordRelevance(listOf(table.name, table.description), keywords)

            // 在表格数据中搜索
            val rows = mutableListOf<MemoryTableRow>()
            repository.getRowsByTableId(table.id).collect { rows.addAll(it) }

            val dataRelevance = if (rows.isNotEmpty()) {
                val allText = rows.joinToString(" ") { row -> row.rowData.values.joinToString(" ") }
                calculateKeywordRelevance(listOf(allText), keywords)
            } else {
                0.0
            }

            val totalRelevance = (nameRelevance + dataRelevance) / 2.0

            if (totalRelevance > 0.1) { // 相关性阈值
                val matchedColumns = table.columns.filter { column ->
                    keywords.any { keyword ->
                        column.name.contains(keyword, ignoreCase = true)
                    }
                }.map { it.name }

                relevantTables.add(
                    TableWithContext(
                        table = table,
                        rows = rows,
                        relevanceScore = totalRelevance,
                        matchedColumns = matchedColumns
                    )
                )
            }
        }

        return relevantTables
            .sortedByDescending { it.relevanceScore }
            .take(maxTables)
    }

    /**
     * 计算关键词相关性分数
     */
    private fun calculateKeywordRelevance(texts: List<String>, keywords: List<String>): Double {
        if (keywords.isEmpty() || texts.isEmpty()) return 0.0

        val combinedText = texts.joinToString(" ").lowercase()
        var score = 0.0

        keywords.forEach { keyword ->
            val lowerKeyword = keyword.lowercase()
            if (combinedText.contains(lowerKeyword)) {
                score += 1.0
            }
        }

        return score.coerceAtMost(keywords.size.toDouble()) / keywords.size.toDouble()
    }
}