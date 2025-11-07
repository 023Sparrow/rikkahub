package me.rerere.rikkahub.ui.pages.worldbook

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
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
import me.rerere.rikkahub.data.db.entity.WorldBookEntry
import me.rerere.rikkahub.ui.components.nav.BackButton
import me.rerere.rikkahub.ui.context.LocalNavController
import me.rerere.rikkahub.ui.viewmodel.WorldBookViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * World Book 主页面（简化版本）
 * 提供World Book条目的列表、搜索、编辑和管理功能
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldBookPage(
    assistantId: String,
    vm: WorldBookViewModel = koinViewModel()
) {
    val navController = LocalNavController.current

    // 状态收集
    val filteredEntries by vm.filteredEntries.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val searchQuery by vm.searchQuery.collectAsStateWithLifecycle()
    val statistics by vm.statistics.collectAsStateWithLifecycle()

    // UI状态
    val listState = rememberLazyListState()

    // 初始化助手ID
    LaunchedEffect(assistantId) {
        vm.setAssistantId(assistantId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("世界书") },
                navigationIcon = {
                    BackButton()
                },
                actions = {
                    // 搜索栏
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = vm::updateSearchQuery,
                        placeholder = { Text("搜索条目...") },
                        singleLine = true,
                        modifier = Modifier
                            .width(200.dp)
                            .padding(end = 8.dp)
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = vm::showAddDialog
            ) {
                Text("+")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 统计卡片
            StatisticsCard(
                statistics = statistics,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // 条目列表
            if (filteredEntries.isEmpty() && !isLoading) {
                EmptyState(
                    searchQuery = searchQuery,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 88.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = filteredEntries,
                        key = { it.id }
                    ) { entry ->
                        WorldBookEntryCard(
                            entry = entry,
                            onClick = { vm.selectEntry(entry) },
                            onToggleEnabled = { vm.toggleEntryEnabled(entry) }
                        )
                    }
                }
            }

            // 加载指示器
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        }
    }
}

/**
 * 统计卡片
 */
@Composable
private fun StatisticsCard(
    statistics: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "统计信息",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticItem("总数", statistics["total"] ?: 0)
                StatisticItem("启用", statistics["enabled"] ?: 0)
                StatisticItem("禁用", statistics["disabled"] ?: 0)
                StatisticItem("常驻", statistics["constant"] ?: 0)
            }
        }
    }
}

/**
 * 统计项
 */
@Composable
private fun StatisticItem(
    label: String,
    count: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * World Book条目卡片（简化版本）
 */
@Composable
private fun WorldBookEntryCard(
    entry: WorldBookEntry,
    onClick: () -> Unit,
    onToggleEnabled: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 标题和内容
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                ) {
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (entry.comment.isNotEmpty()) {
                        Text(
                            text = entry.comment,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // 关键词
                    if (entry.keywords.isNotEmpty()) {
                        Text(
                            text = "关键词: ${entry.keywords.take(3).joinToString(", ")}${if (entry.keywords.size > 3) "..." else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // 启用开关
                Switch(
                    checked = entry.isEnabled,
                    onCheckedChange = { onToggleEnabled() }
                )
            }

            // 内容预览
            if (entry.content.isNotEmpty()) {
                Text(
                    text = entry.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // 底部信息
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 特殊标记
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (entry.priority > 0) {
                        Text(
                            text = "优先级: ${entry.priority}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    if (entry.isConstant) {
                        Text(
                            text = "常驻",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (entry.isSelective) {
                        Text(
                            text = "选择性",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                // 时间信息
                Text(
                    text = formatDate(entry.updatedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 空状态页面
 */
@Composable
private fun EmptyState(
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (searchQuery.isBlank()) "暂无World Book条目" else "未找到匹配的条目",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (searchQuery.isBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "点击右下角的 + 按钮添加第一个条目",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 格式化日期
 */
private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}