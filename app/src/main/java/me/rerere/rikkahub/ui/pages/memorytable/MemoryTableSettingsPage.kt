package me.rerere.rikkahub.ui.pages.memorytable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.Trash2
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dokar.sonner.ToastType
import kotlinx.coroutines.launch
import me.rerere.rikkahub.data.db.entity.MemoryTable
import me.rerere.rikkahub.data.db.entity.MemoryColumn
import me.rerere.rikkahub.data.db.entity.ColumnType
import me.rerere.rikkahub.ui.components.ui.FormItem
import me.rerere.rikkahub.ui.viewmodel.MemoryTableViewModel
import me.rerere.rikkahub.ui.context.LocalToaster
import org.koin.androidx.compose.koinViewModel
import kotlin.uuid.Uuid

/**
 * 记忆表格设置页面
 * 包含表格模板管理、自动总结配置等功能
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryTableSettingsPage(
    assistantId: String,
    vm: MemoryTableViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()
    val toaster = LocalToaster.current

    // 状态收集
    val tables by vm.filteredTables.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val searchQuery by vm.searchQuery.collectAsStateWithLifecycle()
    val statistics by vm.statistics.collectAsStateWithLifecycle()

    // 自动总结配置状态
    var autoSummaryInterval by remember { mutableStateOf(5) }
    var autoSummaryEnabled by remember { mutableStateOf(false) }
    var showTemplateDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var selectedTable by remember { mutableStateOf<MemoryTable?>(null) }

    // 填写表格配置状态
    var fillTablePrompt by remember { mutableStateOf("") }
    var summaryPrompt by remember { mutableStateOf("") }

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
        // 统计信息卡片
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "统计信息",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FormItem(
                        label = { Text("总表格") },
                        content = { Text(statistics["totalTables"]?.toString() ?: "0") }
                    )
                    FormItem(
                        label = { Text("总行数") },
                        content = { Text(statistics["totalRows"]?.toString() ?: "0") }
                    )
                    FormItem(
                        label = { Text("已启用") },
                        content = { Text(statistics["enabledTables"]?.toString() ?: "0") }
                    )
                }
            }
        }

        // 自动总结设置卡片
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "自动总结设置",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                // 启用自动总结开关
                FormItem(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("启用自动总结") },
                    description = { Text("每隔一定轮次的用户消息后，自动总结聊天记录并填写到表格") },
                    tail = {
                        Switch(
                            checked = autoSummaryEnabled,
                            onCheckedChange = { autoSummaryEnabled = it }
                        )
                    }
                )

                // 自动总结间隔设置
                if (autoSummaryEnabled) {
                    FormItem(
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("总结间隔") },
                        description = { Text("每隔多少轮用户消息进行一次自动总结") }
                    ) {
                        Column {
                            Slider(
                                value = autoSummaryInterval.toFloat(),
                                onValueChange = { autoSummaryInterval = it.toInt() },
                                valueRange = 1f..20f,
                                steps = 18,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "每 ${autoSummaryInterval} 轮用户消息总结一次",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Prompt 设置
                    FormItem(
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("总结 Prompt") },
                        description = { Text("用于自动总结聊天记录的提示词模板") }
                    ) {
                        OutlinedTextField(
                            value = summaryPrompt,
                            onValueChange = { summaryPrompt = it },
                            placeholder = { Text("请输入总结提示词...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            maxLines = 4
                        )
                    }
                }
            }
        }

        // 表格模板管理卡片
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
                        text = "表格模板管理",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Button(
                        onClick = { showTemplateDialog = true }
                    ) {
                        Text("添加模板")
                    }
                }

                if (tables.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无表格模板",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = tables,
                            key = { it.id }
                        ) { table ->
                            TableTemplateItem(
                                table = table,
                                onEdit = { /* TODO: 编辑模板 */ },
                                onDelete = {
                                    selectedTable = table
                                    showDeleteConfirmDialog = true
                                },
                                onToggleEnabled = { vm.toggleTableEnabled(it) }
                            )
                        }
                    }
                }
            }
        }

        // 填写表格设置卡片
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "填写表格设置",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                FormItem(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("填写 Prompt") },
                    description = { Text("用于手动填写表格的提示词模板") }
                ) {
                    OutlinedTextField(
                        value = fillTablePrompt,
                        onValueChange = { fillTablePrompt = it },
                        placeholder = { Text("请输入填写提示词...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        maxLines = 4
                    )
                }
            }
        }
    }

    // 添加模板对话框
    if (showTemplateDialog) {
        TableTemplateDialog(
            onDismiss = { showTemplateDialog = false },
            onConfirm = { name, description, columns ->
                scope.launch {
                    try {
                        vm.addTable(
                            name = name,
                            description = description,
                            columns = columns
                        )
                        toaster.show("表格模板创建成功")
                        showTemplateDialog = false
                    } catch (e: Exception) {
                        toaster.show("创建失败: ${e.message}", type = ToastType.Error)
                    }
                }
            }
        )
    }

    // 删除确认对话框
    if (showDeleteConfirmDialog && selectedTable != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmDialog = false
                selectedTable = null
            },
            title = { Text("删除表格") },
            text = { Text("确定要删除表格「${selectedTable!!.name}」吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.deleteTable(selectedTable!!.id)
                        showDeleteConfirmDialog = false
                        selectedTable = null
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteConfirmDialog = false
                    selectedTable = null
                }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun TableTemplateItem(
    table: MemoryTable,
    onEdit: (MemoryTable) -> Unit,
    onDelete: (MemoryTable) -> Unit,
    onToggleEnabled: (MemoryTable) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = table.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                Switch(
                    checked = table.isEnabled,
                    onCheckedChange = { onToggleEnabled(table) }
                )
            }

            // 列信息
            if (table.columns.isNotEmpty()) {
                Text(
                    text = "列数: ${table.columns.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "列名: ${table.columns.joinToString(", ") { it.name }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 2
                )
            }

            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onEdit(table) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("编辑")
                }

                OutlinedButton(
                    onClick = { onDelete(table) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("删除")
                }
            }
        }
    }
}

/**
 * 表格模板创建对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TableTemplateDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String, columns: List<MemoryColumn>) -> Unit
) {
    var tableName by remember { mutableStateOf("") }
    var tableDescription by remember { mutableStateOf("") }
    var columnName by remember { mutableStateOf("") }
    var columnType by remember { mutableStateOf(ColumnType.TEXT) }
    var columnDescription by remember { mutableStateOf("") }
    var columnRequired by remember { mutableStateOf(false) }
    var columnDefaultValue by remember { mutableStateOf("") }

    val columns = remember { mutableStateListOf<MemoryColumn>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("创建表格模板")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 表格基本信息
                Text(
                    text = "表格信息",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )

                OutlinedTextField(
                    value = tableName,
                    onValueChange = { tableName = it },
                    label = { Text("表格名称 *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = tableDescription,
                    onValueChange = { tableDescription = it },
                    label = { Text("表格描述") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 列信息
                Text(
                    text = "列定义",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )

                OutlinedTextField(
                    value = columnName,
                    onValueChange = { columnName = it },
                    label = { Text("列名称 *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 列类型选择
                OutlinedTextField(
                    value = columnType.name,
                    onValueChange = { },
                    label = { Text("列类型") },
                    readOnly = true,
                    trailingIcon = {
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            IconButton(onClick = { expanded = true }) {
                                Icon(
                                    imageVector = Lucide.ChevronDown,
                                    contentDescription = "选择类型"
                                )
                            }
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                ColumnType.values().forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type.name) },
                                        onClick = {
                                            columnType = type
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = columnDescription,
                    onValueChange = { columnDescription = it },
                    label = { Text("列描述") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = columnRequired,
                        onCheckedChange = { columnRequired = it }
                    )
                    Text("必填列")
                }

                OutlinedTextField(
                    value = columnDefaultValue,
                    onValueChange = { columnDefaultValue = it },
                    label = { Text("默认值") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 添加列按钮
                Button(
                    onClick = {
                        if (columnName.isNotBlank()) {
                            columns.add(
                                MemoryColumn(
                                    id = java.util.UUID.randomUUID().toString(),
                                    sheetId = "", // 临时值，将在创建表格后设置
                                    name = columnName,
                                    columnType = columnType,
                                    description = columnDescription,
                                    isRequired = columnRequired,
                                    defaultValue = columnDefaultValue.ifBlank { null }
                                )
                            )
                            // 清空输入
                            columnName = ""
                            columnDescription = ""
                            columnRequired = false
                            columnDefaultValue = ""
                        }
                    },
                    enabled = columnName.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("添加列")
                }

                // 显示已添加的列
                if (columns.isNotEmpty()) {
                    Text(
                        text = "已添加的列 (${columns.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )

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
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
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
                                        if (column.description.isNotBlank()) {
                                            Text(
                                                text = column.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = { columns.removeAt(index) }
                                    ) {
                                        Icon(
                                            imageVector = Lucide.Trash2,
                                            contentDescription = "删除列",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (tableName.isNotBlank() && columns.isNotEmpty()) {
                        onConfirm(tableName, tableDescription, columns.toList())
                    }
                },
                enabled = tableName.isNotBlank() && columns.isNotEmpty()
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