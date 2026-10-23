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
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.spellarcade.app.ui.components.*
import com.spellarcade.app.data.model.AvatarConfig
import com.spellarcade.app.ui.components.avatar.HeroAnimationState
import com.spellarcade.app.ui.components.avatar.HeroSprite
import com.spellarcade.app.ui.screens.profile.character.CharacterLayerType
import com.spellarcade.app.util.MusicManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetSpaScreen(
    onBack: () -> Unit,
    viewModel: PetSpaViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val petSpaState by viewModel.petSpaState.collectAsState()

    // ===================== ADJUSTABLE SETTINGS =====================
    var tubVerticalOffset by remember { mutableFloatStateOf(20f) } // Bathtub vertical offset (+ down, - up)
    var keyboardBottomPadding by remember { mutableFloatStateOf(4f) } // Keyboard bottom padding
    var keyboardWidthFactor by remember { mutableFloatStateOf(0.45f) } // Keyboard width scale factor (smaller = smaller keyboard)
    var keyHeightDp by remember { mutableFloatStateOf(30f) } // Key tile height in dp
    var petVerticalOffset by remember { mutableFloatStateOf(-20f) } // Pet vertical offset (+ down, - up)
    var heroScale by remember { mutableFloatStateOf(2.6f) } // Hero avatar scale factor (larger = bigger hero)
    var heroHorizontalOffset by remember { mutableFloatStateOf(-20f) } // Hero horizontal offset from left in dp
    var heroVerticalOffset by remember { mutableFloatStateOf(-20f) } // Hero vertical offset from bottom in dp
    // ===============================================================

    @Composable
    fun PetSpaKeyTile(
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
            modifier = modifier.height(keyHeightDp.dp)
        ) {
            Text(
                text = displayLabel,
                fontWeight = FontWeight.Bold,
                fontSize = if (isSpecialKey) 13.sp else 15.sp,
                color = contentColor
            )
        }
    }

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
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    petSpaState.maskedWord.forEach { char ->
                        PetSpaBlankTile(char = char)
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

            // Left Side Hero Character (Selected Avatar with T-shirt & special Pet Spa bottoms, no shoes)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = heroHorizontalOffset.dp, y = (-heroVerticalOffset).dp)
                    .zIndex(2f)
            ) {
                val heroConfig = remember(uiState.avatar) { AvatarConfig.fromAvatar(uiState.avatar) }
                val spaTopColor = if (heroConfig.topColor.isBlank()) "blue" else heroConfig.topColor
                val spaBottomColor = if (heroConfig.bottomColor.isBlank()) "blue" else heroConfig.bottomColor
                val spaSpecialPath = "sprites/special/pet_spa/pet_spa_$spaBottomColor.png"

                Box(modifier = Modifier.size((110f * heroScale).dp, (150f * heroScale).dp)) {
                    HeroSprite(
                        config = heroConfig.copy(top = "tshirt", topColor = spaTopColor, bottom = "none"),
                        modifier = Modifier.fillMaxSize(),
                        animationState = HeroAnimationState.IDLE,
                        sway = false,
                        breath = false,
                        layerVisibility = mapOf(
                            CharacterLayerType.SHOES to false,
                            CharacterLayerType.BOTTOM to false
                        )
                    )
                    val spaBitmap = remember(context, spaSpecialPath) { loadAssetImageBitmap(context, spaSpecialPath) }
                    if (spaBitmap != null) {
                        Image(
                            bitmap = spaBitmap,
                            contentDescription = "Pet Spa Clothes Overlay",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            // Center Pet Visual inside the Bathtub
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = tubVerticalOffset.dp)
                    .zIndex(1f),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.size(260.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val tubBitmap = remember(context) { loadAssetImageBitmap(context, "games/pet_spa/pet_tub.png") }
                    if (tubBitmap != null) {
                        Image(
                            bitmap = tubBitmap,
                            contentDescription = "Pet Bathtub",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }

                    PuppySprite(
                        modifier = Modifier
                            .size(150.dp)
                            .offset(y = (-20).dp + petVerticalOffset.dp)
                    )

                    // Soap bubbles: same size/position as the tub image so the water ellipse lines up.
                    // Set tubAspectRatio to tub PNG width / height if it isn't square.
                    TubBubblesOverlay(
                        modifier = Modifier.fillMaxSize(),
                        tubAspectRatio = 1f,
                        config = BubbleConfig() // e.g. BubbleConfig(maxBubbles = 4)
                    )

                    val tubOverlayBitmap = remember(context) { loadAssetImageBitmap(context, "games/pet_spa/pet_tub_overlay.png") }
                    if (tubOverlayBitmap != null) {
                        Image(
                            bitmap = tubOverlayBitmap,
                            contentDescription = "Pet Bathtub Front Overlay",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
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
                        .align(Alignment.BottomCenter)
                        .padding(bottom = keyboardBottomPadding.dp)
                        .fillMaxWidth(keyboardWidthFactor)
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

@Composable
private fun PuppySprite(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val puppyBitmaps = remember(context) {
        (1..24).map { i ->
            val formatted = if (i < 10) "0$i" else "$i"
            loadAssetImageBitmap(context, "games/pet_spa/animations/puppy/dirty_puppy/frames_512/puppy_idle_$formatted.png")
        }
    }

    var frameIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        val frameMs = 60L // 24 frames @ 60ms per frame = ~1.44s loop
        while (true) {
            frameIndex = (frameIndex + 1) % 24
            delay(frameMs)
        }
    }

    val bmp = puppyBitmaps.getOrNull(frameIndex)
    if (bmp != null) {
        Image(
            bitmap = bmp,
            contentDescription = "Animated Dirty Puppy",
            modifier = modifier,
            contentScale = ContentScale.Fit
        )
    }
}

// =====================================================================
// SOAP BUBBLES: float up from the water surface, then pop
// =====================================================================

private const val BUBBLE_DIR = "games/pet_spa/animations/tub_bubbles/frames_256"
private const val VISIBLE_FRACTION = 0.52f // visible bubble fills ~52% of the 256px frame

/** Tunable settings. Fractions are relative to the tub image width unless noted. */
data class BubbleConfig(
    val minSpawnIntervalMs: Long = 1200L,
    val maxSpawnIntervalMs: Long = 2600L,
    val maxBubbles: Int = 5,
    val minDiameterFrac: Float = 0.035f,
    val maxDiameterFrac: Float = 0.07f,
    val minRiseFrac: Float = 0.15f,
    val maxRiseFrac: Float = 0.26f,
    val minRiseDurationMs: Long = 3000L,
    val maxRiseDurationMs: Long = 4500L,
    val minSwayDp: Float = 8f,
    val maxSwayDp: Float = 22f,
    val minSwayCycles: Float = 1f,
    val maxSwayCycles: Float = 2f,
    val minSpacingFrac: Float = 0.20f,     // min horizontal gap from previous spawn
    // Water ellipse as fractions of the tub image
    val waterLeft: Float = 0.10f,
    val waterRight: Float = 0.90f,
    val waterTop: Float = 0.40f,
    val waterBottom: Float = 0.58f,
    val floatFrameMs: Long = 90L,
    val popFrameMs: Long = 70L
)

private data class BubbleSpec(
    val id: Long,
    val cx: Float,          // start center, px within tub rect
    val cy: Float,
    val diameter: Float,    // visible diameter, px
    val rise: Float,        // px
    val durationMs: Int,
    val swayPx: Float,
    val swayCycles: Float
)

/**
 * Place this over the tub image with the SAME size/position as the tub Image.
 * [tubAspectRatio] = tub image width / height (used because the tub is drawn with ContentScale.Fit).
 */
@Composable
fun TubBubblesOverlay(
    modifier: Modifier = Modifier,
    config: BubbleConfig = BubbleConfig(),
    tubAspectRatio: Float = 1f
) {
    val context = LocalContext.current
    val floatFrames = remember(context) {
        (1..8).map { loadAssetImageBitmap(context, "$BUBBLE_DIR/bubble_float_%02d.png".format(it)) }
    }
    val popFrames = remember(context) {
        (1..6).map { loadAssetImageBitmap(context, "$BUBBLE_DIR/bubble_pop_%02d.png".format(it)) }
    }

    // Start/stop spawning with the lifecycle (onResume / onPause)
    val lifecycleOwner = LocalLifecycleOwner.current
    var resumed by remember {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) resumed = true
            if (event == Lifecycle.Event.ON_PAUSE) resumed = false
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val bubbles = remember { mutableStateListOf<BubbleSpec>() }
    val lastSpawnX = remember { floatArrayOf(Float.NaN) }
    var nextId by remember { mutableLongStateOf(0L) }

    @Suppress("UnusedBoxWithConstraintsScope")
    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val boxW = constraints.maxWidth.toFloat()
        val boxH = constraints.maxHeight.toFloat()

        // Rect the tub image actually occupies (ContentScale.Fit, centered)
        val tubW = minOf(boxW, boxH * tubAspectRatio)
        val tubH = tubW / tubAspectRatio
        val originX = (boxW - tubW) / 2f
        val originY = (boxH - tubH) / 2f

        LaunchedEffect(resumed, tubW, tubH) {
            if (!resumed || tubW <= 0f) return@LaunchedEffect
            while (true) {
                delay((config.minSpawnIntervalMs..config.maxSpawnIntervalMs).random())

                if (bubbles.size >= config.maxBubbles) continue // never exceed the cap

                // Uniform random point inside the water ellipse, spaced from the last bubble
                val ecx = (config.waterLeft + config.waterRight) / 2f * tubW
                val ecy = (config.waterTop + config.waterBottom) / 2f * tubH
                val erx = (config.waterRight - config.waterLeft) / 2f * tubW
                val ery = (config.waterBottom - config.waterTop) / 2f * tubH

                val rnd = kotlin.random.Random
                var x = ecx
                var y = ecy
                for (attempt in 0 until 12) {
                    val r = sqrt(rnd.nextFloat())            // sqrt => uniform over the area
                    val a = rnd.nextFloat() * 2f * PI.toFloat()
                    x = ecx + erx * r * cos(a)
                    y = ecy + ery * r * sin(a)
                    val last = lastSpawnX[0]
                    if (last.isNaN() || abs(x - last) >= config.minSpacingFrac * tubW) break
                }
                lastSpawnX[0] = x

                val dir = if (rnd.nextBoolean()) 1f else -1f
                bubbles.add(
                    BubbleSpec(
                        id = nextId++,
                        cx = x,
                        cy = y,
                        diameter = tubW * (rnd.nextFloat() * (config.maxDiameterFrac - config.minDiameterFrac) + config.minDiameterFrac),
                        rise = tubW * (rnd.nextFloat() * (config.maxRiseFrac - config.minRiseFrac) + config.minRiseFrac),
                        durationMs = (config.minRiseDurationMs..config.maxRiseDurationMs).random().toInt(),
                        swayPx = with(density) {
                            (rnd.nextFloat() * (config.maxSwayDp - config.minSwayDp) + config.minSwayDp).dp.toPx()
                        } * dir,
                        swayCycles = rnd.nextFloat() * (config.maxSwayCycles - config.minSwayCycles) + config.minSwayCycles
                    )
                )
            }
        }

        bubbles.forEach { spec ->
            key(spec.id) {
                BubbleView(
                    spec = spec,
                    floatFrames = floatFrames,
                    popFrames = popFrames,
                    config = config,
                    originX = originX,
                    originY = originY,
                    onFinished = { bubbles.removeAll { it.id == spec.id } }
                )
            }
        }
    }
}

@Composable
private fun BubbleView(
    spec: BubbleSpec,
    floatFrames: List<ImageBitmap?>,
    popFrames: List<ImageBitmap?>,
    config: BubbleConfig,
    originX: Float,
    originY: Float,
    onFinished: () -> Unit
) {
    val density = LocalDensity.current
    val progress = remember { Animatable(0f) }   // 0..1 linear time through the rise
    var popping by remember { mutableStateOf(false) }
    var frame by remember { mutableIntStateOf(0) }

    // Movement: rise, then pop when the top is reached
    LaunchedEffect(popping) {
        if (!popping) {
            progress.animateTo(1f, tween(spec.durationMs, easing = LinearEasing))
            popping = true
        }
    }

    // Frames: float loop (90 ms) while rising, pop once (70 ms) then remove
    LaunchedEffect(popping) {
        if (!popping) {
            var i = 0
            while (true) {
                frame = i % floatFrames.size
                delay(config.floatFrameMs)
                i++
            }
        } else {
            for (i in popFrames.indices) {
                frame = i
                delay(config.popFrameMs)
            }
            onFinished()
        }
    }

    val viewSizePx = spec.diameter / VISIBLE_FRACTION
    val viewSizeDp = with(density) { viewSizePx.toDp() }
    val bmp = if (popping) popFrames.getOrNull(frame) else floatFrames.getOrNull(frame)

    if (bmp != null) {
        Image(
            bitmap = bmp,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(viewSizeDp)
                .offset {
                    val t = progress.value
                    val eased = 1f - (1f - t) * (1f - t)                  // ease-out
                    val sway = spec.swayPx * eased *                       // grows with height
                            sin(2f * PI.toFloat() * spec.swayCycles * t)
                    IntOffset(
                        (originX + spec.cx + sway - viewSizePx / 2f).roundToInt(),
                        (originY + spec.cy - spec.rise * eased - viewSizePx / 2f).roundToInt()
                    )
                }
                .graphicsLayer {
                    if (popping) {
                        scaleX = 1f; scaleY = 1f; alpha = 1f
                    } else {
                        val g = (progress.value / 0.12f).coerceIn(0f, 1f)  // grow/fade in over first 12%
                        val s = 0.35f + 0.65f * g
                        scaleX = s; scaleY = s; alpha = g
                    }
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = !popping
                ) { popping = true }                                       // tap to pop early
        )
    }
}