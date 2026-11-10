package com.spellarade.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.spellarade.core.designsystem.theme.SpellArcadeTheme

@Composable
fun App() {
    SpellArcadeTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            // Navigation host will be placed here in Phase 2
            Greeting("SpellArcade")
        }
    }
}

@Composable
fun Greeting(name: String) {
    androidx.compose.material3.Text(text = "Welcome to $name")
}
