package me.rerere.rikkahub.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * 记忆单元格实体类
 * 存储MemorySheet中的具体数据
 */
@Entity(
    tableName = "memory_cell",
    indices = [
        Index(value = ["sheetId"]),
        Index(value = ["sheetId", "rowIndex"]),
        Index(value = ["sheetId", "columnName"]),
        Index(value = ["sheetId", "rowIndex", "columnName"], unique = true),
        Index(value = ["createdAt"]),
        Index(value = ["updatedAt"])
    ]
)
@Serializable
data class MemoryCell(
    @PrimaryKey
    val id: String,

    /**
     * 所属表格ID
     */
    val sheetId: String,

    /**
     * 行索引
     */
    val rowIndex: Int,

    /**
     * 列名
     */
    val columnName: String,

    /**
     * 单元格内容
     */
    val content: String = "",

    /**
     * 单元格类型
     */
    val contentType: CellType = CellType.TEXT,

    /**
     * 是否为关键字段
     */
    val isKey: Boolean = false,

    /**
     * 数据来源
     * MANUAL: 手动输入
     * AUTO_SUMMARY: 自动总结
     * DERIVED: 派生计算
     */
    val source: DataSource = DataSource.MANUAL,

    /**
     * 信任级别
     * 0-10，数值越高表示越可信
     */
    val trustLevel: Int = 5,

    /**
     * 最后验证时间
     */
    val lastVerifiedAt: Long = 0L,

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
 * 单元格类型枚举
 */
@Serializable
enum class CellType {
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
     * JSON对象
     */
    JSON,

    /**
     * URL链接
     */
    URL
}

/**
 * 数据来源枚举
 */
@Serializable
enum class DataSource {
    /**
     * 手动输入
     */
    MANUAL,

    /**
     * 自动总结生成
     */
    AUTO_SUMMARY,

    /**
     * 从其他单元格派生
     */
    DERIVED,

    /**
     * 系统预设
     */
    SYSTEM
}