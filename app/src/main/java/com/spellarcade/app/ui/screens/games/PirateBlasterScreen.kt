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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.spellarcade.app.data.model.AvatarConfig
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
        ReadyOverlay(
            title = "Pirate Blaster",
            emoji = "🏴‍☠️",
            description = "Aim your cannon at the barrel with the correct missing letter!",
            uiState = uiState,
            onSwitchList = { listId -> viewModel.switchList(listId) },
            onStart = { viewModel.startGame() },
            onBack = onBack
        )
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
    ) {
        val canvasW = this.maxWidth
        val canvasH = this.maxHeight

        val scene1Bitmap = remember(context) { loadAssetImageBitmap(context, "games/pirate_blaster/scene1.png") }
        val foregroundBitmap = remember(context) { loadAssetImageBitmap(context, "games/pirate_blaster/foreground.png") }
        val parrotBitmap = remember(context) { loadAssetImageBitmap(context, "games/pirate_blaster/animations/parrot.png") }

        // ===================== ADJUSTABLE SETTINGS =====================
        var backgroundScrollSpeed by remember { mutableFloatStateOf(105f) }   // pixels per second
        var zoomFactor by remember { mutableFloatStateOf(1.0f) }           // 1.0 = exact height, higher = zoom in
        var backgroundVerticalOffset by remember { mutableFloatStateOf(-50f) } // dp (0 = aligned to top)
        var seamOffset by remember { mutableFloatStateOf(-735f) }              // negative = pull copies closer (try -2f to -8f)
        var heroBottomPadding by remember { mutableFloatStateOf(130f) }
        var heroEndPadding by remember { mutableFloatStateOf(-60f) }
        var parrotBottomPadding by remember { mutableFloatStateOf(165f) }
        var parrotStartPadding by remember { mutableFloatStateOf(5f) }
        var foregroundWidthScale by remember { mutableFloatStateOf(1.02f) } // 1.02 = slightly wider to cover edge gaps
// ===============================================================

        val density = LocalDensity.current
        val screenHeightPx = with(density) { canvasH.toPx() }

// Use REAL bitmap size (this is the most important fix)
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

        val effectiveWidth = imageWidthPx + seamOffset
        val x1 = - (scrollDistance % effectiveWidth)
        val x2 = x1 + effectiveWidth

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

                    // CANNON + HERO + COMPANION (Nested directly inside dock container!)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(
                                x = (-heroEndPadding).dp,
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
        galleryState.targets.forEach { target ->
            val targetX = canvasW * target.positionRatio.x
            val targetY = (canvasH * target.positionRatio.y) + waveBob.dp

            Box(
                modifier = Modifier
                    .offset(x = targetX - 32.dp, y = targetY - 36.dp)
                    .size(width = 68.dp, height = 76.dp)
                    .zIndex(2f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        when (target.state) {
                            PirateTargetState.HIT_CORRECT -> Color(0xFF22C55E)
                            PirateTargetState.HIT_INCORRECT -> Color(0xFFEF4444)
                            else -> Color(0xFF78350F)
                        }
                    )
                    .border(
                        3.dp,
                        when (target.state) {
                            PirateTargetState.HIT_CORRECT -> Color.White
                            PirateTargetState.HIT_INCORRECT -> Color.White
                            else -> Color(0xFFF59E0B)
                        },
                        RoundedCornerShape(16.dp)
                    )
                    .clickable(
                        enabled = !galleryState.isFiring && !galleryState.isResolving && !uiState.showFeedback,
                        onClick = { viewModel.fireAtTarget(target) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🛢️", fontSize = 18.sp)
                    Text(
                        target.letter.toString(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }

        // ===================== CANNONBALL =====================
        galleryState.activeShot?.let { shot ->
            val p = shot.progress
            val shotX = canvasW * (shot.startRatio.x + (shot.targetRatio.x - shot.startRatio.x) * p)
            val shotY = canvasH * (shot.startRatio.y + (shot.targetRatio.y - shot.startRatio.y) * p)

            Box(
                modifier = Modifier
                    .offset(x = shotX - 10.dp, y = shotY - 10.dp)
                    .size(20.dp)
                    .zIndex(2f)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B))
                    .border(2.dp, Color(0xFFF59E0B), CircleShape)
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