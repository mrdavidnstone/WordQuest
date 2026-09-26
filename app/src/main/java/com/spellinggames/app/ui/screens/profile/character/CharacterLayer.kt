package com.spellinggames.app.ui.screens.profile.character

enum class CharacterLayerType {
    BODY,
    SHOES,
    BOTTOM,
    TOP,
    HAIR,
    HAT
}

data class CharacterLayerSpec(
    val type: CharacterLayerType,
    val name: String,
    val isVisible: Boolean = true
)
