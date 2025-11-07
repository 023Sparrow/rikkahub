package me.rerere.rikkahub.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * 记忆表格实体类
 * 基于st-memory-enhancement插件的Sheet设计
 */
@Entity(
    tableName = "memory_sheet",
    indices = [
        Index(value = ["assistantId"]),
        Index(value = ["assistantId", "sheetType"]),
        Index(value = ["sheetType"]),
        Index(value = ["isEnabled"]),
        Index(value = ["createdAt"]),
        Index(value = ["updatedAt"])
    ]
)
@Serializable
data class MemorySheet(
    @PrimaryKey
    val id: String,

    /**
     * 关联的助手ID
     */
    val assistantId: String,

    /**
     * 表格名称
     */
    val name: String,

    /**
     * 表格描述
     */
    val description: String = "",

    /**
     * 表格类型
     * BASE: 基础表格，存储原始记忆数据
     * DERIVED: 派生表格，从其他表格计算得出
     * USER: 用户自定义表格
     * APP: 应用系统表格
     */
    val sheetType: SheetType = SheetType.USER,

    /**
     * 表格样式配置
     * 存储表格的视觉样式和布局设置
     */
    val styleConfig: String = "{}",

    /**
     * 表格是否启用
     */
    val isEnabled: Boolean = true,

    /**
     * 排序权重
     */
    val order: Int = 0,

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
 * 表格类型枚举
 */
@Serializable
enum class SheetType {
    /**
     * 基础表格 - 存储原始记忆数据
     */
    BASE,

    /**
     * 派生表格 - 从其他表格计算或派生得出
     */
    DERIVED,

    /**
     * 用户表格 - 用户创建的自定义表格
     */
    USER,

    /**
     * 应用表格 - 系统预定义的表格
     */
    APP
}