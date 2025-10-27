package me.rerere.rikkahub.data.db.converter

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object StringListConverter {
    @TypeConverter
    fun fromString(value: String?): List<String> {
        return if (value.isNullOrEmpty()) {
            emptyList()
        } else {
            Json.decodeFromString(value)
        }
    }

    @TypeConverter
    fun toString(list: List<String>?): String {
        return if (list == null || list.isEmpty()) {
            "[]"
        } else {
            Json.encodeToString(list)
        }
    }
}