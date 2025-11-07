package me.rerere.rikkahub.ui.pages.memorytable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import me.rerere.rikkahub.data.db.entity.MemoryTable
import me.rerere.rikkahub.ui.components.nav.BackButton
import me.rerere.rikkahub.ui.context.LocalNavController
import me.rerere.rikkahub.ui.viewmodel.MemoryTableViewModel
import me.rerere.rikkahub.Screen
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Memory Table 主页面
 * 包含表格页面和设置页面的双页面设计
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryTablePage(
    assistantId: String,
    vm: MemoryTableViewModel = koinViewModel()
) {
    val navController = LocalNavController.current
    val scope = rememberCoroutineScope()

    // 状态收集
    val filteredTables by vm.filteredTables.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val searchQuery by vm.searchQuery.collectAsStateWithLifecycle()
    val statistics by vm.statistics.collectAsStateWithLifecycle()

    // UI状态
    val listState = rememberLazyListState()

    // 初始化助手ID
    LaunchedEffect(assistantId) {
        vm.setAssistantId(assistantId)
    }

    // 标签页配置
    val tabs = listOf("表格", "设置")
    val pagerState = rememberPagerState { tabs.size }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("记忆表格") },
                navigationIcon = {
                    BackButton()
                }
            )
        },
        floatingActionButton = {
            // 只在表格页面显示添加按钮
            if (pagerState.currentPage == 0) {
                FloatingActionButton(
                    onClick = vm::showAddDialog
                ) {
                    Text("+")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 标签页导航
            SecondaryScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier.fillMaxWidth(),
                edgePadding = 20.dp,
                minTabWidth = 20.dp,
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = index == pagerState.currentPage,
                        onClick = { scope.launch { pagerState.scrollToPage(index) } },
                        text = {
                            Text(tab)
                        }
                    )
                }
            }

            // 页面内容
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                when (page) {
                    0 -> {
                        // 表格页面
                        TableContent(
                            filteredTables = filteredTables,
                            isLoading = isLoading,
                            searchQuery = searchQuery,
                            statistics = statistics,
                            listState = listState,
                            onSearchQueryChange = vm::updateSearchQuery,
                            onTableClick = { vm.selectTable(it) },
                            onToggleEnabled = { vm.toggleTableEnabled(it) },
                            onAddTable = vm::showAddDialog
                        )
                    }
                    1 -> {
                        // 设置页面
                        MemoryTableSettingsPage(assistantId = assistantId)
                    }
                }
            }
        }
    }
}

/**
 * 表格内容页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TableContent(
    filteredTables: List<MemoryTable>,
    isLoading: Boolean,
    searchQuery: String,
    statistics: Map<String, Int>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onSearchQueryChange: (String) -> Unit,
    onTableClick: (MemoryTable) -> Unit,
    onToggleEnabled: (MemoryTable) -> Unit,
    onAddTable: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 搜索栏
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("搜索表格...") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }

        // 统计卡片
        StatisticsCard(
            statistics = statistics,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        // 表格列表
        if (filteredTables.isEmpty() && !isLoading) {
            EmptyState(
                searchQuery = searchQuery,
                onAddTable = onAddTable,
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
                    items = filteredTables,
                    key = { it.id }
                ) { table ->
                    MemoryTableCard(
                        table = table,
                        onClick = { onTableClick(table) },
                        onToggleEnabled = { onToggleEnabled(table) }
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
                StatisticItem("表格数", statistics["totalTables"] ?: 0)
                StatisticItem("启用", statistics["enabledTables"] ?: 0)
                StatisticItem("总行数", statistics["totalRows"] ?: 0)
                StatisticItem("平均行数", statistics["averageRowsPerTable"] ?: 0)
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
 * Memory Table表格卡片（简化版本）
 */
@Composable
private fun MemoryTableCard(
    table: MemoryTable,
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
                // 标题和描述
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                ) {
                    Text(
                        text = table.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (table.description.isNotEmpty()) {
                        Text(
                            text = table.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // 列信息
                    if (table.columns.isNotEmpty()) {
                        Text(
                            text = "列: ${table.columns.size} | ${table.columns.take(3).joinToString(", ") { it.name }}${if (table.columns.size > 3) "..." else ""}",
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
                    checked = table.isEnabled,
                    onCheckedChange = { onToggleEnabled() }
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
                // 数据统计
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${table.rowCount} 行",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Text(
                        text = "${table.columns.size} 列",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                // 时间信息
                Text(
                    text = formatDate(table.updatedAt),
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
    onAddTable: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (searchQuery.isBlank()) "暂无Memory Table表格" else "未找到匹配的表格",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (searchQuery.isBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "点击右下角的 + 按钮创建第一个表格",
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