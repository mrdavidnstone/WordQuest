package com.spellarcade.app.ui.screens.games

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spellarcade.app.ui.components.*
import com.spellarcade.app.util.TtsHelper
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListenSpellScreen(
    onBack: () -> Unit,
    viewModel: ListenSpellViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var answer by remember { mutableStateOf("") }
    val context = LocalContext.current
    val tts = remember { TtsHelper(context) }

    DisposableEffect(Unit) { onDispose { tts.shutdown() } }

    LaunchedEffect(uiState.currentWord, uiState.isReady) {
        if (uiState.isReady && uiState.currentWord != null && !uiState.showFeedback && !uiState.isFinished) {
            tts.speak(uiState.currentWord!!.text)
        }
        answer = ""
    }

    LaunchedEffect(uiState.showFeedback) {
        if (uiState.showFeedback) {
            delay(1800)
            viewModel.nextAfterFeedback()
        }
    }

    if (!uiState.isReady) {
        ReadyOverlay(
            title = "Listen & Spell",
            emoji = "🎧",
            description = "Listen carefully to the word and type it correctly.",
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
                title = "Listen & Spell",
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

            Text("Listen carefully and type the word", style = MaterialTheme.typography.bodyLarge, color = Color.White)
            Spacer(Modifier.height(32.dp))

            IconButton(
                onClick = { uiState.currentWord?.let { tts.speak(it.text) } },
                modifier = Modifier.size(100.dp).background(Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(Icons.Default.VolumeUp, "Replay", modifier = Modifier.size(56.dp), tint = Color.White)
            }
            Spacer(Modifier.height(8.dp))
            Text("Tap to replay", color = Color.White.copy(alpha = 0.6f))

            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { uiState.currentWord?.let { tts.speak(it.text, slow = true) } },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
                ) { Text("Slow", color = Color.White) }
                
                Button(
                    onClick = { uiState.currentWord?.let { tts.speak(it.text) } },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
                ) { Text("Normal", color = Color.White) }
            }

            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = answer,
                onValueChange = { answer = it },
                label = { Text("Your spelling", color = Color.White.copy(alpha = 0.6f)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF00B0FF),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (answer.isNotBlank() && !uiState.showFeedback) {
                        viewModel.submitAnswer(viewModel.isSpellingCorrect(answer))
                    }
                }),
                enabled = !uiState.showFeedback
            )

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    viewModel.submitAnswer(viewModel.isSpellingCorrect(answer))
                },
                enabled = answer.isNotBlank() && !uiState.showFeedback,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Submit") }

            Spacer(Modifier.height(24.dp))
            FeedbackBanner(
                isCorrect = uiState.isCorrect == true,
                message = uiState.feedback ?: "",
                visible = uiState.showFeedback
            )
        }
    }
}
