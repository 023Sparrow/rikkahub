package me.rerere.rikkahub.ui.pages.worldbook

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import me.rerere.rikkahub.Screen
import me.rerere.rikkahub.data.db.entity.WorldBookEntry
import me.rerere.rikkahub.ui.components.nav.BackButton
import me.rerere.rikkahub.ui.context.LocalNavController
import me.rerere.rikkahub.ui.viewmodel.WorldBookViewModel
import me.rerere.rikkahub.utils.showInputDialog
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldBookEditorPage(
    navController: NavController = LocalNavController.current,
    entryId: String? = null
) {
    val viewModel: WorldBookViewModel = koinViewModel()
    val context = LocalContext.current
    val isEditing = entryId != null

    // Form state
    var title by remember { mutableStateOf("") }
    var keywords by remember { mutableStateOf(mutableListOf<String>()) }
    var content by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var isConstant by remember { mutableStateOf(false) }
    var isSelective by remember { mutableStateOf(false) }
    var priority by remember { mutableStateOf(0) }
    var injectionPosition by remember { mutableStateOf(0) }
    var isEnabled by remember { mutableStateOf(true) }
    var excludeRecursion by remember { mutableStateOf(false) }
    var useRegex by remember { mutableStateOf(false) }
    var newKeyword by remember { mutableStateOf("") }

    // Load entry if editing
    LaunchedEffect(entryId) {
        if (isEditing && entryId != null) {
            viewModel.getEntryById(entryId)?.let { entry ->
                title = entry.title
                keywords = entry.keywords.toMutableList()
                content = entry.content
                comment = entry.comment
                isConstant = entry.isConstant
                isSelective = entry.isSelective
                priority = entry.priority
                injectionPosition = entry.injectionPosition
                isEnabled = entry.isEnabled
                excludeRecursion = entry.excludeRecursion
                useRegex = entry.useRegex
            }
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit World Book Entry" else "New World Book Entry") },
                navigationIcon = { BackButton() }
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
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                // TODO: Show validation error
                                return@Button
                            }
                            if (keywords.isEmpty()) {
                                // TODO: Show validation error
                                return@Button
                            }
                            if (content.isBlank()) {
                                // TODO: Show validation error
                                return@Button
                            }

                            if (isEditing && entryId != null) {
                                viewModel.updateEntry(
                                    WorldBookEntry(
                                        id = entryId,
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
                                )
                            } else {
                                viewModel.addEntry(
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
                            navController.popBackStack()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isEditing) "Save" : "Create")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = title.isBlank()
            )

            // Keywords
            Text(
                text = "Keywords *",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                keywords.forEach { keyword ->
                    AssistChip(
                        onClick = { /* Edit keyword */ },
                        label = { Text(keyword) },
                        trailingIcon = {
                            IconButton(
                                onClick = { keywords.remove(keyword) }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", modifier = Modifier.size(16.dp))
                            }
                        }
                    )
                }
                // Add keyword button
                IconButton(
                    onClick = {
                        context.showInputDialog(
                            title = "Add Keyword",
                            hint = "Enter keyword",
                            onConfirm = { kw ->
                                if (kw.isNotBlank()) {
                                    keywords.add(kw)
                                }
                            }
                        )
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Keyword", modifier = Modifier.size(20.dp))
                }
            }

            // Content
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                minLines = 5,
                maxLines = 10,
                isError = content.isBlank()
            )

            // Comment (optional)
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Comment (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Advanced Settings
            Text(
                text = "Advanced Settings",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Priority
            Text(
                text = "Priority: $priority",
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = priority.toFloat(),
                onValueChange = { priority = it.toInt() },
                valueRange = 0f..100f,
                modifier = Modifier.fillMaxWidth()
            )

            // Injection Position
            Text(
                text = "Injection Position: ${
                    when (injectionPosition) {
                        0 -> "Beginning"
                        1 -> "End"
                        else -> "Custom"
                    }
                }",
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = injectionPosition.toFloat(),
                onValueChange = { injectionPosition = it.toInt() },
                valueRange = 0f..2f,
                steps = 1,
                modifier = Modifier.fillMaxWidth()
            )

            // Toggle switches
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Constant Entry",
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = isConstant,
                    onCheckedChange = { isConstant = it }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Use Selective Matching",
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = isSelective,
                    onCheckedChange = { isSelective = it }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Use Regex",
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = useRegex,
                    onCheckedChange = { useRegex = it }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Exclude Recursion",
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = excludeRecursion,
                    onCheckedChange = { excludeRecursion = it }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Enabled",
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { isEnabled = it }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
