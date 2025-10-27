package me.rerere.rikkahub.data.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import me.rerere.rikkahub.data.db.entity.MemoryTable
import me.rerere.rikkahub.data.db.entity.MemoryTableRow

@Dao
interface MemoryTableDAO {
    // MemoryTable 操作
    @Query("SELECT * FROM memory_table WHERE assistant_id = :assistantId")
    fun getMemoryTablesByAssistant(assistantId: String): Flow<List<MemoryTable>>
    
    @Query("SELECT * FROM memory_table WHERE id = :id")
    suspend fun getMemoryTableById(id: String): MemoryTable?
    
    @Insert
    suspend fun insertTable(table: MemoryTable): Long
    
    @Update
    suspend fun updateTable(table: MemoryTable)
    
    @Delete
    suspend fun deleteTable(table: MemoryTable)
    
    @Query("DELETE FROM memory_table WHERE assistant_id = :assistantId")
    suspend fun deleteAllTablesForAssistant(assistantId: String)
    
    // MemoryTableRow 操作
    @Query("SELECT * FROM memory_table_row WHERE table_id = :tableId ORDER BY row_order ASC")
    fun getRowsByTableId(tableId: String): Flow<List<MemoryTableRow>>
    
    @Query("SELECT * FROM memory_table_row WHERE id = :id")
    suspend fun getRowById(id: String): MemoryTableRow?
    
    @Insert
    suspend fun insertRow(row: MemoryTableRow): Long
    
    @Update
    suspend fun updateRow(row: MemoryTableRow)
    
    @Delete
    suspend fun deleteRow(row: MemoryTableRow)
    
    @Query("DELETE FROM memory_table_row WHERE table_id = :tableId")
    suspend fun deleteAllRowsForTable(tableId: String)
    
    // 搜索功能
    @Query("SELECT * FROM memory_table WHERE assistant_id = :assistantId AND name LIKE :searchText OR description LIKE :searchText")
    fun searchMemoryTables(assistantId: String, searchText: String): Flow<List<MemoryTable>>
}