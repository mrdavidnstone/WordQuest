package com.spellinggames.app.ui.screens.profile.character

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * CharacterRenderer for Build Your Hero Portal.
 * Renders hero using 285x335 layer overlay system.
 */
@Composable
fun CharacterRenderer(
    character: CharacterState,
    modifier: Modifier = Modifier,
    debugMode: Boolean = false,
    layerVisibility: Map<CharacterLayerType, Boolean> = emptyMap()
) {
    Box(
        modifier = modifier
            .then(
                if (debugMode) Modifier.border(2.dp, Color(0xFF00B0FF), RoundedCornerShape(12.dp))
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        AvatarSpriteRenderer(
            character = character,
            modifier = Modifier.fillMaxSize(),
            layerVisibility = layerVisibility
        )

        // Debug Canvas Overlay showing 285x335 master alignment grid
        if (debugMode) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
                drawRect(Color(0xFF00B0FF), style = stroke)
            }
            Text(
                "285 × 335 MASTER CANVAS (ALIGNMENT PROOF)",
                fontSize = 10.sp,
                color = Color(0xFF00B0FF),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 4.dp)
            )
        }
    }
}
