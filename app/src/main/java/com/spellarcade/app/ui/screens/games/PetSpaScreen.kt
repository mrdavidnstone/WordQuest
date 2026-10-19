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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.spellarcade.app.ui.components.*
import com.spellarcade.app.util.MusicManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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

    LaunchedEffect(Unit) {
        MusicManager.playMusic(context, "music/pet_spa/gem-waltz.wav")
    }

    // Portrait on Ready/Info screen, Landscape + Immersive Full-Screen during gameplay
    DisposableEffect(uiState.isReady) {
        val activity = context as? Activity
        val window = activity?.window
        val windowInsetsController = window?.let { WindowCompat.getInsetsController(it, it.decorView) }

        if (uiState.isReady) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            windowInsetsController?.let { controller ->
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            windowInsetsController?.let { controller ->
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }

        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            windowInsetsController?.let { controller ->
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
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
            // Top Center Word Panel
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp)
                    .zIndex(10f),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF1E1B4B).copy(alpha = 0.95f),
                border = BorderStroke(2.dp, Color(0xFFEC4899))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🐾", fontSize = 22.sp)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = petSpaState.petName.uppercase(),
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color(0xFF38BDF8)
                        )
                        Spacer(Modifier.height(2.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            petSpaState.maskedWord.forEach { char ->
                                PetSpaBlankTile(char = char)
                            }
                        }
                    }
                }
            }

            // Top Left Back Button & Mistake Counter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .align(Alignment.TopStart)
                    .zIndex(10f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1E1B4B).copy(alpha = 0.9f),
                    border = BorderStroke(1.5.dp, Color(0xFF22C55E))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Mistakes: ${petSpaState.wrongGuesses}/${petSpaState.maxWrong}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        Text("🐾", fontSize = 16.sp)
                    }
                }
            }

            // Center Pet Visual
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        modifier = Modifier.size(160.dp),
                        shape = CircleShape,
                        color = Color(0xFF1E293B).copy(alpha = 0.85f),
                        border = BorderStroke(3.dp, Color(0xFFEC4899))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(petSpaState.petEmoji, fontSize = 72.sp)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F172A).copy(alpha = 0.85f)
                    ) {
                        Text(
                            text = if (petSpaState.phase == PetSpaPhase.GROOMING) "✨ Spell solved! Tap tools to groom!" else petSpaState.petName,
                            fontWeight = FontWeight.Bold,
                            color = if (petSpaState.phase == PetSpaPhase.GROOMING) Color(0xFF22C55E) else Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Right Side Grooming Tools Vertical Stack
            if (petSpaState.phase == PetSpaPhase.GROOMING || petSpaState.phase == PetSpaPhase.ROUND_COMPLETE) {
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                        .zIndex(10f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    GroomingTool.entries.forEach { tool ->
                        val isDone = petSpaState.completedGroomingTools.contains(tool)
                        Surface(
                            modifier = Modifier
                                .size(width = 160.dp, height = 52.dp)
                                .clickable(
                                    enabled = !isDone && petSpaState.phase == PetSpaPhase.GROOMING,
                                    onClick = { viewModel.useGroomingTool(tool) }
                                ),
                            shape = RoundedCornerShape(16.dp),
                            color = if (isDone) Color(0xFF22C55E).copy(alpha = 0.85f) else Color(0xFF3B0764).copy(alpha = 0.95f),
                            border = BorderStroke(2.dp, if (isDone) Color(0xFF22C55E) else Color(0xFFEC4899))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(tool.emoji, fontSize = 22.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Text(tool.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                }
                                if (isDone) {
                                    Text("✅", fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Bottom QWERTY Keyboard (Lab Meltdown Style, Pet Spa Themed)
            if (petSpaState.phase == PetSpaPhase.SPELLING) {
                var isCapitalized by remember { mutableStateOf(false) }
                var isNumbersMode by remember { mutableStateOf(false) }
                val requireCapsSetting = uiState.requireCapitalLetters

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 24.dp, bottom = 12.dp)
                        .width(460.dp)
                        .zIndex(10f)
                        .clip(RoundedCornerShape(20.dp)),
                    color = Color(0xFF1E1B4B).copy(alpha = 0.96f),
                    border = BorderStroke(1.5.dp, Color(0xFFEC4899))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (!isNumbersMode) {
                            val rawRow1 = "qwertyuiop".toList()
                            val rawRow2 = "asdfghjkl".toList()
                            val rawRow3 = "zxcvbnm".toList()
                            val targetWord = uiState.currentWord?.text ?: ""

                            // Row 1 (Q to P)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)
                            ) {
                                rawRow1.forEach { rawChar ->
                                    val letterChar = if (!requireCapsSetting) rawChar.uppercaseChar() else if (isCapitalized) rawChar.uppercaseChar() else rawChar.lowercaseChar()
                                    val isGuessed = if (requireCapsSetting) petSpaState.guessedLetters.contains(letterChar) else (petSpaState.guessedLetters.contains(rawChar) || petSpaState.guessedLetters.contains(rawChar.uppercaseChar()))
                                    val isCorrect = if (requireCapsSetting) (isGuessed && targetWord.contains(letterChar)) else (isGuessed && targetWord.contains(rawChar, ignoreCase = true))

                                    PetSpaKeyTile(
                                        displayLabel = letterChar.toString(),
                                        isGuessed = isGuessed,
                                        isCorrect = isCorrect,
                                        onClick = { viewModel.guessLetter(letterChar) },
                                        disabled = uiState.showFeedback || uiState.isFinished,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            // Row 2 (A to L)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                            ) {
                                rawRow2.forEach { rawChar ->
                                    val letterChar = if (!requireCapsSetting) rawChar.uppercaseChar() else if (isCapitalized) rawChar.uppercaseChar() else rawChar.lowercaseChar()
                                    val isGuessed = if (requireCapsSetting) petSpaState.guessedLetters.contains(letterChar) else (petSpaState.guessedLetters.contains(rawChar) || petSpaState.guessedLetters.contains(rawChar.uppercaseChar()))
                                    val isCorrect = if (requireCapsSetting) (isGuessed && targetWord.contains(letterChar)) else (isGuessed && targetWord.contains(rawChar, ignoreCase = true))

                                    PetSpaKeyTile(
                                        displayLabel = letterChar.toString(),
                                        isGuessed = isGuessed,
                                        isCorrect = isCorrect,
                                        onClick = { viewModel.guessLetter(letterChar) },
                                        disabled = uiState.showFeedback || uiState.isFinished,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            // Row 3 (Shift + Z to M + 123)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)
                            ) {
                                if (requireCapsSetting) {
                                    PetSpaKeyTile(
                                        displayLabel = "⇧",
                                        isGuessed = false,
                                        isCorrect = false,
                                        onClick = { isCapitalized = !isCapitalized },
                                        disabled = false,
                                        modifier = Modifier.weight(1.3f),
                                        isSpecialKey = true,
                                        isActiveKey = isCapitalized
                                    )
                                }

                                rawRow3.forEach { rawChar ->
                                    val letterChar = if (!requireCapsSetting) rawChar.uppercaseChar() else if (isCapitalized) rawChar.uppercaseChar() else rawChar.lowercaseChar()
                                    val isGuessed = if (requireCapsSetting) petSpaState.guessedLetters.contains(letterChar) else (petSpaState.guessedLetters.contains(rawChar) || petSpaState.guessedLetters.contains(rawChar.uppercaseChar()))
                                    val isCorrect = if (requireCapsSetting) (isGuessed && targetWord.contains(letterChar)) else (isGuessed && targetWord.contains(rawChar, ignoreCase = true))

                                    PetSpaKeyTile(
                                        displayLabel = letterChar.toString(),
                                        isGuessed = isGuessed,
                                        isCorrect = isCorrect,
                                        onClick = { viewModel.guessLetter(letterChar) },
                                        disabled = uiState.showFeedback || uiState.isFinished,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                PetSpaKeyTile(
                                    displayLabel = "123",
                                    isGuessed = false,
                                    isCorrect = false,
                                    onClick = { isNumbersMode = true },
                                    disabled = false,
                                    modifier = Modifier.weight(1.3f),
                                    isSpecialKey = true
                                )
                            }
                        } else {
                            // --- NUMBERS & SYMBOLS MODE ---
                            val numRow1 = "1234567890".toList()
                            val numRow2 = "@#$%&-+()/".toList()

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)
                            ) {
                                numRow1.forEach { symbol ->
                                    PetSpaKeyTile(
                                        displayLabel = symbol.toString(),
                                        isGuessed = false,
                                        isCorrect = false,
                                        onClick = { viewModel.guessLetter(symbol) },
                                        disabled = uiState.showFeedback || uiState.isFinished,
                                        modifier = Modifier.weight(1f),
                                        isSpecialKey = true
                                    )
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                            ) {
                                numRow2.forEach { symbol ->
                                    PetSpaKeyTile(
                                        displayLabel = symbol.toString(),
                                        isGuessed = false,
                                        isCorrect = false,
                                        onClick = { viewModel.guessLetter(symbol) },
                                        disabled = uiState.showFeedback || uiState.isFinished,
                                        modifier = Modifier.weight(1f),
                                        isSpecialKey = true
                                    )
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)
                            ) {
                                PetSpaKeyTile(
                                    displayLabel = "ABC",
                                    isGuessed = false,
                                    isCorrect = false,
                                    onClick = { isNumbersMode = false },
                                    disabled = false,
                                    modifier = Modifier.weight(1.5f),
                                    isSpecialKey = true
                                )
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
private fun PetSpaKeyTile(
    displayLabel: String,
    isGuessed: Boolean,
    isCorrect: Boolean,
    onClick: () -> Unit,
    disabled: Boolean,
    modifier: Modifier = Modifier,
    isSpecialKey: Boolean = false,
    isActiveKey: Boolean = false
) {
    val containerColor = when {
        isActiveKey -> Color(0xFFEC4899)
        isCorrect -> Color(0xFF22C55E).copy(alpha = 0.85f)
        isGuessed -> Color(0xFF334155).copy(alpha = 0.6f)
        isSpecialKey -> Color(0xFF581C87)
        else -> Color(0xFF4C1D95)
    }

    val contentColor = if (isGuessed && !isCorrect && !isActiveKey) Color.White.copy(alpha = 0.3f) else Color.White

    Button(
        onClick = onClick,
        enabled = !disabled && !(isGuessed && !isSpecialKey),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.4f),
            disabledContentColor = contentColor.copy(alpha = 0.4f)
        ),
        border = BorderStroke(1.dp, Color(0xFFEC4899).copy(alpha = 0.7f)),
        modifier = modifier.height(36.dp)
    ) {
        Text(
            text = displayLabel,
            fontWeight = FontWeight.Bold,
            fontSize = if (isSpecialKey) 14.sp else 16.sp,
            color = contentColor
        )
    }
}

@Composable
private fun PetSpaBlankTile(char: Char) {
    Surface(
        modifier = Modifier.size(width = 36.dp, height = 44.dp),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF1E1B4B).copy(alpha = 0.9f),
        border = BorderStroke(2.dp, Color(0xFFEC4899))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = if (char == '_') "" else char.toString(),
                fontSize = 22.sp,
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
            .zIndex(250f)
            .background(Color(0xFF030712).copy(alpha = 0.95f)),
        contentAlignment = Alignment.Center
    ) {
        ContainerOverlayCard(uiState, onRetry, onBack, isGameWon, missedWords)
    }
}

@Composable
private fun ContainerOverlayCard(
    uiState: GameUiState,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    isGameWon: Boolean,
    missedWords: Int
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

private fun loadAssetImageBitmap(context: Context, assetName: String): ImageBitmap? {
    return try {
        context.assets.open(assetName).use { stream ->
            BitmapFactory.decodeStream(stream)?.asImageBitmap()
        }
    } catch (_: Exception) {
        null
    }
}
