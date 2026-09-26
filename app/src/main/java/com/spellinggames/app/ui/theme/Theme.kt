package com.spellinggames.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Child-friendly bright palette
val SkyBlue = Color(0xFF4FC3F7)
val SoftPurple = Color(0xFFCE93D8)
val SunnyYellow = Color(0xFFFFF176)
val FreshGreen = Color(0xFF81C784)
val Coral = Color(0xFFFF8A65)
val SoftPink = Color(0xFFF48FB1)
val DeepBlue = Color(0xFF0288D1)
val WarmOrange = Color(0xFFFFB74D)

private val LightColorScheme = lightColorScheme(
    primary = DeepBlue,
    onPrimary = Color.White,
    primaryContainer = SkyBlue,
    onPrimaryContainer = Color(0xFF01579B),
    secondary = SoftPurple,
    onSecondary = Color.White,
    tertiary = FreshGreen,
    onTertiary = Color.White,
    background = Color(0xFFF5F9FC),
    surface = Color.White,
    onBackground = Color(0xFF1A237E),
    onSurface = Color(0xFF1A237E),
    error = Color(0xFFE57373),
    errorContainer = Color(0xFFFFCDD2)
)

private val DarkColorScheme = darkColorScheme(
    primary = SkyBlue,
    onPrimary = Color(0xFF01579B),
    primaryContainer = DeepBlue,
    secondary = SoftPurple,
    tertiary = FreshGreen,
    background = Color(0xFF0D1B2A),
    surface = Color(0xFF1B263B),
    onBackground = Color(0xFFE0E7FF),
    onSurface = Color(0xFFE0E7FF)
)

@Composable
fun SpellingGamesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
