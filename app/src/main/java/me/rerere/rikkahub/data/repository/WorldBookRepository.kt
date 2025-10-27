package me.rerere.rikkahub.data.repository

import kotlinx.coroutines.flow.Flow
import me.rerere.rikkahub.data.db.dao.WorldBookDAO
import me.rerere.rikkahub.data.db.entity.WorldBookEntry
import java.util.UUID

class WorldBookRepository(
    private val worldBookDAO: WorldBookDAO
) {
    fun getWorldBookEntriesByAssistant(assistantId: String): Flow<List<WorldBookEntry>> =
        worldBookDAO.getWorldBookEntriesByAssistant(assistantId)
    
    suspend fun getActiveWorldBookEntries(assistantId: String): List<WorldBookEntry> =
        worldBookDAO.getActiveWorldBookEntries(assistantId)
    
    suspend fun getWorldBookEntryById(id: String): WorldBookEntry? =
        worldBookDAO.getWorldBookEntryById(id)
    
    suspend fun addEntry(
        assistantId: String,
        title: String,
        keywords: List<String>,
        content: String,
        comment: String = "",
        isConstant: Boolean = false,
        isSelective: Boolean = false,
        priority: Int = 0,
        injectionPosition: Int = 0,
        isEnabled: Boolean = true,
        excludeRecursion: Boolean = false,
        useRegex: Boolean = false
    ): WorldBookEntry {
        val entry = WorldBookEntry(
            id = UUID.randomUUID().toString(),
            assistantId = assistantId,
            title = title,
            keywords = keywords,
            content = content,
            comment = comment,
            isConstant = isConstant,
            isSelective = isSelective,
            priority = priority,
            injectionPosition = injectionPosition,
            isEnabled = isEnabled,
            excludeRecursion = excludeRecursion,
            useRegex = useRegex
        )
        worldBookDAO.insert(entry)
        return entry
    }
    
    suspend fun updateEntry(entry: WorldBookEntry) {
        worldBookDAO.update(entry)
    }
    
    suspend fun deleteEntry(id: String) {
        worldBookDAO.getWorldBookEntryById(id)?.let { worldBookDAO.delete(it) }
    }
    
    fun searchWorldBookEntries(assistantId: String, searchText: String): Flow<List<WorldBookEntry>> =
        worldBookDAO.searchWorldBookEntries(assistantId, "%$searchText%")
}