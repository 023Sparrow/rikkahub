package me.rerere.rikkahub.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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

    /**
     * 获取按优先级排序的活跃条目
     */
    suspend fun getActiveEntriesSortedByPriority(assistantId: String): List<WorldBookEntry> {
        return getActiveWorldBookEntries(assistantId)
            .sortedWith(compareByDescending<WorldBookEntry> { it.priority }
                .thenBy { it.title })
    }

    /**
     * 批量更新条目启用状态
     */
    suspend fun updateEntriesEnabledStatus(entryIds: List<String>, enabled: Boolean) {
        entryIds.forEach { id ->
            getWorldBookEntryById(id)?.let { entry ->
                updateEntry(entry.copy(isEnabled = enabled))
            }
        }
    }

    /**
     * 批量更新条目优先级
     */
    suspend fun updateEntriesPriority(entryIds: List<String>, priority: Int) {
        entryIds.forEach { id ->
            getWorldBookEntryById(id)?.let { entry ->
                updateEntry(entry.copy(priority = priority))
            }
        }
    }

    /**
     * 删除指定助手的所有条目
     */
    suspend fun deleteAllEntriesForAssistant(assistantId: String) {
        // 由于DAO没有直接的方法，我们需要逐个删除
        val entries = getActiveWorldBookEntries(assistantId)
        entries.forEach { entry ->
            deleteEntry(entry.id)
        }
    }

    /**
     * 获取启用的常量条目（优先级最高）
     */
    suspend fun getConstantEntries(assistantId: String): List<WorldBookEntry> {
        return getActiveWorldBookEntries(assistantId)
            .filter { it.isConstant }
            .sortedByDescending { it.priority }
    }

    /**
     * 获取选择性条目
     */
    suspend fun getSelectiveEntries(assistantId: String): List<WorldBookEntry> {
        return getActiveWorldBookEntries(assistantId)
            .filter { it.isSelective }
            .sortedByDescending { it.priority }
    }

    /**
     * 检查条目标题是否重复
     */
    suspend fun isTitleDuplicate(assistantId: String, title: String, excludeId: String? = null): Boolean {
        val entries = getActiveWorldBookEntries(assistantId)
        return entries.any { entry ->
            entry.title.equals(title, ignoreCase = true) && entry.id != excludeId
        }
    }

    /**
     * 获取条目统计信息
     */
    fun getEntryStatistics(assistantId: String): Flow<Map<String, Int>> {
        return getWorldBookEntriesByAssistant(assistantId).map { entries ->
            mapOf(
                "total" to entries.size,
                "enabled" to entries.count { it.isEnabled },
                "disabled" to entries.count { !it.isEnabled },
                "constant" to entries.count { it.isConstant },
                "selective" to entries.count { it.isSelective }
            )
        }
    }
}