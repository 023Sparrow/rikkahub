package me.rerere.rikkahub.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import me.rerere.rikkahub.data.repository.WorldBookRepository
import me.rerere.rikkahub.data.db.entity.WorldBookEntry
import me.rerere.rikkahub.service.WorldBookMatcher
import me.rerere.rikkahub.service.WorldBookInjector
import me.rerere.ai.ui.UIMessage
import kotlin.uuid.Uuid

/**
 * World Book ViewModel
 * 提供World Book的MVVM架构状态管理和业务逻辑处理
 */
class WorldBookViewModel(
    private val repository: WorldBookRepository,
    private val matcher: WorldBookMatcher,
    private val injector: WorldBookInjector
) : ViewModel() {

    // 基础状态
    private val _assistantId = MutableStateFlow("")
    val assistantId: StateFlow<String> = _assistantId.asStateFlow()

    private val _entries = MutableStateFlow<List<WorldBookEntry>>(emptyList())
    val entries: StateFlow<List<WorldBookEntry>> = _entries.asStateFlow()

    // 搜索和过滤状态
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filteredEntries = MutableStateFlow<List<WorldBookEntry>>(emptyList())
    val filteredEntries: StateFlow<List<WorldBookEntry>> = _filteredEntries.asStateFlow()

    // UI状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedEntry = MutableStateFlow<WorldBookEntry?>(null)
    val selectedEntry: StateFlow<WorldBookEntry?> = _selectedEntry.asStateFlow()

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    private val _showImportDialog = MutableStateFlow(false)
    val showImportDialog: StateFlow<Boolean> = _showImportDialog.asStateFlow()

    // 统计信息
    private val _statistics = MutableStateFlow<Map<String, Int>>(emptyMap())
    val statistics: StateFlow<Map<String, Int>> = _statistics.asStateFlow()

    // 批量操作状态
    private val _selectedEntries = MutableStateFlow<Set<String>>(emptySet())
    val selectedEntries: StateFlow<Set<String>> = _selectedEntries.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    // 排序和筛选状态
    private val _sortBy = MutableStateFlow(SortOption.CREATED_AT)
    val sortBy: StateFlow<SortOption> = _sortBy.asStateFlow()

    private val _filterOption = MutableStateFlow(FilterOption.ALL)
    val filterOption: StateFlow<FilterOption> = _filterOption.asStateFlow()

    init {
        // 设置搜索和过滤逻辑
        combine(
            _entries,
            _searchQuery,
            _sortBy,
            _filterOption
        ) { entries, query, sortBy, filterOption ->
            applyFiltersAndSorting(entries, query, sortBy, filterOption)
        }.onEach { filtered ->
            _filteredEntries.value = filtered
        }.launchIn(viewModelScope)

        // 加载统计信息
        combine(_assistantId) { assistantId ->
            if (assistantId.isNotEmpty()) {
                loadStatistics(assistantId.first())
            }
        }.launchIn(viewModelScope)
    }

    /**
     * 设置当前助手ID并加载条目
     */
    fun setAssistantId(assistantId: String) {
        if (_assistantId.value != assistantId) {
            _assistantId.value = assistantId
            loadEntries(assistantId)
        }
    }

    /**
     * 加载World Book条目
     */
    private fun loadEntries(assistantId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getWorldBookEntriesByAssistant(assistantId).collect { entries ->
                    _entries.value = entries
                }
            } catch (e: Exception) {
                // 处理错误
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 加载统计信息
     */
    private fun loadStatistics(assistantId: String) {
        viewModelScope.launch {
            repository.getEntryStatistics(assistantId).collect { stats ->
                _statistics.value = stats
            }
        }
    }

    /**
     * 更新搜索查询
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * 添加新条目
     */
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
            try {
                _isLoading.value = true
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
                _showAddDialog.value = false
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 更新条目
     */
    fun updateEntry(entry: WorldBookEntry) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.updateEntry(entry)
                _isEditMode.value = false
                _selectedEntry.value = null
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 删除条目
     */
    fun deleteEntry(id: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.deleteEntry(id)
                _selectedEntry.value = null
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 删除条目（重载）
     */
    fun deleteEntry(entry: WorldBookEntry) {
        deleteEntry(entry.id)
    }

    /**
     * 批量删除选中条目
     */
    fun deleteSelectedEntries() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _selectedEntries.value.forEach { id ->
                    repository.deleteEntry(id)
                }
                clearSelection()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 切换条目启用状态
     */
    fun toggleEntryEnabled(entry: WorldBookEntry) {
        viewModelScope.launch {
            repository.updateEntry(entry.copy(isEnabled = !entry.isEnabled))
        }
    }

    /**
     * 批量更新条目启用状态
     */
    fun updateEntriesEnabledStatus(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateEntriesEnabledStatus(_selectedEntries.value.toList(), enabled)
            clearSelection()
        }
    }

    /**
     * 批量更新条目优先级
     */
    fun updateEntriesPriority(priority: Int) {
        viewModelScope.launch {
            repository.updateEntriesPriority(_selectedEntries.value.toList(), priority)
            clearSelection()
        }
    }

    /**
     * 选择条目进行编辑
     */
    fun selectEntry(entry: WorldBookEntry) {
        _selectedEntry.value = entry
        _isEditMode.value = true
    }

    /**
     * 取消编辑
     */
    fun cancelEdit() {
        _selectedEntry.value = null
        _isEditMode.value = false
    }

    /**
     * 显示添加对话框
     */
    fun showAddDialog() {
        _showAddDialog.value = true
    }

    /**
     * 隐藏添加对话框
     */
    fun hideAddDialog() {
        _showAddDialog.value = false
    }

    /**
     * 显示导入对话框
     */
    fun showImportDialog() {
        _showImportDialog.value = true
    }

    /**
     * 隐藏导入对话框
     */
    fun hideImportDialog() {
        _showImportDialog.value = false
    }

    /**
     * 导出条目（适配UI方法名）
     */
    fun exportEntries() {
        exportToFile()
    }

    /**
     * 显示编辑对话框
     */
    fun showEditDialog(entry: WorldBookEntry) {
        selectEntry(entry)
        _isEditMode.value = true
    }

    /**
     * 选择模式切换
     */
    fun toggleSelectionMode() {
        _isSelectionMode.value = !_isSelectionMode.value
        if (!_isSelectionMode.value) {
            clearSelection()
        }
    }

    /**
     * 选择/取消选择条目
     */
    fun toggleEntrySelection(entryId: String) {
        val currentSelection = _selectedEntries.value.toMutableSet()
        if (currentSelection.contains(entryId)) {
            currentSelection.remove(entryId)
        } else {
            currentSelection.add(entryId)
        }
        _selectedEntries.value = currentSelection
    }

    /**
     * 全选/取消全选
     */
    fun toggleSelectAll() {
        val currentFiltered = _filteredEntries.value.map { it.id }.toSet()
        if (_selectedEntries.value.size == currentFiltered.size) {
            clearSelection()
        } else {
            _selectedEntries.value = currentFiltered
        }
    }

    /**
     * 清除选择
     */
    fun clearSelection() {
        _selectedEntries.value = emptySet()
    }

    /**
     * 设置排序选项
     */
    fun setSortBy(sortOption: SortOption) {
        _sortBy.value = sortOption
    }

    /**
     * 设置过滤选项
     */
    fun setFilterOption(filterOption: FilterOption) {
        _filterOption.value = filterOption
    }

    /**
     * 测试条目匹配
     */
    suspend fun testMatching(
        input: String,
        conversationHistory: List<UIMessage>
    ): List<WorldBookMatcher.MatchedEntry> {
        val activeEntries = repository.getActiveWorldBookEntries(_assistantId.value)
        return matcher.matchEntries(input, conversationHistory, activeEntries)
    }

    /**
     * 测试上下文注入
     */
    suspend fun testInjection(
        input: String,
        conversationHistory: List<UIMessage>,
        config: WorldBookInjector.InjectionConfig = WorldBookInjector.InjectionConfig()
    ): List<UIMessage> {
        val matchedEntries = testMatching(input, conversationHistory)
        return injector.injectWorldBook(conversationHistory, matchedEntries, getCurrentAssistant(), config)
    }

    /**
     * 获取当前助手（简化版本）
     */
    private fun getCurrentAssistant() = me.rerere.rikkahub.data.model.Assistant(
        id = Uuid.parse(_assistantId.value),
        name = "Current Assistant"
    )

    /**
     * 检查标题是否重复
     */
    suspend fun isTitleDuplicate(title: String, excludeId: String? = null): Boolean {
        return repository.isTitleDuplicate(_assistantId.value, title, excludeId)
    }

    /**
     * 获取条目详情
     */
    suspend fun getEntryById(id: String): WorldBookEntry? {
        return repository.getWorldBookEntryById(id)
    }

    /**
     * 应用过滤和排序
     */
    private fun applyFiltersAndSorting(
        entries: List<WorldBookEntry>,
        query: String,
        sortBy: SortOption,
        filterOption: FilterOption
    ): List<WorldBookEntry> {
        var filtered = entries

        // 应用过滤
        filtered = when (filterOption) {
            FilterOption.ALL -> entries
            FilterOption.ENABLED -> entries.filter { it.isEnabled }
            FilterOption.DISABLED -> entries.filter { !it.isEnabled }
            FilterOption.CONSTANT -> entries.filter { it.isConstant }
            FilterOption.SELECTIVE -> entries.filter { it.isSelective }
            FilterOption.HIGH_PRIORITY -> entries.filter { it.priority > 5 }
        }

        // 应用搜索
        if (query.isNotBlank()) {
            filtered = filtered.filter { entry ->
                entry.title.contains(query, ignoreCase = true) ||
                entry.content.contains(query, ignoreCase = true) ||
                entry.comment.contains(query, ignoreCase = true) ||
                entry.keywords.any { it.contains(query, ignoreCase = true) }
            }
        }

        // 应用排序
        filtered = when (sortBy) {
            SortOption.TITLE -> filtered.sortedBy { it.title }
            SortOption.PRIORITY -> filtered.sortedByDescending { it.priority }
            SortOption.CREATED_AT -> filtered.sortedBy { it.createdAt }
            SortOption.UPDATED_AT -> filtered.sortedByDescending { it.updatedAt }
        }

        return filtered
    }

    /**
     * 排序选项
     */
    enum class SortOption {
        TITLE, PRIORITY, CREATED_AT, UPDATED_AT
    }

    /**
     * 过滤选项
     */
    enum class FilterOption {
        ALL, ENABLED, DISABLED, CONSTANT, SELECTIVE, HIGH_PRIORITY
    }

    // ==================== 导入导出功能 ====================

    private val _importExportStatus = MutableStateFlow<ImportExportStatus>(ImportExportStatus.Idle)
    val importExportStatus: StateFlow<ImportExportStatus> = _importExportStatus.asStateFlow()

    /**
     * 导出世界书到SillyTavern格式（简化版本）
     */
    fun exportToFile() {
        // 暂时简化，只显示占位消息
        viewModelScope.launch {
            _importExportStatus.value = ImportExportStatus.Exporting("导出功能开发中...")
            kotlinx.coroutines.delay(1000)
            _importExportStatus.value = ImportExportStatus.ExportSuccess("导出功能暂未实现")
        }
    }

    /**
     * 从SillyTavern格式导入世界书（简化版本）
     */
    fun importFromFile(jsonContent: String) {
        // 暂时简化，只显示占位消息
        viewModelScope.launch {
            _importExportStatus.value = ImportExportStatus.Importing("导入功能开发中...")
            kotlinx.coroutines.delay(1000)
            _importExportStatus.value = ImportExportStatus.ImportSuccess("导入功能暂未实现")
        }
    }

    /**
     * 重置导入导出状态
     */
    fun resetImportExportStatus() {
        _importExportStatus.value = ImportExportStatus.Idle
    }

    /**
     * 导入导出状态
     */
    sealed class ImportExportStatus {
        object Idle : ImportExportStatus()
        data class Importing(val message: String) : ImportExportStatus()
        data class ImportSuccess(val message: String) : ImportExportStatus()
        data class ImportError(val error: String) : ImportExportStatus()
        data class Exporting(val message: String) : ImportExportStatus()
        data class ExportSuccess(val data: String) : ImportExportStatus()
        data class ExportError(val error: String) : ImportExportStatus()
    }

    /**
     * SillyTavern格式世界书条目数据类
     */
    @kotlinx.serialization.Serializable
    data class SillyTavernWorldBookEntry(
        val uid: String,
        val name: String,
        val keys: List<String>,
        val content: String,
        val order: Int,
        val enabled: Boolean = true,
        val constant: Boolean = false,
        val selective: Boolean = false,
        val comment: String = ""
    )
}