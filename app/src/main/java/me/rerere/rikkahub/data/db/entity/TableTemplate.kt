package me.rerere.rikkahub.data.db.entity

import androidx.room.*
import kotlin.uuid.Uuid

/**
 * 表格模板实体（简化版本）
 * 用于定义记忆表格的结构模板
 */
@Entity(
    tableName = "table_template",
    indices = [
        Index(value = ["assistantId"]),
        Index(value = ["name"]),
        Index(value = ["createdAt"])
    ]
)
data class TableTemplate(
    @PrimaryKey
    val id: String = Uuid.random().toString(),

    /** 关联的助手ID */
    val assistantId: String,

    /** 模板名称 */
    val name: String,

    /** 模板描述 */
    val description: String = "",

    /** 列定义JSON字符串 */
    val columnsJson: String,

    /** 是否启用 */
    val isEnabled: Boolean = true,

    /** 创建时间 */
    val createdAt: Long = System.currentTimeMillis(),

    /** 更新时间 */
    val updatedAt: Long = System.currentTimeMillis(),

    /** 使用次数 */
    val usageCount: Int = 0
)