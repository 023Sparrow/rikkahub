package me.rerere.rikkahub.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import me.rerere.rikkahub.data.db.entity.MemoryColumn
import me.rerere.rikkahub.data.db.entity.ColumnType

/**
 * 记忆表格列定义数据访问对象
 */
@Dao
interface MemoryColumnDao {

    /**
     * 插入新的列定义
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertColumn(column: MemoryColumn)

    /**
     * 批量插入列定义
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertColumns(columns: List<MemoryColumn>)

    /**
     * 更新列定义
     */
    @Update
    suspend fun updateColumn(column: MemoryColumn)

    /**
     * 删除列定义
     */
    @Delete
    suspend fun deleteColumn(column: MemoryColumn)

    /**
     * 根据ID删除列定义
     */
    @Query("DELETE FROM memory_column WHERE id = :columnId")
    suspend fun deleteColumnById(columnId: String)

    /**
     * 根据ID获取列定义
     */
    @Query("SELECT * FROM memory_column WHERE id = :columnId")
    suspend fun getColumnById(columnId: String): MemoryColumn?

    /**
     * 根据ID获取列定义（Flow）
     */
    @Query("SELECT * FROM memory_column WHERE id = :columnId")
    fun getColumnByIdFlow(columnId: String): Flow<MemoryColumn?>

    /**
     * 获取指定表格的所有列定义
     */
    @Query("SELECT * FROM memory_column WHERE sheetId = :sheetId ORDER BY columnIndex ASC")
    fun getColumnsBySheet(sheetId: String): Flow<List<MemoryColumn>>

    /**
     * 获取指定表格的所有列定义（同步）
     */
    @Query("SELECT * FROM memory_column WHERE sheetId = :sheetId ORDER BY columnIndex ASC")
    suspend fun getColumnsBySheetSync(sheetId: String): List<MemoryColumn>

    /**
     * 根据表格ID和列名获取列定义
     */
    @Query("SELECT * FROM memory_column WHERE sheetId = :sheetId AND name = :columnName")
    suspend fun getColumnBySheetAndName(sheetId: String, columnName: String): MemoryColumn?

    /**
     * 根据表格ID和列索引获取列定义
     */
    @Query("SELECT * FROM memory_column WHERE sheetId = :sheetId AND columnIndex = :columnIndex")
    suspend fun getColumnBySheetAndIndex(sheetId: String, columnIndex: Int): MemoryColumn?

    /**
     * 获取指定表格的最大列索引
     */
    @Query("SELECT MAX(columnIndex) FROM memory_column WHERE sheetId = :sheetId")
    suspend fun getMaxColumnIndex(sheetId: String): Int?

    /**
     * 获取指定表格的列数量
     */
    @Query("SELECT COUNT(*) FROM memory_column WHERE sheetId = :sheetId")
    suspend fun getColumnCount(sheetId: String): Int

    /**
     * 更新列名
     */
    @Query("UPDATE memory_column SET name = :name, updatedAt = :updatedAt WHERE id = :columnId")
    suspend fun updateColumnName(columnId: String, name: String, updatedAt: Long = System.currentTimeMillis())

    /**
     * 更新列描述
     */
    @Query("UPDATE memory_column SET description = :description, updatedAt = :updatedAt WHERE id = :columnId")
    suspend fun updateColumnDescription(columnId: String, description: String, updatedAt: Long = System.currentTimeMillis())

    /**
     * 更新列类型
     */
    @Query("UPDATE memory_column SET columnType = :columnType, updatedAt = :updatedAt WHERE id = :columnId")
    suspend fun updateColumnType(columnId: String, columnType: ColumnType, updatedAt: Long = System.currentTimeMillis())

    /**
     * 更新列索引
     */
    @Query("UPDATE memory_column SET columnIndex = :columnIndex, updatedAt = :updatedAt WHERE id = :columnId")
    suspend fun updateColumnIndex(columnId: String, columnIndex: Int, updatedAt: Long = System.currentTimeMillis())

    /**
     * 更新列的必填状态
     */
    @Query("UPDATE memory_column SET isRequired = :isRequired, updatedAt = :updatedAt WHERE id = :columnId")
    suspend fun updateColumnRequired(columnId: String, isRequired: Boolean, updatedAt: Long = System.currentTimeMillis())

    /**
     * 更新列的默认值
     */
    @Query("UPDATE memory_column SET defaultValue = :defaultValue, updatedAt = :updatedAt WHERE id = :columnId")
    suspend fun updateColumnDefaultValue(columnId: String, defaultValue: String?, updatedAt: Long = System.currentTimeMillis())

    /**
     * 更新列宽度比例
     */
    @Query("UPDATE memory_column SET widthRatio = :widthRatio, updatedAt = :updatedAt WHERE id = :columnId")
    suspend fun updateColumnWidthRatio(columnId: String, widthRatio: Float, updatedAt: Long = System.currentTimeMillis())

    /**
     * 更新列的编辑状态
     */
    @Query("UPDATE memory_column SET isEditable = :isEditable, updatedAt = :updatedAt WHERE id = :columnId")
    suspend fun updateColumnEditable(columnId: String, isEditable: Boolean, updatedAt: Long = System.currentTimeMillis())

    /**
     * 更新列的验证规则
     */
    @Query("UPDATE memory_column SET validationRules = :validationRules, updatedAt = :updatedAt WHERE id = :columnId")
    suspend fun updateColumnValidationRules(columnId: String, validationRules: String, updatedAt: Long = System.currentTimeMillis())

    /**
     * 删除指定表格的所有列定义
     */
    @Query("DELETE FROM memory_column WHERE sheetId = :sheetId")
    suspend fun deleteAllColumnsBySheet(sheetId: String)

    /**
     * 获取指定表格的必填列
     */
    @Query("SELECT * FROM memory_column WHERE sheetId = :sheetId AND isRequired = 1 ORDER BY columnIndex ASC")
    suspend fun getRequiredColumns(sheetId: String): List<MemoryColumn>

    /**
     * 获取指定表格的可编辑列
     */
    @Query("SELECT * FROM memory_column WHERE sheetId = :sheetId AND isEditable = 1 ORDER BY columnIndex ASC")
    suspend fun getEditableColumns(sheetId: String): List<MemoryColumn>

    /**
     * 获取指定类型的列
     */
    @Query("SELECT * FROM memory_column WHERE sheetId = :sheetId AND columnType = :columnType ORDER BY columnIndex ASC")
    suspend fun getColumnsByType(sheetId: String, columnType: ColumnType): List<MemoryColumn>

    /**
     * 检查列是否存在
     */
    @Query("SELECT COUNT(*) > 0 FROM memory_column WHERE sheetId = :sheetId AND name = :columnName")
    suspend fun columnExists(sheetId: String, columnName: String): Boolean

    /**
     * 搜索列定义
     */
    @Query("""
        SELECT * FROM memory_column
        WHERE sheetId = :sheetId
        AND (name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
        ORDER BY columnIndex ASC
    """)
    suspend fun searchColumns(sheetId: String, query: String): List<MemoryColumn>

    /**
     * 批量更新列索引
     * 用于重新排序列
     */
    @Query("UPDATE memory_column SET columnIndex = :newIndex, updatedAt = :updatedAt WHERE id = :columnId")
    suspend fun updateColumnIndexBatch(columnId: String, newIndex: Int, updatedAt: Long = System.currentTimeMillis())

    /**
     * 获取有默认值的列
     */
    @Query("SELECT * FROM memory_column WHERE sheetId = :sheetId AND defaultValue IS NOT NULL AND defaultValue != '' ORDER BY columnIndex ASC")
    suspend fun getColumnsWithDefaultValues(sheetId: String): List<MemoryColumn>
}