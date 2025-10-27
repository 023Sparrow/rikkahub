package me.rerere.rikkahub.data.db.converter

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object MemoryTableCellConverter {
    @TypeConverter
    fun fromString(value: String?): Map<String, String> {
        return if (value.isNullOrEmpty()) {
            emptyMap()
        } else {
            Json.decodeFromString(value)
        }
    }

    @TypeConverter
    fun toString(map: Map<String, String>?): String {
        return if (map == null || map.isEmpty()) {
            "{}"
        } else {
            Json.encodeToString(map)
        }
    }
}