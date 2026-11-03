package com.spellarcade.app.ui.screens.games

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

data class GalleryAttraction(
    val id: String,
    val title: String,
    val emoji: String,
    val color: Color,
    val isPlayable: Boolean,
    val statusLabel: String,
    val previewBg: List<Color>
)

val galleryAttractions = listOf(
    GalleryAttraction(
        id = "carnival_pop",
        title = "Carnival Pop & Spell",
        emoji = "🎈",
        color = Color(0xFFEC4899),
        isPlayable = false,
        statusLabel = "COMING SOON",
        previewBg = listOf(Color(0xFF31103F), Color(0xFF831843), Color(0xFF9D174D))
    ),
    GalleryAttraction(
        id = "western_shootout",
        title = "Western Shootout",
        emoji = "🤠",
        color = Color(0xFF8B5CF6),
        isPlayable = false,
        statusLabel = "LOCKED",
        previewBg = listOf(Color(0xFF2E1065), Color(0xFF5B21B6), Color(0xFF4C1D95))
    ),
    GalleryAttraction(
        id = "space_ray",
        title = "Space Ray Gun",
        emoji = "🚀",
        color = Color(0xFF00B0FF),
        isPlayable = false,
        statusLabel = "LOCKED",
        previewBg = listOf(Color(0xFF0B192C), Color(0xFF1E3E62), Color(0xFF000000))
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordGalleryInfoScreen(
    onBack: () -> Unit,
    onLaunchAttraction: (String) -> Unit,
    viewModel: WordGalleryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var selectedAttractionId by remember { mutableStateOf("carnival_pop") }
    var listDropdownExpanded by remember { mutableStateOf(false) }

    val selectedAttraction = galleryAttractions.find { it.id == selectedAttractionId } ?: galleryAttractions[0]

    // Enforce Landscape Orientation for Word Gallery Selection Screen
    DisposableEffect(Unit) {
        val activity = context as? Activity
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF2E1065))
                )
            )
            .padding(6.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // TOP HEADER: Back Button + Word Gallery Banner + Spelling List Selector Dropdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        (context as? Activity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        onBack()
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                }

                Text(
                    "WORD GALLERY",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFF472B6),
                    letterSpacing = 1.5.sp
                )

                // SPELLING LIST SELECTOR DROPDOWN (Applies to all Word Gallery games)
                Box {
                    Surface(
                        onClick = { listDropdownExpanded = true },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.5.dp, Color(0xFFF472B6))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "📜 ${uiState.listName ?: "Select List"}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowDropDown, "List Menu", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }

                    DropdownMenu(
                        expanded = listDropdownExpanded,
                        onDismissRequest = { listDropdownExpanded = false },
                        modifier = Modifier.background(Color(0xFF0F172A))
                    ) {
                        uiState.allLists.forEach { spellingList ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        spellingList.name,
                                        color = if (spellingList.id == uiState.activeListId) Color(0xFFF472B6) else Color.White,
                                        fontWeight = if (spellingList.id == uiState.activeListId) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                },
                                onClick = {
                                    viewModel.switchList(spellingList.id)
                                    listDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // CENTER: LARGE FEATURED GAME SCREENSHOT / PREVIEW CARD
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.verticalGradient(selectedAttraction.previewBg))
                    .border(2.dp, selectedAttraction.color, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Mockup Game Screen Elements inside the Featured Preview
                when (selectedAttraction.id) {
                    "pirate_blaster" -> {
                        // Pirate Cove Mockup Preview
                        Column(
                            modifier = Modifier.fillMaxSize().padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("🏴‍☠️ PIRATE BLASTER", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFFF59E0B))
                                Text("SCORE: 0/10", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF78350F), border = BorderStroke(1.dp, Color(0xFFF59E0B))) {
                                    Text("🛢️ A", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF78350F), border = BorderStroke(1.dp, Color(0xFFF59E0B))) {
                                    Text("🛢️ E", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF78350F), border = BorderStroke(1.dp, Color(0xFFF59E0B))) {
                                    Text("🛢️ U", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF78350F), border = BorderStroke(1.dp, Color(0xFFF59E0B))) {
                                    Text("🛢️ T", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            }

                            // Large Action Button Overlay
                            Button(
                                onClick = { onLaunchAttraction("pirate_blaster") },
                                modifier = Modifier
                                    .fillMaxWidth(0.45f)
                                    .height(36.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("PLAY PIRATE BLASTER NOW", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White)
                                }
                            }
                        }
                    }
                    "carnival_pop" -> {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("🎈 CARNIVAL POP & SPELL 🎈", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFFEC4899))
                            Spacer(Modifier.height(8.dp))
                            Text("Pop balloon letter targets in the carnival booth!", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                            Spacer(Modifier.height(16.dp))
                            Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = 0.2f)) {
                                Text("COMING SOON", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
                            }
                        }
                    }
                    else -> {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(36.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(selectedAttraction.title, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
                            Spacer(Modifier.height(4.dp))
                            Text("Attraction Locked", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // BOTTOM: MINIATURE SCREENSHOTS / THUMBNAILS STRIP
            Text(
                "SWIPE TO SELECT GAME",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = Color.White.copy(alpha = 0.6f),
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
            Spacer(Modifier.height(4.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 4.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                items(galleryAttractions, key = { it.id }) { attraction ->
                    val isSelected = attraction.id == selectedAttractionId
                    Surface(
                        modifier = Modifier
                            .width(130.dp)
                            .fillMaxHeight()
                            .clickable {
                                selectedAttractionId = attraction.id
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = attraction.color.copy(alpha = if (isSelected) 0.4f else 0.15f),
                        border = BorderStroke(if (isSelected) 2.5.dp else 1.dp, if (isSelected) attraction.color else Color.White.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(attraction.emoji, fontSize = 20.sp)
                            Column {
                                Text(
                                    attraction.title,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1
                                )
                                Text(
                                    attraction.statusLabel,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (attraction.isPlayable) Color(0xFF22C55E) else Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
