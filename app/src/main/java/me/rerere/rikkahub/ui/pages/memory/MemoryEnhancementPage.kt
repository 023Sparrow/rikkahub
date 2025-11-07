package me.rerere.rikkahub.ui.pages.memory

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Trash2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import me.rerere.rikkahub.data.db.entity.*
import me.rerere.rikkahub.data.ai.memory.*
import me.rerere.rikkahub.ui.components.ui.FormItem
import me.rerere.rikkahub.ui.context.LocalToaster
import me.rerere.rikkahub.ui.viewmodel.MemoryEnhancementViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * 记忆增强主页面
 * 基于st-memory-enhancement插件的UI设计实现Android版本
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryEnhancementPage(
    assistantId: String,
    vm: MemoryEnhancementViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()
    val toaster = LocalToaster.current

    // 状态收集
    val sheets by vm.filteredSheets.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val searchQuery by vm.searchQuery.collectAsStateWithLifecycle()
    val statistics by vm.statistics.collectAsStateWithLifecycle()
    val showCreateDialog by vm.showCreateDialog.collectAsStateWithLifecycle()
    val showEditDialog by vm.showEditDialog.collectAsStateWithLifecycle()
    val showDeleteConfirmDialog by vm.showDeleteConfirmDialog.collectAsStateWithLifecycle()

    // 初始化助手ID
    LaunchedEffect(assistantId) {
        vm.setAssistantId(assistantId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 标题和统计信息
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "记忆增强系统",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = { vm.showCreateDialog() }
                    ) {
                        Icon(Lucide.Plus, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("新建表格")
                    }
                }

                // 统计信息
                statistics?.let { stats ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FormItem(
                            label = { Text("表格数") },
                            content = { Text(stats.totalSheets.toString()) }
                        )
                        FormItem(
                            label = { Text("总行数") },
                            content = { Text(stats.totalRows.toString()) }
                        )
                        FormItem(
                            label = { Text("总单元格") },
                            content = { Text(stats.totalCells.toString()) }
                        )
                        FormItem(
                            label = { Text("预估Token") },
                            content = { Text(stats.estimatedTokens.toString()) }
                        )
                    }
                }
            }
        }

        // 搜索栏
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = vm::updateSearchQuery,
                    placeholder = { Text("搜索记忆表格...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Lucide.Search, contentDescription = null)
                    }
                )
            }
        }

        // 表格列表
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (sheets.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "暂无记忆表格",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "点击上方\"新建表格\"按钮创建第一个记忆表格",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = sheets,
                    key = { it.id }
                ) { sheet ->
                    MemorySheetCard(
                        sheet = sheet,
                        onEdit = { vm.showEditDialog(sheet.id) },
                        onDelete = { vm.showDeleteConfirmDialog(sheet.id) },
                        onToggleEnabled = { vm.toggleSheetEnabled(it) },
                        onView = { /* TODO: 打开表格详情页 */ }
                    )
                }
            }
        }
    }

    // 创建表格对话框
    if (showCreateDialog) {
        MemorySheetCreateDialog(
            onDismiss = { vm.hideCreateDialog() },
            onConfirm = { name, description, sheetType, styleConfig, columns ->
                vm.createSheetWithStyle(name, description, sheetType, styleConfig, columns)
            }
        )
    }

    // 删除确认对话框
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { vm.hideDeleteConfirmDialog() },
            title = { Text("删除记忆表格") },
            text = { Text("确定要删除这个记忆表格吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = { vm.deleteSheet() }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.hideDeleteConfirmDialog() }) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 记忆表格卡片
 */
@Composable
private fun MemorySheetCard(
    sheet: MemorySheet,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit,
    onToggleEnabled: (String) -> Unit,
    onView: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onView(sheet.id) }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sheet.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    if (sheet.description.isNotBlank()) {
                        Text(
                            text = sheet.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = sheet.isEnabled,
                    onCheckedChange = { onToggleEnabled(sheet.id) }
                )
            }

            // 类型标签和统计
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 类型标签
                Surface(
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                    color = when (sheet.sheetType) {
                        SheetType.BASE -> MaterialTheme.colorScheme.primaryContainer
                        SheetType.DERIVED -> MaterialTheme.colorScheme.secondaryContainer
                        SheetType.USER -> MaterialTheme.colorScheme.tertiaryContainer
                        SheetType.APP -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = when (sheet.sheetType) {
                            SheetType.BASE -> "基础表格"
                            SheetType.DERIVED -> "派生表格"
                            SheetType.USER -> "用户表格"
                            SheetType.APP -> "系统表格"
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = when (sheet.sheetType) {
                            SheetType.BASE -> MaterialTheme.colorScheme.onPrimaryContainer
                            SheetType.DERIVED -> MaterialTheme.colorScheme.onSecondaryContainer
                            SheetType.USER -> MaterialTheme.colorScheme.onTertiaryContainer
                            SheetType.APP -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                // 操作按钮
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = { onEdit(sheet.id) }) {
                        Icon(Lucide.Pencil, contentDescription = "编辑")
                    }
                    IconButton(onClick = { onDelete(sheet.id) }) {
                        Icon(Lucide.Trash2, contentDescription = "删除")
                    }
                }
            }
        }
    }
}

/**
 * 记忆表格创建对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemorySheetCreateDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String, sheetType: SheetType, styleConfig: MemorySheetStyle, columns: List<MemoryColumn>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var sheetType by remember { mutableStateOf(SheetType.USER) }
    var showColumnDialog by remember { mutableStateOf(false) }

    // 样式配置
    var formatType by remember { mutableStateOf(MemoryFormatType.TABLE) }
    var showHeaders by remember { mutableStateOf(true) }
    var maxRows by remember { mutableStateOf(50) }
    var truncateContent by remember { mutableStateOf(true) }

    val columns = remember { mutableStateListOf<MemoryColumn>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("创建记忆表格")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 基本信息
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("表格名称 *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("表格描述") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 表格类型选择
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = { }
                ) {
                    OutlinedTextField(
                        value = when (sheetType) {
                            SheetType.BASE -> "基础表格"
                            SheetType.DERIVED -> "派生表格"
                            SheetType.USER -> "用户表格"
                            SheetType.APP -> "系统表格"
                        },
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("表格类型") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = false)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = false,
                        onDismissRequest = { }
                    ) {
                        SheetType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(when (type) {
                                    SheetType.BASE -> "基础表格"
                                    SheetType.DERIVED -> "派生表格"
                                    SheetType.USER -> "用户表格"
                                    SheetType.APP -> "系统表格"
                                }) },
                                onClick = { sheetType = type }
                            )
                        }
                    }
                }

                // 列定义
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "列定义 (${columns.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )

                    OutlinedButton(
                        onClick = { showColumnDialog = true }
                    ) {
                        Icon(Lucide.Plus, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("添加列")
                    }
                }

                // 显示已添加的列
                if (columns.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        columns.forEachIndexed { index, column ->
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${index + 1}. ${column.name}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "类型: ${column.columnType.name}${if (column.isRequired) " (必填)" else ""}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    IconButton(
                                        onClick = { columns.removeAt(index) }
                                    ) {
                                        Icon(
                                            Lucide.Trash2,
                                            contentDescription = "删除列",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 样式配置
                Text(
                    text = "样式配置",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )

                // 格式类型
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = { }
                ) {
                    OutlinedTextField(
                        value = when (formatType) {
                            MemoryFormatType.TABLE -> "表格格式"
                            MemoryFormatType.LIST -> "列表格式"
                            MemoryFormatType.JSON -> "JSON格式"
                            MemoryFormatType.CUSTOM -> "自定义模板"
                        },
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("显示格式") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = false)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                }

                // 最大行数
                OutlinedTextField(
                    value = maxRows.toString(),
                    onValueChange = { maxRows = it.toIntOrNull() ?: 50 },
                    label = { Text("最大显示行数") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // 开关选项
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = showHeaders,
                            onCheckedChange = { showHeaders = it }
                        )
                        Text("显示表头")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = truncateContent,
                            onCheckedChange = { truncateContent = it }
                        )
                        Text("截断长内容")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && columns.isNotEmpty()) {
                        val styleConfig = MemorySheetStyle(
                            formatType = formatType,
                            showHeaders = showHeaders,
                            maxRows = maxRows,
                            truncateContent = truncateContent
                        )
                        onConfirm(name, description, sheetType, styleConfig, columns.toList())
                    }
                },
                enabled = name.isNotBlank() && columns.isNotEmpty()
            ) {
                Text("创建")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        modifier = Modifier.height(600.dp)
    )
}