package me.rerere.rikkahub.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.rerere.rikkahub.data.repository.WorldBookRepository
import me.rerere.rikkahub.data.db.entity.WorldBookEntry
import java.util.UUID

class WorldBookViewModel(
    private val repository: WorldBookRepository
) : ViewModel() {
    private val _entries = MutableStateFlow<List<WorldBookEntry>>(emptyList())
    val entries: StateFlow<List<WorldBookEntry>> = _entries.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _assistantId = MutableStateFlow("")
    val assistantId: StateFlow<String> = _assistantId.asStateFlow()

    fun setAssistantId(assistantId: String) {
        _assistantId.value = assistantId
        viewModelScope.launch {
            repository.getWorldBookEntriesByAssistant(assistantId).collect {
                _entries.value = it
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addEntry(
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
    ) {
        viewModelScope.launch {
            repository.addEntry(
                assistantId = _assistantId.value,
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
        }
    }

    fun updateEntry(entry: WorldBookEntry) {
        viewModelScope.launch {
            repository.updateEntry(entry)
        }
    }

    fun deleteEntry(id: String) {
        viewModelScope.launch {
            repository.deleteEntry(id)
        }
    }

    suspend fun getEntryById(id: String) = repository.getWorldBookEntryById(id)
}