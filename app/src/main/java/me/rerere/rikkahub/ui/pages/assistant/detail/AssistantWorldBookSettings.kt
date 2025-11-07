package me.rerere.rikkahub.ui.pages.assistant.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dokar.sonner.ToastType
import kotlinx.coroutines.launch
import me.rerere.rikkahub.ui.context.LocalToaster
import me.rerere.rikkahub.data.db.entity.WorldBookEntry
import me.rerere.rikkahub.ui.viewmodel.WorldBookViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * 助手详情页中的世界书设置标签页（简化版本）
 * 提供世界书条目的管理功能，包括查看、添加、编辑、删除
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantWorldBookSettings(
    assistantId: String,
    vm: WorldBookViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()
    val toaster = LocalToaster.current

    // 状态收集
    val filteredEntries by vm.filteredEntries.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val searchQuery by vm.searchQuery.collectAsStateWithLifecycle()
    val showAddDialog by vm.showAddDialog.collectAsStateWithLifecycle()
    val showImportDialog by vm.showImportDialog.collectAsStateWithLifecycle()
    val isEditMode by vm.isEditMode.collectAsStateWithLifecycle()
    val selectedEntry by vm.selectedEntry.collectAsStateWithLifecycle()
    val importExportStatus by vm.importExportStatus.collectAsStateWithLifecycle()

    // UI状态
    val listState = rememberLazyListState()

    // 监听导入导出状态变化
    LaunchedEffect(importExportStatus) {
        when (val status = importExportStatus) {
            is WorldBookViewModel.ImportExportStatus.ImportSuccess -> {
                toaster.show("导入成功: ${status.message}", type = ToastType.Success)
                vm.hideImportDialog()
                vm.resetImportExportStatus()
            }
            is WorldBookViewModel.ImportExportStatus.ImportError -> {
                toaster.show("导入失败: ${status.error}", type = ToastType.Error)
                vm.resetImportExportStatus()
            }
            is WorldBookViewModel.ImportExportStatus.ExportSuccess -> {
                toaster.show("导出成功", type = ToastType.Success)
                vm.resetImportExportStatus()
            }
            is WorldBookViewModel.ImportExportStatus.ExportError -> {
                toaster.show("导出失败: ${status.error}", type = ToastType.Error)
                vm.resetImportExportStatus()
            }
            else -> {}
        }
    }

    // 初始化助手ID
    LaunchedEffect(assistantId) {
        vm.setAssistantId(assistantId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 搜索区域
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "世界书管理",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                // 搜索栏
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = vm::updateSearchQuery,
                    placeholder = { Text("搜索世界书条目...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 操作按钮区域
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { vm.showAddDialog() },
                modifier = Modifier.weight(1f)
            ) {
                Text("添加条目")
            }

            OutlinedButton(
                onClick = { vm.showImportDialog() },
                modifier = Modifier.weight(1f)
            ) {
                Text("导入")
            }

            OutlinedButton(
                onClick = { vm.exportEntries() },
                modifier = Modifier.weight(1f)
            ) {
                Text("导出")
            }
        }

        // 条目列表
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (filteredEntries.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "暂无世界书条目",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "点击上方\"添加条目\"按钮创建第一个世界书条目",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = filteredEntries,
                        key = { it.id }
                    ) { entry ->
                        WorldBookEntryItem(
                            entry = entry,
                            onEdit = { vm.showEditDialog(it) },
                            onDelete = { vm.deleteEntry(it) },
                            onToggleEnabled = { vm.toggleEntryEnabled(it) }
                        )
                    }
                }
            }
        }
    }

    // TODO: 对话框功能暂时禁用，等待后续修复
    /*
    // 添加条目对话框
    if (showAddDialog.value) {
        WorldBookEntryDialog(
            entry = null,
            onDismiss = { vm.hideAddDialog() },
            onConfirm = { title, keywords, content, comment, isConstant, isSelective, priority, injectionPosition, isEnabled, excludeRecursion, useRegex ->
                vm.addEntry(
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
        )
    }

    // 导入对话框
    if (showImportDialog.value) {
        WorldBookImportDialog(
            onDismiss = { vm.hideImportDialog() },
            onImport = { jsonContent ->
                vm.importFromFile(jsonContent)
            }
        )
    }

    // 编辑条目对话框 - 临时简化
    if (isEditMode.value && selectedEntry.value != null) {
        // TODO: 恢复完整的编辑对话框功能
        WorldBookEntryDialog(
            entry = selectedEntry.value,
            onDismiss = { vm.cancelEdit() },
            onConfirm = { title, keywords, content, comment, isConstant, isSelective, priority, injectionPosition, isEnabled, excludeRecursion, useRegex ->
                // 临时简化实现
            }
        )
    }
    */
}

@Composable
private fun WorldBookEntryItem(
    entry: WorldBookEntry,
    onEdit: (WorldBookEntry) -> Unit,
    onDelete: (WorldBookEntry) -> Unit,
    onToggleEnabled: (WorldBookEntry) -> Unit
) {
    val dateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onEdit(entry) }
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
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // 启用状态开关
                Switch(
                    checked = entry.isEnabled,
                    onCheckedChange = { onToggleEnabled(entry) }
                )
            }
        }
    }
}

/**
 * 世界书导入对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorldBookImportDialog(
    onDismiss: () -> Unit,
    onImport: (String) -> Unit
) {
    var jsonContent by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("导入世界书")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "请粘贴SillyTavern格式的世界书JSON数据：",
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = jsonContent,
                    onValueChange = { jsonContent = it },
                    label = { Text("JSON数据") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    placeholder = { Text("粘贴JSON格式的世界书数据...") }
                )

                Text(
                    text = "注意：导入的数据将添加到当前助手的世界书中。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (jsonContent.isNotBlank()) {
                        onImport(jsonContent)
                    }
                }
            ) {
                Text("导入")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 世界书条目添加/编辑对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorldBookEntryDialog(
    entry: WorldBookEntry? = null,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        keywords: List<String>,
        content: String,
        comment: String,
        isConstant: Boolean,
        isSelective: Boolean,
        priority: Int,
        injectionPosition: Int,
        isEnabled: Boolean,
        excludeRecursion: Boolean,
        useRegex: Boolean
    ) -> Unit
) {
    var title by remember { mutableStateOf(entry?.title ?: "") }
    var keywords by remember { mutableStateOf(entry?.keywords?.joinToString(", ") ?: "") }
    var content by remember { mutableStateOf(entry?.content ?: "") }
    var comment by remember { mutableStateOf(entry?.comment ?: "") }
    var isConstant by remember { mutableStateOf(entry?.isConstant ?: false) }
    var isSelective by remember { mutableStateOf(entry?.isSelective ?: false) }
    var priority by remember { mutableStateOf(entry?.priority ?: 0) }
    var injectionPosition by remember { mutableStateOf(entry?.injectionPosition ?: 0) }
    var isEnabled by remember { mutableStateOf(entry?.isEnabled ?: true) }
    var excludeRecursion by remember { mutableStateOf(entry?.excludeRecursion ?: false) }
    var useRegex by remember { mutableStateOf(entry?.useRegex ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (entry == null) "添加世界书条目" else "编辑世界书条目")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 标题
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("标题 *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 关键词
                OutlinedTextField(
                    value = keywords,
                    onValueChange = { keywords = it },
                    label = { Text("关键词 (用逗号分隔)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("例如: 角色,设定,背景") }
                )

                // 内容
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("内容 *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = { Text("输入世界书条目的详细内容...") }
                )

                // 备注
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("备注") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 优先级
                OutlinedTextField(
                    value = priority.toString(),
                    onValueChange = {
                        priority = it.toIntOrNull() ?: 0
                    },
                    label = { Text("优先级") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 注入位置
                OutlinedTextField(
                    value = injectionPosition.toString(),
                    onValueChange = {
                        injectionPosition = it.toIntOrNull() ?: 0
                    },
                    label = { Text("注入位置") },
                    singleLine = true,
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
                            checked = isConstant,
                            onCheckedChange = { isConstant = it }
                        )
                        Text("常驻")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isSelective,
                            onCheckedChange = { isSelective = it }
                        )
                        Text("选择性")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isEnabled,
                            onCheckedChange = { isEnabled = it }
                        )
                        Text("启用")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = excludeRecursion,
                            onCheckedChange = { excludeRecursion = it }
                        )
                        Text("排除递归")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = useRegex,
                            onCheckedChange = { useRegex = it }
                        )
                        Text("使用正则")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val keywordList = keywords.split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }

                    if (title.isNotBlank() && content.isNotBlank()) {
                        onConfirm(
                            title,
                            keywordList,
                            content,
                            comment,
                            isConstant,
                            isSelective,
                            priority,
                            injectionPosition,
                            isEnabled,
                            excludeRecursion,
                            useRegex
                        )
                    }
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}