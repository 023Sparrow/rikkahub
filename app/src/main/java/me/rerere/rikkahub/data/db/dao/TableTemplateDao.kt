package me.rerere.rikkahub.data.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import me.rerere.rikkahub.data.db.entity.TableTemplate

/**
 * 表格模板数据访问对象（简化版本）
 */
@Dao
interface TableTemplateDao {

    /**
     * 插入新模板
     */
    @Insert
    suspend fun insertTemplate(template: TableTemplate)

    /**
     * 更新模板
     */
    @Update
    suspend fun updateTemplate(template: TableTemplate)

    /**
     * 删除模板
     */
    @Delete
    suspend fun deleteTemplate(template: TableTemplate)

    /**
     * 根据ID删除模板
     */
    @Query("DELETE FROM table_template WHERE id = :id")
    suspend fun deleteTemplateById(id: String)

    /**
     * 根据ID获取模板
     */
    @Query("SELECT * FROM table_template WHERE id = :id")
    suspend fun getTemplateById(id: String): TableTemplate?

    /**
     * 根据助手ID获取所有模板
     */
    @Query("SELECT * FROM table_template WHERE assistantId = :assistantId ORDER BY createdAt DESC")
    fun getTemplatesByAssistant(assistantId: String): Flow<List<TableTemplate>>

    /**
     * 根据助手ID获取启用的模板
     */
    @Query("SELECT * FROM table_template WHERE assistantId = :assistantId AND isEnabled = 1 ORDER BY createdAt DESC")
    fun getEnabledTemplatesByAssistant(assistantId: String): Flow<List<TableTemplate>>

    /**
     * 切换模板启用状态
     */
    @Query("UPDATE table_template SET isEnabled = :isEnabled, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateTemplateEnabled(id: String, isEnabled: Boolean, updatedAt: Long = System.currentTimeMillis())
}