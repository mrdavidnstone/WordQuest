package com.spellinggames.app.ui.screens.games

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
import com.spellinggames.app.data.model.AvatarConfig
import com.spellinggames.app.ui.components.GameHeader
import com.spellinggames.app.ui.components.ReadyOverlay
import com.spellinggames.app.ui.components.avatar.HeroAnimationState
import com.spellinggames.app.ui.components.avatar.HeroSprite
import com.spellinggames.app.util.MusicManager
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

    // Subtle Rocking / Floating Animation for Ship Foreground (±8px vertical, ±0.6° rotation)
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

        // Adjustable settings (Background zoom, speed, positioning, and avatar placement)
        var backgroundScrollSpeed by remember { mutableFloatStateOf(4f) }
        var backgroundVerticalOffset by remember { mutableFloatStateOf(50f) } // Positive moves the background down
        var zoomFactor by remember { mutableFloatStateOf(6.0f) } // Much closer/larger background matching concept art
        var avatarBottomPadding by remember { mutableFloatStateOf(16f) } // Lower down near bottom edge
        var avatarEndPadding by remember { mutableFloatStateOf(4f) }

        val density = LocalDensity.current.density
        val imageW = remember(canvasH, density, zoomFactor) { 9600f * ((canvasH.value * zoomFactor) / 1080f) }
        val imageWDp = remember(imageW, density) { (imageW / density).dp }
        val scaledH = canvasH * zoomFactor
        val verticalOffset = ((canvasH.value - scaledH.value) / 2f) + backgroundVerticalOffset

        var scene1X by remember { mutableFloatStateOf(0f) }
        var initialized by remember { mutableStateOf(false) }

        if (!initialized && imageW > 0f) {
            scene1X = 0f
            initialized = true
        }

        LaunchedEffect(imageW) {
            var lastTime = System.nanoTime()
            while (true) {
                val currentTime = withFrameNanos { it }
                val deltaSeconds = (currentTime - lastTime) / 1_000_000_000f
                lastTime = currentTime

                if (deltaSeconds > 0 && deltaSeconds < 0.1f) {
                    scene1X -= backgroundScrollSpeed * deltaSeconds
                    if (scene1X <= -imageW) {
                        scene1X += imageW
                    }
                }
            }
        }

        val scene1CopyX = scene1X + imageW

        // SCENE 1 ENDLESS SCROLLING BACKGROUND (Seamlessly joined, no gaps)
        if (scene1Bitmap != null) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    bitmap = scene1Bitmap,
                    contentDescription = "Scene 1 Background",
                    modifier = Modifier
                        .size(width = imageWDp, height = scaledH)
                        .graphicsLayer {
                            translationX = scene1X
                            translationY = verticalOffset
                        },
                    contentScale = ContentScale.FillBounds
                )
                Image(
                    bitmap = scene1Bitmap,
                    contentDescription = "Scene 1 Background Copy",
                    modifier = Modifier
                        .size(width = imageWDp, height = scaledH)
                        .graphicsLayer {
                            translationX = scene1CopyX
                            translationY = verticalOffset
                        },
                    contentScale = ContentScale.FillBounds
                )
            }
        } else {
            // Fallback gradient if bitmap is missing
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.45f)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF0F172A), Color(0xFF0284C7))
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.55f)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF0284C7), Color(0xFF075985), Color(0xFF0C4A6E))
                            )
                        )
                )
            }
        }

        // FOREGROUND DOCK OVERLAY WITH SUBTLE SHIP FLOATING / ROCKING MOTION (zIndex 1f)
        if (foregroundBitmap != null) {
            val bitmapWidth = foregroundBitmap.width.toFloat()
            val bitmapHeight = foregroundBitmap.height.toFloat()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f),
                contentAlignment = Alignment.BottomCenter
            ) {
                Image(
                    bitmap = foregroundBitmap,
                    contentDescription = "Foreground Dock",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(if (bitmapWidth > 0f && bitmapHeight > 0f) bitmapWidth / bitmapHeight else (16f / 9f))
                        .offset(y = 24.dp)
                        .graphicsLayer {
                            translationY = dockOffsetY
                            rotationZ = dockRotation
                            transformOrigin = TransformOrigin(0.5f, 1.0f)
                        },
                    contentScale = ContentScale.Fit
                )
            }
        }

        // FLOATING BARREL TARGETS (Converted missing letter choices)
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

        // CANNONBALL PROJECTILE IN FLIGHT
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

        // FOREGROUND LOWER-RIGHT: CANNON & HERO AVATAR & PIRATE COMPANION (Z-index on top of foreground)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = avatarEndPadding.dp, bottom = avatarBottomPadding.dp)
                .zIndex(2f)
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                // Pirate Captain Companion
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
                    Text("🦜🏴‍☠️", fontSize = 18.sp)
                }

                Spacer(Modifier.width(6.dp))

                // Cannon Barrel
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

                // Hero Sprite Avatar (Scaled down to 30.dp)
                val heroConfig = remember(uiState.avatar) { AvatarConfig.fromAvatar(uiState.avatar) }
                HeroSprite(
                    config = heroConfig,
                    modifier = Modifier.height(20.dp),
                    animationState = if (uiState.isCorrect == true) HeroAnimationState.VICTORY else HeroAnimationState.IDLE,
                    sway = false,
                    breath = false
                )
            }
        }

        // INCOMPLETE WORD DISPLAY (CENTER BOTTOM)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
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

        // LANDSCAPE TOP HUD
        Row(
            modifier = Modifier
                .fillMaxWidth()
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

        // RESULT OVERLAY ON ROUND FINISH
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
    try {
        context.assets.open(assetName).use { stream ->
            BitmapFactory.decodeStream(stream)?.asImageBitmap()?.let { return it }
        }
    } catch (_: Exception) {}
    return null
}
