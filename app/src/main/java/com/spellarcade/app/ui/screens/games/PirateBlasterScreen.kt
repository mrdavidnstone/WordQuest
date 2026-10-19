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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.math.cos
import kotlin.math.sin
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.math.roundToInt
import com.spellarcade.app.data.model.AvatarConfig
import com.spellarcade.app.ui.components.ActiveListBadge
import com.spellarcade.app.ui.components.ReadyOverlay
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
            // Full-Size Title Logo Overlay (Exact size & position matching Lab Meltdown)
            if (titleBitmap != null) {
                Image(
                    bitmap = titleBitmap,
                    contentDescription = "Pirate Blaster Title Overlay",
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
                                    containerColor = Color(0xFFD97706)
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
    val dockOffsetY by dockAnim.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dock_vertical"
    )
    val dockRotation by dockAnim.animateFloat(
        initialValue = -0.6f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dock_rotation"
    )

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
                    if (!galleryState.isFiring && !galleryState.isResolving && !uiState.showFeedback && !uiState.isFinished) {
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
        var backgroundScrollSpeed by remember { mutableFloatStateOf(105f) }   // pixels per second
        var zoomFactor by remember { mutableFloatStateOf(1.0f) }           // 1.0 = exact height, higher = zoom in
        var backgroundVerticalOffset by remember { mutableFloatStateOf(-50f) } // dp (0 = aligned to top)
        var cannonBottomPadding by remember { mutableFloatStateOf(40f) } // Lower toward bottom of screen
        var cannonHorizontalOffset by remember { mutableFloatStateOf(0f) }
        var cannonScale by remember { mutableFloatStateOf(10f) } // Cannon scale factor
        var heroBottomPadding by remember { mutableFloatStateOf(130f) }
        var heroHorizontalOffset by remember { mutableFloatStateOf(200f) } // Horizontal offset (+ right, - left)
        var parrotBottomPadding by remember { mutableFloatStateOf(170f) }
        var parrotStartPadding by remember { mutableFloatStateOf(70f) }
        var foregroundWidthScale by remember { mutableFloatStateOf(1.02f) } // 1.02 = slightly wider to cover edge gaps
        var foregroundScale by remember { mutableFloatStateOf(1.15f) } // Overscan/bleed scale to cover edges during rocking rotation
        var seamOffset by remember { mutableFloatStateOf(-900f) } // Fine-tune loop gap (+ to add gap, - for overlap)
        var barrelLetterVerticalOffset by remember { mutableFloatStateOf(0f) } // Vertical offset for barrel letters (+ down, - up)
        var heroScale by remember { mutableFloatStateOf(0.25f) } // Hero avatar scale factor (smaller = smaller hero)
// ===============================================================

        val density = LocalDensity.current
        val screenHeightPx = with(density) { canvasH.toPx() }

// Use REAL bitmap size (2172x724)
        val originalWidthPx = scene1Bitmap?.width?.toFloat() ?: 2172f
        val originalHeightPx = scene1Bitmap?.height?.toFloat() ?: 724f

        val scale = (screenHeightPx * zoomFactor) / originalHeightPx
        val imageWidthPx = originalWidthPx * scale
        val imageHeightPx = originalHeightPx * scale

        val verticalOffsetPx = 0f + with(density) { backgroundVerticalOffset.dp.toPx() }

        var scrollDistance by remember { mutableFloatStateOf(0f) }

// Continuous scrolling
        LaunchedEffect(Unit) {
            var lastTime = withFrameNanos { it }
            while (true) {
                val currentTime = withFrameNanos { it }
                val delta = (currentTime - lastTime) / 1_000_000_000f
                lastTime = currentTime

                if (delta in 0.001f..0.1f) {
                    scrollDistance += backgroundScrollSpeed * delta
                }
            }
        }

        val roundedWidth = (imageWidthPx + seamOffset).roundToInt().toFloat()
        val x1 = - (scrollDistance % roundedWidth).roundToInt().toFloat()
        val x2 = x1 + roundedWidth

// ===================== BACKGROUND =====================
        if (scene1Bitmap != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // First copy
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

                // Second copy
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
                // Play 15-frame barrel explosion animation matching exact barrel dimensions with letter visible for first few frames
                BarrelExplosionOverlay(
                    letter = target.letter,
                    scale = target.scale,
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
                    // Real Barrel Asset Image with Target Circle
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

                    // Weathered Stencil Letter centered over the painted white circle target
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
                modifier = Modifier
                    .offset(x = shotX - 18.dp, y = shotY - 18.dp)
                    .zIndex(4f)
            )
        }

        // ===================== FOREGROUND DOCK & PARROT =====================
        if (foregroundBitmap != null) {
            val bitmapWidth = foregroundBitmap.width.toFloat()
            val bitmapHeight = foregroundBitmap.height.toFloat()

            val extraWidth = 60.dp
            Box(
                modifier = Modifier
                     .fillMaxSize()
                    .zIndex(3f),
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                         .fillMaxSize()
                        .offset(y = 50.dp)
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

                    // PIRATE PARROT PERCHED ON LEFT DOCK POST (Synchronized inside dock rocking container)
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

                            // Independent floating speech bubble above parrot (zero layout impact on hero/cannon)
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

                    // Independent Cannon Box (zIndex 2.0f)
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
                            cannonAngle = galleryState.cannonAngle,
                            modifier = Modifier.size(110.dp)
                        )
                    }

                    // Independent Hero Box (zIndex 2.5f)
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
                            animationState = HeroAnimationState.IDLE, // Locked to IDLE so hero never jumps or repositions
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
        galleryState.targets.filter { it.state != PirateTargetState.DESTROYED }.forEach { target ->
            val targetX = canvasW * target.x
            val targetY = (canvasH * target.y) + waveBob.dp

            val isExploding = target.state == PirateTargetState.HIT_CORRECT || target.state == PirateTargetState.HIT_INCORRECT

            if (isExploding) {
                // Play 15-frame barrel explosion animation matching exact barrel dimensions with letter visible for first few frames
                BarrelExplosionOverlay(
                    letter = target.letter,
                    scale = target.scale,
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
                    // Real Barrel Asset Image with Target Circle
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

                    // Weathered Stencil Letter centered over the painted white circle target
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
                modifier = Modifier
                    .offset(x = shotX - 18.dp, y = shotY - 18.dp)
                    .zIndex(4f)
            )
        }

        // ===================== WORD DISPLAY =====================
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .systemBarsPadding()
                .zIndex(100f),
            contentAlignment = Alignment.Center
        ) {
            // Wooden sign background container composed of left, repeating middle (per letter), and right
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

            // Letter tiles overlaid on top of the wooden sign background
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
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

                HeartsBanner(healthUnits = galleryState.healthUnits, difficulty = uiState.overallDifficulty)
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.88f),
                border = BorderStroke(1.5.dp, Color(0xFFF59E0B))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🏴‍☠️ PIRATE BLASTER", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFFF59E0B))
                    Text("SCORE: ${uiState.score}/${uiState.total}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("MISSES: ${galleryState.missesCount}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                }
            }

            Spacer(Modifier.width(36.dp))
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(250f)
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(24.dp)
                        .background(Color(0xFF0F172A), RoundedCornerShape(24.dp))
                        .border(BorderStroke(2.dp, if (isGameLost) Color(0xFFEF4444) else Color(0xFFF59E0B)), RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    Text(
                        if (isGameLost) "💥 GAME OVER! 💥" else "🏴‍☠️ PIRATE VOYAGE COMPLETE! 🏴‍☠️",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isGameLost) Color(0xFFEF4444) else Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (isGameLost) "The pirate ship suffered a critical defeat!" else "Score: ${uiState.score} / ${uiState.total}",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                    if (isGameLost) {
                        Spacer(Modifier.height(4.dp))
                        Text("Score: ${uiState.score} / ${uiState.total}", fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
                    }
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = { viewModel.startGame() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                    ) {
                        Text("🔄 RESTART NEW GAME", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(10.dp))
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
        1 -> 5 // Easy: 5 hearts (10 half-heart units)
        2 -> 4 // Medium: 4 hearts (8 half-heart units)
        3 -> 3 // Hard: 3 hearts (6 half-heart units)
        else -> 5
    }

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
                    Image(bitmap = middleBitmap, contentDescription = null, modifier = Modifier.fillMaxHeight().width(36.dp), contentScale = ContentScale.FillBounds)
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
            for (i in 0 until numHearts) {
                val unitsForThisHeart = (healthUnits - (2 * i)).coerceIn(0, 2)
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
private fun BarrelExplosionOverlay(
    modifier: Modifier = Modifier,
    letter: Char? = null,
    scale: Float = 1f
) {
    val context = LocalContext.current
    var frameIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        val frameMs = 50L // 15 frames @ 20fps = 750ms
        for (i in 0 until 15) {
            frameIndex = i
            delay(frameMs)
        }
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

        // Keep the letter visible during the first few frames (frameIndex <= 3) before dissolving
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
    cannonAngle: Float,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val manager = remember(context) { CannonSpriteManager.getInstance(context) }
    var yawIndex by remember { mutableIntStateOf(2) }
    var pitchIndex by remember { mutableIntStateOf(2) }

    val rad = Math.toRadians(cannonAngle.toDouble())
    val aimX = cos(rad).toFloat()
    val aimY = -sin(rad).toFloat()

    LaunchedEffect(aimX, aimY) {
        val (yI, pI) = getAimIndices(aimX, aimY, yawIndex, pitchIndex)
        yawIndex = yI
        pitchIndex = pI
    }

    val sprite = manager.getSprite(yawIndex, pitchIndex)

    Canvas(modifier = modifier) {
        val bmp = sprite?.bitmap ?: return@Canvas
        val pivot = manager.pivot
        val s = 0.32f // Uniform scale

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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var frameIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        val frameMs = 60L // 16 frames @ 60ms per frame
        var index = 0
        while (true) {
            frameIndex = index
            delay(frameMs)
            index = (index + 1) % 16
        }
    }

    val formattedFrame = if (frameIndex < 10) "0$frameIndex" else "$frameIndex"
    val framePath = "games/pirate_blaster/animations/cannonball/cannonball_$formattedFrame.png"
    val cannonballBitmap = remember(context, framePath) {
        loadAssetImageBitmap(context, framePath)
    }

    if (cannonballBitmap != null) {
        Image(
            bitmap = cannonballBitmap,
            contentDescription = "Animated Cannonball",
            modifier = modifier.size(36.dp),
            contentScale = ContentScale.Fit
        )
    }
}