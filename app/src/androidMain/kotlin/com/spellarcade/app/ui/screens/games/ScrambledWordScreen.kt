package com.spellarcade.app.ui.screens.games

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
fun ScrambledWordScreen(
    onBack: () -> Unit,
    viewModel: ScrambledWordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val tiles by viewModel.tiles.collectAsState()
    val selected by viewModel.selected.collectAsState()

    LaunchedEffect(uiState.currentWord) {
        viewModel.prepareTiles()
    }

    LaunchedEffect(uiState.showFeedback) {
        if (uiState.showFeedback) {
            delay(1600)
            viewModel.nextAfterFeedback()
        }
    }

    if (!uiState.isReady) {
        ReadyOverlay(
            title = "Scrambled Word",
            emoji = "🔀",
            description = "Unscramble the letters to spell the word correctly.",
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
                title = "Scrambled Word",
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

            Text("Tap the letters in order", style = MaterialTheme.typography.bodyLarge, color = Color.White)
            Spacer(Modifier.height(24.dp))

            // Built word so far
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        selected.joinToString("").uppercase().ifEmpty { "..." },
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = if (selected.isEmpty()) Color.White.copy(alpha = 0.3f) else Color.White
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // Scrambled tiles
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                tiles.forEachIndexed { index, letter ->
                    val used = viewModel.isTileUsed(index)
                    Button(
                        onClick = { if (!used && !uiState.showFeedback) viewModel.tapTile(index) },
                        enabled = !used && !uiState.showFeedback,
                        modifier = Modifier.size(width = 46.dp, height = 58.dp),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.15f),
                            contentColor = Color.White,
                            disabledContainerColor = Color.White.copy(alpha = 0.05f)
                        )
                    ) {
                        Text(letter.uppercase(), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { viewModel.clearSelection() },
                    enabled = !uiState.showFeedback,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
                ) {
                    Text("Clear", color = Color.White)
                }
                Button(
                    onClick = { viewModel.checkAnswer() },
                    enabled = selected.isNotEmpty() && !uiState.showFeedback,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B0FF))
                ) {
                    Text("Check", fontWeight = FontWeight.Bold)
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
