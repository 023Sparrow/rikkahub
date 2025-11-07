package me.rerere.rikkahub.data.db.converter

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.rerere.rikkahub.data.db.entity.MemoryColumn

object MemoryTableConverter {
    @TypeConverter
    fun fromMemoryColumnList(value: String?): List<MemoryColumn> {
        return if (value.isNullOrEmpty()) {
            emptyList()
        } else {
            try {
                Json.decodeFromString(value)
            } catch (e: Exception) {
                // 如果解析失败，返回空列表而不是崩溃
                emptyList()
            }
        }
    }

    @TypeConverter
    fun toMemoryColumnList(columns: List<MemoryColumn>?): String {
        return if (columns == null || columns.isEmpty()) {
            "[]"
        } else {
            Json.encodeToString(columns)
        }
    }
}