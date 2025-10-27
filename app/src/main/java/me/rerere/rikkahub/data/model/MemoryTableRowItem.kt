package me.rerere.rikkahub.data.model

data class MemoryTableRowItem(
    val id: Long,
    val rowName: String,
    val cells: Map<String, String> // Map column name to cell value
)