package com.spellarcade.app.ui.screens.games

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.spellarcade.app.ui.components.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhichCorrectScreen(
    onBack: () -> Unit,
    viewModel: WhichCorrectViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val options by viewModel.options.collectAsState()
    val timerProgress by viewModel.timerProgress.collectAsState()

    LaunchedEffect(uiState.currentWord) {
        viewModel.generateOptions()
    }

    LaunchedEffect(uiState.showFeedback) {
        if (uiState.showFeedback) {
            delay(1600)
            viewModel.nextAfterFeedback()
        }
    }

    if (!uiState.isReady) {
        ReadyOverlay(
            title = "Which One Is Correct?",
            emoji = "✅",
            description = "Pick the correctly spelled word from the options.",
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
                title = "Which One Is Correct?",
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

            Text("Which spelling is correct?", style = MaterialTheme.typography.titleLarge, color = Color.White)
            Spacer(Modifier.height(24.dp))

            // Timer Bar
            LinearProgressIndicator(
                progress = { timerProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = if (timerProgress < 0.3f) Color(0xFFEF5350) else Color(0xFF00E676),
                trackColor = Color.White.copy(alpha = 0.1f),
            )

            Spacer(Modifier.height(40.dp))

            options.forEach { opt ->
                Button(
                    onClick = {
                        if (!uiState.showFeedback) viewModel.select(opt)
                    },
                    enabled = !uiState.showFeedback,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.1f),
                        contentColor = Color.White,
                        disabledContainerColor = Color.White.copy(alpha = 0.05f)
                    )
                ) {
                    Text(opt, fontSize = 22.sp, fontWeight = FontWeight.Bold)
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
