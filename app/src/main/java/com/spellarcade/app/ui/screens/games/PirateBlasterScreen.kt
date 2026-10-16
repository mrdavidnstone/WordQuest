package com.spellarcade.app.ui.screens.games

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.BitmapFactory
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

    // Portrait on Ready/Info screen, Landscape during Pirate Blaster gameplay
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
        val barrelBitmap = remember(context) { loadAssetImageBitmap(context, "games/pirate_blaster/barrels/barrel.png") }

        // ===================== ADJUSTABLE SETTINGS =====================
        var backgroundScrollSpeed by remember { mutableFloatStateOf(105f) }   // pixels per second
        var zoomFactor by remember { mutableFloatStateOf(1.0f) }           // 1.0 = exact height, higher = zoom in
        var backgroundVerticalOffset by remember { mutableFloatStateOf(-50f) } // dp (0 = aligned to top)
        var heroBottomPadding by remember { mutableFloatStateOf(120f) }
        var heroEndPadding by remember { mutableFloatStateOf(60f) } // Offset from right side
        var parrotBottomPadding by remember { mutableFloatStateOf(155f) }
        var parrotStartPadding by remember { mutableFloatStateOf(70f) }
        var foregroundWidthScale by remember { mutableFloatStateOf(1.02f) } // 1.02 = slightly wider to cover edge gaps
        var foregroundScale by remember { mutableFloatStateOf(1.12f) } // Overscan/bleed scale to cover edges during rocking rotation
        var seamOffset by remember { mutableFloatStateOf(-775f) } // Fine-tune loop gap (+ to add gap, - for overlap)
        var barrelLetterVerticalOffset by remember { mutableFloatStateOf(0f) } // Vertical offset for barrel letters (+ down, - up)
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

        // ===================== FOREGROUND DOCK & PARROT =====================
        if (foregroundBitmap != null) {
            val bitmapWidth = foregroundBitmap.width.toFloat()
            val bitmapHeight = foregroundBitmap.height.toFloat()

            val extraWidth = 60.dp
            Box(
                modifier = Modifier
                     .fillMaxSize()
                    .zIndex(1f),
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
                        }
                    }

                    // CANNON + HERO + COMPANION
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(
                                x = heroEndPadding.dp,
                                y = (-heroBottomPadding).dp
                            )
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            // Pirate Companion
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                galleryState.pirateSpeech?.let { speech ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF0F172A).copy(alpha = 0.9f),
                                        border = BorderStroke(1.dp, Color(0xFFF59E0B))
                                    ) {
                                        Text(
                                            speech,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                    Spacer(Modifier.height(2.dp))
                                }
                            }

                            Spacer(Modifier.width(6.dp))

                            // Cannon
                            Box(
                                modifier = Modifier
                                    .size(width = 44.dp, height = 26.dp)
                                    .rotate(galleryState.cannonAngle)
                                    .background(Color(0xFF334155), RoundedCornerShape(8.dp))
                                    .border(1.5.dp, Color(0xFFF59E0B), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(start = 3.dp)
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0F172A))
                                )
                            }

                            Spacer(Modifier.width(6.dp))

                            // Hero
                            val heroConfig = remember(uiState.avatar) { AvatarConfig.fromAvatar(uiState.avatar) }
                            HeroSprite(
                                config = heroConfig,
                                modifier = Modifier
                                    .height(48.dp)
                                    .graphicsLayer {
                                        scaleX = 0.45f
                                        scaleY = 0.45f
                                    },
                                animationState = if (uiState.isCorrect == true) HeroAnimationState.VICTORY else HeroAnimationState.IDLE,
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
        }

        // ===================== BARREL TARGETS =====================
        galleryState.targets.filter { it.state != PirateTargetState.DESTROYED }.forEach { target ->
            val targetX = canvasW * target.positionRatio.x
            val targetY = (canvasH * target.positionRatio.y) + waveBob.dp

            val isExploding = target.state == PirateTargetState.HIT_CORRECT || target.state == PirateTargetState.HIT_INCORRECT

            if (isExploding) {
                // Play 15-frame barrel explosion animation
                BarrelExplosionOverlay(
                    modifier = Modifier
                        .offset(x = targetX - 60.dp, y = targetY - 60.dp)
                        .zIndex(3f)
                )
            } else {
                Box(
                    modifier = Modifier
                        .offset(x = targetX - 34.dp, y = targetY - 40.dp)
                        .size(width = 68.dp, height = 80.dp)
                        .zIndex(2f)
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
                        fontSize = 24.sp,
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
                    .zIndex(2.5f)
            )
        }

        // ===================== WORD DISPLAY =====================
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .systemBarsPadding()
                .zIndex(2f)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.92f),
                border = BorderStroke(2.dp, Color(0xFFF59E0B))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    galleryState.maskedWord.forEachIndexed { i, ch ->
                        val isTarget = galleryState.missingIndices.getOrNull(galleryState.currentPointer) == i
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isTarget) Color(0xFFF59E0B) else Color(0xFF334155),
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
                .zIndex(2f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
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

        // ===================== RESULT OVERLAY =====================
        if (uiState.isFinished) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(10f)
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(24.dp)
                        .background(Color(0xFF0F172A), RoundedCornerShape(24.dp))
                        .border(BorderStroke(2.dp, Color(0xFFF59E0B)), RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    Text("🏴‍☠️ PIRATE VOYAGE COMPLETE! 🏴‍☠️", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Spacer(Modifier.height(8.dp))
                    Text("Score: ${uiState.score} / ${uiState.total}", fontSize = 16.sp, color = Color.White.copy(alpha = 0.8f))
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onBack,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                    ) {
                        Text("Back to Word Gallery", color = Color.White, fontWeight = FontWeight.Bold)
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

@Composable
private fun BarrelExplosionOverlay(
    modifier: Modifier = Modifier
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

    if (explosionBitmap != null) {
        Image(
            bitmap = explosionBitmap,
            contentDescription = "Barrel Explosion",
            modifier = modifier.size(120.dp),
            contentScale = ContentScale.Fit
        )
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