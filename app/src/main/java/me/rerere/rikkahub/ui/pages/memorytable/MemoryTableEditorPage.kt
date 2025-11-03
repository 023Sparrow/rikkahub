package me.rerere.rikkahub.ui.pages.memorytable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import me.rerere.rikkahub.Screen
import me.rerere.rikkahub.data.db.entity.MemoryTable
import me.rerere.rikkahub.data.db.entity.MemoryTableRow
import me.rerere.rikkahub.ui.components.nav.BackButton
import me.rerere.rikkahub.ui.context.LocalNavController
import me.rerere.rikkahub.ui.viewmodel.MemoryTableViewModel
import me.rerere.rikkahub.utils.showInputDialog
import org.koin.androidx.compose.koinViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryTableEditorPage(
    navController: NavController = LocalNavController.current,
    tableId: String
) {
    val viewModel: MemoryTableViewModel = koinViewModel()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var table by remember { mutableStateOf<MemoryTable?>(null) }
    val rows by viewModel.tableRows.collectAsStateWithLifecycle()

    // Load table when entering the page
    LaunchedEffect(tableId) {
        viewModel.selectTable(tableId)
        table = viewModel.getTableById(tableId)
    }

    // Edit state for table name and description
    var isEditing by remember { mutableStateOf(false) }
    var tableName by remember { mutableStateOf("") }
    var tableDescription by remember { mutableStateOf("") }

    LaunchedEffect(table) {
        table?.let {
            tableName = it.name
            tableDescription = it.description
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(table?.name ?: "Memory Table") },
                navigationIcon = { BackButton() },
                actions = {
                    IconButton(
                        onClick = {
                            table?.let {
                                val updatedTable = it.copy(
                                    name = tableName,
                                    description = tableDescription
                                )
                                scope.launch {
                                    viewModel.updateTable(updatedTable)
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Done")
                    }
                    Button(
                        onClick = {
                            val newRow = table?.columnHeaders?.associateWith { "" }
                            if (newRow != null) {
                                scope.launch {
                                    viewModel.addRow(tableId, newRow)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Row")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Table info section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Table Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = tableName,
                        onValueChange = { tableName = it },
                        label = { Text("Table Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = tableDescription,
                        onValueChange = { tableDescription = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            // Table data section
            Text(
                text = "Table Data (${rows.size} rows)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Column headers
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp)
                ) {
                    table?.columnHeaders?.forEach { column ->
                        Text(
                            text = column,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Rows
            if (rows.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No rows yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = rows,
                        key = { it.id }
                    ) { row ->
                        MemoryTableRowEditor(
                            row = row,
                            columns = table?.columnHeaders ?: emptyList(),
                            onRowChanged = { updatedRow ->
                                scope.launch {
                                    viewModel.updateRow(updatedRow)
                                }
                            },
                            onRowDeleted = {
                                scope.launch {
                                    viewModel.deleteRow(row.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MemoryTableRowEditor(
    row: MemoryTableRow,
    columns: List<String>,
    onRowChanged: (MemoryTableRow) -> Unit,
    onRowDeleted: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            columns.forEach { column ->
                OutlinedTextField(
                    value = row.rowData[column] ?: "",
                    onValueChange = { newValue ->
                        val updatedData = row.rowData.toMutableMap().apply {
                            this[column] = newValue
                        }
                        onRowChanged(row.copy(rowData = updatedData))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onRowDeleted) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Row")
            }
        }
    }
}
