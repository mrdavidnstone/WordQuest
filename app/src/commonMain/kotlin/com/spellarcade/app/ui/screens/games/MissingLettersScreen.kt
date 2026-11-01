package com.spellarcade.app.ui.screens.games

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spellarcade.app.ui.components.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissingLettersScreen(
    onBack: () -> Unit,
    viewModel: MissingLettersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val puzzle by viewModel.puzzle.collectAsState()

    LaunchedEffect(uiState.currentWord) {
        viewModel.buildPuzzleForCurrentWord()
    }

    LaunchedEffect(uiState.showFeedback) {
        if (uiState.showFeedback) {
            delay(1600)
            viewModel.nextAfterFeedback()
        }
    }

    if (!uiState.isReady) {
        ReadyOverlay(
            title = "Missing Letters",
            emoji = "🔤",
            description = "Fill in the missing letters of the word.",
            uiState = uiState,
            onSwitchList = { viewModel.switchList(it) },
            onStart = { viewModel.startGame() },
            onBack = onBack
        )
        return
    }

    Scaffold(
        containerColor = Color(0xFF021024), // Dark Blue
        topBar = {
            GameHeader(
                title = "Missing Letters",
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
                Text("🎉 Round Complete!", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(8.dp))
                Text("Score: ${uiState.score} / ${uiState.total}", color = Color.White.copy(alpha = 0.8f))
                Spacer(Modifier.height(24.dp))
                Button(onClick = onBack) { Text("Back to Games") }
                return@Column
            }

            Text("Fill in the missing letter(s)", style = MaterialTheme.typography.bodyLarge, color = Color.White)
            Spacer(Modifier.height(32.dp))

            // Display the masked word
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                puzzle.maskedWord.forEachIndexed { i, ch ->
                    val isTarget = puzzle.missingIndices.getOrNull(puzzle.currentPointer) == i
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when {
                            isTarget -> Color(0xFF00B0FF)
                            ch == '_' -> Color.White.copy(alpha = 0.1f)
                            else -> Color.White.copy(alpha = 0.2f)
                        },
                        modifier = Modifier.size(width = 44.dp, height = 56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                if (ch == '_') " " else ch.uppercaseChar().toString(),
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(48.dp))

            // Letter choices
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                puzzle.choices.forEach { letter ->
                    Button(
                        onClick = {
                            if (!uiState.showFeedback) {
                                viewModel.selectLetter(letter)
                            }
                        },
                        enabled = !uiState.showFeedback,
                        modifier = Modifier.size(64.dp),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.1f),
                            contentColor = Color.White
                        )
                    ) {
                        Text(letter.uppercase(), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
            FeedbackBanner(
                isCorrect = uiState.isCorrect == true,
                message = uiState.feedback ?: "",
                visible = uiState.showFeedback
            )
        }
    }
}
