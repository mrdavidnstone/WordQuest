package com.spellarcade.app.ui.screens.games

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
import com.spellarcade.app.data.model.AvatarConfig
import com.spellarcade.app.ui.components.*
import com.spellarcade.app.ui.components.avatar.HeroAnimationState
import com.spellarcade.app.ui.components.avatar.HeroSprite
import com.spellarcade.app.ui.screens.profile.character.CharacterLayerType
import com.spellarcade.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// 10 Lab Chaos Animations (No repeats in the same round)
import com.spellarcade.app.util.MusicManager

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
fun LabMeltdownScreen(
    onBack: () -> Unit,
    viewModel: LabMeltdownViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val labMeltdownState by viewModel.labMeltdownState.collectAsState()

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

    LaunchedEffect(labMeltdownState.wrongGuesses) {
        if (labMeltdownState.wrongGuesses > 0 && !uiState.isFinished) {
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
            delay(1200)
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

    // RANDOM UNSTABLE OBJECT STABILIZATION CONTROLLER (Arm, Green Tank, Alien, Monitor, Spark, Steam Pipe)
    var isArmStabilized by remember { mutableStateOf(false) }
    var isTankStabilized by remember { mutableStateOf(false) }
    var isAlienStabilized by remember { mutableStateOf(false) }
    var isMonitorStabilized by remember { mutableStateOf(false) }
    var isSparkStabilized by remember { mutableStateOf(false) }
    var isSteamPipeStabilized by remember { mutableStateOf(false) }

    val maxAllowedMissedWords = when (uiState.overallDifficulty) {
        1 -> 6 // Easy: Game Over at 6 missed words
        2 -> 5 // Medium: Game Over at 5 missed words
        3 -> 4 // Difficult/Hard: Game Over at 4 missed words
        else -> 6
    }

    val missedWords = (uiState.total - uiState.score).coerceAtLeast(0)
    val isGameLost = uiState.isFinished && (uiState.score < 7 || missedWords >= maxAllowedMissedWords)

    // LOSS MELTDOWN TIMELINE CONTROLLER
    var isUserLosesSequenceActive by remember { mutableStateOf(false) }
    var isDoorSequenceStable by remember { mutableStateOf(false) }
    var showResultModal by remember { mutableStateOf(false) }

    LaunchedEffect(isGameLost) {
        if (isGameLost) {
            // Step 1: Immediately activate all unstable animations
            isArmStabilized = false
            isTankStabilized = false
            isAlienStabilized = false
            isMonitorStabilized = false
            isSparkStabilized = false
            isSteamPipeStabilized = false

            // Step 2: Wait 3 seconds before starting meltdown blast door animation
            delay(3000L)
            isUserLosesSequenceActive = true
        } else {
            isUserLosesSequenceActive = false
            isDoorSequenceStable = false
            showResultModal = false
        }
    }

    LaunchedEffect(isDoorSequenceStable) {
        if (isDoorSequenceStable) {
            // Step 3: Once blast doors are closed, wait 2 seconds before showing popup modal
            delay(2000L)
            showResultModal = true
        }
    }

    // Pick a random unstable object to stabilize on each correct word or victory
    LaunchedEffect(uiState.isCorrect, uiState.isFinished) {
        if (uiState.isFinished) {
            if (uiState.score >= 7) {
                // Game Won - Stabilize all lab systems!
                isArmStabilized = true
                isTankStabilized = true
                isAlienStabilized = true
                isMonitorStabilized = true
                isSparkStabilized = true
                isSteamPipeStabilized = true
            } else {
                // Game Lost - Activate Full Meltdown Mode on all lab systems!
                isArmStabilized = false
                isTankStabilized = false
                isAlienStabilized = false
                isMonitorStabilized = false
                isSparkStabilized = false
                isSteamPipeStabilized = false
            }
        } else if (uiState.isCorrect == true) {
            val unstableList = mutableListOf<String>()
            if (!isArmStabilized) unstableList.add("ARM")
            if (!isTankStabilized) unstableList.add("TANK")
            if (!isAlienStabilized) unstableList.add("ALIEN")
            if (!isMonitorStabilized) unstableList.add("MONITOR")
            if (!isSparkStabilized) unstableList.add("SPARK")
            if (!isSteamPipeStabilized) unstableList.add("STEAM_PIPE")

            if (unstableList.isNotEmpty()) {
                val chosen = unstableList.random()
                if (chosen == "ARM") isArmStabilized = true
                if (chosen == "TANK") isTankStabilized = true
                if (chosen == "ALIEN") isAlienStabilized = true
                if (chosen == "MONITOR") isMonitorStabilized = true
                if (chosen == "SPARK") isSparkStabilized = true
                if (chosen == "STEAM_PIPE") isSteamPipeStabilized = true
            }
        }
    }

    // Reset object stabilization state when a new game round starts
    LaunchedEffect(uiState.isFinished, uiState.score, uiState.total) {
        if (!uiState.isFinished && uiState.score == 0 && uiState.total == 0) {
            isArmStabilized = false
            isTankStabilized = false
            isAlienStabilized = false
            isMonitorStabilized = false
            isSparkStabilized = false
            isSteamPipeStabilized = false
            isUserLosesSequenceActive = false
            isDoorSequenceStable = false
            showResultModal = false
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

        // 2. LAB STATUS OVERLAY DIRECTLY ON TOP OF BACKGROUND IMAGE (starts at 7.png, steps down 1 number on each correct word, ending at 0.png when stable)
        val currentStatusNum = when {
            isGameLost -> 7
            uiState.score >= 7 -> 0
            else -> (7 - uiState.score).coerceIn(0, 7)
        }
        StatusOverlay(statusNum = currentStatusNum)

        // 3. RIGHT PURPLE TANK ANIMATION (Constant Liquid Shimmer & Translucent Bubbles)
        RightTankOverlay()

        // 3. LEFT UNSTABLE ROBOT ARM ANIMATION STATE MACHINE
        RobotArmOverlay(
            isStabilized = isArmStabilized,
            wrongCount = labMeltdownState.wrongGuesses
        )

        // 4. GREEN UNSTABLE TANK ANIMATION (unstable -> settle -> stable_idle)
        GreenTankOverlay(
            isStabilized = isTankStabilized
        )

        // 5. CUTE ALIEN MASCOT ANIMATION (unstable story loop -> settle -> stable_idle)
        AlienOverlay(
            isStabilized = isAlienStabilized
        )

        // 6. LAB MONITOR SCREEN ANIMATION (glitch warning -> settle -> system online)
        MonitorOverlay(
            isStabilized = isMonitorStabilized
        )

        // 7. CONSOLE SPARK BURST ANIMATION OVERLAYS
        SparkOverlay(
            isStabilized = isSparkStabilized
        )

        // 8. STEAM PIPE ANIMATION (unstable leak -> settle -> stable_idle)
        SteamPipeOverlay(
            isStabilized = isSteamPipeStabilized
        )

        // 8. ACTIVE LAB CHAOS OVERLAYS (Flashes, Steam, Slime)
        LabChaosOverlay(
            effect = if (isGameLost) LabChaosEffect.ALARM_FLASH else activeEffect,
            wrongCount = if (isGameLost) 6 else labMeltdownState.wrongGuesses,
            isSparkStabilized = isSparkStabilized
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // COMBINED TOP BAR: Back Arrow + Status Stepping Bar + Win/Miss Progress Counters
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

                // CRITICAL REACTOR STATUS & MISTAKES HEADER BAR (Matching Concept Art)
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.92f),
                    border = BorderStroke(1.5.dp, Color(0xFFA855F7).copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // LEFT SIDE: Misses in current word (0/maxMisses rounded boxes in warning neon red)
                        val maxMisses = labMeltdownState.maxWrong
                        val currentMisses = labMeltdownState.wrongGuesses.coerceIn(0, maxMisses)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Column {
                                Text(
                                    "Misses",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "$currentMisses/$maxMisses",
                                    color = if (currentMisses >= maxMisses - 1) Color(0xFFEF4444) else Color(0xFF22C55E),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            // Dynamic Rounded Rectangular Segments based on Difficulty (6 for Easy, 4 for Medium, 3 for Hard)
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                for (i in 1..maxMisses) {
                                    val isFilled = i <= currentMisses
                                    val boxColor = if (isFilled) Color(0xFFEF4444) else Color(0xFF1E293B)
                                    val borderColor = if (isFilled) Color(0xFFF87171) else Color(0xFF334155)

                                    Box(
                                        modifier = Modifier
                                            .width(9.dp)
                                            .height(16.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(boxColor)
                                            .border(1.dp, borderColor, RoundedCornerShape(3.dp))
                                    )
                                }
                            }
                        }

                        // VERTICAL DIVIDER SEPARATOR
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(26.dp)
                                .background(Color.White.copy(alpha = 0.15f))
                        )

                        // RIGHT SIDE: Stability Bar (0/7 to 7/7 rounded boxes in glowing neon green)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "Stability",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                val currentStability = uiState.score.coerceIn(0, 7)
                                Text(
                                    "$currentStability/7",
                                    color = if (currentStability >= 7) Color(0xFF22C55E) else if (currentStability <= 2) Color(0xFFEF4444) else Color(0xFF22C55E),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            // Exactly 7 Rounded Rectangular Segments for Stability (Glowing Neon Green)
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                val currentStability = uiState.score.coerceIn(0, 7)
                                for (i in 1..7) {
                                    val isFilled = i <= currentStability
                                    val boxColor = if (isFilled) Color(0xFF22C55E) else Color(0xFF1E293B)
                                    val borderColor = if (isFilled) Color(0xFF4ADE80) else Color(0xFF334155)

                                    Box(
                                        modifier = Modifier
                                            .width(10.dp)
                                            .height(16.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(boxColor)
                                            .border(1.dp, borderColor, RoundedCornerShape(3.dp))
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
                    LabLetterBlanksGrid(labMeltdownState.maskedWord)
                }

                Spacer(Modifier.weight(1f))

                // BOTTOM LEFT: Larger Hero Character standing on the far left edge above keyboard (Hazard suit on loss, lab coat during gameplay)
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.BottomStart
                ) {
                    val heroConfig = remember(uiState.avatar) { AvatarConfig.fromAvatar(uiState.avatar) }
                    val labCoatPath = if (isGameLost) {
                        "sprites/special/lab_meltdown/lab_meltdown_hazard_suite.png"
                    } else {
                        "sprites/special/lab_meltdown/lab_meltdown_coat_${heroConfig.topColor}.png"
                    }
                    val heroLayerVisibility = if (isGameLost) {
                        mapOf(
                            CharacterLayerType.SHOES to false,
                            CharacterLayerType.BOTTOM to false,
                            CharacterLayerType.HAIR to false,
                            CharacterLayerType.HAT to false
                        )
                    } else {
                        emptyMap()
                    }
                    HeroSprite(
                        config = heroConfig,
                        modifier = Modifier
                            .height(275.dp)
                            .aspectRatio(285f / 335f),
                        animationState = if (uiState.isCorrect == true) HeroAnimationState.VICTORY else HeroAnimationState.IDLE,
                        sway = false,
                        breath = false,
                        isJumping = false,
                        overrideTopPath = labCoatPath,
                        layerVisibility = heroLayerVisibility
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
                    .graphicsLayer { alpha = if (isUserLosesSequenceActive) 0f else 1f }
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
                                val isGuessed = if (requireCapsSetting) labMeltdownState.guessedLetters.contains(letterChar) else (labMeltdownState.guessedLetters.contains(rawChar) || labMeltdownState.guessedLetters.contains(rawChar.uppercaseChar()))
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
                                val isGuessed = if (requireCapsSetting) labMeltdownState.guessedLetters.contains(letterChar) else (labMeltdownState.guessedLetters.contains(rawChar) || labMeltdownState.guessedLetters.contains(rawChar.uppercaseChar()))
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
                                val isGuessed = if (requireCapsSetting) labMeltdownState.guessedLetters.contains(letterChar) else (labMeltdownState.guessedLetters.contains(rawChar) || labMeltdownState.guessedLetters.contains(rawChar.uppercaseChar()))
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
                                val isGuessed = labMeltdownState.guessedLetters.contains(symbol)
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
                                val isGuessed = labMeltdownState.guessedLetters.contains(symbol)
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
                                val isGuessed = labMeltdownState.guessedLetters.contains(symbol)
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
        }

        // 9. USER LOSES MELTDOWN ANIMATION (ON TOP OF EVERYTHING: Hero, Keyboard, Header, Scene)
        UserLosesOverlay(
            isLossActive = isUserLosesSequenceActive && !(uiState.isFinished && showResultModal),
            onAnimationComplete = { isDoorSequenceStable = true }
        )

        // 10. GAME OVER RESULT MODAL OVERLAY (ON TOP OF USER LOSES BLAST DOORS)
        if (uiState.isFinished && (uiState.score >= 7 || showResultModal)) {
            ResultOverlay(
                uiState = uiState,
                onRetry = {
                    isArmStabilized = false
                    isTankStabilized = false
                    isAlienStabilized = false
                    isMonitorStabilized = false
                    isSparkStabilized = false
                    isSteamPipeStabilized = false
                    isUserLosesSequenceActive = false
                    isDoorSequenceStable = false
                    showResultModal = false
                    viewModel.startGame()
                },
                onBack = onBack
            )
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
private fun LabChaosOverlay(
    effect: LabChaosEffect?,
    wrongCount: Int,
    isSparkStabilized: Boolean = false
) {
    val context = LocalContext.current
    val sparkBitmap = remember(context) {
        loadAssetImageBitmap(context, "games/lab_meltdown/animations/spark.png")
    }

    val isSparkActive = (!isSparkStabilized) && (
        effect in listOf(
            LabChaosEffect.CONSOLE_SPARKS,
            LabChaosEffect.POWER_SURGE,
            LabChaosEffect.ROBOT_ARM_GLITCH,
            LabChaosEffect.ALARM_FLASH
        ) || wrongCount >= 4
    )

    if (effect == null && wrongCount < 4 && !isSparkActive) return

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

enum class AlienState {
    UNSTABLE,
    SETTLING,
    STABLE_IDLE
}

@Composable
private fun AlienOverlay(
    isStabilized: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var alienState by remember { mutableStateOf(AlienState.UNSTABLE) }
    var currentFrameIndex by remember { mutableIntStateOf(0) }
    val randomOffset = remember { (0..3000).random().toLong() }

    LaunchedEffect(isStabilized) {
        if (isStabilized && alienState == AlienState.UNSTABLE) {
            alienState = AlienState.SETTLING
        } else if (!isStabilized) {
            alienState = AlienState.UNSTABLE
        }
    }

    LaunchedEffect(alienState) {
        val startTime = System.currentTimeMillis()
        while (true) {
            delay(16L) // ~60fps ticker
            val now = System.currentTimeMillis()

            when (alienState) {
                AlienState.UNSTABLE -> {
                    // Cycles through all 40 story frames (0-39) @ 10fps loop
                    val elapsed = (now - startTime) + randomOffset
                    currentFrameIndex = ((elapsed * 10L) / 1000L).toInt() % 40
                }
                AlienState.SETTLING -> {
                    // Plays settling frames (38-39) once then goes to STABLE_IDLE
                    val elapsed = now - startTime
                    val idx = 38 + ((elapsed * 10L) / 1000L).toInt()
                    if (idx >= 40) {
                        alienState = AlienState.STABLE_IDLE
                        currentFrameIndex = 0
                    } else {
                        currentFrameIndex = idx
                    }
                }
                AlienState.STABLE_IDLE -> {
                    // Cycles gentle happy idle frames (0-4) @ 8fps loop
                    val elapsed = now - startTime
                    currentFrameIndex = ((elapsed * 8L) / 1000L).toInt() % 5
                }
            }
        }
    }

    val formattedFrame = if (currentFrameIndex < 10) "0$currentFrameIndex" else "$currentFrameIndex"
    val alienPath = "games/lab_meltdown/animations/alien/frames/alien_$formattedFrame.png"

    val alienBitmap = remember(context, alienPath) {
        loadAssetImageBitmap(context, alienPath)
    }

    val animConfig = remember(context) {
        LabAnimConfig.load(
            context,
            "games/lab_meltdown/animations/alien/animation.json",
            defaultW = 340f, defaultH = 380f, defaultX = 525f, defaultY = 785f
        )
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val canvasW = this.maxWidth
        val canvasH = this.maxHeight

        val alienW = canvasW * (animConfig.frameW / animConfig.canvasW)
        val alienH = canvasH * (animConfig.frameH / animConfig.canvasH)
        val alienX = canvasW * (animConfig.drawX / animConfig.canvasW)
        val alienY = canvasH * (animConfig.drawY / animConfig.canvasH)

        Box(
            modifier = Modifier
                .offset(x = alienX, y = alienY)
                .size(width = alienW, height = alienH)
        ) {
            if (alienBitmap != null) {
                Image(
                    bitmap = alienBitmap,
                    contentDescription = "Alien Mascot Frame",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
private fun SparkOverlay(
    isStabilized: Boolean,
    modifier: Modifier = Modifier
) {
    if (isStabilized) return

    val context = LocalContext.current
    var currentFrameIndex by remember { mutableIntStateOf(0) }
    val randomOffset = remember { (0..2000).random().toLong() }

    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        while (true) {
            delay(16L) // ~60fps ticker
            val now = System.currentTimeMillis()
            val elapsed = (now - startTime) + randomOffset
            currentFrameIndex = ((elapsed * 24L) / 1000L).toInt() % 24
        }
    }

    val formattedFrame = if (currentFrameIndex < 10) "0$currentFrameIndex" else "$currentFrameIndex"
    val sparkPath = "games/lab_meltdown/animations/spark/spark_burst/spark_$formattedFrame.png"

    val sparkBitmap = remember(context, sparkPath) {
        loadAssetImageBitmap(context, sparkPath)
    }

    val animConfig = remember(context) {
        LabAnimConfig.load(
            context,
            "games/lab_meltdown/animations/spark/animation.json",
            defaultW = 280f, defaultH = 260f, defaultX = 500f, defaultY = 685f
        )
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val canvasW = this.maxWidth
        val canvasH = this.maxHeight

        val sparkW = canvasW * (animConfig.frameW / animConfig.canvasW)
        val sparkH = canvasH * (animConfig.frameH / animConfig.canvasH)
        val sparkX = canvasW * (animConfig.drawX / animConfig.canvasW)
        val sparkY = canvasH * (animConfig.drawY / animConfig.canvasH)

        Box(
            modifier = Modifier
                .offset(x = sparkX, y = sparkY)
                .size(width = sparkW, height = sparkH)
        ) {
            if (sparkBitmap != null) {
                Image(
                    bitmap = sparkBitmap,
                    contentDescription = "Console Spark Frame",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

enum class SteamPipeState {
    UNSTABLE,
    SETTLING,
    STABLE_IDLE
}

@Composable
private fun SteamPipeOverlay(
    isStabilized: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var pipeState by remember { mutableStateOf(SteamPipeState.UNSTABLE) }
    var phaseStart by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var currentFrameIndex by remember { mutableIntStateOf(0) }

    val randomOffset = remember { (0..2000).random().toLong() }

    LaunchedEffect(isStabilized) {
        if (isStabilized && pipeState == SteamPipeState.UNSTABLE) {
            pipeState = SteamPipeState.SETTLING
            phaseStart = System.currentTimeMillis()
            currentFrameIndex = 0
        } else if (!isStabilized) {
            pipeState = SteamPipeState.UNSTABLE
            phaseStart = System.currentTimeMillis()
        }
    }

    LaunchedEffect(pipeState) {
        while (true) {
            delay(16L) // ~60fps ticker
            val now = System.currentTimeMillis()

            when (pipeState) {
                SteamPipeState.UNSTABLE -> {
                    // 20 frames @ 10fps loop
                    val elapsed = (now - phaseStart) + randomOffset
                    currentFrameIndex = ((elapsed * 10L) / 1000L).toInt() % 20
                }
                SteamPipeState.SETTLING -> {
                    // 24 frames @ 8fps ONCE
                    val elapsed = now - phaseStart
                    val idx = ((elapsed * 8L) / 1000L).toInt()
                    if (idx >= 24) {
                        pipeState = SteamPipeState.STABLE_IDLE
                        phaseStart = System.currentTimeMillis()
                        currentFrameIndex = 0
                    } else {
                        currentFrameIndex = idx
                    }
                }
                SteamPipeState.STABLE_IDLE -> {
                    // 2 frames @ 1fps loop
                    val elapsed = now - phaseStart
                    currentFrameIndex = ((elapsed * 1L) / 1000L).toInt() % 2
                }
            }
        }
    }

    if (pipeState == SteamPipeState.STABLE_IDLE) return

    val formattedFrame = if (currentFrameIndex < 10) "0$currentFrameIndex" else "$currentFrameIndex"
    val pipePath = when (pipeState) {
        SteamPipeState.UNSTABLE -> {
            "games/lab_meltdown/animations/steam_pipe/unstable/steam_$formattedFrame.png"
        }
        SteamPipeState.SETTLING -> {
            "games/lab_meltdown/animations/steam_pipe/settle/steam_$formattedFrame.png"
        }
        SteamPipeState.STABLE_IDLE -> {
            "games/lab_meltdown/animations/steam_pipe/stable_idle/steam_$formattedFrame.png"
        }
    }

    val pipeBitmap = remember(context, pipePath) {
        loadAssetImageBitmap(context, pipePath)
    }

    val animConfig = remember(context) {
        LabAnimConfig.load(
            context,
            "games/lab_meltdown/animations/steam_pipe/animation.json",
            defaultW = 851f, defaultH = 1849f, defaultX = 0f, defaultY = 0f
        )
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenW = this.maxWidth
        val screenH = this.maxHeight

        val scale = maxOf(screenW.value / animConfig.canvasW, screenH.value / animConfig.canvasH)
        val offsetX = (screenW.value - animConfig.canvasW * scale) / 2f
        val offsetY = (screenH.value - animConfig.canvasH * scale) / 2f

        val pipeW = (animConfig.frameW * scale).dp
        val pipeH = (animConfig.frameH * scale).dp
        val pipeX = (offsetX + animConfig.drawX * scale).dp
        val pipeY = (offsetY + animConfig.drawY * scale).dp

        Box(
            modifier = Modifier
                .offset(x = pipeX, y = pipeY)
                .size(width = pipeW, height = pipeH)
        ) {
            if (pipeBitmap != null) {
                Image(
                    bitmap = pipeBitmap,
                    contentDescription = "Steam Pipe Frame",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
            }
        }
    }
}

enum class MonitorState {
    UNSTABLE,
    SETTLING,
    STABLE_IDLE
}

@Composable
private fun MonitorOverlay(
    isStabilized: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var monitorState by remember { mutableStateOf(MonitorState.UNSTABLE) }
    var phaseStart by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var currentFrameIndex by remember { mutableIntStateOf(0) }

    val randomOffset = remember { (0..3000).random().toLong() }

    LaunchedEffect(isStabilized) {
        if (isStabilized && monitorState == MonitorState.UNSTABLE) {
            monitorState = MonitorState.SETTLING
            phaseStart = System.currentTimeMillis()
            currentFrameIndex = 0
        } else if (!isStabilized) {
            monitorState = MonitorState.UNSTABLE
            phaseStart = System.currentTimeMillis()
        }
    }

    LaunchedEffect(monitorState) {
        while (true) {
            delay(16L) // ~60fps ticker
            val now = System.currentTimeMillis()

            when (monitorState) {
                MonitorState.UNSTABLE -> {
                    val elapsed = (now - phaseStart) + randomOffset
                    currentFrameIndex = ((elapsed * 8L) / 1000L).toInt() % 24
                }
                MonitorState.SETTLING -> {
                    val elapsed = now - phaseStart
                    val idx = ((elapsed * 12L) / 1000L).toInt()
                    if (idx >= 12) {
                        monitorState = MonitorState.STABLE_IDLE
                        phaseStart = System.currentTimeMillis()
                        currentFrameIndex = 0
                    } else {
                        currentFrameIndex = idx
                    }
                }
                MonitorState.STABLE_IDLE -> {
                    val elapsed = now - phaseStart
                    currentFrameIndex = ((elapsed * 8L) / 1000L).toInt() % 24
                }
            }
        }
    }

    val formattedFrame = if (currentFrameIndex < 10) "0$currentFrameIndex" else "$currentFrameIndex"
    val monitorPath = when (monitorState) {
        MonitorState.UNSTABLE -> {
            "games/lab_meltdown/animations/monitor/monitor_unstable/monitor_$formattedFrame.png"
        }
        MonitorState.SETTLING -> {
            "games/lab_meltdown/animations/monitor/monitor_settle/monitor_$formattedFrame.png"
        }
        MonitorState.STABLE_IDLE -> {
            "games/lab_meltdown/animations/monitor/monitor_stable_idle/monitor_$formattedFrame.png"
        }
    }

    val monitorBitmap = remember(context, monitorPath) {
        loadAssetImageBitmap(context, monitorPath)
    }

    val animConfig = remember(context) {
        LabAnimConfig.load(
            context,
            "games/lab_meltdown/animations/monitor/animation.json",
            defaultW = 180f, defaultH = 150f, defaultX = 0f, defaultY = 320f
        )
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val canvasW = this.maxWidth
        val canvasH = this.maxHeight

        val monW = canvasW * (animConfig.frameW / animConfig.canvasW)
        val monH = canvasH * (animConfig.frameH / animConfig.canvasH)
        val monX = canvasW * (animConfig.drawX / animConfig.canvasW)
        val monY = canvasH * (animConfig.drawY / animConfig.canvasH)

        Box(
            modifier = Modifier
                .offset(x = monX, y = monY)
                .size(width = monW, height = monH)
        ) {
            if (monitorBitmap != null) {
                Image(
                    bitmap = monitorBitmap,
                    contentDescription = "Lab Monitor Frame",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
            }
        }
    }
}

@Composable
private fun UserLosesOverlay(
    isLossActive: Boolean,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isLossActive) return

    val context = LocalContext.current

    var animPhase by remember { mutableIntStateOf(1) }
    var currentFrameIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        val fpsMs = 1000L / 15L

        // Combined 30-frame sequence: Slime drips 0->29, doors start closing at frame 12 and finish at frame 29
        for (i in 0 until 30) {
            animPhase = if (i < 12) 1 else 2
            currentFrameIndex = i
            delay(fpsMs)
        }

        // Phase 3: Shut Hazard Loop (Doors fully shut, hazard flashing)
        animPhase = 3
        onAnimationComplete()

        var loopIdx = 0
        while (true) {
            currentFrameIndex = loopIdx
            delay(fpsMs)
            loopIdx = (loopIdx + 1) % 24
        }
    }

    val leftOffsets = listOf(-343f, -343f, -343f, -343f, -341f, -333f, -322f, -305f, -283f, -257f, -226f, -191f, -150f, -105f, -55f, 0f, -10f, 0f)
    val rightOffsets = listOf(330f, 330f, 330f, 330f, 328f, 321f, 309f, 293f, 273f, 248f, 218f, 183f, 144f, 101f, 53f, 0f, 10f, 0f)
    val frameAlphas = listOf(0.25f, 0.5f, 0.8f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f)

    // During phase 1 and 2, slime progresses from frame 0 to 29
    val slimeIdx = if (animPhase == 1) currentFrameIndex else currentFrameIndex.coerceIn(0, 29)
    val formattedSlime = if (slimeIdx < 10) "00$slimeIdx" else if (slimeIdx < 100) "0$slimeIdx" else "$slimeIdx"
    val slimePath = "games/lab_meltdown/animations/user_loses/slime/slime_$formattedSlime.png"

    val hazardIdx = if (animPhase == 3) currentFrameIndex else 0
    val formattedHazard = if (hazardIdx < 10) "0$hazardIdx" else "$hazardIdx"
    val hazardPath = "games/lab_meltdown/animations/user_loses/hazard/hazard_$formattedHazard.png"

    val slimeBitmap = remember(context, slimePath) { if (animPhase <= 2) loadAssetImageBitmap(context, slimePath) else null }
    val frameBitmap = remember(context) { loadAssetImageBitmap(context, "games/lab_meltdown/animations/user_loses/door/door_frame_static.png") }
    val doorLeftBitmap = remember(context) { loadAssetImageBitmap(context, "games/lab_meltdown/animations/user_loses/door/door_left.png") }
    val doorRightBitmap = remember(context) { loadAssetImageBitmap(context, "games/lab_meltdown/animations/user_loses/door/door_right.png") }
    val hazardBitmap = remember(context, hazardPath) { if (animPhase == 3) loadAssetImageBitmap(context, hazardPath) else null }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenW = this.maxWidth
        val scaleX = screenW.value / 851f

        // 1. SLIME LAYER (Full screen 851x1849)
        if (animPhase <= 2 && slimeBitmap != null) {
            Image(
                bitmap = slimeBitmap,
                contentDescription = "Slime Drip",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
        }

        // 2. BLAST DOORS & FRAME
        if (animPhase >= 2) {
            val doorFrameIdx = if (animPhase == 2) (currentFrameIndex - 12).coerceIn(0, 17) else 17
            val currentAlpha = frameAlphas[doorFrameIdx]
            val leftOffX = leftOffsets[doorFrameIdx]
            val rightOffX = rightOffsets[doorFrameIdx]

            // Left Blast Door Leaf (Full screen 851x1849 with slide offset)
            if (doorLeftBitmap != null && (animPhase == 3 || currentFrameIndex >= 12)) {
                Image(
                    bitmap = doorLeftBitmap,
                    contentDescription = "Left Blast Door",
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(x = (leftOffX * scaleX).dp),
                    contentScale = ContentScale.FillBounds
                )
            }

            // Right Blast Door Leaf (Full screen 851x1849 with slide offset)
            if (doorRightBitmap != null && (animPhase == 3 || currentFrameIndex >= 12)) {
                Image(
                    bitmap = doorRightBitmap,
                    contentDescription = "Right Blast Door",
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(x = (rightOffX * scaleX).dp),
                    contentScale = ContentScale.FillBounds
                )
            }

            // Static Door Frame (Full screen 851x1849)
            if (frameBitmap != null) {
                Image(
                    bitmap = frameBitmap,
                    contentDescription = "Door Frame",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { this.alpha = currentAlpha },
                    contentScale = ContentScale.FillBounds
                )
            }
        }

        // 3. HAZARD WARNING LIGHT FLASHES (Full screen 851x1849)
        if (animPhase == 3 && hazardBitmap != null) {
            Image(
                bitmap = hazardBitmap,
                contentDescription = "Hazard Light Flashes",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
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

                    // Parse frame_size from root, same_box_for_all_sequences, or sequences
                    var fW = defaultW
                    var fH = defaultH
                    val rootFrame = json.optJSONArray("frame_size")
                    if (rootFrame != null) {
                        fW = rootFrame.optDouble(0, defaultW.toDouble()).toFloat()
                        fH = rootFrame.optDouble(1, defaultH.toDouble()).toFloat()
                    } else {
                        val sameBox = json.optJSONObject("same_box_for_all_sequences")
                        val sameFrame = sameBox?.optJSONArray("frame_size")
                        if (sameFrame != null) {
                            fW = sameFrame.optDouble(0, defaultW.toDouble()).toFloat()
                            fH = sameFrame.optDouble(1, defaultH.toDouble()).toFloat()
                        } else {
                            val seqObj = json.optJSONObject("sequences")
                            val firstSeq = seqObj?.keys()?.asSequence()?.firstOrNull()?.let { seqObj.optJSONObject(it) }
                            val seqFrame = firstSeq?.optJSONArray("frame_size")
                            if (seqFrame != null) {
                                fW = seqFrame.optDouble(0, defaultW.toDouble()).toFloat()
                                fH = seqFrame.optDouble(1, defaultH.toDouble()).toFloat()
                            }
                        }
                    }

                    // Parse draw_at_xy from root, same_box_for_all_sequences, or sequences
                    var dX = defaultX
                    var dY = defaultY
                    val rootDraw = json.optJSONArray("draw_at_xy")
                    if (rootDraw != null) {
                        dX = rootDraw.optDouble(0, defaultX.toDouble()).toFloat()
                        dY = rootDraw.optDouble(1, defaultY.toDouble()).toFloat()
                    } else {
                        val sameBox = json.optJSONObject("same_box_for_all_sequences")
                        val sameDraw = sameBox?.optJSONArray("draw_at_xy")
                        if (sameDraw != null) {
                            dX = sameDraw.optDouble(0, defaultX.toDouble()).toFloat()
                            dY = sameDraw.optDouble(1, defaultY.toDouble()).toFloat()
                        } else {
                            val seqObj = json.optJSONObject("sequences")
                            val firstSeq = seqObj?.keys()?.asSequence()?.firstOrNull()?.let { seqObj.optJSONObject(it) }
                            val seqDraw = firstSeq?.optJSONArray("draw_at_xy")
                            if (seqDraw != null) {
                                dX = seqDraw.optDouble(0, defaultX.toDouble()).toFloat()
                                dY = seqDraw.optDouble(1, defaultY.toDouble()).toFloat()
                            }
                        }
                    }

                    // Parse placement_in_concept_art if present (as in alien/animation.json)
                    val conceptObj = json.optJSONObject("placement_in_concept_art")
                    if (conceptObj != null) {
                        val xyArr = conceptObj.optJSONArray("frame_top_left_xy")
                        if (xyArr != null) {
                            val origX = xyArr.optDouble(0, 484.0).toFloat()
                            val origY = xyArr.optDouble(1, 600.0).toFloat()
                            dX = origX * (cW / 784f)
                            dY = origY * (cH / 1168f)
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
    val missedWords = (uiState.total - uiState.score).coerceAtLeast(0)
    val isGameWon = uiState.score >= 7

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030712)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(28.dp)
                .background(Color(0xFF0F172A), RoundedCornerShape(24.dp))
                .border(BorderStroke(2.dp, if (isGameWon) Color(0xFF22C55E) else Color(0xFFEF4444)), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Text(
                if (isGameWon) "🧪 STABILIZED! 🧪" else "💥 GAME OVER! 💥",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = if (isGameWon) Color(0xFF22C55E) else Color(0xFFEF4444),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            Text(
                if (isGameWon) "Great job!" else "The lab suffered a critical meltdown!",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Correct: ${uiState.score}/7", fontWeight = FontWeight.Bold, color = Color(0xFF22C55E), fontSize = 14.sp)
                Text("Missed: $missedWords/3", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444), fontSize = 14.sp)
            }

            Spacer(Modifier.height(24.dp))

            // RESTART NEW GAME PROMPT BUTTON
            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7))
            ) {
                Text("🔄 RESTART NEW GAME", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
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
    val pathsToTry = if (assetName.startsWith("games/")) {
        listOf(assetName, assetName.removePrefix("games/lab_meltdown/"))
    } else {
        listOf(
            "games/lab_meltdown/$assetName",
            assetName
        )
    }
    for (path in pathsToTry) {
        try {
            context.assets.open(path).use { stream ->
                BitmapFactory.decodeStream(stream)?.asImageBitmap()?.let { return it }
            }
        } catch (_: Exception) {}
    }
    return null
}

@Composable
private fun StatusOverlay(
    statusNum: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val statusPath = "games/lab_meltdown/statuses/1x/$statusNum.png"
    val statusBitmap = remember(context, statusPath) {
        loadAssetImageBitmap(context, statusPath)
    }

    if (statusBitmap == null) return

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Draw 851x1849 native status overlay directly on top of lab_meltdown_bg.png
        Image(
            bitmap = statusBitmap,
            contentDescription = "Lab Status $statusNum Overlay",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}
