package com.spellinggames.app.ui.screens.games

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spellinggames.app.data.model.AvatarConfig
import com.spellinggames.app.ui.components.GameHeader
import com.spellinggames.app.ui.components.ReadyOverlay
import com.spellinggames.app.ui.components.avatar.HeroAnimationState
import com.spellinggames.app.ui.components.avatar.HeroSprite
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PirateBlasterScreen(
    onBack: () -> Unit,
    viewModel: WordGalleryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val galleryState by viewModel.galleryState.collectAsState()

    // Enforce Landscape Orientation for Pirate Blaster gameplay, revert to Portrait on exit
    DisposableEffect(Unit) {
        val activity = context as? Activity
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
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
            title = "Pirate Word Blaster",
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

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F172A), Color(0xFF0284C7), Color(0xFF0369A1))
                )
            )
    ) {
        val canvasW = this.maxWidth
        val canvasH = this.maxHeight

        // PIRATE COVE ENVIRONMENT (Ocean, Wooden Docks, Sky)
        Column(modifier = Modifier.fillMaxSize()) {
            // Sky & Clouds
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

            // Ocean Water & Cove
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

        // FLOATING BARREL TARGETS (Converted missing letter choices)
        galleryState.targets.forEach { target ->
            val targetX = canvasW * target.positionRatio.x
            val targetY = (canvasH * target.positionRatio.y) + waveBob.dp

            Box(
                modifier = Modifier
                    .offset(x = targetX - 32.dp, y = targetY - 36.dp)
                    .size(width = 68.dp, height = 76.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        when (target.state) {
                            TargetState.HIT_CORRECT -> Color(0xFF22C55E)
                            TargetState.HIT_INCORRECT -> Color(0xFFEF4444)
                            else -> Color(0xFF78350F)
                        }
                    )
                    .border(
                        3.dp,
                        when (target.state) {
                            TargetState.HIT_CORRECT -> Color.White
                            TargetState.HIT_INCORRECT -> Color.White
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
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B))
                    .border(2.dp, Color(0xFFF59E0B), CircleShape)
            )
        }

        // FOREGROUND LOWER-LEFT: CANNON & HERO AVATAR & PIRATE COMPANION
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                // Hero Sprite Avatar
                val heroConfig = remember(uiState.avatar) { AvatarConfig.fromAvatar(uiState.avatar) }
                HeroSprite(
                    config = heroConfig,
                    modifier = Modifier.height(140.dp),
                    animationState = if (uiState.isCorrect == true) HeroAnimationState.VICTORY else HeroAnimationState.IDLE,
                    sway = false,
                    breath = false
                )

                Spacer(Modifier.width(8.dp))

                // Cannon Barrel
                Box(
                    modifier = Modifier
                        .size(width = 80.dp, height = 48.dp)
                        .rotate(galleryState.cannonAngle)
                        .background(Color(0xFF334155), RoundedCornerShape(12.dp))
                        .border(2.dp, Color(0xFFF59E0B), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Box(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F172A))
                    )
                }

                Spacer(Modifier.width(12.dp))

                // Pirate Captain Companion
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    galleryState.pirateSpeech?.let { speech ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0F172A).copy(alpha = 0.9f),
                            border = BorderStroke(1.dp, Color(0xFFF59E0B))
                        ) {
                            Text(
                                speech,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                    Text("🦜🏴‍☠️", fontSize = 28.sp)
                }
            }
        }

        // INCOMPLETE WORD DISPLAY (CENTER BOTTOM)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
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
                .padding(horizontal = 16.dp, vertical = 10.dp),
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
