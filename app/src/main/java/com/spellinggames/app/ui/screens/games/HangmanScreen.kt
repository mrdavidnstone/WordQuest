package com.spellinggames.app.ui.screens.games

import android.content.Context
import android.graphics.BitmapFactory
import org.json.JSONObject
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
import com.spellinggames.app.util.MusicManager

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

    LaunchedEffect(Unit) {
        MusicManager.playMusic(context, "music/lab_meltdown.ogg")
    }

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF030712))
        ) {
            // 1. Full-screen Lab Scene Background Image
            if (bgBitmap != null) {
                Image(
                    bitmap = bgBitmap,
                    contentDescription = "Lab Scene Background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // 2. Full-Size Lab Meltdown Title Logo Overlay (Exact size & position matching gameplay)
            if (titleBitmap != null) {
                Image(
                    bitmap = titleBitmap,
                    contentDescription = "Lab Meltdown Title Overlay",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // 3. Foreground UI Layer (3-Tier Layout: Top Back Button, Center Info Card, Bottom Action Button)
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // TOP SECTION: Back Arrow Button (Identical position & design matching gameplay screen)
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

                    if (titleBitmap == null) {
                        Text("LAB MELTDOWN", color = Color(0xFFA855F7), fontWeight = FontWeight.Black, fontSize = 22.sp)
                    } else {
                        Spacer(Modifier.height(48.dp))
                    }

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
                    // Flexible top space to clear logo and vertically center the info card
                    Spacer(Modifier.weight(1f))

                    // CENTER SECTION: Game Information & Lesson Selector (Vertically Centered)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        shape = RoundedCornerShape(22.dp),
                        color = Color(0xFF0F172A).copy(alpha = 0.92f),
                        border = BorderStroke(1.5.dp, Color(0xFFA855F7).copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "Prevent the laboratory meltdown by spelling words correctly! Guess letters to keep the lab stable.",
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

                    // Flexible bottom space to keep card vertically centered
                    Spacer(Modifier.weight(1f))

                    // BOTTOM SECTION: Play Game Action Button (Thumb Zone)
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
                                    containerColor = Color(0xFFA855F7)
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

    // RANDOM UNSTABLE OBJECT STABILIZATION CONTROLLER
    var isArmStabilized by remember { mutableStateOf(false) }
    var isTankStabilized by remember { mutableStateOf(false) }

    // Pick a random unstable object to stabilize on each correct word or victory
    LaunchedEffect(uiState.isCorrect, uiState.isFinished) {
        if (uiState.isFinished && uiState.isCorrect == true) {
            isArmStabilized = true
            isTankStabilized = true
        } else if (uiState.isCorrect == true) {
            val unstableList = mutableListOf<String>()
            if (!isArmStabilized) unstableList.add("ARM")
            if (!isTankStabilized) unstableList.add("TANK")

            if (unstableList.isNotEmpty()) {
                val chosen = unstableList.random()
                if (chosen == "ARM") isArmStabilized = true
                if (chosen == "TANK") isTankStabilized = true
            }
        }
    }

    // Reset object stabilization state when a new game round starts
    LaunchedEffect(uiState.isReady) {
        if (uiState.isReady && uiState.score == 0 && !uiState.showFeedback) {
            isArmStabilized = false
            isTankStabilized = false
        }
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

        // 2. RIGHT PURPLE TANK ANIMATION (Constant Liquid Shimmer & Translucent Bubbles)
        RightTankOverlay()

        // 3. LEFT UNSTABLE ROBOT ARM ANIMATION STATE MACHINE
        RobotArmOverlay(
            isStabilized = isArmStabilized,
            wrongCount = hangmanState.wrongGuesses
        )

        // 4. GREEN UNSTABLE TANK ANIMATION (unstable -> settle -> stable_idle)
        GreenTankOverlay(
            isStabilized = isTankStabilized
        )

        // 3. ACTIVE LAB CHAOS ANIMATION OVERLAYS (Sparks, Steam, Flashes, Slime)
        LabChaosOverlay(effect = activeEffect, wrongCount = hangmanState.wrongGuesses)

        Column(modifier = Modifier.fillMaxSize()) {
            // 3. COMBINED TOP BAR: Back Arrow + Mistakes & Stability Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.88f),
                    border = BorderStroke(1.5.dp, Color(0xFFA855F7).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Mistakes Counter
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Mistakes",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "${hangmanState.wrongGuesses}/6",
                                color = if (hangmanState.wrongGuesses >= 5) Color(0xFFEF4444) else Color(0xFF22C55E),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (hangmanState.wrongGuesses >= 5) Color(0xFFEF4444) else Color(0xFF22C55E))
                            )
                        }

                        // Stability Bar
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Stability",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
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
                                            .width(12.dp)
                                            .height(12.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(segmentColor)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // 5. MAIN PLAY AREA (Top: Horizontally Centered Letter Blanks; Bottom-Left: Larger Hero Character)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(8.dp))

                // TOP SIDE: Horizontally Centered Letter Blanks
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LabLetterBlanksGrid(hangmanState.maskedWord)
                }

                Spacer(Modifier.weight(1f))

                // BOTTOM LEFT: Larger Hero Character standing on the far left edge above keyboard (No idle motion until victory)
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.BottomStart
                ) {
                    val heroConfig = remember(uiState.avatar) { AvatarConfig.fromAvatar(uiState.avatar) }
                    val labCoatPath = "sprites/special/lab_meltdown/lab_meltdown_coat_${heroConfig.topColor}.png"
                    HeroSprite(
                        config = heroConfig,
                        modifier = Modifier
                            .height(275.dp)
                            .aspectRatio(285f / 335f),
                        animationState = if (uiState.isCorrect == true) HeroAnimationState.VICTORY else HeroAnimationState.IDLE,
                        sway = false,
                        breath = false,
                        isJumping = false,
                        overrideTopPath = labCoatPath
                    )
                }
            }

            var isCapitalized by remember { mutableStateOf(false) }
            var isNumbersMode by remember { mutableStateOf(false) }
            val requireCapsSetting = uiState.requireCapitalLetters

            // 6. FULL KEYBOARD WITH DYNAMIC CAPITALIZATION & NUMBERS MODE
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                color = Color(0xFF1E1B4B).copy(alpha = 0.95f),
                border = BorderStroke(1.5.dp, Color(0xFFA855F7).copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (!isNumbersMode) {
                        // --- LETTERS MODE ---
                        val rawRow1 = "qwertyuiop".toList()
                        val rawRow2 = "asdfghjkl".toList()
                        val rawRow3 = "zxcvbnm".toList()

                        val targetWord = uiState.currentWord?.text ?: ""

                        // Row 1 (Q to P)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)
                        ) {
                            rawRow1.forEach { rawChar ->
                                val letterChar = if (!requireCapsSetting) rawChar.uppercaseChar() else if (isCapitalized) rawChar.uppercaseChar() else rawChar.lowercaseChar()
                                val isGuessed = if (requireCapsSetting) hangmanState.guessedLetters.contains(letterChar) else (hangmanState.guessedLetters.contains(rawChar) || hangmanState.guessedLetters.contains(rawChar.uppercaseChar()))
                                val isCorrect = if (requireCapsSetting) (isGuessed && targetWord.contains(letterChar)) else (isGuessed && targetWord.contains(rawChar, ignoreCase = true))

                                LabKeyTile(
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
                            horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)
                        ) {
                            rawRow2.forEach { rawChar ->
                                val letterChar = if (!requireCapsSetting) rawChar.uppercaseChar() else if (isCapitalized) rawChar.uppercaseChar() else rawChar.lowercaseChar()
                                val isGuessed = if (requireCapsSetting) hangmanState.guessedLetters.contains(letterChar) else (hangmanState.guessedLetters.contains(rawChar) || hangmanState.guessedLetters.contains(rawChar.uppercaseChar()))
                                val isCorrect = if (requireCapsSetting) (isGuessed && targetWord.contains(letterChar)) else (isGuessed && targetWord.contains(rawChar, ignoreCase = true))

                                LabKeyTile(
                                    displayLabel = letterChar.toString(),
                                    isGuessed = isGuessed,
                                    isCorrect = isCorrect,
                                    onClick = { viewModel.guessLetter(letterChar) },
                                    disabled = uiState.showFeedback || uiState.isFinished,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Row 3 (Optional Shift Key if Require Capitals is ON + Z to M + Mode Switcher 123)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)
                        ) {
                            // SHIFT / CAPITALIZE KEY (Only shown if requireCapsSetting is ON)
                            if (requireCapsSetting) {
                                LabKeyTile(
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
                                val isGuessed = if (requireCapsSetting) hangmanState.guessedLetters.contains(letterChar) else (hangmanState.guessedLetters.contains(rawChar) || hangmanState.guessedLetters.contains(rawChar.uppercaseChar()))
                                val isCorrect = if (requireCapsSetting) (isGuessed && targetWord.contains(letterChar)) else (isGuessed && targetWord.contains(rawChar, ignoreCase = true))

                                LabKeyTile(
                                    displayLabel = letterChar.toString(),
                                    isGuessed = isGuessed,
                                    isCorrect = isCorrect,
                                    onClick = { viewModel.guessLetter(letterChar) },
                                    disabled = uiState.showFeedback || uiState.isFinished,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // MODE SWITCHER TO NUMBERS/PUNCTUATION (123)
                            LabKeyTile(
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
                        // --- NUMBERS & PUNCTUATION MODE ---
                        val numRow1 = "1234567890".toList()
                        val numRow2 = "@#$%&-+()/".toList()
                        val numRow3 = "*\"':;!?,.".toList()

                        val targetWord = uiState.currentWord?.text ?: ""

                        // Row 1 (Numbers 1-0)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)
                        ) {
                            numRow1.forEach { symbol ->
                                val isGuessed = hangmanState.guessedLetters.contains(symbol)
                                val isCorrect = isGuessed && targetWord.contains(symbol)

                                LabKeyTile(
                                    displayLabel = symbol.toString(),
                                    isGuessed = isGuessed,
                                    isCorrect = isCorrect,
                                    onClick = { viewModel.guessLetter(symbol) },
                                    disabled = uiState.showFeedback || uiState.isFinished,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Row 2 (Symbols)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)
                        ) {
                            numRow2.forEach { symbol ->
                                val isGuessed = hangmanState.guessedLetters.contains(symbol)
                                val isCorrect = isGuessed && targetWord.contains(symbol)

                                LabKeyTile(
                                    displayLabel = symbol.toString(),
                                    isGuessed = isGuessed,
                                    isCorrect = isCorrect,
                                    onClick = { viewModel.guessLetter(symbol) },
                                    disabled = uiState.showFeedback || uiState.isFinished,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Row 3 (ABC Mode Switcher + Punctuation)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)
                        ) {
                            // MODE SWITCHER BACK TO LETTERS (ABC)
                            LabKeyTile(
                                displayLabel = "ABC",
                                isGuessed = false,
                                isCorrect = false,
                                onClick = { isNumbersMode = false },
                                disabled = false,
                                modifier = Modifier.weight(1.5f),
                                isSpecialKey = true
                            )

                            numRow3.forEach { symbol ->
                                val isGuessed = hangmanState.guessedLetters.contains(symbol)
                                val isCorrect = isGuessed && targetWord.contains(symbol)

                                LabKeyTile(
                                    displayLabel = symbol.toString(),
                                    isGuessed = isGuessed,
                                    isCorrect = isCorrect,
                                    onClick = { viewModel.guessLetter(symbol) },
                                    disabled = uiState.showFeedback || uiState.isFinished,
                                    modifier = Modifier.weight(1f)
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
    // Check if the masked word contains spaces (multi-word phrase)
    val wordRows = remember(maskedWord) {
        if (maskedWord.contains(' ')) {
            // Split by space so each word gets its own row (skipping a row for spaces)
            maskedWord.split(' ').filter { it.isNotEmpty() }
        } else {
            // Single word: if longer than 7 letters, split into 2 balanced rows
            if (maskedWord.length > 7) {
                val mid = (maskedWord.length + 1) / 2
                listOf(maskedWord.take(mid), maskedWord.drop(mid))
            } else {
                listOf(maskedWord)
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        wordRows.forEach { wordChunk ->
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                wordChunk.forEach { char ->
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
            .size(width = 38.dp, height = 44.dp),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.92f),
        border = BorderStroke(2.dp, Color(0xFFA855F7))
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
private fun LabKeyTile(
    displayLabel: String,
    isGuessed: Boolean,
    isCorrect: Boolean,
    onClick: () -> Unit,
    disabled: Boolean,
    modifier: Modifier = Modifier,
    isSpecialKey: Boolean = false,
    isActiveKey: Boolean = false
) {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    val bgColor = when {
        isActiveKey -> Color(0xFFA855F7)
        isCorrect -> Color(0xFF22C55E)
        isGuessed && !isSpecialKey -> Color(0xFF334155)
        isSpecialKey -> Color(0xFF3730A3)
        else -> Color(0xFF4C1D95)
    }

    Surface(
        modifier = modifier
            .height(40.dp)
            .scale(scale.value),
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        border = BorderStroke(1.dp, if (isActiveKey) Color.White else if (isGuessed && !isSpecialKey) Color.Transparent else Color(0xFFA855F7)),
        enabled = (!isGuessed || isSpecialKey) && !disabled,
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
                text = displayLabel,
                fontSize = if (displayLabel.length > 2) 13.sp else 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isGuessed && !isCorrect && !isSpecialKey) Color.Gray else Color.White,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun LabChaosOverlay(effect: LabChaosEffect?, wrongCount: Int) {
    val context = LocalContext.current
    val sparkBitmap = remember(context) {
        loadAssetImageBitmap(context, "games/lab_meltdown/animations/spark.png")
    }

    val isSparkActive = effect in listOf(
        LabChaosEffect.CONSOLE_SPARKS,
        LabChaosEffect.POWER_SURGE,
        LabChaosEffect.ROBOT_ARM_GLITCH,
        LabChaosEffect.ALARM_FLASH
    ) || wrongCount >= 4

    if (effect == null && wrongCount < 4) return

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

    // Spark Animation Overlay
    SparkAnimationOverlay(sparkBitmap = sparkBitmap, isActive = isSparkActive)

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
private fun SparkAnimationOverlay(
    sparkBitmap: ImageBitmap?,
    isActive: Boolean
) {
    if (sparkBitmap == null || !isActive) return

    val infiniteTransition = rememberInfiniteTransition(label = "spark_anim")

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(110, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "spark_alpha"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(160, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "spark_scale"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Left Robot Arm Spark
        Image(
            bitmap = sparkBitmap,
            contentDescription = "Console Spark",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp, top = 55.dp)
                .size(130.dp)
                .graphicsLayer {
                    this.alpha = alpha
                    scaleX = scale
                    scaleY = scale
                    rotationZ = -15f
                },
            contentScale = ContentScale.Fit
        )

        // Right Console Spark
        Image(
            bitmap = sparkBitmap,
            contentDescription = "Power Spark",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 16.dp, top = 65.dp)
                .size(140.dp)
                .graphicsLayer {
                    this.alpha = alpha * 0.95f
                    scaleX = scale * 1.1f
                    scaleY = scale * 1.1f
                    rotationZ = 35f
                },
            contentScale = ContentScale.Fit
        )

        // Center Power Grid Spark
        Image(
            bitmap = sparkBitmap,
            contentDescription = "Center Spark",
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 50.dp)
                .size(145.dp)
                .graphicsLayer {
                    this.alpha = alpha * 0.85f
                    scaleX = scale
                    scaleY = scale
                    rotationZ = 180f
                },
            contentScale = ContentScale.Fit
        )
    }
}

enum class RobotArmState {
    UNSTABLE,
    WAITING_TO_SETTLE,
    SETTLING,
    STABLE_HOLD,
    STABLE_IDLE
}

@Composable
private fun RobotArmOverlay(
    isStabilized: Boolean,
    wrongCount: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var armState by remember { mutableStateOf(RobotArmState.UNSTABLE) }
    var phaseStart by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var currentFrameIndex by remember { mutableIntStateOf(0) }

    val randomOffset = remember { (0..800).random().toLong() }

    LaunchedEffect(isStabilized) {
        if (isStabilized && armState == RobotArmState.UNSTABLE) {
            armState = RobotArmState.WAITING_TO_SETTLE
        } else if (!isStabilized) {
            armState = RobotArmState.UNSTABLE
            phaseStart = System.currentTimeMillis()
        }
    }

    // Ticker loop driving the state machine and exact frame timing
    LaunchedEffect(armState) {
        if (armState == RobotArmState.STABLE_HOLD) {
            delay(1500L)
            armState = RobotArmState.STABLE_IDLE
            phaseStart = System.currentTimeMillis()
            currentFrameIndex = 0
            return@LaunchedEffect
        }

        while (true) {
            delay(16L) // ~60fps ticker
            val now = System.currentTimeMillis()

            when (armState) {
                RobotArmState.UNSTABLE -> {
                    val elapsed = (now - phaseStart) + randomOffset
                    currentFrameIndex = ((elapsed * 12L) / 1000L).toInt() % 24
                }
                RobotArmState.WAITING_TO_SETTLE -> {
                    val elapsed = (now - phaseStart) + randomOffset
                    val idx = ((elapsed * 12L) / 1000L).toInt() % 24
                    currentFrameIndex = idx
                    // Cut precisely at frame 9 of L3_meltdown to match settle_to_normal start pose!
                    if (idx == 9) {
                        armState = RobotArmState.SETTLING
                        phaseStart = System.currentTimeMillis()
                        currentFrameIndex = 0
                    }
                }
                RobotArmState.SETTLING -> {
                    val elapsed = now - phaseStart
                    val idx = ((elapsed * 12L) / 1000L).toInt()
                    if (idx >= 16) {
                        armState = RobotArmState.STABLE_HOLD
                        phaseStart = System.currentTimeMillis()
                        currentFrameIndex = 0
                    } else {
                        currentFrameIndex = idx
                    }
                }
                RobotArmState.STABLE_IDLE -> {
                    val elapsed = now - phaseStart
                    currentFrameIndex = ((elapsed * 12L) / 1000L).toInt() % 48
                }
                else -> {}
            }
        }
    }

    // Resolve current frame asset paths
    val formattedFrame = if (currentFrameIndex < 10) "0$currentFrameIndex" else "$currentFrameIndex"

    val (armPath, sparkPath) = when (armState) {
        RobotArmState.UNSTABLE, RobotArmState.WAITING_TO_SETTLE -> {
            "games/lab_meltdown/animations/left_robot_arm/L3_meltdown/arm_$formattedFrame.png" to
            "games/lab_meltdown/animations/left_robot_arm/L3_meltdown_sparks/spark_$formattedFrame.png"
        }
        RobotArmState.SETTLING -> {
            "games/lab_meltdown/animations/left_robot_arm/settle_to_normal/arm_$formattedFrame.png" to null
        }
        RobotArmState.STABLE_HOLD -> {
            "games/lab_meltdown/animations/left_robot_arm/arm_normal.png" to null
        }
        RobotArmState.STABLE_IDLE -> {
            "games/lab_meltdown/animations/left_robot_arm/stable_idle/arm_$formattedFrame.png" to null
        }
    }

    val armBitmap = remember(context, armPath) {
        loadAssetImageBitmap(context, armPath)
    }

    val sparkBitmap = remember(context, sparkPath) {
        if (sparkPath != null) loadAssetImageBitmap(context, sparkPath) else null
    }

    val animConfig = remember(context) {
        LabAnimConfig.load(
            context,
            "games/lab_meltdown/animations/left_robot_arm/animation.json",
            defaultW = 496f, defaultH = 528f, defaultX = -98f, defaultY = 428f
        )
    }

    // Matrix Coordinate Scaling matching 851x1849 Game Canvas from animation.json
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val canvasW = this.maxWidth
        val canvasH = this.maxHeight

        val armW = canvasW * (animConfig.frameW / animConfig.canvasW)
        val armH = canvasH * (animConfig.frameH / animConfig.canvasH)
        val armX = canvasW * (animConfig.drawX / animConfig.canvasW)
        val armY = canvasH * (animConfig.drawY / animConfig.canvasH)

        Box(
            modifier = Modifier
                .offset(x = armX, y = armY)
                .size(width = armW, height = armH)
        ) {
            if (armBitmap != null) {
                Image(
                    bitmap = armBitmap,
                    contentDescription = "Robot Arm Frame",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            if (sparkBitmap != null) {
                Image(
                    bitmap = sparkBitmap,
                    contentDescription = "Robot Arm Spark Frame",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

enum class GreenTankState {
    UNSTABLE,
    SETTLING,
    STABLE_IDLE
}

@Composable
private fun GreenTankOverlay(
    isStabilized: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var tankState by remember { mutableStateOf(GreenTankState.UNSTABLE) }
    var phaseStart by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var currentFrameIndex by remember { mutableIntStateOf(0) }

    val randomOffset = remember { (0..4000).random().toLong() }

    LaunchedEffect(isStabilized) {
        if (isStabilized && tankState == GreenTankState.UNSTABLE) {
            tankState = GreenTankState.SETTLING
            phaseStart = System.currentTimeMillis()
            currentFrameIndex = 0
        } else if (!isStabilized) {
            tankState = GreenTankState.UNSTABLE
            phaseStart = System.currentTimeMillis()
        }
    }

    LaunchedEffect(tankState) {
        while (true) {
            delay(16L) // ~60fps ticker
            val now = System.currentTimeMillis()

            when (tankState) {
                GreenTankState.UNSTABLE -> {
                    val elapsed = (now - phaseStart) + randomOffset
                    currentFrameIndex = ((elapsed * 12L) / 1000L).toInt() % 48
                }
                GreenTankState.SETTLING -> {
                    val elapsed = now - phaseStart
                    val idx = ((elapsed * 12L) / 1000L).toInt()
                    if (idx >= 24) {
                        tankState = GreenTankState.STABLE_IDLE
                        phaseStart = System.currentTimeMillis()
                        currentFrameIndex = 0
                    } else {
                        currentFrameIndex = idx
                    }
                }
                GreenTankState.STABLE_IDLE -> {
                    val elapsed = now - phaseStart
                    currentFrameIndex = ((elapsed * 12L) / 1000L).toInt() % 48
                }
            }
        }
    }

    val formattedFrame = if (currentFrameIndex < 10) "0$currentFrameIndex" else "$currentFrameIndex"
    val tankPath = when (tankState) {
        GreenTankState.UNSTABLE -> {
            "games/lab_meltdown/animations/green_tank/tank_unstable/tank_$formattedFrame.png"
        }
        GreenTankState.SETTLING -> {
            "games/lab_meltdown/animations/green_tank/tank_settle/tank_$formattedFrame.png"
        }
        GreenTankState.STABLE_IDLE -> {
            "games/lab_meltdown/animations/green_tank/tank_stable_idle/tank_$formattedFrame.png"
        }
    }

    val tankBitmap = remember(context, tankPath) {
        loadAssetImageBitmap(context, tankPath)
    }

    val animConfig = remember(context) {
        LabAnimConfig.load(
            context,
            "games/lab_meltdown/animations/green_tank/animation.json",
            defaultW = 186f, defaultH = 486f, defaultX = 369f, defaultY = 557f
        )
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val canvasW = this.maxWidth
        val canvasH = this.maxHeight

        val tankW = canvasW * (animConfig.frameW / animConfig.canvasW)
        val tankH = canvasH * (animConfig.frameH / animConfig.canvasH)
        val tankX = canvasW * (animConfig.drawX / animConfig.canvasW)
        val tankY = canvasH * (animConfig.drawY / animConfig.canvasH)

        Box(
            modifier = Modifier
                .offset(x = tankX, y = tankY)
                .size(width = tankW, height = tankH)
        ) {
            if (tankBitmap != null) {
                Image(
                    bitmap = tankBitmap,
                    contentDescription = "Green Tank Frame",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
private fun RightTankOverlay(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentFrameIndex by remember { mutableIntStateOf(0) }
    val randomOffset = remember { (0..4000).random().toLong() }

    val animConfig = remember(context) {
        LabAnimConfig.load(
            context,
            "games/lab_meltdown/animations/right_tank/animation.json",
            defaultW = 130f, defaultH = 214f, defaultX = 544f, defaultY = 588f
        )
    }

    // Always-running 48 frames @ 12fps ticker loop
    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        while (true) {
            delay(16L) // ~60fps
            val now = System.currentTimeMillis()
            val elapsed = (now - startTime) + randomOffset
            currentFrameIndex = ((elapsed * 12L) / 1000L).toInt() % 48
        }
    }

    val formattedFrame = if (currentFrameIndex < 10) "0$currentFrameIndex" else "$currentFrameIndex"
    val tankPath = "games/lab_meltdown/animations/right_tank/tank_stable_idle/tank_$formattedFrame.png"

    val tankBitmap = remember(context, tankPath) {
        loadAssetImageBitmap(context, tankPath)
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val canvasW = this.maxWidth
        val canvasH = this.maxHeight

        val tankW = canvasW * (animConfig.frameW / animConfig.canvasW)
        val tankH = canvasH * (animConfig.frameH / animConfig.canvasH)
        val tankX = canvasW * (animConfig.drawX / animConfig.canvasW)
        val tankY = canvasH * (animConfig.drawY / animConfig.canvasH)

        Box(
            modifier = Modifier
                .offset(x = tankX, y = tankY)
                .size(width = tankW, height = tankH)
        ) {
            if (tankBitmap != null) {
                Image(
                    bitmap = tankBitmap,
                    contentDescription = "Right Purple Tank Frame",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

data class LabAnimConfig(
    val canvasW: Float,
    val canvasH: Float,
    val frameW: Float,
    val frameH: Float,
    val drawX: Float,
    val drawY: Float
) {
    companion object {
        fun load(context: Context, jsonPath: String, defaultW: Float, defaultH: Float, defaultX: Float, defaultY: Float): LabAnimConfig {
            return try {
                context.assets.open(jsonPath).use { stream ->
                    val jsonStr = stream.bufferedReader().use { it.readText() }
                    val json = JSONObject(jsonStr)

                    val canvasArr = json.optJSONArray("game_canvas")
                    val cW = canvasArr?.optDouble(0, 851.0)?.toFloat() ?: 851f
                    val cH = canvasArr?.optDouble(1, 1849.0)?.toFloat() ?: 1849f

                    val frameArr = json.optJSONArray("frame_size")
                    val fW = frameArr?.optDouble(0, defaultW.toDouble())?.toFloat() ?: defaultW
                    val fH = frameArr?.optDouble(1, defaultH.toDouble())?.toFloat() ?: defaultH

                    val drawArr = json.optJSONArray("draw_at_xy")
                    var dX = drawArr?.optDouble(0, defaultX.toDouble())?.toFloat() ?: defaultX
                    var dY = drawArr?.optDouble(1, defaultY.toDouble())?.toFloat() ?: defaultY

                    if (dX == defaultX && dY == defaultY) {
                        val seqObj = json.optJSONObject("sequences")
                        val firstSeq = seqObj?.keys()?.asSequence()?.firstOrNull()?.let { seqObj.optJSONObject(it) }
                        val seqDraw = firstSeq?.optJSONArray("draw_at_xy")
                        if (seqDraw != null) {
                            dX = seqDraw.optDouble(0, dX.toDouble()).toFloat()
                            dY = seqDraw.optDouble(1, dY.toDouble()).toFloat()
                        }
                    }

                    LabAnimConfig(cW, cH, fW, fH, dX, dY)
                }
            } catch (_: Exception) {
                LabAnimConfig(851f, 1849f, defaultW, defaultH, defaultX, defaultY)
            }
        }
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
    val pathsToTry = listOf(
        "games/lab_meltdown/$assetName",
        assetName
    )
    for (path in pathsToTry) {
        try {
            context.assets.open(path).use { stream ->
                BitmapFactory.decodeStream(stream)?.asImageBitmap()?.let { return it }
            }
        } catch (_: Exception) {}
    }
    return null
}
