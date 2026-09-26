package com.spellinggames.app.ui.screens.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spellinggames.app.data.local.SpellingListEntity
import com.spellinggames.app.data.local.WordEntity
import com.spellinggames.app.ui.components.EmptyState
import com.spellinggames.app.ui.theme.*

@Composable
fun ListsScreen(
    onOpenParent: () -> Unit,
    viewModel: ListsViewModel = hiltViewModel()
) {
    val lists by viewModel.lists.collectAsState()
    val wordCounts by viewModel.wordCounts.collectAsState()
    val activeId by viewModel.activeListId.collectAsState()
    val isUnlocked by viewModel.isParentUnlocked.collectAsState()
    val viewingListId by viewModel.viewingListId.collectAsState()
    val viewingWords by viewModel.viewingWords.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var editingList by remember { mutableStateOf<SpellingListEntity?>(null) }
    var editingWords by remember { mutableStateOf("") }

    LaunchedEffect(editingList) {
        editingList?.let {
            editingWords = viewModel.getWordsText(it.id)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF021024))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "My Lessons",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Row {
                    IconButton(onClick = onOpenParent) {
                        Icon(if (isUnlocked) Icons.Default.LockOpen else Icons.Default.Lock, contentDescription = "Parent", tint = Color.White)
                    }
                    if (isUnlocked) {
                        Button(
                            onClick = { showCreateDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B0FF))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("New")
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            if (lists.isEmpty()) {
                EmptyState(
                    emoji = "📝",
                    title = "No lessons yet",
                    message = "Create your first spelling lesson to get started. Parents can also paste a whole list of words at once.",
                    actionLabel = "Create Lesson",
                    onAction = { showCreateDialog = true }
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(lists, key = { it.id }) { list ->
                        ListCard(
                            list = list,
                            wordCount = wordCounts[list.id] ?: 0,
                            isActive = list.id == activeId,
                            onSelect = { viewModel.setActive(list.id) },
                            onEdit = { editingList = list },
                            onDuplicate = { viewModel.duplicate(list.id) },
                            onDelete = { viewModel.delete(list.id) },
                            onViewWords = { viewModel.setViewingList(list.id) },
                            isUnlocked = isUnlocked
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateEditListDialog(
            title = "New Spelling Lesson",
            initialName = "",
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, wordsText ->
                viewModel.createList(name, wordsText)
                showCreateDialog = false
            }
        )
    }

    editingList?.let { list ->
        CreateEditListDialog(
            title = "Edit Lesson",
            initialName = list.name,
            initialWords = editingWords,
            onDismiss = { editingList = null },
            onConfirm = { name, words ->
                viewModel.updateList(list.id, name, words)
                editingList = null
            },
            showWordsField = true
        )
    }

    if (viewingListId != null) {
        val currentList = lists.find { it.id == viewingListId }
        AlertDialog(
            onDismissRequest = { viewModel.setViewingList(null) },
            title = { Text(currentList?.name ?: "Lesson Details") },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                    if (viewingWords.isEmpty()) {
                        Text("No words in this lesson yet.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(viewingWords) { word ->
                                WordItem(word)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.setViewingList(null) }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun WordItem(word: WordEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(word.text, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            if (!word.definition.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(word.definition, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ListCard(
    list: SpellingListEntity,
    wordCount: Int,
    isActive: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onViewWords: () -> Unit,
    isUnlocked: Boolean
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(0xFF1E3A8A).copy(alpha = 0.4f)
            else Color.White.copy(alpha = 0.05f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 8.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).clickable { onSelect() }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        list.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (isActive) {
                        Spacer(Modifier.width(12.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF00E676)
                        ) {
                            Text(
                                "ACTIVE",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }
                Text(
                    "$wordCount words",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
            
            IconButton(onClick = onViewWords) {
                Icon(Icons.Default.Visibility, contentDescription = "View Words", tint = Color(0xFF00B0FF))
            }

            if (isUnlocked) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.White)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Edit Lesson") },
                            onClick = { showMenu = false; onEdit() },
                            leadingIcon = { Icon(Icons.Default.Edit, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Duplicate") },
                            onClick = { showMenu = false; onDuplicate() },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = { showMenu = false; onDelete() },
                            leadingIcon = { Icon(Icons.Default.Delete, null) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreateEditListDialog(
    title: String,
    initialName: String,
    initialWords: String = "",
    onDismiss: () -> Unit,
    onConfirm: (name: String, wordsText: String) -> Unit,
    showWordsField: Boolean = true
) {
    var name by remember { mutableStateOf(initialName) }
    var wordsText by remember { mutableStateOf(initialWords) }
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.verticalScroll(scrollState)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Lesson name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (showWordsField) {
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = wordsText,
                        onValueChange = { wordsText = it },
                        label = { Text("Words (one per line or comma-separated)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        placeholder = { Text("because\nfriend\nschool\n...") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name.trim(), wordsText) },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
