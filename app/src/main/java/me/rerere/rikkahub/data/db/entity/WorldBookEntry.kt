package me.rerere.rikkahub.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "world_book_entry")
data class WorldBookEntry(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    @ColumnInfo(name = "assistant_id")
    val assistantId: String,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "keywords")
    val keywords: List<String>,
    
    @ColumnInfo(name = "secondary_keywords")
    val secondaryKeywords: List<String> = emptyList(),
    
    @ColumnInfo(name = "content")
    val content: String,
    
    @ColumnInfo(name = "comment")
    val comment: String = "",
    
    @ColumnInfo(name = "is_constant")
    val isConstant: Boolean = false,
    
    @ColumnInfo(name = "is_selective")
    val isSelective: Boolean = false,
    
    @ColumnInfo(name = "priority")
    val priority: Int = 0,
    
    @ColumnInfo(name = "injection_position")
    val injectionPosition: Int = 0, // 0=开头, 1=结尾, 2=自定义
    
    @ColumnInfo(name = "is_enabled")
    val isEnabled: Boolean = true,
    
    @ColumnInfo(name = "exclude_recursion")
    val excludeRecursion: Boolean = false,
    
    @ColumnInfo(name = "use_regex")
    val useRegex: Boolean = false,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)