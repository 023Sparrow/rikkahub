package me.rerere.rikkahub.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * 记忆表格列定义实体类
 * 定义MemorySheet的列结构
 */
@Entity(
    tableName = "memory_column",
    indices = [
        Index(value = ["sheetId"]),
        Index(value = ["sheetId", "columnIndex"], unique = true),
        Index(value = ["columnType"]),
        Index(value = ["isRequired"])
    ]
)
@Serializable
data class MemoryColumn(
    @PrimaryKey
    val id: String,

    /**
     * 所属表格ID
     */
    val sheetId: String,

    /**
     * 列名
     */
    val name: String,

    /**
     * 列描述
     */
    val description: String = "",

    /**
     * 列类型
     */
    val columnType: ColumnType = ColumnType.TEXT,

    /**
     * 列在表格中的位置
     */
    val columnIndex: Int = 0,

    /**
     * 是否为必填列
     */
    val isRequired: Boolean = false,

    /**
     * 默认值
     */
    val defaultValue: String? = null,

    /**
     * 列宽度比例
     * 用于UI布局
     */
    val widthRatio: Float = 1.0f,

    /**
     * 是否可编辑
     */
    val isEditable: Boolean = true,

    /**
     * 验证规则
     * JSON格式存储验证规则
     */
    val validationRules: String = "{}",

    /**
     * 创建时间
     */
    val createdAt: Long = System.currentTimeMillis(),

    /**
     * 更新时间
     */
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 列类型枚举
 */
@Serializable
enum class ColumnType {
    /**
     * 纯文本
     */
    TEXT,

    /**
     * 数字
     */
    NUMBER,

    /**
     * 日期时间
     */
    DATETIME,

    /**
     * 布尔值
     */
    BOOLEAN,

    /**
     * 选择列表
     */
    SELECT,

    /**
     * 多行文本
     */
    TEXTAREA,

    /**
     * URL链接
     */
    URL,

    /**
     * 标签
     */
    TAGS
}