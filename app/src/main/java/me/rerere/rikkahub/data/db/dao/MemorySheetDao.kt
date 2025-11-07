package me.rerere.rikkahub.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import me.rerere.rikkahub.data.db.entity.MemorySheet
import me.rerere.rikkahub.data.db.entity.SheetType

/**
 * 记忆表格数据访问对象
 */
@Dao
interface MemorySheetDao {

    /**
     * 插入新的记忆表格
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSheet(sheet: MemorySheet)

    /**
     * 批量插入记忆表格
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSheets(sheets: List<MemorySheet>)

    /**
     * 更新记忆表格
     */
    @Update
    suspend fun updateSheet(sheet: MemorySheet)

    /**
     * 删除记忆表格
     */
    @Delete
    suspend fun deleteSheet(sheet: MemorySheet)

    /**
     * 根据ID删除记忆表格
     */
    @Query("DELETE FROM memory_sheet WHERE id = :sheetId")
    suspend fun deleteSheetById(sheetId: String)

    /**
     * 根据ID获取记忆表格
     */
    @Query("SELECT * FROM memory_sheet WHERE id = :sheetId")
    suspend fun getSheetById(sheetId: String): MemorySheet?

    /**
     * 根据ID获取记忆表格（Flow）
     */
    @Query("SELECT * FROM memory_sheet WHERE id = :sheetId")
    fun getSheetByIdFlow(sheetId: String): Flow<MemorySheet?>

    /**
     * 获取指定助手的所有记忆表格
     */
    @Query("SELECT * FROM memory_sheet WHERE assistantId = :assistantId ORDER BY `order` ASC, createdAt DESC")
    fun getSheetsByAssistant(assistantId: String): Flow<List<MemorySheet>>

    /**
     * 获取指定助手的已启用记忆表格
     */
    @Query("SELECT * FROM memory_sheet WHERE assistantId = :assistantId AND isEnabled = 1 ORDER BY `order` ASC, createdAt DESC")
    suspend fun getEnabledSheetsByAssistant(assistantId: String): List<MemorySheet>

    /**
     * 获取指定助手和类型的记忆表格
     */
    @Query("SELECT * FROM memory_sheet WHERE assistantId = :assistantId AND sheetType = :sheetType ORDER BY `order` ASC, createdAt DESC")
    fun getSheetsByAssistantAndType(assistantId: String, sheetType: SheetType): Flow<List<MemorySheet>>

    /**
     * 获取所有记忆表格
     */
    @Query("SELECT * FROM memory_sheet ORDER BY createdAt DESC")
    fun getAllSheets(): Flow<List<MemorySheet>>

    /**
     * 更新记忆表格启用状态
     */
    @Query("UPDATE memory_sheet SET isEnabled = :isEnabled, updatedAt = :updatedAt WHERE id = :sheetId")
    suspend fun updateSheetEnabledStatus(sheetId: String, isEnabled: Boolean, updatedAt: Long = System.currentTimeMillis())

    /**
     * 更新记忆表格排序
     */
    @Query("UPDATE memory_sheet SET `order` = :order, updatedAt = :updatedAt WHERE id = :sheetId")
    suspend fun updateSheetOrder(sheetId: String, order: Int, updatedAt: Long = System.currentTimeMillis())

    /**
     * 更新记忆表格名称和描述
     */
    @Query("UPDATE memory_sheet SET name = :name, description = :description, updatedAt = :updatedAt WHERE id = :sheetId")
    suspend fun updateSheetInfo(sheetId: String, name: String, description: String, updatedAt: Long = System.currentTimeMillis())

    /**
     * 更新记忆表格样式配置
     */
    @Query("UPDATE memory_sheet SET styleConfig = :styleConfig, updatedAt = :updatedAt WHERE id = :sheetId")
    suspend fun updateSheetStyleConfig(sheetId: String, styleConfig: String, updatedAt: Long = System.currentTimeMillis())

    /**
     * 检查记忆表格是否存在
     */
    @Query("SELECT COUNT(*) > 0 FROM memory_sheet WHERE id = :sheetId")
    suspend fun sheetExists(sheetId: String): Boolean

    /**
     * 获取记忆表格数量
     */
    @Query("SELECT COUNT(*) FROM memory_sheet WHERE assistantId = :assistantId")
    suspend fun getSheetCountByAssistant(assistantId: String): Int

    /**
     * 获取已启用的记忆表格数量
     */
    @Query("SELECT COUNT(*) FROM memory_sheet WHERE assistantId = :assistantId AND isEnabled = 1")
    suspend fun getEnabledSheetCountByAssistant(assistantId: String): Int

    /**
     * 删除指定助手的所有记忆表格
     */
    @Query("DELETE FROM memory_sheet WHERE assistantId = :assistantId")
    suspend fun deleteAllSheetsByAssistant(assistantId: String)

    /**
     * 搜索记忆表格
     */
    @Query("""
        SELECT * FROM memory_sheet
        WHERE assistantId = :assistantId
        AND (name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
        ORDER BY `order` ASC, createdAt DESC
    """)
    fun searchSheets(assistantId: String, query: String): Flow<List<MemorySheet>>
}