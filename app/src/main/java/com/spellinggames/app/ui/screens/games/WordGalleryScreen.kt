package com.spellinggames.app.ui.screens.games

import androidx.compose.runtime.Composable

@Composable
fun WordGalleryScreen(
    onBack: () -> Unit,
    onLaunchAttraction: (String) -> Unit = {}
) {
    WordGalleryInfoScreen(
        onBack = onBack,
        onLaunchAttraction = onLaunchAttraction
    )
}
