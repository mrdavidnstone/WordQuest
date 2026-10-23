package com.spellarcade.app.ui.screens.games

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.BitmapFactory
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.math.roundToInt
import com.spellarcade.app.data.model.AvatarConfig
import com.spellarcade.app.ui.components.ActiveListBadge
import com.spellarcade.app.ui.components.avatar.HeroAnimationState
import com.spellarcade.app.ui.components.avatar.HeroSprite
import com.spellarcade.app.ui.screens.profile.character.CharacterLayerType
import com.spellarcade.app.util.MusicManager
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PirateBlasterScreen(
    onBack: () -> Unit,
    viewModel: PirateBlasterViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val galleryState by viewModel.blasterState.collectAsState()

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

    LaunchedEffect(Unit) {
        MusicManager.playMusic(context, "music/pirate_blaster_scene1.wav")
    }

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
        val titleBitmap = remember(context) { loadAssetImageBitmap(context, "games/pirate_blaster/pirate_blaster_title .png") }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF030712))
        ) {
            if (titleBitmap != null) {
                Image(
                    bitmap = titleBitmap,
                    contentDescription = "Pirate Blaster Title Overlay",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.fillMaxSize()) {
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

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.weight(1f))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        shape = RoundedCornerShape(22.dp),
                        color = Color(0xFF0F172A).copy(alpha = 0.92f),
                        border = BorderStroke(1.5.dp, Color(0xFFF59E0B).copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "Aim your cannon at the barrel with the correct missing letter!",
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
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
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

    // Gentle Bobbing Wave Animation for Barrels
    val infiniteAnim = rememberInfiniteTransition(label = "pirate_cove_anim")
    val waveBob by infiniteAnim.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bob"
    )

    var cheerCount by remember { mutableIntStateOf(0) }
    var missCount by remember { mutableIntStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }

    // Visual placement of the cannon sprite (dp) – only affects the drawn cannon image
    var cannonHorizontalOffset by remember { mutableFloatStateOf(0f) }

    // Screen Shake effect for damage & game over
    val shakeAnim = remember { Animatable(0f) }
    var showDamageFlash by remember { mutableStateOf(false) }

    LaunchedEffect(galleryState.damageCount) {
        if (galleryState.damageCount > 0) {
            triggerVibration(context)
            shakeAnim.animateTo(10f, tween(100))
            shakeAnim.animateTo(-10f, tween(100))
            shakeAnim.animateTo(5f, tween(100))
            shakeAnim.animateTo(0f, tween(100))
            showDamageFlash = true
            delay(1200)
            showDamageFlash = false
        }
    }

    // ============================================================
    // CANNONBALL START POSITION (screen ratios 0..1)
    // ViewModel does: startX = 0.50 + horizontalOffset
    //                 startY = 0.82 + verticalOffset
    // Tune these two numbers if the ball still looks off.
    // ============================================================
    LaunchedEffect(Unit) {
        viewModel.setCannonSettings(
            horizontalOffset = 0.00f,   // 0 = dead center
            verticalOffset   = 0.08f    // positive = lower on screen (closer to dock)
        )
    }

    LaunchedEffect(uiState.isCorrect) {
        if (uiState.isCorrect == true) {
            cheerCount++
        }
    }
    LaunchedEffect(galleryState.missesCount) {
        if (galleryState.missesCount > 0) {
            missCount++
        }
    }

    // Subtle Rocking / Floating Animation for Ship Foreground
    val dockAnim = rememberInfiniteTransition(label = "dock_floating")
    val rawDockY by dockAnim.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dock_vertical"
    )
    val dockOffsetY = if (uiState.isFinished) 0f else rawDockY

    val rawDockRot by dockAnim.animateFloat(
        initialValue = -0.6f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dock_rotation"
    )
    val dockRotation = if (uiState.isFinished) 0f else rawDockRot

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .navigationBarsPadding()
            .graphicsLayer {
                translationX = shakeAnim.value
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    if (!uiState.isFinished && !isPaused && !galleryState.isFiring && !galleryState.isResolving && !uiState.showFeedback) {
                        val ratioX = (offset.x / size.width).coerceIn(0f, 1f)
                        val ratioY = (offset.y / size.height).coerceIn(0f, 1f)
                        viewModel.fireAtPoint(ratioX, ratioY)
                    }
                }
            }
    ) {
        val canvasW = this.maxWidth
        val canvasH = this.maxHeight

        val scene1Bitmap = remember(context) { loadAssetImageBitmap(context, "games/pirate_blaster/scene1.png") }
        val foregroundBitmap = remember(context) { loadAssetImageBitmap(context, "games/pirate_blaster/foreground.png") }
        val parrotBitmap = remember(context) { loadAssetImageBitmap(context, "games/pirate_blaster/animations/parrot.png") }
        val barrelBitmap = remember(context) { loadAssetImageBitmap(context, "games/pirate_blaster/animations/barrel_explosion/frame_00.png") }
        val leftWordBitmap = remember(context) { loadAssetImageBitmap(context, "games/pirate_blaster/left_word.png") }
        val middleWordBitmap = remember(context) { loadAssetImageBitmap(context, "games/pirate_blaster/middle_word.png") }
        val rightWordBitmap = remember(context) { loadAssetImageBitmap(context, "games/pirate_blaster/right_word.png") }

        // ===================== ADJUSTABLE SETTINGS =====================
        var backgroundScrollSpeed by remember { mutableFloatStateOf(105f) }
        var zoomFactor by remember { mutableFloatStateOf(1.0f) }
        var backgroundVerticalOffset by remember { mutableFloatStateOf(-40f) }
        var cannonBottomPadding by remember { mutableFloatStateOf(40f) }   // visual only
        var cannonScale by remember { mutableFloatStateOf(1.5f) } // Cannon scale factor
        var cannonballScale by remember { mutableFloatStateOf(28f) } // Cannonball size in dp
        var heroBottomPadding by remember { mutableFloatStateOf(120f) }
        var heroHorizontalOffset by remember { mutableFloatStateOf(200f) }
        var parrotBottomPadding by remember { mutableFloatStateOf(135f) }
        var parrotStartPadding by remember { mutableFloatStateOf(70f) }
        var foregroundScale by remember { mutableFloatStateOf(1.15f) }
        var seamOffset by remember { mutableFloatStateOf(-900f) }
        var barrelLetterVerticalOffset by remember { mutableFloatStateOf(0f) }
        var heroScale by remember { mutableFloatStateOf(0.25f) }
        var foregroundVerticalOffset by remember { mutableFloatStateOf(50f) }
        var wordDisplayBottomPadding by remember { mutableFloatStateOf(5f) }
        // ===============================================================

        val density = LocalDensity.current
        val screenHeightPx = with(density) { canvasH.toPx() }

        val originalWidthPx = scene1Bitmap?.width?.toFloat() ?: 2172f
        val originalHeightPx = scene1Bitmap?.height?.toFloat() ?: 724f

        val scale = (screenHeightPx * zoomFactor) / originalHeightPx
        val imageWidthPx = originalWidthPx * scale
        val imageHeightPx = originalHeightPx * scale

        val verticalOffsetPx = 0f + with(density) { backgroundVerticalOffset.dp.toPx() }

        LaunchedEffect(isPaused) {
        viewModel.setPaused(isPaused)
    }

    var scrollDistance by remember { mutableFloatStateOf(0f) }

        LaunchedEffect(Unit) {
            var lastTime = withFrameNanos { it }
            while (true) {
                val currentTime = withFrameNanos { it }
                val delta = (currentTime - lastTime) / 1_000_000_000f
                lastTime = currentTime
                if (!isPaused && delta in 0.001f..0.1f) {
                    scrollDistance += backgroundScrollSpeed * delta
                }
            }
        }

        val roundedWidth = (imageWidthPx + seamOffset).roundToInt().toFloat()
        val x1 = -(scrollDistance % roundedWidth).roundToInt().toFloat()
        val x2 = x1 + roundedWidth

        // ===================== BACKGROUND =====================
        if (scene1Bitmap != null) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    bitmap = scene1Bitmap,
                    contentDescription = null,
                    modifier = Modifier
                        .size(
                            width = with(density) { imageWidthPx.toDp() },
                            height = with(density) { imageHeightPx.toDp() }
                        )
                        .graphicsLayer {
                            translationX = x1
                            translationY = verticalOffsetPx
                        },
                    contentScale = ContentScale.FillBounds
                )
                Image(
                    bitmap = scene1Bitmap,
                    contentDescription = null,
                    modifier = Modifier
                        .size(
                            width = with(density) { imageWidthPx.toDp() },
                            height = with(density) { imageHeightPx.toDp() }
                        )
                        .graphicsLayer {
                            translationX = x2
                            translationY = verticalOffsetPx
                        },
                    contentScale = ContentScale.FillBounds
                )
            }
        }

        // ===================== BARREL TARGETS =====================
        galleryState.targets.filter { it.state != PirateTargetState.DESTROYED }.forEach { target ->
            val targetX = canvasW * target.x
            val targetY = (canvasH * target.y) + waveBob.dp

            val isExploding = target.state == PirateTargetState.HIT_CORRECT || target.state == PirateTargetState.HIT_INCORRECT

            if (isExploding) {
                BarrelExplosionOverlay(
                    letter = target.letter,
                    scale = target.scale,
                    onComplete = { viewModel.destroyTarget(target.id) },
                    modifier = Modifier
                        .offset(x = targetX - (34 * target.scale).dp, y = targetY - (40 * target.scale).dp)
                        .size(width = (68 * target.scale).dp, height = (80 * target.scale).dp)
                        .zIndex(3f + target.scale)
                )
            } else {
                Box(
                    modifier = Modifier
                        .offset(x = targetX - (34 * target.scale).dp, y = targetY - (40 * target.scale).dp)
                        .size(width = (68 * target.scale).dp, height = (80 * target.scale).dp)
                        .zIndex(target.scale)
                        .clickable(
                            enabled = !galleryState.isFiring && !galleryState.isResolving && !uiState.showFeedback,
                            onClick = { viewModel.fireAtTarget(target) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (barrelBitmap != null) {
                        Image(
                            bitmap = barrelBitmap,
                            contentDescription = "Barrel Target",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF78350F),
                            border = BorderStroke(2.dp, Color(0xFFF59E0B)),
                            modifier = Modifier.fillMaxSize()
                        ) {}
                    }

                    Text(
                        text = target.letter.toString(),
                        fontSize = (24 * target.scale).sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Serif,
                        color = Color(0xFF0F2B48),
                        modifier = Modifier
                            .offset(y = barrelLetterVerticalOffset.dp)
                            .graphicsLayer { alpha = 0.92f }
                    )
                }
            }
        }

        // ===================== CANNONBALL =====================
        galleryState.activeShot?.let { shot ->
            val p = shot.progress
            val shotX = canvasW * (shot.startRatio.x + (shot.targetRatio.x - shot.startRatio.x) * p)
            val shotY = canvasH * (shot.startRatio.y + (shot.targetRatio.y - shot.startRatio.y) * p)

            CannonballShotOverlay(
                sizeDp = cannonballScale.dp,
                modifier = Modifier
                    .offset(x = shotX - (cannonballScale / 2f).dp, y = shotY - (cannonballScale / 2f).dp)
                    .zIndex(1000f)
            )
        }

        // ===================== FOREGROUND DOCK & PARROT =====================
        if (foregroundBitmap != null) {
            val bitmapWidth = foregroundBitmap.width.toFloat()
            val bitmapHeight = foregroundBitmap.height.toFloat()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(3f),
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = foregroundVerticalOffset.dp)
                        .graphicsLayer {
                            scaleX = foregroundScale
                            scaleY = foregroundScale
                            translationY = dockOffsetY
                            rotationZ = dockRotation
                            transformOrigin = TransformOrigin(0.5f, 1.0f)
                        }
                ) {
                    Image(
                        bitmap = foregroundBitmap,
                        contentDescription = "Foreground Dock",
                        modifier = Modifier
                            .fillMaxSize()
                            .aspectRatio(
                                if (bitmapWidth > 0f && bitmapHeight > 0f)
                                    bitmapWidth / bitmapHeight
                                else 16f / 9f
                            ),
                        contentScale = ContentScale.FillWidth
                    )

                    if (parrotBitmap != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .offset(x = parrotStartPadding.dp, y = (-parrotBottomPadding).dp)
                        ) {
                            ParrotSprite(
                                bitmap = parrotBitmap,
                                height = 68.dp,
                                cheerCount = cheerCount,
                                missCount = missCount,
                                celebrate = uiState.isFinished && uiState.score >= uiState.total
                            )

                            galleryState.pirateSpeech?.let { speech ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF0F172A).copy(alpha = 0.95f),
                                    border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .offset(y = (-50).dp)
                                        .zIndex(10f)
                                ) {
                                    Text(
                                        speech,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Cannon sprite (visual only)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(
                                x = cannonHorizontalOffset.dp,
                                y = (-cannonBottomPadding).dp
                            )
                            .zIndex(2.0f)
                    ) {
                        CannonAimingView(
                            modifier = Modifier.size((154f * cannonScale).dp),
                            aimX = galleryState.aimX,
                            aimY = galleryState.aimY,
                            scale = cannonScale
                        )
                    }

                    // Hero
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(
                                x = heroHorizontalOffset.dp,
                                y = (-heroBottomPadding).dp
                            )
                            .zIndex(2.5f)
                    ) {
                        val heroConfig = remember(uiState.avatar) { AvatarConfig.fromAvatar(uiState.avatar) }
                        HeroSprite(
                            config = heroConfig,
                            modifier = Modifier
                                .height(48.dp)
                                .graphicsLayer {
                                    scaleX = heroScale
                                    scaleY = heroScale
                                },
                            animationState = HeroAnimationState.IDLE,
                            sway = false,
                            breath = false,
                            overrideTopPath = "sprites/special/pirate_blaster/pirate_blaster_clothes.png",
                            layerVisibility = mapOf(
                                CharacterLayerType.HAIR to false,
                                CharacterLayerType.HAT to false,
                                CharacterLayerType.SHOES to false,
                                CharacterLayerType.BOTTOM to false
                            )
                        )
                    }
                }
            }
        }

        // ===================== WORD DISPLAY =====================
        if (!uiState.isFinished) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = wordDisplayBottomPadding.dp)
                    .systemBarsPadding()
                    .zIndex(2000f),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.height(64.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (leftWordBitmap != null) {
                        Image(
                            bitmap = leftWordBitmap,
                            contentDescription = null,
                            modifier = Modifier.fillMaxHeight(),
                            contentScale = ContentScale.FillHeight
                        )
                    }

                    val letterCount = galleryState.maskedWord.size.coerceAtLeast(1)
                    Row(modifier = Modifier.fillMaxHeight()) {
                        repeat(letterCount) {
                            if (middleWordBitmap != null) {
                                Image(
                                    bitmap = middleWordBitmap,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(44.dp),
                                    contentScale = ContentScale.FillBounds
                                )
                            }
                        }
                    }

                    if (rightWordBitmap != null) {
                        Image(
                            bitmap = rightWordBitmap,
                            contentDescription = null,
                            modifier = Modifier.fillMaxHeight(),
                            contentScale = ContentScale.FillHeight
                        )
                    }
                }

                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    galleryState.maskedWord.forEachIndexed { i, ch ->
                        val isTarget = galleryState.missingIndices.getOrNull(galleryState.currentPointer) == i
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isTarget) Color(0xFFF59E0B).copy(alpha = 0.9f) else Color(0xFF1E293B).copy(alpha = 0.85f),
                            border = BorderStroke(1.5.dp, Color(0xFFF59E0B)),
                            modifier = Modifier.size(width = 36.dp, height = 44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    if (ch == '_') "" else ch.toString(),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // ===================== TOP HUD =====================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .systemBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .zIndex(200f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            HeartsBanner(healthUnits = galleryState.healthUnits, difficulty = uiState.overallDifficulty)
            CoinBanner(coins = galleryState.coins)

            val pauseBitmap = remember(context) { loadAssetImageBitmap(context, "games/pirate_blaster/pause.png") }
            IconButton(
                onClick = { isPaused = true },
                enabled = !uiState.isFinished,
                modifier = Modifier.size(42.dp)
            ) {
                if (pauseBitmap != null) {
                    Image(
                        bitmap = pauseBitmap,
                        contentDescription = "Pause",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.5.dp, Color(0xFFF59E0B)),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("⏸️", fontSize = 16.sp)
                        }
                    }
                }
            }
        }

        // ===================== PAUSE OVERLAY =====================
        if (isPaused) {
            val pausePanelBitmap = remember(context) { loadAssetImageBitmap(context, "games/pirate_blaster/game_paused_panel.png") }
            val continueBitmap = remember(context) { loadAssetImageBitmap(context, "games/pirate_blaster/continue_playing_button.png") }
            val restartBitmap = remember(context) { loadAssetImageBitmap(context, "games/pirate_blaster/restart_game_button.png") }
            val backBitmap = remember(context) { loadAssetImageBitmap(context, "games/pirate_blaster/back_to_games_button.png") }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(3000f)
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.80f)
                        .aspectRatio(if (pausePanelBitmap != null) pausePanelBitmap.width.toFloat() / pausePanelBitmap.height.toFloat() else 16f/9f),
                    contentAlignment = Alignment.Center
                ) {
                    if (pausePanelBitmap != null) {
                        Image(
                            bitmap = pausePanelBitmap,
                            contentDescription = "Game Paused Panel",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Color(0xFF0F172A),
                            border = BorderStroke(2.dp, Color(0xFFF59E0B)),
                            modifier = Modifier.fillMaxSize()
                        ) {}
                    }

                    // Buttons overlaid on top of the pause panel graphic
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.60f)
                            .padding(top = 110.dp)
                    ) {
                        // Continue Playing Button
                        if (continueBitmap != null) {
                            Image(
                                bitmap = continueBitmap,
                                contentDescription = "Continue Playing",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clickable { isPaused = false },
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Button(
                                onClick = { isPaused = false },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                            ) {
                                Text("▶️ CONTINUE PLAYING", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Restart New Game Button
                        if (restartBitmap != null) {
                            Image(
                                bitmap = restartBitmap,
                                contentDescription = "Restart New Game",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clickable {
                                        isPaused = false
                                        MusicManager.stopMusic()
                                        MusicManager.playMusic(context, "music/pirate_blaster_scene1.wav")
                                        viewModel.startGame()
                                    },
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Button(
                                onClick = {
                                    isPaused = false
                                    MusicManager.stopMusic()
                                    MusicManager.playMusic(context, "music/pirate_blaster_scene1.wav")
                                    viewModel.startGame()
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                            ) {
                                Text("🔄 RESTART NEW GAME", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Back to Games Button
                        if (backBitmap != null) {
                            Image(
                                bitmap = backBitmap,
                                contentDescription = "Back to Games",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clickable { onBack() },
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            OutlinedButton(
                                onClick = onBack,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                            ) {
                                Text("Back to Games", color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Translucent Red Flash Overlay on damage or game over
        if (showDamageFlash || (uiState.isFinished && galleryState.healthUnits <= 0)) {
            val infiniteAnim = rememberInfiniteTransition(label = "damage_flash")
            val alphaAnim by infiniteAnim.animateFloat(
                initialValue = 0.3f,
                targetValue = 0.8f,
                animationSpec = infiniteRepeatable(tween(400, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
                label = "flash"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(150f)
                    .background(Color.Red.copy(alpha = alphaAnim * 0.4f))
            )
        }

        // ===================== RESULT OVERLAY =====================
        if (uiState.isFinished) {
            val isGameLost = galleryState.healthUnits <= 0 || galleryState.isGameOver
            val gameOverPanelBitmap = remember(context) { loadAssetImageBitmap(context, "games/pirate_blaster/game_over_panel.png") }
            val restartBitmap = remember(context) { loadAssetImageBitmap(context, "games/pirate_blaster/restart_game_button.png") }
            val backBitmap = remember(context) { loadAssetImageBitmap(context, "games/pirate_blaster/back_to_games_button.png") }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(250f)
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .aspectRatio(if (gameOverPanelBitmap != null) gameOverPanelBitmap.width.toFloat() / gameOverPanelBitmap.height.toFloat() else 16f/9f),
                    contentAlignment = Alignment.Center
                ) {
                    if (gameOverPanelBitmap != null) {
                        Image(
                            bitmap = gameOverPanelBitmap,
                            contentDescription = "Game Over Panel",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Color(0xFF0F172A),
                            border = BorderStroke(2.dp, if (isGameLost) Color(0xFFEF4444) else Color(0xFFF59E0B)),
                            modifier = Modifier.fillMaxSize()
                        ) {}
                    }

                    // Content & Buttons overlaid on top of panel graphic
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .padding(top = 115.dp)
                    ) {
                        Text(
                            text = "Score: ${uiState.score} / ${uiState.total}   |   🪙 Coins: ${galleryState.coins}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFF59E0B),
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(8.dp))

                        // Restart Button
                        if (restartBitmap != null) {
                            Image(
                                bitmap = restartBitmap,
                                contentDescription = "Restart New Game",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .clickable {
                                        MusicManager.stopMusic()
                                        MusicManager.playMusic(context, "music/pirate_blaster_scene1.wav")
                                        viewModel.startGame()
                                    },
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Button(
                                onClick = {
                                    MusicManager.stopMusic()
                                    MusicManager.playMusic(context, "music/pirate_blaster_scene1.wav")
                                    viewModel.startGame()
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                            ) {
                                Text("🔄 RESTART NEW GAME", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Back to Games Button
                        if (backBitmap != null) {
                            Image(
                                bitmap = backBitmap,
                                contentDescription = "Back to Games",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .clickable { onBack() },
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            OutlinedButton(
                                onClick = onBack,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                            ) {
                                Text("Back to Games", color = Color.White)
                            }
                        }
                    }
                }
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

@SuppressLint("MissingPermission")
private fun triggerVibration(context: Context) {
    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(120)
        }
    } catch (_: Exception) {}
}

@Composable
private fun HeartsBanner(
    healthUnits: Int,
    difficulty: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val leftBitmap = remember(context) { loadAssetImageBitmap(context, "games/pirate_blaster/left_word.png") }
    val middleBitmap = remember(context) { loadAssetImageBitmap(context, "games/pirate_blaster/middle_word.png") }
    val rightBitmap = remember(context) { loadAssetImageBitmap(context, "games/pirate_blaster/right_word.png") }
    val fullHeartBitmap = remember(context) { loadAssetImageBitmap(context, "games/pirate_blaster/full_heart.png") }
    val halfHeartBitmap = remember(context) { loadAssetImageBitmap(context, "games/pirate_blaster/half_heart.png") }
    val emptyHeartBitmap = remember(context) { loadAssetImageBitmap(context, "games/pirate_blaster/empty_heart.png") }

    val numHearts = when (difficulty) {
        1 -> 5
        2 -> 4
        3 -> 3
        else -> 5
    }

    Log.d("PirateBlaster", "HEART_UI_UPDATE: healthUnits=$healthUnits, displayedHearts=$numHearts")

    Box(
        modifier = modifier.height(52.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leftBitmap != null) {
                Image(bitmap = leftBitmap, contentDescription = null, modifier = Modifier.fillMaxHeight(), contentScale = ContentScale.FillHeight)
            }
            repeat(numHearts) {
                if (middleBitmap != null) {
                    Image(bitmap = middleBitmap, contentDescription = null, modifier = Modifier.fillMaxHeight().width(30.dp), contentScale = ContentScale.FillBounds)
                }
            }
            if (rightBitmap != null) {
                Image(bitmap = rightBitmap, contentDescription = null, modifier = Modifier.fillMaxHeight(), contentScale = ContentScale.FillHeight)
            }
        }

        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var remainingUnits = healthUnits
            for (i in 0 until numHearts) {
                val unitsForThisHeart = remainingUnits.coerceIn(0, 2)
                remainingUnits = (remainingUnits - unitsForThisHeart).coerceAtLeast(0)
                val heartBmp = when (unitsForThisHeart) {
                    2 -> fullHeartBitmap
                    1 -> halfHeartBitmap
                    else -> emptyHeartBitmap
                }
                if (heartBmp != null) {
                    Image(
                        bitmap = heartBmp,
                        contentDescription = "Heart $i",
                        modifier = Modifier.size(28.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

@Composable
private fun CoinBanner(
    coins: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val leftBitmap = remember(context) { loadAssetImageBitmap(context, "games/pirate_blaster/coin_box_left.png") }
    val middleBitmap = remember(context) { loadAssetImageBitmap(context, "games/pirate_blaster/middle_top_box.png") }
    val rightBitmap = remember(context) { loadAssetImageBitmap(context, "games/pirate_blaster/right_top_box.png") }

    Box(
        modifier = modifier.height(44.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leftBitmap != null) {
                Image(bitmap = leftBitmap, contentDescription = null, modifier = Modifier.fillMaxHeight(), contentScale = ContentScale.FillHeight)
            }
            repeat(2) {
                if (middleBitmap != null) {
                    Image(bitmap = middleBitmap, contentDescription = null, modifier = Modifier.fillMaxHeight().width(32.dp), contentScale = ContentScale.FillBounds)
                }
            }
            if (rightBitmap != null) {
                Image(bitmap = rightBitmap, contentDescription = null, modifier = Modifier.fillMaxHeight(), contentScale = ContentScale.FillHeight)
            }
        }

        Text(
            text = coins.toString(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}

@Composable
private fun BarrelExplosionOverlay(
    modifier: Modifier = Modifier,
    letter: Char? = null,
    scale: Float = 1f,
    onComplete: () -> Unit = {}
) {
    val context = LocalContext.current
    var frameIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        val frameMs = 50L // 15 frames @ 20fps = 750ms
        for (i in 0 until 15) {
            frameIndex = i
            delay(frameMs)
        }
        onComplete()
    }

    val formattedFrame = if (frameIndex < 10) "0$frameIndex" else "$frameIndex"
    val framePath = "games/pirate_blaster/animations/barrel_explosion/frame_$formattedFrame.png"
    val explosionBitmap = remember(context, framePath) {
        loadAssetImageBitmap(context, framePath)
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (explosionBitmap != null) {
            Image(
                bitmap = explosionBitmap,
                contentDescription = "Barrel Explosion",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        if (letter != null && frameIndex <= 3) {
            Text(
                text = letter.toString(),
                fontSize = (24 * scale).sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Serif,
                color = Color(0xFF0F2B48),
                modifier = Modifier
                    .offset(y = 0.dp)
                    .graphicsLayer {
                        alpha = if (frameIndex <= 2) 0.92f else 0.4f
                    }
            )
        }
    }
}

@Composable
fun CannonAimingView(
    modifier: Modifier = Modifier,
    aimX: Float = 0f,
    aimY: Float = 0f,
    scale: Float = 1.0f
) {
    val context = LocalContext.current
    val manager = remember(context) { CannonSpriteManager.getInstance(context) }

    val rawYaw = (aimX.coerceIn(-1f, 1f) * 2f) + 2f // 0..4
    val rawPitch = 2f - (aimY.coerceIn(-1f, 1f) * 2f) // 0..4
    val yawIndex = rawYaw.roundToInt().coerceIn(0, 4)
    val pitchIndex = rawPitch.roundToInt().coerceIn(0, 4)

    val sprite = manager.getSprite(yawIndex, pitchIndex)

    Canvas(modifier = modifier) {
        val bmp = sprite?.bitmap ?: return@Canvas
        val pivot = manager.pivot
        val s = scale // Uses scale parameter

        val mountX = size.width / 2f
        val mountY = size.height

        val left = mountX - pivot.x * s
        val top = mountY - pivot.y * s

        val drawOp: DrawScope.() -> Unit = {
            drawImage(
                image = bmp,
                dstOffset = IntOffset(left.roundToInt(), top.roundToInt()),
                dstSize = IntSize((bmp.width * s).roundToInt(), (bmp.height * s).roundToInt())
            )
        }

        if (sprite.flipX) {
            withTransform({
                scale(scaleX = -1f, scaleY = 1f, pivot = Offset(mountX, mountY))
            }) {
                drawOp()
            }
        } else {
            drawOp()
        }
    }
}

@Composable
private fun CannonballShotOverlay(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 28.dp
) {
    val context = LocalContext.current
    val cannonballBitmaps = remember(context) {
        (0..15).map { i ->
            val formatted = if (i < 10) "0$i" else "$i"
            loadAssetImageBitmap(context, "games/pirate_blaster/animations/cannonball/cannonball_$formatted.png")
        }
    }

    var frameIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        val frameMs = 60L // 16 frames @ 60ms per frame
        while (true) {
            frameIndex = (frameIndex + 1) % 16
            delay(frameMs)
        }
    }

    val bmp = cannonballBitmaps.getOrNull(frameIndex)
    if (bmp != null) {
        Image(
            bitmap = bmp,
            contentDescription = "Animated Cannonball",
            modifier = modifier.size(sizeDp),
            contentScale = ContentScale.Fit
        )
    } else {
        Box(
            modifier = modifier
                .size(sizeDp)
                .clip(CircleShape)
                .background(Color(0xFF1E293B))
                .border(2.dp, Color(0xFFF59E0B), CircleShape)
        )
    }
}
