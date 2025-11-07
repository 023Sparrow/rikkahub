package me.rerere.rikkahub.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import me.rerere.rikkahub.data.repository.MemoryTableRepository
import me.rerere.rikkahub.data.db.entity.MemoryTable
import me.rerere.rikkahub.data.db.entity.MemoryTableRow
import me.rerere.rikkahub.data.db.entity.MemoryColumn
import me.rerere.rikkahub.data.db.entity.ColumnType
import me.rerere.rikkahub.service.MemoryTableInjector
import me.rerere.ai.ui.UIMessage
import kotlin.uuid.Uuid

/**
 * Memory Table ViewModel
 * 提供Memory Table的MVVM架构状态管理和业务逻辑处理
 */
class MemoryTableViewModel(
    private val repository: MemoryTableRepository,
    private val injector: MemoryTableInjector
) : ViewModel() {

    // 基础状态
    private val _assistantId = MutableStateFlow("")
    val assistantId: StateFlow<String> = _assistantId.asStateFlow()

    private val _tables = MutableStateFlow<List<MemoryTable>>(emptyList())
    val tables: StateFlow<List<MemoryTable>> = _tables.asStateFlow()

    // 搜索和过滤状态
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filteredTables = MutableStateFlow<List<MemoryTable>>(emptyList())
    val filteredTables: StateFlow<List<MemoryTable>> = _filteredTables.asStateFlow()

    // UI状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedTable = MutableStateFlow<MemoryTable?>(null)
    val selectedTable: StateFlow<MemoryTable?> = _selectedTable.asStateFlow()

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    // 表格行数据状态
    private val _tableRows = MutableStateFlow<List<MemoryTableRow>>(emptyList())
    val tableRows: StateFlow<List<MemoryTableRow>> = _tableRows.asStateFlow()

    // 统计信息
    private val _statistics = MutableStateFlow<Map<String, Int>>(emptyMap())
    val statistics: StateFlow<Map<String, Int>> = _statistics.asStateFlow()

    // 批量操作状态
    private val _selectedTables = MutableStateFlow<Set<String>>(emptySet())
    val selectedTables: StateFlow<Set<String>> = _selectedTables.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    // 排序和筛选状态
    private val _sortBy = MutableStateFlow(SortOption.NAME)
    val sortBy: StateFlow<SortOption> = _sortBy.asStateFlow()

    private val _filterOption = MutableStateFlow(FilterOption.ALL)
    val filterOption: StateFlow<FilterOption> = _filterOption.asStateFlow()

    init {
        // 设置搜索和过滤逻辑
        combine(
            _tables,
            _searchQuery,
            _sortBy,
            _filterOption
        ) { tables, query, sortBy, filterOption ->
            applyFiltersAndSorting(tables, query, sortBy, filterOption)
        }.onEach { filtered ->
            _filteredTables.value = filtered
        }.launchIn(viewModelScope)

        // 加载统计信息
        combine(_assistantId) { assistantId ->
            if (assistantId.isNotEmpty()) {
                loadStatistics(assistantId.first())
            }
        }.launchIn(viewModelScope)
    }

    /**
     * 设置当前助手ID并加载表格
     */
    fun setAssistantId(assistantId: String) {
        if (_assistantId.value != assistantId) {
            _assistantId.value = assistantId
            loadTables(assistantId)
        }
    }

    /**
     * 加载Memory Table表格
     */
    private fun loadTables(assistantId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getMemoryTablesByAssistant(assistantId).collect { tables ->
                    _tables.value = tables
                }
            } catch (e: Exception) {
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
            repository.getTableStatistics(assistantId).collect { stats ->
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
     * 添加新表格
     */
    fun addTable(
        name: String,
        description: String = "",
        columns: List<MemoryColumn>
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.addTableWithColumns(
                    assistantId = _assistantId.value,
                    name = name,
                    description = description,
                    columns = columns
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
     * 添加简单表格（兼容旧接口）
     */
    fun addSimpleTable(
        name: String,
        description: String = "",
        columnHeaders: List<String>
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.addTable(
                    assistantId = _assistantId.value,
                    name = name,
                    description = description,
                    columnHeaders = columnHeaders
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
     * 更新表格
     */
    fun updateTable(table: MemoryTable) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.updateTable(table)
                _isEditMode.value = false
                _selectedTable.value = null
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 删除表格
     */
    fun deleteTable(id: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.deleteTableWithRows(id)
                _selectedTable.value = null
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 批量删除选中表格
     */
    fun deleteSelectedTables() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _selectedTables.value.forEach { id ->
                    repository.deleteTableWithRows(id)
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
     * 切换表格启用状态
     */
    fun toggleTableEnabled(table: MemoryTable) {
        viewModelScope.launch {
            repository.updateTable(table.copy(isEnabled = !table.isEnabled))
        }
    }

    /**
     * 批量更新表格启用状态
     */
    fun updateTablesEnabledStatus(enabled: Boolean) {
        viewModelScope.launch {
            _selectedTables.value.forEach { tableId ->
                repository.getMemoryTableById(tableId)?.let { table ->
                    repository.updateTable(table.copy(isEnabled = enabled))
                }
            }
            clearSelection()
        }
    }

    /**
     * 选择表格进行编辑
     */
    fun selectTable(table: MemoryTable) {
        _selectedTable.value = table
        _isEditMode.value = true
        loadTableRows(table.id)
    }

    /**
     * 取消编辑
     */
    fun cancelEdit() {
        _selectedTable.value = null
        _isEditMode.value = false
        _tableRows.value = emptyList()
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
     * 加载表格行数据
     */
    fun loadTableRows(tableId: String) {
        viewModelScope.launch {
            repository.getRowsByTableId(tableId).collect { rows ->
                _tableRows.value = rows
            }
        }
    }

    /**
     * 添加行数据
     */
    fun addRow(rowData: Map<String, String>) {
        val tableId = _selectedTable.value?.id ?: return
        viewModelScope.launch {
            try {
                repository.addRow(tableId, rowData, _tableRows.value.size)
                // 更新行数统计
                repository.updateTableRowCount(tableId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 更新行数据
     */
    fun updateRow(row: MemoryTableRow) {
        viewModelScope.launch {
            try {
                repository.updateRow(row)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 删除行数据
     */
    fun deleteRow(id: String) {
        val tableId = _selectedTable.value?.id ?: return
        viewModelScope.launch {
            try {
                repository.deleteRow(id)
                // 更新行数统计
                repository.updateTableRowCount(tableId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 批量添加行数据
     */
    fun addRows(rowsData: List<Map<String, String>>) {
        val tableId = _selectedTable.value?.id ?: return
        viewModelScope.launch {
            try {
                repository.addRows(tableId, rowsData)
                // 重新加载行数据
                loadTableRows(tableId)
                // 更新行数统计
                repository.updateTableRowCount(tableId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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
     * 选择/取消选择表格
     */
    fun toggleTableSelection(tableId: String) {
        val currentSelection = _selectedTables.value.toMutableSet()
        if (currentSelection.contains(tableId)) {
            currentSelection.remove(tableId)
        } else {
            currentSelection.add(tableId)
        }
        _selectedTables.value = currentSelection
    }

    /**
     * 全选/取消全选
     */
    fun toggleSelectAll() {
        val currentFiltered = _filteredTables.value.map { it.id }.toSet()
        if (_selectedTables.value.size == currentFiltered.size) {
            clearSelection()
        } else {
            _selectedTables.value = currentFiltered
        }
    }

    /**
     * 清除选择
     */
    fun clearSelection() {
        _selectedTables.value = emptySet()
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
     * 测试表格注入
     */
    suspend fun testInjection(
        messages: List<UIMessage>,
        config: MemoryTableInjector.InjectionConfig = MemoryTableInjector.InjectionConfig()
    ): List<UIMessage> {
        val tableId = _selectedTable.value?.id ?: return messages
        val table = repository.getMemoryTableById(tableId) ?: return messages
        val rows = mutableListOf<MemoryTableRow>()
        repository.getRowsByTableId(tableId).collect { rows.addAll(it) }

        val tableWithContext = MemoryTableInjector.TableWithContext(
            table = table,
            rows = rows,
            relevanceScore = 1.0,
            matchedColumns = table.columns.map { it.name }
        )

        return injector.injectMemoryTables(messages, listOf(tableWithContext), config)
    }

    /**
     * 搜索相关表格
     */
    suspend fun searchRelevantTables(
        keywords: List<String>,
        maxTables: Int = 3
    ): List<MemoryTableInjector.TableWithContext> {
        return injector.searchRelevantTables(
            repository = repository,
            assistantId = _assistantId.value,
            keywords = keywords,
            maxTables = maxTables
        )
    }

    /**
     * 检查表格名称是否重复
     */
    suspend fun isTableNameDuplicate(name: String, excludeId: String? = null): Boolean {
        return repository.isTableNameDuplicate(_assistantId.value, name, excludeId)
    }

    /**
     * 获取表格详情
     */
    suspend fun getTableById(id: String): MemoryTable? {
        return repository.getMemoryTableById(id)
    }

    /**
     * 导出表格为CSV
     */
    suspend fun exportTableToCSV(tableId: String): String? {
        return repository.exportTableToCSV(tableId)
    }

    /**
     * 格式化表格为AI上下文
     */
    suspend fun formatTableForAI(tableId: String): String? {
        return repository.formatTableForAI(tableId)
    }

    /**
     * 按列类型过滤表格
     */
    suspend fun getTablesWithColumnType(columnType: ColumnType): List<MemoryTable> {
        return repository.getTablesWithColumnType(_assistantId.value, columnType)
    }

    /**
     * 在表格行中搜索
     */
    suspend fun searchInTableRows(tableId: String, searchText: String): List<MemoryTableRow> {
        return repository.searchInTableRows(tableId, searchText)
    }

    /**
     * 应用过滤和排序
     */
    private fun applyFiltersAndSorting(
        tables: List<MemoryTable>,
        query: String,
        sortBy: SortOption,
        filterOption: FilterOption
    ): List<MemoryTable> {
        var filtered = tables

        // 应用过滤
        filtered = when (filterOption) {
            FilterOption.ALL -> tables
            FilterOption.ENABLED -> tables.filter { it.isEnabled }
            FilterOption.DISABLED -> tables.filter { !it.isEnabled }
            FilterOption.WITH_DATA -> tables.filter { it.rowCount > 0 }
            FilterOption.EMPTY -> tables.filter { it.rowCount == 0 }
            FilterOption.TEXT_COLUMNS -> tables.filter { table ->
                table.columns.any { it.columnType == ColumnType.TEXT }
            }
            FilterOption.NUMBER_COLUMNS -> tables.filter { table ->
                table.columns.any { it.columnType == ColumnType.NUMBER }
            }
        }

        // 应用搜索
        if (query.isNotBlank()) {
            filtered = filtered.filter { table ->
                table.name.contains(query, ignoreCase = true) ||
                table.description.contains(query, ignoreCase = true) ||
                table.columns.any { it.name.contains(query, ignoreCase = true) }
            }
        }

        // 应用排序
        filtered = when (sortBy) {
            SortOption.NAME -> filtered.sortedBy { it.name }
            SortOption.ROW_COUNT -> filtered.sortedByDescending { it.rowCount }
            SortOption.CREATED_AT -> filtered.sortedBy { it.createdAt }
            SortOption.UPDATED_AT -> filtered.sortedByDescending { it.updatedAt }
            SortOption.COLUMN_COUNT -> filtered.sortedByDescending { it.columns.size }
        }

        return filtered
    }

    /**
     * 排序选项
     */
    enum class SortOption {
        NAME, ROW_COUNT, CREATED_AT, UPDATED_AT, COLUMN_COUNT
    }

    /**
     * 过滤选项
     */
    enum class FilterOption {
        ALL, ENABLED, DISABLED, WITH_DATA, EMPTY, TEXT_COLUMNS, NUMBER_COLUMNS
    }
}