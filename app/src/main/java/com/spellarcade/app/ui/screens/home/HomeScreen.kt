package com.spellarcade.app.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spellarcade.app.ui.components.ActiveListBadge
import com.spellarcade.app.ui.components.AvatarDisplay
import com.spellarcade.app.ui.components.EmptyState
import com.spellarcade.app.ui.theme.*
import com.spellarcade.app.ui.screens.profile.Avatar
import kotlin.random.Random

@Composable
fun HomeScreen(
    onNavigateToGames: () -> Unit,
    onNavigateToLists: () -> Unit,
    onNavigateToParent: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF021024)) // Deep Dark Blue
    ) {
        // 1. Background Layers
        StarryBackground()
        HomeBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(30.dp))
            
            // 2. Header Section (Hero + Title)
            Box(modifier = Modifier.fillMaxWidth()) {
                // Lock Icon - Top Right
                Surface(
                    onClick = onNavigateToParent,
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        if (uiState.isParentUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                        contentDescription = "Parent Area",
                        tint = Color.White,
                        modifier = Modifier.padding(8.dp).size(18.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 0.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hero (Shifted to far-left edge)
                    Box(modifier = Modifier.size(105.dp)) {
                        AvatarDisplay(
                            avatar = uiState.avatar,
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { onNavigateToProfile() },
                            sway = false,
                            isJumping = false
                        )
                    }
                    
                    Spacer(Modifier.width(4.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            "SpellArcade",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 30.sp,
                            lineHeight = 34.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⭐", fontSize = 10.sp)
                            Spacer(Modifier.width(3.dp))
                            Text(
                                "Let's play & learn!",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(3.dp))
                            Text("⭐", fontSize = 10.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // 3. Lesson Badge
            ActiveListBadge(
                listName = uiState.activeListName,
                allLists = uiState.allLists,
                onListSelected = { viewModel.setActiveList(it) },
                label = "",
                modifier = Modifier.widthIn(max = 200.dp)
            )

            Spacer(Modifier.height(16.dp))

            if (uiState.activeListName == null) {
                EmptyState(
                    emoji = "📚",
                    title = "No Lessons Yet",
                    message = "Ask a parent to create your first spelling lesson!",
                    actionLabel = "Go to Lessons",
                    onAction = onNavigateToLists
                )
            } else {
                // 4. Continue Playing Card
                ContinuePlayingCard(onClick = onNavigateToGames)

                Spacer(Modifier.height(20.dp))

                // 5. Your Progress Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Your Progress",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.width(12.dp))
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.White.copy(alpha = 0.2f)))
                }
                
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProgressStatCard(
                        modifier = Modifier.weight(1f),
                        emoji = "⭐",
                        value = "${uiState.masteredCount}",
                        label = "Mastered",
                        color = Color(0xFF4CAF50)
                    )
                    ProgressStatCard(
                        modifier = Modifier.weight(1f),
                        emoji = "🔥",
                        value = "${uiState.learningCount}",
                        label = "Learning",
                        color = Color(0xFFFF9800)
                    )
                    ProgressStatCard(
                        modifier = Modifier.weight(1f),
                        emoji = "💪",
                        value = "${uiState.needsPracticeCount}",
                        label = "Practice",
                        color = Color(0xFF9C27B0)
                    )
                }

                Spacer(Modifier.height(20.dp))

                // 6. Progress Bar Row
                if (uiState.wordCount > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${(uiState.overallProgress * 100).toInt()}% complete • ${uiState.wordCount} words",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(10.dp)
                                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(uiState.overallProgress.coerceIn(0.01f, 1f))
                                    .background(
                                        Brush.horizontalGradient(listOf(Color(0xFF00E5FF), Color(0xFF00E676))),
                                        CircleShape
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StarryBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val random = Random(42)
        repeat(100) {
            val x = random.nextFloat() * size.width
            val y = random.nextFloat() * size.height * 0.6f
            val radius = random.nextFloat() * 2.dp.toPx()
            val alpha = random.nextFloat() * 0.8f
            drawCircle(Color.White, radius, Offset(x, y), alpha = alpha)
        }
    }
}

@Composable
fun HomeBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Dark distant mountains
        val path1 = Path().apply {
            moveTo(0f, h * 0.45f)
            lineTo(w * 0.2f, h * 0.38f)
            lineTo(w * 0.4f, h * 0.46f)
            lineTo(w * 0.6f, h * 0.35f)
            lineTo(w * 0.8f, h * 0.42f)
            lineTo(w, h * 0.35f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(path1, Color(0xFF010A1A))

        // Blue glow mountains
        val path2 = Path().apply {
            moveTo(0f, h * 0.55f)
            cubicTo(w * 0.3f, h * 0.5f, w * 0.7f, h * 0.65f, w, h * 0.58f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path2,
            brush = Brush.verticalGradient(
                listOf(Color(0xFF1E88E5).copy(alpha = 0.1f), Color.Transparent)
            )
        )
    }
}

@Composable
fun ContinuePlayingCard(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        border = BorderStroke(2.dp, Color(0xFF00B0FF).copy(alpha = 0.6f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0091EA), Color(0xFF00B0FF))
                    )
                )
                .padding(16.dp)
        ) {
            // Little sparkles
            Text("✨", modifier = Modifier.align(Alignment.TopEnd).offset(x = (-5).dp, y = 2.dp))
            Text("✨", modifier = Modifier.align(Alignment.BottomStart).offset(x = 5.dp, y = (-2).dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🎮", fontSize = 28.sp)
                    }
                }
                
                Spacer(Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Continue Playing",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        "Tap to start your adventure!",
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier.size(48.dp),
                    shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color(0xFF0091EA),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressStatCard(
    modifier: Modifier,
    emoji: String,
    value: String,
    label: String,
    color: Color
) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(color.copy(alpha = 0.6f), color.copy(alpha = 0.3f))
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(emoji, fontSize = 20.sp)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        label,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    value,
                    fontWeight = FontWeight.Black,
                    fontSize = 28.sp,
                    color = Color.White
                )
            }
        }
    }
}
