package me.rerere.rikkahub.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import me.rerere.rikkahub.data.db.entity.MemoryCell
import me.rerere.rikkahub.data.db.entity.CellType
import me.rerere.rikkahub.data.db.entity.DataSource

/**
 * 记忆单元格数据访问对象
 */
@Dao
interface MemoryCellDao {

    /**
     * 插入新的记忆单元格
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCell(cell: MemoryCell)

    /**
     * 批量插入记忆单元格
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCells(cells: List<MemoryCell>)

    /**
     * 更新记忆单元格
     */
    @Update
    suspend fun updateCell(cell: MemoryCell)

    /**
     * 删除记忆单元格
     */
    @Delete
    suspend fun deleteCell(cell: MemoryCell)

    /**
     * 根据ID删除记忆单元格
     */
    @Query("DELETE FROM memory_cell WHERE id = :cellId")
    suspend fun deleteCellById(cellId: String)

    /**
     * 根据ID获取记忆单元格
     */
    @Query("SELECT * FROM memory_cell WHERE id = :cellId")
    suspend fun getCellById(cellId: String): MemoryCell?

    /**
     * 根据ID获取记忆单元格（Flow）
     */
    @Query("SELECT * FROM memory_cell WHERE id = :cellId")
    fun getCellByIdFlow(cellId: String): Flow<MemoryCell?>

    /**
     * 获取指定表格的所有单元格
     */
    @Query("SELECT * FROM memory_cell WHERE sheetId = :sheetId ORDER BY rowIndex ASC, columnName ASC")
    fun getCellsBySheet(sheetId: String): Flow<List<MemoryCell>>

    /**
     * 获取指定表格的所有单元格（同步版本）
     */
    @Query("SELECT * FROM memory_cell WHERE sheetId = :sheetId ORDER BY rowIndex ASC, columnName ASC")
    suspend fun getCellsBySheetSync(sheetId: String): List<MemoryCell>

    /**
     * 获取指定表格和行的所有单元格
     */
    @Query("SELECT * FROM memory_cell WHERE sheetId = :sheetId AND rowIndex = :rowIndex ORDER BY columnName ASC")
    suspend fun getCellsByRow(sheetId: String, rowIndex: Int): List<MemoryCell>

    /**
     * 获取指定表格的指定单元格
     */
    @Query("SELECT * FROM memory_cell WHERE sheetId = :sheetId AND rowIndex = :rowIndex AND columnName = :columnName")
    suspend fun getCell(sheetId: String, rowIndex: Int, columnName: String): MemoryCell?

    /**
     * 获取指定表格的指定单元格（Flow）
     */
    @Query("SELECT * FROM memory_cell WHERE sheetId = :sheetId AND rowIndex = :rowIndex AND columnName = :columnName")
    fun getCellFlow(sheetId: String, rowIndex: Int, columnName: String): Flow<MemoryCell?>

    /**
     * 获取指定表格的所有行索引
     */
    @Query("SELECT DISTINCT rowIndex FROM memory_cell WHERE sheetId = :sheetId ORDER BY rowIndex ASC")
    suspend fun getRowIndices(sheetId: String): List<Int>

    /**
     * 获取指定表格的所有列名
     */
    @Query("SELECT DISTINCT columnName FROM memory_cell WHERE sheetId = :sheetId ORDER BY columnName ASC")
    suspend fun getColumnNames(sheetId: String): List<String>

    /**
     * 获取指定表格的最大行索引
     */
    @Query("SELECT MAX(rowIndex) FROM memory_cell WHERE sheetId = :sheetId")
    suspend fun getMaxRowIndex(sheetId: String): Int?

    /**
     * 更新单元格内容
     */
    @Query("UPDATE memory_cell SET content = :content, updatedAt = :updatedAt WHERE id = :cellId")
    suspend fun updateCellContent(cellId: String, content: String, updatedAt: Long = System.currentTimeMillis())

    /**
     * 更新单元格信任级别
     */
    @Query("UPDATE memory_cell SET trustLevel = :trustLevel, lastVerifiedAt = :lastVerifiedAt, updatedAt = :updatedAt WHERE id = :cellId")
    suspend fun updateCellTrustLevel(cellId: String, trustLevel: Int, lastVerifiedAt: Long = System.currentTimeMillis(), updatedAt: Long = System.currentTimeMillis())

    /**
     * 更新单元格关键字段状态
     */
    @Query("UPDATE memory_cell SET isKey = :isKey, updatedAt = :updatedAt WHERE id = :cellId")
    suspend fun updateCellKeyStatus(cellId: String, isKey: Boolean, updatedAt: Long = System.currentTimeMillis())

    /**
     * 删除指定表格的所有单元格
     */
    @Query("DELETE FROM memory_cell WHERE sheetId = :sheetId")
    suspend fun deleteAllCellsBySheet(sheetId: String)

    /**
     * 删除指定表格的指定行
     */
    @Query("DELETE FROM memory_cell WHERE sheetId = :sheetId AND rowIndex = :rowIndex")
    suspend fun deleteRow(sheetId: String, rowIndex: Int)

    /**
     * 删除指定表格的指定列
     */
    @Query("DELETE FROM memory_cell WHERE sheetId = :sheetId AND columnName = :columnName")
    suspend fun deleteColumn(sheetId: String, columnName: String)

    /**
     * 批量删除指定表格的多行
     */
    @Query("DELETE FROM memory_cell WHERE sheetId = :sheetId AND rowIndex IN (:rowIndices)")
    suspend fun deleteRows(sheetId: String, rowIndices: List<Int>)

    /**
     * 获取指定数据来源的单元格
     */
    @Query("SELECT * FROM memory_cell WHERE sheetId = :sheetId AND source = :source ORDER BY rowIndex ASC, columnName ASC")
    suspend fun getCellsBySource(sheetId: String, source: DataSource): List<MemoryCell>

    /**
     * 获取关键字段
     */
    @Query("SELECT * FROM memory_cell WHERE sheetId = :sheetId AND isKey = 1 ORDER BY rowIndex ASC, columnName ASC")
    suspend fun getKeyCells(sheetId: String): List<MemoryCell>

    /**
     * 获取指定信任级别以上的单元格
     */
    @Query("SELECT * FROM memory_cell WHERE sheetId = :sheetId AND trustLevel >= :minTrustLevel ORDER BY rowIndex ASC, columnName ASC")
    suspend fun getCellsByMinTrustLevel(sheetId: String, minTrustLevel: Int): List<MemoryCell>

    /**
     * 搜索单元格内容
     */
    @Query("""
        SELECT * FROM memory_cell
        WHERE sheetId = :sheetId
        AND content LIKE '%' || :query || '%'
        ORDER BY rowIndex ASC, columnName ASC
    """)
    suspend fun searchCells(sheetId: String, query: String): List<MemoryCell>

    /**
     * 获取单元格数量
     */
    @Query("SELECT COUNT(*) FROM memory_cell WHERE sheetId = :sheetId")
    suspend fun getCellCount(sheetId: String): Int

    /**
     * 获取行数量
     */
    @Query("SELECT COUNT(DISTINCT rowIndex) FROM memory_cell WHERE sheetId = :sheetId")
    suspend fun getRowCount(sheetId: String): Int

    /**
     * 检查单元格是否存在
     */
    @Query("SELECT COUNT(*) > 0 FROM memory_cell WHERE sheetId = :sheetId AND rowIndex = :rowIndex AND columnName = :columnName")
    suspend fun cellExists(sheetId: String, rowIndex: Int, columnName: String): Boolean

    /**
     * 获取最近更新的单元格
     */
    @Query("SELECT * FROM memory_cell WHERE sheetId = :sheetId ORDER BY updatedAt DESC LIMIT :limit")
    suspend fun getRecentlyUpdatedCells(sheetId: String, limit: Int = 10): List<MemoryCell>
}