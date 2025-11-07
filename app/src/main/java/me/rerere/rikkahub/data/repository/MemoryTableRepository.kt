package me.rerere.rikkahub.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.rerere.rikkahub.data.db.dao.MemoryTableDAO
import me.rerere.rikkahub.data.db.entity.MemoryTable
import me.rerere.rikkahub.data.db.entity.MemoryTableRow
import me.rerere.rikkahub.data.db.entity.MemoryColumn
import me.rerere.rikkahub.data.db.entity.ColumnType
import java.util.UUID

class MemoryTableRepository(
    private val memoryTableDAO: MemoryTableDAO
) {
    // 表格相关操作
    fun getMemoryTablesByAssistant(assistantId: String): Flow<List<MemoryTable>> =
        memoryTableDAO.getMemoryTablesByAssistant(assistantId)
    
    suspend fun getMemoryTableById(id: String): MemoryTable? =
        memoryTableDAO.getMemoryTableById(id)
    
    suspend fun addTable(
        assistantId: String,
        name: String,
        description: String = "",
        columnHeaders: List<String>
    ): MemoryTable {
        // 将旧的列头列表转换为新的MemoryColumn列表
        val columns = columnHeaders.map { header ->
            MemoryColumn(
                id = UUID.randomUUID().toString(),
                sheetId = "", // 临时值，稍后更新
                name = header,
                columnType = ColumnType.TEXT,
                description = "",
                isRequired = false,
                defaultValue = null
            )
        }

        val tableId = UUID.randomUUID().toString()
        val table = MemoryTable(
            id = tableId,
            assistantId = assistantId,
            name = name,
            description = description,
            columns = columns.map { it.copy(sheetId = tableId) }
        )
        memoryTableDAO.insertTable(table)
        return table
    }

    suspend fun addTableWithColumns(
        assistantId: String,
        name: String,
        description: String = "",
        columns: List<MemoryColumn>
    ): MemoryTable {
        val tableId = UUID.randomUUID().toString()
        val table = MemoryTable(
            id = tableId,
            assistantId = assistantId,
            name = name,
            description = description,
            columns = columns.map { it.copy(sheetId = tableId) }
        )
        memoryTableDAO.insertTable(table)
        return table
    }
    
    suspend fun updateTable(table: MemoryTable) {
        memoryTableDAO.updateTable(table)
    }
    
    suspend fun deleteTable(id: String) {
        memoryTableDAO.getMemoryTableById(id)?.let { memoryTableDAO.deleteTable(it) }
    }
    
    fun searchMemoryTables(assistantId: String, searchText: String): Flow<List<MemoryTable>> =
        memoryTableDAO.searchMemoryTables(assistantId, "%$searchText%")
    
    // 表格行相关操作
    suspend fun addRow(
        tableId: String,
        rowData: Map<String, String>,
        rowOrder: Int = 0
    ): MemoryTableRow {
        val row = MemoryTableRow(
            id = UUID.randomUUID().toString(),
            tableId = tableId,
            rowData = rowData,
            rowOrder = rowOrder
        )
        memoryTableDAO.insertRow(row)
        return row
    }
    
    suspend fun updateRow(row: MemoryTableRow) {
        memoryTableDAO.updateRow(row)
    }
    
    suspend fun deleteRow(id: String) {
        memoryTableDAO.getRowById(id)?.let { memoryTableDAO.deleteRow(it) }
    }
    
    fun getRowsByTableId(tableId: String): Flow<List<MemoryTableRow>> =
        memoryTableDAO.getRowsByTableId(tableId)

    /**
     * 获取表格的完整数据（包括行数据）
     */
    suspend fun getTableWithRows(tableId: String): Pair<MemoryTable?, List<MemoryTableRow>> {
        val table = getMemoryTableById(tableId)
        val rows = mutableListOf<MemoryTableRow>()
        getRowsByTableId(tableId).collect { rows.addAll(it) }
        return Pair(table, rows)
    }

    /**
     * 批量添加行数据
     */
    suspend fun addRows(tableId: String, rowsData: List<Map<String, String>>) {
        rowsData.forEachIndexed { index, rowData ->
            addRow(tableId, rowData, index)
        }
    }

    /**
     * 更新表格的行数统计
     */
    suspend fun updateTableRowCount(tableId: String) {
        val rows = mutableListOf<MemoryTableRow>()
        getRowsByTableId(tableId).collect { rows.addAll(it) }

        getMemoryTableById(tableId)?.let { table ->
            updateTable(table.copy(rowCount = rows.size))
        }
    }

    /**
     * 检查表格名称是否重复
     */
    suspend fun isTableNameDuplicate(assistantId: String, name: String, excludeId: String? = null): Boolean {
        val tables = mutableListOf<MemoryTable>()
        getMemoryTablesByAssistant(assistantId).collect { tables.addAll(it) }

        return tables.any { table ->
            table.name.equals(name, ignoreCase = true) && table.id != excludeId
        }
    }

    /**
     * 获取表格统计信息
     */
    fun getTableStatistics(assistantId: String): Flow<Map<String, Int>> {
        return getMemoryTablesByAssistant(assistantId).map { tables ->
            val totalRows = tables.sumOf { it.rowCount }
            mapOf(
                "totalTables" to tables.size,
                "enabledTables" to tables.count { it.isEnabled },
                "totalRows" to totalRows,
                "averageRowsPerTable" to if (tables.isNotEmpty()) totalRows / tables.size else 0
            )
        }
    }

    /**
     * 格式化表格数据为JSON（用于AI上下文注入）
     */
    suspend fun formatTableForAI(tableId: String): String? {
        val table = getMemoryTableById(tableId) ?: return null
        val rows = mutableListOf<MemoryTableRow>()
        getRowsByTableId(tableId).collect { rows.addAll(it) }

        return buildString {
            appendLine("表格: ${table.name}")
            if (table.description.isNotEmpty()) {
                appendLine("描述: ${table.description}")
            }

            // 表头
            val headers = table.columns.map { it.name }
            appendLine("列: ${headers.joinToString(" | ")}")

            // 数据行
            if (rows.isNotEmpty()) {
                appendLine("数据:")
                rows.forEach { row ->
                    val values = headers.map { header ->
                        row.rowData[header] ?: ""
                    }
                    appendLine(values.joinToString(" | "))
                }
            } else {
                appendLine("(暂无数据)")
            }
        }
    }

    /**
     * 批量删除表格及其所有行数据
     */
    suspend fun deleteTableWithRows(tableId: String) {
        // 先删除所有行
        memoryTableDAO.deleteAllRowsForTable(tableId)
        // 再删除表格
        deleteTable(tableId)
    }

    /**
     * 按列类型过滤表格
     */
    suspend fun getTablesWithColumnType(assistantId: String, columnType: ColumnType): List<MemoryTable> {
        val tables = mutableListOf<MemoryTable>()
        getMemoryTablesByAssistant(assistantId).collect { tables.addAll(it) }

        return tables.filter { table ->
            table.columns.any { it.columnType == columnType }
        }
    }

    /**
     * 在表格行中搜索特定内容
     */
    suspend fun searchInTableRows(tableId: String, searchText: String): List<MemoryTableRow> {
        val rows = mutableListOf<MemoryTableRow>()
        getRowsByTableId(tableId).collect { rows.addAll(it) }

        return rows.filter { row ->
            row.rowData.values.any { value ->
                value.contains(searchText, ignoreCase = true)
            }
        }
    }

    /**
     * 导出表格数据为CSV格式
     */
    suspend fun exportTableToCSV(tableId: String): String? {
        val table = getMemoryTableById(tableId) ?: return null
        val rows = mutableListOf<MemoryTableRow>()
        getRowsByTableId(tableId).collect { rows.addAll(it) }

        return buildString {
            // CSV头部
            val headers = table.columns.map { it.name }
            appendLine(headers.joinToString(","))

            // 数据行
            rows.forEach { row ->
                val values = headers.map { header ->
                    val value = row.rowData[header] ?: ""
                    // 转义CSV中的逗号和引号
                    if (value.contains(",") || value.contains("\"")) {
                        "\"${value.replace("\"", "\"\"")}\""
                    } else {
                        value
                    }
                }
                appendLine(values.joinToString(","))
            }
        }
    }
}