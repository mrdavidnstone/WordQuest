package com.spellarcade.app

import androidx.compose.ui.window.ComposeUIViewController
import com.spellarcade.app.ui.navigation.IosAppNavHost
import com.spellarcade.app.ui.theme.SpellArcadeTheme

fun MainViewController() = ComposeUIViewController {
    SpellArcadeTheme {
        IosAppNavHost()
    }
}
