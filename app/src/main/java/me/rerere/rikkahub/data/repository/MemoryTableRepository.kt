package me.rerere.rikkahub.data.repository

import kotlinx.coroutines.flow.Flow
import me.rerere.rikkahub.data.db.dao.MemoryTableDAO
import me.rerere.rikkahub.data.db.entity.MemoryTable
import me.rerere.rikkahub.data.db.entity.MemoryTableRow
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
        val table = MemoryTable(
            id = UUID.randomUUID().toString(),
            assistantId = assistantId,
            name = name,
            description = description,
            columnHeaders = columnHeaders
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
}