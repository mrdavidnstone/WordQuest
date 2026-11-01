package com.spellarcade.app.ui.screens.progress

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
import com.spellarcade.app.ui.components.ActiveListBadge
import com.spellarcade.app.ui.components.EmptyState
import com.spellarcade.app.ui.theme.*

@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF021024))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                "Progress",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(16.dp))
            ActiveListBadge(
                listName = uiState.listName,
                allLists = uiState.allLists,
                onListSelected = { viewModel.selectList(it) }
            )
            Spacer(Modifier.height(24.dp))

            if (uiState.listName == null) {
                EmptyState(
                    emoji = "📊",
                    title = "No active lesson",
                    message = "Select a spelling lesson to see progress."
                )
            } else {
                // Summary cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SummaryChip(Modifier.weight(1f), "⭐", "${uiState.mastered}", "Mastered", Color(0xFF00796B))
                    SummaryChip(Modifier.weight(1f), "🔥", "${uiState.learning}", "Learning", Color(0xFFBF360C))
                    SummaryChip(Modifier.weight(1f), "💪", "${uiState.needsPractice}", "Practice", Color(0xFF4A148C))
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    "Words",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(Modifier.height(12.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(uiState.wordStats) { stat ->
                        WordStatRow(stat)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryChip(
    modifier: Modifier,
    emoji: String,
    value: String,
    label: String,
    bgColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 24.sp)
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color.White)
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun WordStatRow(stat: WordStat) {
    val statusColor = when (stat.mastery) {
        "Mastered" -> FreshGreen
        "Practicing" -> SkyBlue
        "Learning" -> WarmOrange
        else -> Coral
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stat.word, fontWeight = FontWeight.Bold, color = Color.White)
                Text(
                    "${stat.accuracy}% • ${stat.correct}/${stat.total} correct",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = statusColor.copy(alpha = 0.15f)
            ) {
                Text(
                    stat.mastery,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }
        }
    }
}
