package com.spellarcade.app.ui.screens.games

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.BitmapFactory
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spellarcade.app.ui.components.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetSpaScreen(
    onBack: () -> Unit,
    viewModel: PetSpaViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val petSpaState by viewModel.petSpaState.collectAsState()

    // Portrait on Ready screen, Landscape during Pet Spa gameplay
    DisposableEffect(uiState.isReady) {
        val activity = context as? Activity
        activity?.requestedOrientation = if (uiState.isReady) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    LaunchedEffect(uiState.showFeedback) {
        if (uiState.showFeedback) {
            delay(1500)
            viewModel.nextAfterFeedback()
        }
    }

    if (!uiState.isReady) {
        val titleBitmap = remember(context) { loadAssetImageBitmap(context, "games/pet_spa/pet_spa_title.png") }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF030712))
        ) {
            // Full-Size Title Logo Overlay (Exact size & position matching Lab Meltdown)
            if (titleBitmap != null) {
                Image(
                    bitmap = titleBitmap,
                    contentDescription = "Pet Spa Title Overlay",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Foreground UI Layer (3-Tier Layout: Top Back Button, Center Info Card, Bottom Action Button)
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // TOP SECTION: Back Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }

                    Spacer(Modifier.height(48.dp))

                    Spacer(Modifier.width(48.dp))
                }

                // Inner Layout for Center Info Card & Bottom Play Button
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.weight(1f))

                    // CENTER SECTION: Game Information & Lesson Selector
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        shape = RoundedCornerShape(22.dp),
                        color = Color(0xFF0F172A).copy(alpha = 0.92f),
                        border = BorderStroke(1.5.dp, Color(0xFFEC4899).copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "Spell the word correctly to unlock grooming tools and pamper the adorable pet!",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = Color.White.copy(alpha = 0.95f),
                                fontSize = 15.sp,
                                lineHeight = 22.sp
                            )

                            Spacer(Modifier.height(18.dp))

                            ActiveListBadge(
                                listName = uiState.listName,
                                allLists = uiState.allLists,
                                onListSelected = { viewModel.switchList(it) }
                            )
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    // BOTTOM SECTION: Play Game Action Button
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (!uiState.hasWords) {
                            Text(
                                "This list has no words. Add some words or pick another list.",
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(12.dp))
                            OutlinedButton(onClick = onBack) { Text("Go Back") }
                        } else {
                            Button(
                                onClick = { viewModel.startGame() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFEC4899)
                                )
                            ) {
                                Text("PLAY GAME", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
        return
    }

    val bgBitmap = remember(context) { loadAssetImageBitmap(context, "games/pet_spa/pet_spa_bg.png") }

    Scaffold(
        containerColor = Color(0xFF0F172A),
        topBar = {
            GameHeader(
                title = "Pet Spa: ${petSpaState.petName}",
                onBack = onBack,
                score = uiState.score,
                total = uiState.total
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            // Background Image
            if (bgBitmap != null) {
                Image(
                    bitmap = bgBitmap,
                    contentDescription = "Pet Spa Background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.fillMaxSize().background(Color(0xFF021024)))
            }

            if (uiState.isFinished) {
                PetSpaResultOverlay(
                    uiState = uiState,
                    onRetry = { viewModel.startGame() },
                    onBack = onBack
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Column: Pet Visual & Status / Grooming Status
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(140.dp),
                            shape = CircleShape,
                            color = Color(0xFF1E293B).copy(alpha = 0.85f),
                            border = BorderStroke(3.dp, Color(0xFFEC4899))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(petSpaState.petEmoji, fontSize = 64.sp)
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0F172A).copy(alpha = 0.85f)
                        ) {
                            Text(
                                petSpaState.petName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        val statusText = when (petSpaState.phase) {
                            PetSpaPhase.SPELLING -> "❌ Mistakes: ${petSpaState.wrongGuesses}/${petSpaState.maxWrong}"
                            PetSpaPhase.GROOMING -> "✨ Spell solved! Time to groom!"
                            PetSpaPhase.ROUND_COMPLETE -> "🎉 Spa treatment complete!"
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0F172A).copy(alpha = 0.85f)
                        ) {
                            Text(
                                statusText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (petSpaState.phase == PetSpaPhase.SPELLING) Color(0xFFEF4444) else Color(0xFF22C55E),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Right Column: Active Phase (SPELLING or GROOMING)
                    Column(
                        modifier = Modifier
                            .weight(1.5f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        when (petSpaState.phase) {
                            PetSpaPhase.SPELLING -> {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF0F172A).copy(alpha = 0.85f)
                                ) {
                                    Text(
                                        "Guess the spelling to start spa!",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                                Spacer(Modifier.height(16.dp))

                                // Masked word blanks
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    petSpaState.maskedWord.forEach { char ->
                                        PetSpaBlankTile(char = char)
                                    }
                                }

                                Spacer(Modifier.height(24.dp))

                                // Alphabet Keyboard
                                val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xFF0F172A).copy(alpha = 0.88f),
                                    modifier = Modifier.padding(4.dp)
                                ) {
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(7),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.height(140.dp).padding(8.dp)
                                    ) {
                                        items(alphabet.toList()) { letter ->
                                            val isGuessed = petSpaState.guessedLetters.contains(letter) ||
                                                    petSpaState.guessedLetters.contains(letter.lowercaseChar())
                                            Button(
                                                onClick = { viewModel.guessLetter(letter) },
                                                enabled = !isGuessed && !uiState.showFeedback,
                                                contentPadding = PaddingValues(0.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isGuessed) Color.White.copy(alpha = 0.1f) else Color(0xFF1E293B),
                                                    contentColor = if (isGuessed) Color.White.copy(alpha = 0.3f) else Color.White
                                                ),
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Text(letter.toString(), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            PetSpaPhase.GROOMING, PetSpaPhase.ROUND_COMPLETE -> {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF0F172A).copy(alpha = 0.88f)
                                ) {
                                    Text(
                                        "Tap grooming tools to pamper ${petSpaState.petName}!",
                                        color = Color(0xFFEC4899),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                                Spacer(Modifier.height(16.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    GroomingTool.entries.forEach { tool ->
                                        val isDone = petSpaState.completedGroomingTools.contains(tool)
                                        Button(
                                            onClick = { viewModel.useGroomingTool(tool) },
                                            enabled = !isDone && petSpaState.phase == PetSpaPhase.GROOMING,
                                            modifier = Modifier.fillMaxWidth().height(48.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isDone) Color(0xFF22C55E).copy(alpha = 0.25f) else Color(0xFF1E293B).copy(alpha = 0.9f),
                                                contentColor = Color.White
                                            ),
                                            border = BorderStroke(1.dp, if (isDone) Color(0xFF22C55E) else Color(0xFFEC4899))
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(tool.emoji, fontSize = 20.sp)
                                                    Spacer(Modifier.width(12.dp))
                                                    Text(tool.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                                }
                                                Text(if (isDone) "✅ Done" else tool.description, fontSize = 12.sp, color = if (isDone) Color(0xFF22C55E) else Color.White.copy(alpha = 0.8f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            FeedbackBanner(
                isCorrect = uiState.isCorrect == true,
                message = uiState.feedback ?: "",
                visible = uiState.showFeedback
            )
        }
    }
}

@Composable
private fun PetSpaBlankTile(char: Char) {
    Surface(
        modifier = Modifier.size(width = 40.dp, height = 48.dp),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF1E293B).copy(alpha = 0.9f),
        border = BorderStroke(2.dp, Color(0xFFEC4899))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = if (char == '_') "" else char.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}

@Composable
private fun PetSpaResultOverlay(
    uiState: GameUiState,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    val missedWords = (uiState.total - uiState.score).coerceAtLeast(0)
    val isGameWon = uiState.score >= 7

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030712).copy(alpha = 0.95f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .padding(24.dp)
                .background(Color(0xFF0F172A), RoundedCornerShape(24.dp))
                .border(BorderStroke(2.dp, if (isGameWon) Color(0xFF22C55E) else Color(0xFFEF4444)), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Text(
                if (isGameWon) "🛁 SPA MASTER! 🛁" else "💥 GAME OVER! 💥",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = if (isGameWon) Color(0xFF22C55E) else Color(0xFFEF4444),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            Text(
                if (isGameWon) "All pets are clean and happy!" else "Too many mistakes! The spa is closed.",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Correct: ${uiState.score}", fontWeight = FontWeight.Bold, color = Color(0xFF22C55E), fontSize = 14.sp)
                Text("Missed: $missedWords", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444), fontSize = 14.sp)
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899))
            ) {
                Text("🔄 PLAY AGAIN", fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color.White)
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
            ) {
                Text("Back to Games", color = Color.White)
            }
        }
    }
}

private fun loadAssetImageBitmap(context: Context, assetName: String): ImageBitmap? {
    return try {
        context.assets.open(assetName).use { stream ->
            BitmapFactory.decodeStream(stream)?.asImageBitmap()
        }
    } catch (_: Exception) {
        null
    }
}
