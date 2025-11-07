package me.rerere.rikkahub.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID
import kotlinx.serialization.Serializable

@Entity(
    tableName = "memory_table",
    indices = [
        Index(value = ["assistant_id"]),
        Index(value = ["name"]),
        Index(value = ["created_at"]),
        Index(value = ["assistant_id", "name"])
    ]
)
@Serializable
data class MemoryTable(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "assistant_id")
    val assistantId: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String = "",

    @ColumnInfo(name = "columns", defaultValue = "[]")
    val columns: List<MemoryColumn>,

    @ColumnInfo(name = "row_count", defaultValue = "0")
    val rowCount: Int = 0,

    @ColumnInfo(name = "is_enabled", defaultValue = "1")
    val isEnabled: Boolean = true,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

