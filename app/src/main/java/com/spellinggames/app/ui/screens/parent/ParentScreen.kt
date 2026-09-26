package com.spellinggames.app.ui.screens.parent

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spellinggames.app.data.local.SpellingListEntity
import com.spellinggames.app.data.local.WordEntity
import com.spellinggames.app.ui.theme.DeepBlue

// Aesthetic Colors for Parent Portal
private val PortalBackground = Color(0xFF08162E)
private val PortalCard = Color(0xFF0D234A)
private val PortalAction = Color(0xFF1E88E5)
private val PortalTextSecondary = Color(0xFF94A3B8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentScreen(
    onBack: () -> Unit,
    viewModel: ParentViewModel = hiltViewModel()
) {
    val isUnlocked by viewModel.isUnlocked.collectAsState()
    val hasPin by viewModel.hasPin.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val lists by viewModel.lists.collectAsState()
    val selectedListId by viewModel.selectedListId.collectAsState()
    val overallDifficulty by viewModel.overallDifficulty.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()
    val activeLessonId by viewModel.activeLessonId.collectAsState()
    val words by viewModel.words.collectAsState()
    val isReorderMode by viewModel.isReorderMode.collectAsState()

    var pinInput by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    
    var isLocking by remember { mutableStateOf(false) }

    var showAddWords by remember { mutableStateOf(false) }
    var showAddVocabulary by remember { mutableStateOf(false) }
    var showCreateLesson by remember { mutableStateOf(false) }
    var newLessonName by remember { mutableStateOf("") }
    
    var wordsToAdd by remember { mutableStateOf("") }
    var vocabWord by remember { mutableStateOf("") }
    var vocabDef by remember { mutableStateOf("") }
    
    var showResetAllConfirm by remember { mutableStateOf(false) }
    var lessonToDelete by remember { mutableStateOf<SpellingListEntity?>(null) }
    var lessonToReset by remember { mutableStateOf<SpellingListEntity?>(null) }
    
    var editingWord by remember { mutableStateOf<WordEntity?>(null) }
    var editWordText by remember { mutableStateOf("") }
    var editWordDef by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(PortalBackground)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                if (!isUnlocked && !isLocking) {
                    TopAppBar(
                        title = { Text("Parent Area", color = Color.White) },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = PortalBackground)
                    )
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (!hasPin) {
                    SetupPinSection(newPin, confirmPin, onNewPinChange = { newPin = it }, onConfirmPinChange = { confirmPin = it }, onSave = {
                        viewModel.setPin(newPin)
                        viewModel.unlock(newPin)
                    })
                } else if (!isUnlocked && !isLocking) {
                    UnlockSection(pinInput, onPinChange = { pinInput = it }, onUnlock = { viewModel.unlock(pinInput) })
                } else if (isUnlocked) {
                    // Two-page content
                    Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                        when (currentPage) {
                            ParentViewModel.ParentPage.Overview -> {
                                ParentOverviewPage(
                                    stats = stats,
                                    difficulty = overallDifficulty,
                                    onDifficultyChange = { viewModel.setOverallDifficulty(it) },
                                    lessons = lists,
                                    activeLessonId = activeLessonId,
                                    onActivate = { viewModel.activateLesson(it) },
                                    onEdit = { viewModel.navigateToLessonDetail(it) },
                                    onDelete = { lessonToDelete = it },
                                    onResetLesson = { lessonToReset = it },
                                    onCreateNew = { 
                                        newLessonName = ""
                                        showCreateLesson = true 
                                    },
                                    onResetAll = { showResetAllConfirm = true },
                                    isReorderMode = isReorderMode,
                                    onToggleReorder = { viewModel.toggleReorderMode() },
                                    onMove = { id, dir -> viewModel.moveLesson(id, dir) }
                                )
                            }
                            ParentViewModel.ParentPage.LessonDetail -> {
                                val currentLesson = lists.find { it.id == selectedListId }
                                LessonDetailPage(
                                    lesson = currentLesson,
                                    words = words,
                                    onBack = { viewModel.navigateToOverview() },
                                    onRename = { viewModel.updateLessonName(currentLesson?.id ?: 0, it) },
                                    onAddWords = { showAddWords = true },
                                    onAddVocab = { showAddVocabulary = true },
                                    onEditWord = { 
                                        editingWord = it
                                        editWordText = it.text
                                        editWordDef = it.definition ?: ""
                                    },
                                    onDeleteWord = { viewModel.deleteWord(it) }
                                )
                            }
                        }
                    }

                    // Bottom Fixed Lock Button
                    Button(
                        onClick = { 
                            isLocking = true
                            viewModel.lock()
                            onBack()
                        },
                        modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00B0FF), // Extremely Bright Cyan/Blue
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 8.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("LOCK PARENT AREA", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    }
                }
            }
        }

        // Dialogs
        if (showCreateLesson) {
            AlertDialog(
                onDismissRequest = { showCreateLesson = false },
                title = { Text("New Lesson") },
                text = {
                    OutlinedTextField(
                        value = newLessonName,
                        onValueChange = { newLessonName = it },
                        label = { Text("Lesson Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newLessonName.isNotBlank()) {
                                viewModel.createAndNavigateToNewLesson(newLessonName)
                                newLessonName = ""
                                showCreateLesson = false
                            }
                        },
                        enabled = newLessonName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PortalAction,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) { Text("Create") }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateLesson = false }) { Text("Cancel") }
                }
            )
        }

        if (showAddWords) {
            AddWordsDialog(wordsToAdd, onWordsChange = { wordsToAdd = it }, onDismiss = { showAddWords = false }, onConfirm = {
                viewModel.addWords(it)
                wordsToAdd = ""
                showAddWords = false
            })
        }

        if (showAddVocabulary) {
            AddVocabDialog(vocabWord, vocabDef, onWordChange = { vocabWord = it }, onDefChange = { vocabDef = it }, onDismiss = { showAddVocabulary = false }, onConfirm = { w, d ->
                viewModel.addVocabulary(w, d)
                vocabWord = ""
                vocabDef = ""
                showAddVocabulary = false
            })
        }

        if (editingWord != null) {
            AlertDialog(
                onDismissRequest = { editingWord = null },
                title = { Text("Edit Item") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = editWordText,
                            onValueChange = { editWordText = it },
                            label = { Text("Word") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = editWordDef,
                            onValueChange = { editWordDef = it },
                            label = { Text("Definition (Optional)") },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            editingWord?.let {
                                viewModel.updateWord(it.id, editWordText, editWordDef)
                            }
                            editingWord = null
                        },
                        enabled = editWordText.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PortalAction,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) { Text("Save Changes") }
                },
                dismissButton = {
                    TextButton(onClick = { editingWord = null }) { Text("Cancel") }
                }
            )
        }

        if (lessonToReset != null) {
            AlertDialog(
                onDismissRequest = { lessonToReset = null },
                title = { Text("Reset Progress?") },
                text = { Text("Clear all progress for '${lessonToReset?.name}'? This cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.selectList(lessonToReset!!.id)
                            viewModel.resetProgress()
                            lessonToReset = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PortalAction,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) { Text("Reset Progress") }
                },
                dismissButton = {
                    TextButton(onClick = { lessonToReset = null }) { Text("Cancel") }
                }
            )
        }

        if (lessonToDelete != null) {
            AlertDialog(
                onDismissRequest = { lessonToDelete = null },
                title = { Text("Delete Lesson?") },
                text = { Text("Are you sure you want to delete '${lessonToDelete?.name}' and all its words?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteLesson(lessonToDelete!!.id)
                            lessonToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PortalAction,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { lessonToDelete = null }) { Text("Cancel") }
                }
            )
        }

        if (showResetAllConfirm) {
            AlertDialog(
                onDismissRequest = { showResetAllConfirm = false },
                title = { Text("Reset ALL Progress?") },
                text = { Text("This will clear ALL progress for ALL lessons. This cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.resetAllProgress()
                            showResetAllConfirm = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PortalAction,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) { Text("Reset All") }
                },
                dismissButton = {
                    TextButton(onClick = { showResetAllConfirm = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun ParentOverviewPage(
    stats: ParentViewModel.ParentStats,
    difficulty: Int,
    onDifficultyChange: (Int) -> Unit,
    lessons: List<SpellingListEntity>,
    activeLessonId: Long?,
    onActivate: (Long) -> Unit,
    onEdit: (Long) -> Unit,
    onDelete: (SpellingListEntity) -> Unit,
    onResetLesson: (SpellingListEntity) -> Unit,
    onCreateNew: () -> Unit,
    onResetAll: () -> Unit,
    isReorderMode: Boolean,
    onToggleReorder: () -> Unit,
    onMove: (Long, Int) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        // Header
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Group, contentDescription = null, tint = PortalAction, modifier = Modifier.size(48.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Parent Portal", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Manage your child's learning journey", style = MaterialTheme.typography.bodySmall, color = PortalTextSecondary)
                }
            }
            IconButton(onClick = onResetAll) {
                Icon(Icons.Default.RestartAlt, contentDescription = "Reset All Progress", tint = PortalTextSecondary)
            }
        }

        Spacer(Modifier.height(24.dp))

        // Difficulty Settings
        Text("Overall Game Difficulty", fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(1 to "Easy", 2 to "Medium", 3 to "Hard").forEach { (level, label) ->
                FilterChip(
                    selected = difficulty == level,
                    onClick = { onDifficultyChange(level) },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PortalAction,
                        selectedLabelColor = Color.White,
                        containerColor = PortalCard,
                        labelColor = PortalTextSecondary
                    )
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Quick Stats
        Text("Quick Stats", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(12.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = PortalCard)) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceAround) {
                StatItem(Icons.Default.Book, "Words", "${stats.wordsLearned}/${stats.totalWords}", Color(0xFF4FC3F7))
                StatItem(Icons.Default.Psychology, "Vocab", "${stats.vocabLearned}/${stats.totalVocab}", Color(0xFFBA68C8))
                StatItem(Icons.Default.TrackChanges, "Lessons", "${stats.totalLessons}", Color(0xFF81C784))
                StatItem(Icons.Default.Star, "Progress", "${stats.progressPercent}%", Color(0xFFFFB74D))
            }
        }

        Spacer(Modifier.height(32.dp))

        // Lessons Section
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Lessons", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            Row {
                IconButton(onClick = onToggleReorder) {
                    Icon(if (isReorderMode) Icons.Default.Check else Icons.Default.Reorder, contentDescription = "Reorder", tint = PortalAction)
                }
                TextButton(onClick = onCreateNew) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = PortalAction)
                    Spacer(Modifier.width(4.dp))
                    Text("New Lesson", color = PortalAction)
                }
            }
        }
        
        Spacer(Modifier.height(8.dp))
        
        lessons.forEachIndexed { index, lesson ->
            LessonItem(
                lesson = lesson,
                isActive = lesson.id == activeLessonId,
                isReorderMode = isReorderMode,
                canMoveUp = index > 0,
                canMoveDown = index < lessons.size - 1,
                onActivate = { onActivate(lesson.id) },
                onEdit = { onEdit(lesson.id) },
                onDelete = { onDelete(lesson) },
                onReset = { onResetLesson(lesson) },
                onMoveUp = { onMove(lesson.id, -1) },
                onMoveDown = { onMove(lesson.id, 1) }
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun LessonDetailPage(
    lesson: SpellingListEntity?,
    words: List<WordEntity>,
    onBack: () -> Unit,
    onRename: (String) -> Unit,
    onAddWords: () -> Unit,
    onAddVocab: () -> Unit,
    onEditWord: (WordEntity) -> Unit,
    onDeleteWord: (Long) -> Unit
) {
    if (lesson == null) return

    var editingName by remember(lesson.name) { mutableStateOf(lesson.name) }
    var isEditing by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            if (isEditing) {
                OutlinedTextField(
                    value = editingName,
                    onValueChange = { editingName = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                IconButton(onClick = { 
                    if (editingName.isNotBlank()) {
                        onRename(editingName)
                        isEditing = false
                    }
                }) {
                    Icon(Icons.Default.Check, contentDescription = "Save", tint = PortalAction)
                }
            } else {
                Text(lesson.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                IconButton(onClick = { isEditing = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Rename", tint = PortalTextSecondary, modifier = Modifier.size(20.dp))
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Action Buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PortalActionButton(Modifier.weight(1f), "Add Words", "Build vocabulary", Icons.Default.Add, PortalAction, onAddWords)
            PortalActionButton(Modifier.weight(1f), "Add Vocabulary", "Expand knowledge", Icons.AutoMirrored.Filled.MenuBook, PortalCard, onAddVocab)
        }

        Spacer(Modifier.height(24.dp))
        
        if (words.isNotEmpty()) {
            Text("Lesson Content", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(12.dp))

            words.forEach { word ->
                WordRow(word, onEdit = { onEditWord(word) }, onDelete = { onDeleteWord(it) })
                Spacer(Modifier.height(8.dp))
            }
        } else {
            EmptyState(
                emoji = "📝",
                title = "No words yet",
                message = "Add some words or vocabulary items to this lesson to get started."
            )
        }
        
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
fun LessonItem(
    lesson: SpellingListEntity,
    isActive: Boolean,
    isReorderMode: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onActivate: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onReset: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PortalCard)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (isReorderMode) {
                Column {
                    IconButton(onClick = onMoveUp, enabled = canMoveUp) {
                        Icon(Icons.Default.KeyboardArrowUp, null, tint = if (canMoveUp) PortalAction else PortalTextSecondary.copy(alpha = 0.3f))
                    }
                    IconButton(onClick = onMoveDown, enabled = canMoveDown) {
                        Icon(Icons.Default.KeyboardArrowDown, null, tint = if (canMoveDown) PortalAction else PortalTextSecondary.copy(alpha = 0.3f))
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PortalAction.copy(alpha = 0.2f))
                        .clickable { onActivate() }, 
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = PortalAction)
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f).clickable(enabled = !isReorderMode) { onActivate() }) {
                Text(lesson.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                if (!isReorderMode) {
                    Text(if (isActive) "Currently Active" else "Tap to activate", color = if (isActive) Color(0xFF81C784) else PortalTextSecondary, fontSize = 11.sp)
                }
            }
            
            if (!isReorderMode) {
                Row {
                    IconButton(onClick = onReset) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset Progress", tint = PortalTextSecondary, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Content", tint = PortalTextSecondary, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = PortalTextSecondary, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(icon: ImageVector, label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(4.dp))
        Text(label, color = PortalTextSecondary, fontSize = 9.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun PortalActionButton(modifier: Modifier, title: String, subtitle: String, icon: ImageVector, bgColor: Color, onClick: () -> Unit) {
    Card(modifier = modifier.height(80.dp).clickable { onClick() }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = bgColor)) {
        Row(modifier = Modifier.fillMaxSize().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(subtitle, color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun WordRow(word: WordEntity, onEdit: () -> Unit, onDelete: (Long) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PortalCard.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(word.text, color = Color.White, fontWeight = FontWeight.SemiBold)
                if (!word.definition.isNullOrBlank()) {
                    Text(
                        word.definition,
                        style = MaterialTheme.typography.bodySmall,
                        color = PortalTextSecondary
                    )
                }
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PortalTextSecondary, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = { onDelete(word.id) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = PortalTextSecondary, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun AddWordsDialog(initialText: String, onWordsChange: (String) -> Unit, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss, title = { Text("Add Words") },
        text = {
            OutlinedTextField(
                value = initialText, onValueChange = onWordsChange, label = { Text("One word per line or comma-separated") },
                modifier = Modifier.fillMaxWidth().height(180.dp)
            )
        },
        confirmButton = { 
            Button(
                onClick = { onConfirm(initialText) }, 
                enabled = initialText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PortalAction,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp)
            ) { Text("Add") } 
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AddVocabDialog(word: String, definition: String, onWordChange: (String) -> Unit, onDefChange: (String) -> Unit, onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss, title = { Text("Add Vocabulary") },
        text = {
            Column {
                OutlinedTextField(value = word, onValueChange = onWordChange, label = { Text("Word") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = definition, onValueChange = onDefChange, label = { Text("Definition") }, modifier = Modifier.fillMaxWidth().height(120.dp))
            }
        },
        confirmButton = { 
            Button(
                onClick = { onConfirm(word, definition) }, 
                enabled = word.isNotBlank() && definition.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PortalAction,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp)
            ) { Text("SAVE VOCABULARY") } 
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun SetupPinSection(newPin: String, confirmPin: String, onNewPinChange: (String) -> Unit, onConfirmPinChange: (String) -> Unit, onSave: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Set Parent PIN", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(8.dp))
        Text("Create a 4-digit code to protect your spelling lessons and settings.", color = PortalTextSecondary)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = newPin, onValueChange = { if (it.length <= 4) onNewPinChange(it.filter { it.isDigit() }) },
            label = { Text("Enter 4-digit PIN") }, visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), singleLine = true,
            modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = confirmPin, onValueChange = { if (it.length <= 4) onConfirmPinChange(it.filter { it.isDigit() }) },
            label = { Text("Confirm PIN") }, visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), singleLine = true,
            isError = confirmPin.isNotEmpty() && confirmPin != newPin,
            modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onSave, modifier = Modifier.fillMaxWidth(), enabled = newPin.length == 4 && newPin == confirmPin) { Text("Save and Unlock") }
    }
}

@Composable
fun UnlockSection(pinInput: String, onPinChange: (String) -> Unit, onUnlock: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Lock, contentDescription = null, tint = PortalAction, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text("Enter Parent PIN", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = pinInput, onValueChange = { if (it.length <= 4) onPinChange(it.filter { it.isDigit() }) },
            label = { Text("4-digit PIN") }, visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), singleLine = true,
            modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onUnlock, modifier = Modifier.fillMaxWidth(), enabled = pinInput.length == 4) { Text("Unlock") }
    }
}

@Composable
fun EmptyState(
    emoji: String,
    title: String,
    message: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color.White
        )
        Spacer(Modifier.height(8.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = PortalTextSecondary
        )
    }
}
