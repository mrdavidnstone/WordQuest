package com.spellinggames.app.ui.screens.games

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spellinggames.app.data.model.AvatarConfig
import com.spellinggames.app.ui.components.*
import com.spellinggames.app.ui.components.avatar.HeroAnimationState
import com.spellinggames.app.ui.components.avatar.HeroSprite
import com.spellinggames.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// 10 Lab Chaos Animations (No repeats in the same round)
enum class LabChaosEffect(val title: String) {
    BEAKER_OVERFLOW("Beaker Overflow"),
    CONSOLE_SPARKS("Console Sparks"),
    ROBOT_ARM_GLITCH("Robot Arm Glitch"),
    ALARM_FLASH("Alarm Flash"),
    CREATURE_ESCAPE("Creature Escape"),
    CHEMICAL_MIXUP("Chemical Mix-Up"),
    GRAVITY_GLITCH("Gravity Glitch"),
    STEAM_BURST("Steam Burst"),
    TEST_TUBE_POP("Test Tube Pop"),
    POWER_SURGE("Power Surge")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HangmanScreen(
    onBack: () -> Unit,
    viewModel: HangmanViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val hangmanState by viewModel.hangmanState.collectAsState()

    val bgBitmap = remember(context) { loadAssetImageBitmap(context, "lab_meltdown_bg.png") }
    val titleBitmap = remember(context) { loadAssetImageBitmap(context, "lab_meltdown_title.png") }

    // Track active Lab Chaos Effect for wrong guesses
    var activeEffect by remember { mutableStateOf<LabChaosEffect?>(null) }
    val playedEffects = remember { mutableSetOf<LabChaosEffect>() }

    // Screen Shake effect for wrong guesses & meltdown
    val shakeAnim = remember { Animatable(0f) }

    LaunchedEffect(hangmanState.wrongGuesses) {
        if (hangmanState.wrongGuesses > 0 && !uiState.isFinished) {
            // Pick unplayed chaos effect
            val available = LabChaosEffect.entries.filter { !playedEffects.contains(it) }
            val chosen = if (available.isNotEmpty()) available.random() else LabChaosEffect.entries.random()
            playedEffects.add(chosen)
            activeEffect = chosen

            // Trigger screen shake
            launch {
                shakeAnim.animateTo(10f, tween(100))
                shakeAnim.animateTo(-10f, tween(100))
                shakeAnim.animateTo(5f, tween(100))
                shakeAnim.animateTo(0f, tween(100))
            }

            // Clear effect after 1.2 seconds
            delay(1300)
            activeEffect = null
        }
    }

    LaunchedEffect(uiState.showFeedback) {
        if (uiState.showFeedback) {
            delay(2500)
            playedEffects.clear()
            viewModel.nextAfterFeedback()
        }
    }

    if (!uiState.isReady) {
        ReadyOverlay(
            title = "Lab Meltdown",
            emoji = "🧪",
            description = "Prevent the laboratory meltdown by spelling words correctly! Guess letters to keep the lab stable.",
            uiState = uiState,
            onSwitchList = { viewModel.switchList(it) },
            onStart = { viewModel.startGame() },
            onBack = onBack
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030712))
            .graphicsLayer {
                translationX = shakeAnim.value
            }
    ) {
        // 1. LAB BACKGROUND IMAGE (lab_meltdown_bg.png)
        if (bgBitmap != null) {
            Image(
                bitmap = bgBitmap,
                contentDescription = "Lab Scene Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0C1020)))
        }

        // 2. ACTIVE LAB CHAOS ANIMATION OVERLAYS (Sparks, Steam, Flashes, Slime)
        LabChaosOverlay(effect = activeEffect, wrongCount = hangmanState.wrongGuesses)

        Column(modifier = Modifier.fillMaxSize()) {
            // 3. TOP BAR WITH TITLE LOGO & BACK BUTTON
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

                // TITLE LOGO LAYER (lab_meltdown_title.png)
                Box(
                    modifier = Modifier.height(65.dp).weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (titleBitmap != null) {
                        Image(
                            bitmap = titleBitmap,
                            contentDescription = "Lab Meltdown Title",
                            modifier = Modifier.fillMaxHeight(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text("LAB MELTDOWN", color = Color(0xFFA855F7), fontWeight = FontWeight.Black, fontSize = 22.sp)
                    }
                }

                Spacer(Modifier.width(48.dp))
            }

            // 4. MISTAKES & STABILITY STATUS BAR (Matching Mockup)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.85f),
                border = BorderStroke(1.5.dp, Color(0xFFA855F7).copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Mistakes Counter (0/6)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Mistakes in",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "${hangmanState.wrongGuesses}/6",
                            color = if (hangmanState.wrongGuesses >= 5) Color(0xFFEF4444) else Color(0xFF22C55E),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (hangmanState.wrongGuesses >= 5) Color(0xFFEF4444) else Color(0xFF22C55E))
                        )
                    }

                    // Stability Bar (6 Segments)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Stability",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            val maxSegments = 6
                            val remainingSegments = (maxSegments - hangmanState.wrongGuesses).coerceIn(0, maxSegments)
                            for (i in 1..maxSegments) {
                                val isFilled = i <= remainingSegments
                                val segmentColor = when {
                                    !isFilled -> Color(0xFF334155)
                                    remainingSegments <= 2 -> Color(0xFFEF4444)
                                    remainingSegments <= 4 -> Color(0xFFF59E0B)
                                    else -> Color(0xFF22C55E)
                                }
                                Box(
                                    modifier = Modifier
                                        .width(14.dp)
                                        .height(14.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(segmentColor)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // 5. MAIN PLAY AREA (Left: Hero Character, Center: Letter Blanks, Right: Slime Blob / Lab Setup)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // LEFT SIDE: Player's Hero Character wearing Lab Coat matching active top color
                    Box(
                        modifier = Modifier
                            .weight(0.35f)
                            .fillMaxHeight()
                            .padding(start = 4.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        val heroConfig = remember(uiState.avatar) { AvatarConfig.fromAvatar(uiState.avatar) }
                        val labCoatPath = "sprites/special/lab_meltdown/lab_meltdown_coat_${heroConfig.topColor}.png"
                        HeroSprite(
                            config = heroConfig,
                            modifier = Modifier
                                .fillMaxHeight(0.85f)
                                .aspectRatio(285f / 335f),
                            animationState = if (uiState.isCorrect == true) HeroAnimationState.VICTORY else HeroAnimationState.IDLE,
                            sway = false,
                            isJumping = false,
                            overrideTopPath = labCoatPath
                        )
                    }

                    // CENTER: Letter Blanks (Wrapped up to 2 rows for 10-12 letter words)
                    Column(
                        modifier = Modifier
                            .weight(0.65f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        LabLetterBlanksGrid(hangmanState.maskedWord)
                    }
                }

                // BOTTOM RIGHT: Cute Green Blob Creature (as in Mockup)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 12.dp, bottom = 4.dp)
                        .size(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🟢", fontSize = 42.sp)
                }
            }

            // 6. FULL A-Z KEYBOARD (Dark Purple Lab Styling with disabled/grayed-out keys)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                color = Color(0xFF1E1B4B).copy(alpha = 0.95f),
                border = BorderStroke(1.5.dp, Color(0xFFA855F7).copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val rows = listOf(
                        "qwertyuiop".toList(),
                        "asdfghjkl".toList(),
                        "zxcvbnm".toList()
                    )
                    rows.forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            row.forEach { letter ->
                                val isGuessed = hangmanState.guessedLetters.contains(letter)
                                val wordText = uiState.currentWord?.text?.lowercase() ?: ""
                                val isCorrect = isGuessed && wordText.contains(letter)

                                LabKeyTile(
                                    letter = letter,
                                    isGuessed = isGuessed,
                                    isCorrect = isCorrect,
                                    onClick = { viewModel.guessLetter(letter) },
                                    disabled = uiState.showFeedback || uiState.isFinished
                                )
                            }
                        }
                    }
                }
            }

            // Status Overlays
            if (uiState.isFinished) {
                ResultOverlay(uiState, onRetry = { viewModel.startGame() }, onBack = onBack)
            }
        }
    }
}

@Composable
private fun LabLetterBlanksGrid(maskedWord: String) {
    val cleanLetters = maskedWord.toList()
    val maxRow1 = if (cleanLetters.size > 6) (cleanLetters.size + 1) / 2 else cleanLetters.size
    val row1 = cleanLetters.take(maxRow1)
    val row2 = cleanLetters.drop(maxRow1)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // First Row
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            row1.forEach { char ->
                LabLetterBlankTile(char = char)
            }
        }
        // Second Row (if > 6 letters)
        if (row2.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row2.forEach { char ->
                    LabLetterBlankTile(char = char)
                }
            }
        }
    }
}

@Composable
private fun LabLetterBlankTile(char: Char) {
    Surface(
        modifier = Modifier
            .size(width = 42.dp, height = 48.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.9f),
        border = BorderStroke(2.dp, Color(0xFFA855F7))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = if (char == '_') "" else char.uppercaseChar().toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}

@Composable
private fun LabKeyTile(
    letter: Char,
    isGuessed: Boolean,
    isCorrect: Boolean,
    onClick: () -> Unit,
    disabled: Boolean
) {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    val bgColor = when {
        isCorrect -> Color(0xFF22C55E)
        isGuessed -> Color(0xFF334155)
        else -> Color(0xFF4C1D95)
    }

    Surface(
        modifier = Modifier
            .size(width = 32.dp, height = 40.dp)
            .scale(scale.value),
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        border = BorderStroke(1.dp, if (isGuessed) Color.Transparent else Color(0xFFA855F7)),
        enabled = !isGuessed && !disabled,
        onClick = {
            scope.launch {
                scale.animateTo(0.9f, tween(50))
                scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioLowBouncy))
                onClick()
            }
        }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = letter.uppercase(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isGuessed && !isCorrect) Color.Gray else Color.White
            )
        }
    }
}

@Composable
private fun LabChaosOverlay(effect: LabChaosEffect?, wrongCount: Int) {
    if (effect == null && wrongCount < 6) return

    val infiniteAnim = rememberInfiniteTransition(label = "chaos")
    val alphaAnim by infiniteAnim.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(400, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "flash"
    )

    when (effect) {
        LabChaosEffect.ALARM_FLASH, LabChaosEffect.POWER_SURGE -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Red.copy(alpha = alphaAnim * 0.4f))
            )
        }
        LabChaosEffect.BEAKER_OVERFLOW, LabChaosEffect.CHEMICAL_MIXUP -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF22C55E).copy(alpha = 0.25f))
            )
        }
        else -> {}
    }

    // Meltdown full effect on 6th mistake
    if (wrongCount >= 6) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Red.copy(alpha = alphaAnim * 0.5f))
        )
    }
}

@Composable
private fun ResultOverlay(
    uiState: GameUiState,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(28.dp)
                .background(Color(0xFF0F172A), RoundedCornerShape(24.dp))
                .border(BorderStroke(2.dp, if (uiState.isCorrect == true) Color(0xFF22C55E) else Color(0xFFEF4444)), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Text(
                if (uiState.isCorrect == true) "🧪 LAB STABILIZED! 🧪" else "💥 LAB MELTDOWN! 💥",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = if (uiState.isCorrect == true) Color(0xFF22C55E) else Color(0xFFEF4444),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            Text("The word was:", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
            Text(
                uiState.currentWord?.text?.uppercase() ?: "",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFA855F7)
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7))
            ) {
                Text("Play Next Word", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
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
