package com.spellinggames.app.ui.screens.games

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spellinggames.app.ui.components.ActiveListBadge
import com.spellinggames.app.ui.components.EmptyState
import com.spellinggames.app.ui.components.GameCard
import com.spellinggames.app.ui.theme.*

data class GameInfo(
    val id: String,
    val title: String,
    val emoji: String,
    val description: String,
    val color: Color,
    val category: String, // "Spelling" or "Vocabulary"
    val iconAsset: String? = null
)

val allGames = listOf(
    GameInfo("listen_spell", "Listen & Spell", "🎧", "Spell the word you hear", Color(0xFF00B0FF), "Spelling"),
    GameInfo("hangman", "Lab Meltdown", "🧪", "Prevent the lab meltdown!", Color(0xFF9333EA), "Spelling", iconAsset = "games/lab_meltdown/lab_meltdown_icon.png"),
    GameInfo("pirate_blaster", "Pirate Blaster", "🏴‍☠️", "Aim the cannon and blast the barrel letters!", Color(0xFFD97706), "Spelling", iconAsset = "games/word_gallery/pirate_word_blaster/background.png"),
    GameInfo("missing_letters", "Missing Letters", "🔤", "Fill in the blanks", Color(0xFF00E676), "Spelling"),
    GameInfo("word_gallery", "Word Gallery", "🖼️", "Restore the missing exhibit letters!", Color(0xFFEC4899), "Spelling", iconAsset = "games/word_gallery/word_gallery_icon.png"),
    GameInfo("scrambled", "Scrambled Word", "🔀", "Unscramble the letters", Color(0xFFFF6D00), "Spelling"),
    GameInfo("which_correct", "Which is Correct?", "✅", "Pick the right spelling", Color(0xFFD500F9), "Spelling"),
    GameInfo("challenge", "Ultimate Challenge", "🏆", "Master everything!", Color(0xFF1E88E5), "Vocabulary")
)

@Composable
fun GamesMenuScreen(
    onGameSelected: (String) -> Unit,
    viewModel: GamesMenuViewModel = hiltViewModel()
) {
    val activeListName by viewModel.activeListName.collectAsState()
    val allLists by viewModel.allLists.collectAsState()
    val hasWords by viewModel.hasWords.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF021024))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                "Games",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(16.dp))

            if (!hasWords) {
                EmptyState(
                    emoji = "🎮",
                    title = "No words to practice",
                    message = "Select or create a spelling lesson with words first, then come back to play!"
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    // Spelling Section
                    item {
                        SectionHeader("Spelling", Color(0xFF00B0FF))
                    }
                    items(allGames.filter { it.category == "Spelling" }) { game ->
                        GameCard(
                            title = game.title,
                            emoji = game.emoji,
                            description = game.description,
                            color = game.color,
                            iconAsset = game.iconAsset,
                            onClick = { onGameSelected(game.id) }
                        )
                    }

                    // Vocabulary Section
                    item {
                        Spacer(Modifier.height(16.dp))
                        SectionHeader("Vocabulary", Color(0xFFD500F9))
                    }
                    items(allGames.filter { it.category == "Vocabulary" }) { game ->
                        GameCard(
                            title = game.title,
                            emoji = game.emoji,
                            description = game.description,
                            color = game.color,
                            onClick = { onGameSelected(game.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 24.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Black,
            color = Color.White.copy(alpha = 0.9f),
            letterSpacing = 1.sp
        )
    }
}
