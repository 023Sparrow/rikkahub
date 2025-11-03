package me.rerere.rikkahub.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.rerere.rikkahub.data.db.entity.MemoryTable
import me.rerere.rikkahub.data.db.entity.MemoryTableRow
import me.rerere.rikkahub.data.repository.MemoryTableRepository
import java.util.UUID

class MemoryTableViewModel(
    private val repository: MemoryTableRepository
) : ViewModel() {
    private val _tables = MutableStateFlow<List<MemoryTable>>(emptyList())
    val tables: StateFlow<List<MemoryTable>> = _tables.asStateFlow()

    private val _selectedTable = MutableStateFlow<MemoryTable?>(null)
    val selectedTable: StateFlow<MemoryTable?> = _selectedTable.asStateFlow()

    private val _tableRows = MutableStateFlow<List<MemoryTableRow>>(emptyList())
    val tableRows: StateFlow<List<MemoryTableRow>> = _tableRows.asStateFlow()

    private val _assistantId = MutableStateFlow("")
    val assistantId: StateFlow<String> = _assistantId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /**
     * 设置助手ID并加载该助手的所有表格
     */
    fun setAssistantId(assistantId: String) {
        _assistantId.value = assistantId
        viewModelScope.launch {
            repository.getMemoryTablesByAssistant(assistantId).collect { tables ->
                _tables.value = tables
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
     * 创建新表格
     */
    suspend fun createTable(
        name: String,
        description: String,
        columns: List<String>
    ): MemoryTable {
        val table = repository.addTable(
            assistantId = _assistantId.value,
            name = name,
            description = description,
            columnHeaders = columns
        )
        return table
    }

    /**
     * 删除表格
     */
    suspend fun deleteTable(id: String) {
        repository.deleteTable(id)
        // 如果删除的是当前选中的表格，清除选择
        _selectedTable.value?.let { selected ->
            if (selected.id == id) {
                _selectedTable.value = null
                _tableRows.value = emptyList()
            }
        }
    }

    /**
     * 选择表格并加载其行数据
     */
    fun selectTable(id: String) {
        viewModelScope.launch {
            val table = repository.getMemoryTableById(id)
            _selectedTable.value = table

            // 加载表格行数据
            table?.let {
                repository.getRowsByTableId(id).collect { rows ->
                    _tableRows.value = rows
                }
            }
        }
    }

    /**
     * 添加行
     */
    suspend fun addRow(
        tableId: String,
        rowData: Map<String, String>
    ): MemoryTableRow {
        val row = repository.addRow(
            tableId = tableId,
            rowData = rowData
        )
        return row
    }

    /**
     * 更新行
     */
    suspend fun updateRow(row: MemoryTableRow) {
        repository.updateRow(row)
    }

    /**
     * 删除行
     */
    suspend fun deleteRow(id: String) {
        repository.deleteRow(id)
    }

    /**
     * 更新表格信息（名称、描述等）
     */
    suspend fun updateTable(table: MemoryTable) {
        repository.updateTable(table)
    }

    /**
     * 重新排序行
     */
    suspend fun reorderRows(tableId: String, rows: List<MemoryTableRow>) {
        repository.reorderRows(tableId, rows)
    }

    /**
     * 获取表格ID
     */
    suspend fun getTableById(id: String): MemoryTable? {
        return repository.getMemoryTableById(id)
    }

    /**
     * 清空选择
     */
    fun clearSelection() {
        _selectedTable.value = null
        _tableRows.value = emptyList()
    }

    /**
     * 获取过滤后的表格列表（基于搜索查询）
     */
    val filteredTables: StateFlow<List<MemoryTable>> =
        combine(tables, searchQuery) { tables, query ->
            if (query.isBlank()) {
                tables
            } else {
                tables.filter { table ->
                    table.name.contains(query, ignoreCase = true) ||
                    table.description.contains(query, ignoreCase = true)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
