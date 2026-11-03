package com.spellarcade.app.ui.screens.games

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spellarcade.app.ui.components.*
import com.spellarcade.app.util.TtsHelper
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeScreen(
    onBack: () -> Unit,
    viewModel: ChallengeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val challengeType by viewModel.currentChallengeType.collectAsState()
    val timerProgress by viewModel.timerProgress.collectAsState()

    LaunchedEffect(uiState.showFeedback) {
        if (uiState.showFeedback) {
            delay(1800)
            viewModel.nextAfterFeedback()
        }
    }

    if (!uiState.isReady) {
        ReadyOverlay(
            title = "Spelling Challenge",
            emoji = "🏆",
            description = "A combination of multiple game styles to test your mastery.",
            uiState = uiState,
            onSwitchList = { viewModel.switchList(it) },
            onStart = { viewModel.startGame() },
            onBack = onBack
        )
        return
    }

    Scaffold(
        topBar = {
            GameHeader(
                title = "Spelling Challenge",
                onBack = onBack,
                score = uiState.score,
                total = uiState.total
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.isFinished) {
                Text("🎉 Challenge Complete!", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("Final Score: ${uiState.score} / ${uiState.total}", fontSize = 20.sp)
                Spacer(Modifier.height(24.dp))
                Button(onClick = onBack) { Text("Back to Games") }
                return@Column
            }

            when (challengeType) {
                ChallengeType.ListenSpell -> ListenSpellChallenge(viewModel, uiState)
                ChallengeType.MissingLetters -> MissingLettersChallenge(viewModel, uiState)
                ChallengeType.Scrambled -> ScrambledChallenge(viewModel, uiState)
                ChallengeType.WhichCorrect -> {
                    TimerBar(timerProgress)
                    WhichCorrectChallenge(viewModel, uiState)
                }
                ChallengeType.Vocabulary -> {
                    TimerBar(timerProgress)
                    VocabularyChallenge(viewModel, uiState)
                }
            }

            Spacer(Modifier.height(24.dp))
            FeedbackBanner(
                isCorrect = uiState.isCorrect == true,
                message = uiState.feedback ?: "",
                visible = uiState.showFeedback
            )
        }
    }
}

@Composable
private fun TimerBar(progress: Float) {
    if (progress >= 1f || progress <= 0f) return 
    LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp)),
        color = if (progress < 0.3f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
    )
}

@Composable
private fun ListenSpellChallenge(viewModel: ChallengeViewModel, uiState: GameUiState) {
    val context = LocalContext.current
    val tts = remember { TtsHelper(context) }
    var answer by remember { mutableStateOf("") }
    
    LaunchedEffect(uiState.currentWord) {
        uiState.currentWord?.let { tts.speak(it.text) }
        answer = ""
    }

    Text("Listen and type the word", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(16.dp))
    IconButton(onClick = { uiState.currentWord?.let { tts.speak(it.text) } }, modifier = Modifier.size(64.dp)) {
        Icon(Icons.AutoMirrored.Filled.VolumeUp, "Replay", modifier = Modifier.size(40.dp))
    }
    Spacer(Modifier.height(16.dp))
    OutlinedTextField(
        value = answer,
        onValueChange = { answer = it },
        label = { Text("Your spelling") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        enabled = !uiState.showFeedback,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = {
            if (answer.isNotBlank()) {
                viewModel.submitAnswer(answer.trim().equals(uiState.currentWord?.text, ignoreCase = true))
            }
        })
    )
    Spacer(Modifier.height(16.dp))
    Button(
        onClick = { viewModel.submitAnswer(answer.trim().equals(uiState.currentWord?.text, ignoreCase = true)) },
        enabled = answer.isNotBlank() && !uiState.showFeedback,
        modifier = Modifier.fillMaxWidth()
    ) { Text("Submit") }
}

@Composable
private fun MissingLettersChallenge(viewModel: ChallengeViewModel, uiState: GameUiState) {
    val puzzle by viewModel.missingPuzzle.collectAsState()
    
    Text("Fill in the missing letters", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(24.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        puzzle.maskedWord.forEachIndexed { i, ch ->
            val isTarget = puzzle.missingIndices.getOrNull(puzzle.currentPointer) == i
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isTarget) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(if (ch == '_') "" else ch.uppercaseChar().toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold,
                        color = if (isTarget) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
    Spacer(Modifier.height(32.dp))
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(puzzle.choices) { letter ->
            Button(onClick = { viewModel.selectMissingLetter(letter) }, enabled = !uiState.showFeedback, modifier = Modifier.size(50.dp), contentPadding = PaddingValues(0.dp)) {
                Text(letter.uppercase(), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ScrambledChallenge(viewModel: ChallengeViewModel, uiState: GameUiState) {
    val tiles by viewModel.tiles.collectAsState()
    val selected by viewModel.selected.collectAsState()
    val usedIndices by viewModel.usedIndices.collectAsState()

    Text("Unscramble the letters", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(16.dp))
    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.fillMaxWidth().height(50.dp)) {
        Box(contentAlignment = Alignment.Center) {
            Text(selected.joinToString("").uppercase().ifEmpty { "..." }, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
    Spacer(Modifier.height(24.dp))
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        itemsIndexed(tiles) { index, char ->
            val used = index in usedIndices
            Button(onClick = { viewModel.tapScrambledTile(index) }, enabled = !used && !uiState.showFeedback, modifier = Modifier.size(48.dp), contentPadding = PaddingValues(0.dp)) {
                Text(char.uppercase(), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
    Spacer(Modifier.height(16.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(onClick = { viewModel.clearScrambled() }, enabled = !uiState.showFeedback) { Text("Clear") }
        Button(onClick = { viewModel.checkScrambled() }, enabled = selected.isNotEmpty() && !uiState.showFeedback) { Text("Check") }
    }
}

@Composable
private fun WhichCorrectChallenge(viewModel: ChallengeViewModel, uiState: GameUiState) {
    val options by viewModel.options.collectAsState()
    
    Text("Which spelling is correct?", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(24.dp))
    options.forEach { opt ->
        OutlinedButton(onClick = { viewModel.selectWhichCorrect(opt) }, enabled = !uiState.showFeedback, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Text(opt, fontSize = 18.sp)
        }
    }
}

@Composable
private fun VocabularyChallenge(viewModel: ChallengeViewModel, uiState: GameUiState) {
    val options by viewModel.options.collectAsState()
    
    Text("Which word matches this definition?", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(16.dp))
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = "\"${uiState.currentWord?.definition ?: ""}\"",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge,
            fontStyle = FontStyle.Italic,
            textAlign = TextAlign.Center
        )
    }
    
    Spacer(Modifier.height(24.dp))
    
    options.forEach { opt ->
        OutlinedButton(
            onClick = { viewModel.selectWhichCorrect(opt) },
            enabled = !uiState.showFeedback,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Text(opt, fontSize = 18.sp)
        }
    }
}
