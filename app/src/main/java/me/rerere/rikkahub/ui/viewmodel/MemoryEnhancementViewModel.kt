package me.rerere.rikkahub.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.rerere.rikkahub.data.ai.memory.*
import me.rerere.rikkahub.data.db.entity.*
import me.rerere.rikkahub.ui.context.LocalToaster

/**
 * 记忆增强ViewModel
 * 管理记忆表格的UI状态和业务逻辑
 */
class MemoryEnhancementViewModel(
    private val memoryEnhancementService: MemoryEnhancementService,
    private val memoryInjector: MemoryInjector
) : ViewModel() {

    // 当前助手ID
    private val _assistantId = MutableStateFlow<String>("")
    val assistantId: StateFlow<String> = _assistantId.asStateFlow()

    // 记忆表格列表
    private val _sheets = MutableStateFlow<List<MemorySheet>>(emptyList())
    val sheets: StateFlow<List<MemorySheet>> = _sheets.asStateFlow()

    // 搜索查询
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // 过滤后的表格
    private val _filteredSheets = MutableStateFlow<List<MemorySheet>>(emptyList())
    val filteredSheets: StateFlow<List<MemorySheet>> = _filteredSheets.asStateFlow()

    // 当前选中的表格
    private val _selectedSheetId = MutableStateFlow<String?>(null)
    val selectedSheetId: StateFlow<String?> = _selectedSheetId.asStateFlow()

    // 当前表格数据
    private val _currentSheetData = MutableStateFlow<MemorySheetWithData?>(null)
    val currentSheetData: StateFlow<MemorySheetWithData?> = _currentSheetData.asStateFlow()

    // UI状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _showCreateDialog = MutableStateFlow(false)
    val showCreateDialog: StateFlow<Boolean> = _showCreateDialog.asStateFlow()

    private val _showEditDialog = MutableStateFlow(false)
    val showEditDialog: StateFlow<Boolean> = _showEditDialog.asStateFlow()

    private val _showDeleteConfirmDialog = MutableStateFlow(false)
    val showDeleteConfirmDialog: StateFlow<Boolean> = _showDeleteConfirmDialog.asStateFlow()

    // 统计信息
    private val _statistics = MutableStateFlow<MemoryStatistics?>(null)
    val statistics: StateFlow<MemoryStatistics?> = _statistics.asStateFlow()

    // 初始化助手ID
    fun setAssistantId(assistantId: String) {
        if (_assistantId.value != assistantId) {
            _assistantId.value = assistantId
            loadSheets()
            loadStatistics()
        }
    }

    // 加载记忆表格
    private fun loadSheets() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                memoryEnhancementService.getSheetsByAssistant(_assistantId.value)
                    .collect { sheetsList ->
                        _sheets.value = sheetsList
                        applySearchFilter()
                    }
            } catch (e: Exception) {
                // 错误处理
                _sheets.value = emptyList()
                _filteredSheets.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 加载统计信息
    private fun loadStatistics() {
        viewModelScope.launch {
            try {
                val stats = memoryInjector.getMemoryStatistics(_assistantId.value)
                _statistics.value = stats
            } catch (e: Exception) {
                _statistics.value = null
            }
        }
    }

    // 更新搜索查询
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        applySearchFilter()
    }

    // 应用搜索过滤
    private fun applySearchFilter() {
        val query = _searchQuery.value.trim()
        val allSheets = _sheets.value

        _filteredSheets.value = if (query.isEmpty()) {
            allSheets
        } else {
            allSheets.filter { sheet ->
                sheet.name.contains(query, ignoreCase = true) ||
                sheet.description.contains(query, ignoreCase = true)
            }
        }
    }

    // 显示创建对话框
    fun showCreateDialog() {
        _showCreateDialog.value = true
    }

    // 隐藏创建对话框
    fun hideCreateDialog() {
        _showCreateDialog.value = false
    }

    // 显示编辑对话框
    fun showEditDialog(sheetId: String) {
        _selectedSheetId.value = sheetId
        loadSheetData(sheetId)
        _showEditDialog.value = true
    }

    // 隐藏编辑对话框
    fun hideEditDialog() {
        _showEditDialog.value = false
        _selectedSheetId.value = null
        _currentSheetData.value = null
    }

    // 显示删除确认对话框
    fun showDeleteConfirmDialog(sheetId: String) {
        _selectedSheetId.value = sheetId
        _showDeleteConfirmDialog.value = true
    }

    // 隐藏删除确认对话框
    fun hideDeleteConfirmDialog() {
        _showDeleteConfirmDialog.value = false
        _selectedSheetId.value = null
    }

    // 加载表格数据
    private fun loadSheetData(sheetId: String) {
        viewModelScope.launch {
            try {
                _currentSheetData.value = memoryEnhancementService.getSheetWithData(sheetId)
            } catch (e: Exception) {
                _currentSheetData.value = null
            }
        }
    }

    // 创建新表格
    fun createSheet(
        name: String,
        description: String,
        sheetType: SheetType,
        columns: List<MemoryColumn>
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = memoryEnhancementService.createSheet(
                    assistantId = _assistantId.value,
                    name = name,
                    description = description,
                    sheetType = sheetType,
                    columns = columns
                )

                if (result.isSuccess) {
                    hideCreateDialog()
                    loadStatistics()
                } else {
                    // 错误处理
                }
            } catch (e: Exception) {
                // 错误处理
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 创建带样式的表格
    fun createSheetWithStyle(
        name: String,
        description: String,
        sheetType: SheetType,
        styleConfig: MemorySheetStyle,
        columns: List<MemoryColumn>
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = memoryEnhancementService.createSheetWithStyle(
                    assistantId = _assistantId.value,
                    name = name,
                    description = description,
                    sheetType = sheetType,
                    styleConfig = styleConfig,
                    columns = columns
                )

                if (result.isSuccess) {
                    hideCreateDialog()
                    loadStatistics()
                } else {
                    // 错误处理
                }
            } catch (e: Exception) {
                // 错误处理
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 切换表格启用状态
    fun toggleSheetEnabled(sheetId: String) {
        viewModelScope.launch {
            try {
                val result = memoryEnhancementService.toggleSheetEnabled(sheetId)
                if (result.isSuccess) {
                    loadStatistics()
                }
            } catch (e: Exception) {
                // 错误处理
            }
        }
    }

    // 删除表格
    fun deleteSheet() {
        val sheetId = _selectedSheetId.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = memoryEnhancementService.deleteSheet(sheetId)
                if (result.isSuccess) {
                    hideDeleteConfirmDialog()
                    loadStatistics()
                }
            } catch (e: Exception) {
                // 错误处理
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 添加新行
    fun addRow(sheetId: String, rowData: Map<String, String>) {
        viewModelScope.launch {
            try {
                val result = memoryEnhancementService.addRow(sheetId, rowData)
                if (result.isSuccess) {
                    loadSheetData(sheetId) // 刷新当前表格数据
                }
            } catch (e: Exception) {
                // 错误处理
            }
        }
    }

    // 更新单元格
    fun updateCell(
        sheetId: String,
        rowIndex: Int,
        columnName: String,
        content: String
    ) {
        viewModelScope.launch {
            try {
                val result = memoryEnhancementService.updateCell(
                    sheetId = sheetId,
                    rowIndex = rowIndex,
                    columnName = columnName,
                    content = content
                )
                if (result.isSuccess) {
                    loadSheetData(sheetId) // 刷新当前表格数据
                }
            } catch (e: Exception) {
                // 错误处理
            }
        }
    }

    // 删除行
    fun deleteRow(sheetId: String, rowIndex: Int) {
        viewModelScope.launch {
            try {
                val result = memoryEnhancementService.deleteRow(sheetId, rowIndex)
                if (result.isSuccess) {
                    loadSheetData(sheetId) // 刷新当前表格数据
                }
            } catch (e: Exception) {
                // 错误处理
            }
        }
    }

    // 搜索表格内容
    suspend fun searchInSheet(sheetId: String, query: String): List<MemoryCell> {
        return try {
            memoryEnhancementService.searchInSheet(sheetId, query)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // 获取表格统计信息
    suspend fun getSheetStatistics(sheetId: String): SheetStatistics? {
        return try {
            memoryEnhancementService.getSheetStatistics(sheetId)
        } catch (e: Exception) {
            null
        }
    }

    // 自动总结并填写表格
    fun autoSummarizeAndFill(
        sheetId: String,
        chatMessages: List<String>,
        summaryPrompt: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = memoryEnhancementService.autoSummarizeAndFill(
                    sheetId = sheetId,
                    chatMessages = chatMessages,
                    summaryPrompt = summaryPrompt
                )

                if (result.isSuccess) {
                    loadSheetData(sheetId) // 刷新表格数据
                }
            } catch (e: Exception) {
                // 错误处理
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 预览记忆注入内容
    suspend fun previewMemoryInjection(maxTokens: Int = 2000): String {
        return try {
            memoryEnhancementService.generateMemoryInjection(_assistantId.value, maxTokens)
        } catch (e: Exception) {
            ""
        }
    }

    // 刷新数据
    fun refresh() {
        loadSheets()
        loadStatistics()
        _selectedSheetId.value?.let { sheetId ->
            loadSheetData(sheetId)
        }
    }
}