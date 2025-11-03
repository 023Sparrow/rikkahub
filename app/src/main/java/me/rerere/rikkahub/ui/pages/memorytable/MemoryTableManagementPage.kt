package me.rerere.rikkahub.ui.pages.memorytable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Table
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import me.rerere.rikkahub.Screen
import me.rerere.rikkahub.data.db.entity.MemoryTable
import me.rerere.rikkahub.ui.components.nav.BackButton
import me.rerere.rikkahub.ui.context.LocalNavController
import me.rerere.rikkahub.ui.viewmodel.MemoryTableViewModel
import me.rerere.rikkahub.utils.showInputDialog
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryTableManagementPage(
    navController: NavController = LocalNavController.current,
    assistantId: String
) {
    val viewModel: MemoryTableViewModel = koinViewModel()
    val context = LocalContext.current

    // Set assistant ID when entering the page
    LaunchedEffect(assistantId) {
        viewModel.setAssistantId(assistantId)
    }

    val tables by viewModel.filteredTables.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var tableToDelete by remember { mutableStateOf<MemoryTable?>(null) }

    // Show input dialog for creating new table
    fun showCreateTableDialog() {
        context.showInputDialog(
            title = "Create New Table",
            hint = "Table name",
            onConfirm = { tableName ->
                if (tableName.isNotBlank()) {
                    // Show column configuration
                    context.showInputDialog(
                        title = "Add Columns",
                        hint = "Column 1, Column 2, Column 3",
                        onConfirm = { columnsText ->
                            val columns = columnsText.split(",")
                                .map { it.trim() }
                                .filter { it.isNotBlank() }
                            if (columns.isNotEmpty()) {
                                viewModel.viewModelScope.launch {
                                    val table = viewModel.createTable(
                                        name = tableName,
                                        description = "",
                                        columns = columns
                                    )
                                    // Navigate to editor
                                    navController.navigate(Screen.MemoryTableEditor(table.id))
                                }
                            }
                        }
                    )
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Memory Tables") },
                navigationIcon = { BackButton() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showCreateTableDialog()
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.updateSearchQuery(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search tables...") },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        Icons.Default.Table,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )

            // Table list or empty state
            if (tables.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Table,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No memory tables yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap the + button to create your first table",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(
                        items = tables,
                        key = { it.id }
                    ) { table ->
                        MemoryTableCard(
                            table = table,
                            onEdit = {
                                navController.navigate(Screen.MemoryTableEditor(table.id))
                            },
                            onDelete = {
                                tableToDelete = table
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }

        // Delete confirmation dialog
        if (showDeleteDialog && tableToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    tableToDelete = null
                },
                title = { Text("Delete Table") },
                text = {
                    Text(
                        "Are you sure you want to delete \"${tableToDelete!!.name}\"? This action will also delete all rows and cannot be undone."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            tableToDelete?.let {
                                viewModel.viewModelScope.launch {
                                    viewModel.deleteTable(it.id)
                                }
                            }
                            showDeleteDialog = false
                            tableToDelete = null
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            tableToDelete = null
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun MemoryTableCard(
    table: MemoryTable,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with name and actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = table.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            if (table.description.isNotBlank()) {
                Text(
                    text = table.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Column headers
            Text(
                text = "Columns: ${table.columnHeaders.joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
