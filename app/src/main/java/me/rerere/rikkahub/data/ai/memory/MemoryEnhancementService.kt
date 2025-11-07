package me.rerere.rikkahub.data.ai.memory

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.rerere.rikkahub.data.db.dao.MemoryCellDao
import me.rerere.rikkahub.data.db.dao.MemoryColumnDao
import me.rerere.rikkahub.data.db.dao.MemorySheetDao
import me.rerere.rikkahub.data.db.entity.*
import me.rerere.rikkahub.utils.JsonInstant
import java.util.*

/**
 * 记忆增强服务
 * 基于st-memory-enhancement插件架构实现的Android版本
 * 提供表格记忆的管理、注入和自动总结功能
 */
class MemoryEnhancementService(
    private val memorySheetDao: MemorySheetDao,
    private val memoryCellDao: MemoryCellDao,
    private val memoryColumnDao: MemoryColumnDao
) {

    /**
     * 获取指定助手的所有记忆表格
     */
    fun getSheetsByAssistant(assistantId: String): Flow<List<MemorySheet>> {
        return memorySheetDao.getSheetsByAssistant(assistantId)
    }

    /**
     * 获取指定助手的已启用记忆表格
     */
    suspend fun getEnabledSheetsByAssistant(assistantId: String): List<MemorySheet> {
        return memorySheetDao.getEnabledSheetsByAssistant(assistantId)
    }

    /**
     * 创建新的记忆表格
     */
    suspend fun createSheet(
        assistantId: String,
        name: String,
        description: String = "",
        sheetType: SheetType = SheetType.USER,
        columns: List<MemoryColumn>
    ): Result<String> {
        return try {
            val sheetId = UUID.randomUUID().toString()
            val sheet = MemorySheet(
                id = sheetId,
                assistantId = assistantId,
                name = name,
                description = description,
                sheetType = sheetType
            )

            // 插入表格
            memorySheetDao.insertSheet(sheet)

            // 插入列定义
            val columnsWithSheetId = columns.mapIndexed { index, column ->
                column.copy(
                    id = UUID.randomUUID().toString(),
                    sheetId = sheetId,
                    columnIndex = index
                )
            }
            memoryColumnDao.insertColumns(columnsWithSheetId)

            Result.success(sheetId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 创建新的记忆表格（带样式配置）
     */
    suspend fun createSheetWithStyle(
        assistantId: String,
        name: String,
        description: String = "",
        sheetType: SheetType = SheetType.USER,
        styleConfig: MemorySheetStyle,
        columns: List<MemoryColumn>
    ): Result<String> {
        return try {
            val sheetId = UUID.randomUUID().toString()
            val sheet = MemorySheet(
                id = sheetId,
                assistantId = assistantId,
                name = name,
                description = description,
                sheetType = sheetType,
                styleConfig = JsonInstant.encodeToString(styleConfig)
            )

            // 插入表格
            memorySheetDao.insertSheet(sheet)

            // 插入列定义
            val columnsWithSheetId = columns.mapIndexed { index, column ->
                column.copy(
                    id = UUID.randomUUID().toString(),
                    sheetId = sheetId,
                    columnIndex = index
                )
            }
            memoryColumnDao.insertColumns(columnsWithSheetId)

            Result.success(sheetId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取表格的完整数据（列定义 + 单元格数据）
     */
    suspend fun getSheetWithData(sheetId: String): MemorySheetWithData? {
        val sheet = memorySheetDao.getSheetById(sheetId) ?: return null
        val columns = memoryColumnDao.getColumnsBySheetSync(sheetId)
        val cells = memoryCellDao.getCellsBySheetSync(sheetId)

        return MemorySheetWithData(
            sheet = sheet,
            columns = columns,
            cells = cells
        )
    }

    /**
     * 获取表格的完整数据（Flow版本）
     */
    fun getSheetWithDataFlow(sheetId: String): Flow<MemorySheetWithData?> {
        return memorySheetDao.getSheetByIdFlow(sheetId).map { sheet ->
            if (sheet == null) return@map null

            val columns = memoryColumnDao.getColumnsBySheetSync(sheetId)
            val cells = memoryCellDao.getCellsBySheetSync(sheetId)

            MemorySheetWithData(
                sheet = sheet,
                columns = columns,
                cells = cells
            )
        }
    }

    /**
     * 添加新行到表格
     */
    suspend fun addRow(sheetId: String, rowData: Map<String, String>): Result<Int> {
        return try {
            val columns = memoryColumnDao.getColumnsBySheetSync(sheetId)
            val maxRowIndex = memoryCellDao.getMaxRowIndex(sheetId) ?: -1
            val newRowIndex = maxRowIndex + 1

            val cells = columns.map { column ->
                val content = rowData[column.name] ?: column.defaultValue ?: ""
                MemoryCell(
                    id = UUID.randomUUID().toString(),
                    sheetId = sheetId,
                    rowIndex = newRowIndex,
                    columnName = column.name,
                    content = content,
                    source = DataSource.MANUAL
                )
            }

            memoryCellDao.insertCells(cells)
            Result.success(newRowIndex)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 更新单元格数据
     */
    suspend fun updateCell(
        sheetId: String,
        rowIndex: Int,
        columnName: String,
        content: String,
        source: DataSource = DataSource.MANUAL
    ): Result<Unit> {
        return try {
            val existingCell = memoryCellDao.getCell(sheetId, rowIndex, columnName)

            if (existingCell != null) {
                // 更新现有单元格
                val updatedCell = existingCell.copy(
                    content = content,
                    source = source,
                    updatedAt = System.currentTimeMillis()
                )
                memoryCellDao.updateCell(updatedCell)
            } else {
                // 创建新单元格
                val newCell = MemoryCell(
                    id = UUID.randomUUID().toString(),
                    sheetId = sheetId,
                    rowIndex = rowIndex,
                    columnName = columnName,
                    content = content,
                    source = source
                )
                memoryCellDao.insertCell(newCell)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 删除行
     */
    suspend fun deleteRow(sheetId: String, rowIndex: Int): Result<Unit> {
        return try {
            memoryCellDao.deleteRow(sheetId, rowIndex)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 删除表格
     */
    suspend fun deleteSheet(sheetId: String): Result<Unit> {
        return try {
            // 删除所有单元格
            memoryCellDao.deleteAllCellsBySheet(sheetId)
            // 删除所有列定义
            memoryColumnDao.deleteAllColumnsBySheet(sheetId)
            // 删除表格
            memorySheetDao.deleteSheetById(sheetId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 切换表格启用状态
     */
    suspend fun toggleSheetEnabled(sheetId: String): Result<Boolean> {
        return try {
            val sheet = memorySheetDao.getSheetById(sheetId) ?: return Result.failure(
                IllegalArgumentException("Sheet not found")
            )
            val newStatus = !sheet.isEnabled
            memorySheetDao.updateSheetEnabledStatus(sheetId, newStatus)
            Result.success(newStatus)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 搜索表格内容
     */
    suspend fun searchInSheet(sheetId: String, query: String): List<MemoryCell> {
        return memoryCellDao.searchCells(sheetId, query)
    }

    /**
     * 获取表格统计信息
     */
    suspend fun getSheetStatistics(sheetId: String): SheetStatistics {
        val cellCount = memoryCellDao.getCellCount(sheetId)
        val rowCount = memoryCellDao.getRowCount(sheetId)
        val columnCount = memoryColumnDao.getColumnCount(sheetId)

        return SheetStatistics(
            cellCount = cellCount,
            rowCount = rowCount,
            columnCount = columnCount
        )
    }

    /**
     * 自动总结聊天记录并填写到表格
     * 基于st-memory-enhancement插件的自动总结功能
     */
    suspend fun autoSummarizeAndFill(
        sheetId: String,
        chatMessages: List<String>,
        summaryPrompt: String
    ): Result<List<MemoryCell>> {
        return try {
            // TODO: 这里需要集成AI服务来生成总结
            // 暂时返回空结果，待后续实现AI集成
            val summaryCells = emptyList<MemoryCell>()
            Result.success(summaryCells)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 生成表格的提示词注入内容
     * 用于在AI请求中注入记忆表格数据
     */
    suspend fun generateMemoryInjection(
        assistantId: String,
        maxTokens: Int = 2000
    ): String {
        val enabledSheets = getEnabledSheetsByAssistant(assistantId)
        if (enabledSheets.isEmpty()) return ""

        val injectionBuilder = StringBuilder()
        injectionBuilder.appendLine("--- 记忆表格数据 ---")

        for (sheet in enabledSheets) {
            val sheetWithData = getSheetWithData(sheet.id) ?: continue
            val styleConfig = try {
                JsonInstant.decodeFromString<MemorySheetStyle>(sheet.styleConfig)
            } catch (e: Exception) {
                MemorySheetStyle() // 默认样式
            }

            injectionBuilder.appendLine("## ${sheet.name}")
            if (sheet.description.isNotBlank()) {
                injectionBuilder.appendLine("${sheet.description}")
            }

            // 格式化表格数据
            val formattedTable = formatTableForInjection(sheetWithData, styleConfig)
            injectionBuilder.appendLine(formattedTable)
            injectionBuilder.appendLine()
        }

        injectionBuilder.appendLine("--- 记忆表格数据结束 ---")

        // 检查token限制
        val injection = injectionBuilder.toString()
        if (injection.length > maxTokens * 4) { // 粗略估算token数
            // TODO: 实现智能截断逻辑
            return injection.take(maxTokens * 4)
        }

        return injection
    }

    /**
     * 格式化表格数据用于注入
     */
    private suspend fun formatTableForInjection(
        sheetWithData: MemorySheetWithData,
        styleConfig: MemorySheetStyle
    ): String {
        val tableBuilder = StringBuilder()

        if (sheetWithData.cells.isEmpty()) {
            return tableBuilder.appendLine("(表格为空)").toString()
        }

        // 按行组织数据
        val rowsMap = sheetWithData.cells.groupBy { it.rowIndex }

        if (styleConfig.formatType == MemoryFormatType.TABLE) {
            // 表格格式
            // 表头
            val headers = sheetWithData.columns.map { it.name }
            tableBuilder.appendLine("| ${headers.joinToString(" | ")} |")
            tableBuilder.appendLine("|${headers.map { "---" }.joinToString("|")}|")

            // 数据行
            rowsMap.toSortedMap().forEach { (rowIndex, cells) ->
                val rowData = headers.map { header ->
                    cells.find { it.columnName == header }?.content ?: ""
                }
                tableBuilder.appendLine("| ${rowData.joinToString(" | ")} |")
            }
        } else {
            // 列表格式
            rowsMap.toSortedMap().forEach { (rowIndex, cells) ->
                tableBuilder.appendLine("行 ${rowIndex + 1}:")
                cells.forEach { cell ->
                    tableBuilder.appendLine("  ${cell.columnName}: ${cell.content}")
                }
                tableBuilder.appendLine()
            }
        }

        return tableBuilder.toString()
    }
}

/**
 * 记忆表格样式配置
 */
data class MemorySheetStyle(
    val formatType: MemoryFormatType = MemoryFormatType.TABLE,
    val showHeaders: Boolean = true,
    val maxRows: Int = 50,
    val truncateContent: Boolean = true,
    val customTemplate: String = ""
)

/**
 * 记忆格式类型
 */
enum class MemoryFormatType {
    TABLE,    // 表格格式
    LIST,     // 列表格式
    JSON,     // JSON格式
    CUSTOM    // 自定义模板
}

/**
 * 带数据的记忆表格
 */
data class MemorySheetWithData(
    val sheet: MemorySheet,
    val columns: List<MemoryColumn>,
    val cells: List<MemoryCell>
)

/**
 * 表格统计信息
 */
data class SheetStatistics(
    val cellCount: Int,
    val rowCount: Int,
    val columnCount: Int
)