package com.spellarcade.app.data.model

import com.spellarcade.app.ui.screens.profile.Avatar

/**
 * Data-driven configuration for the 2D Modular Hero Sprite System.
 * Preserves exact sprite file IDs for pixel-perfect layer rendering across screens.
 */
data class AvatarConfig(
    val gender: String = "boy",
    val skinTone: String = "body_01",
    val skinColorHex: String = "#FFD180",
    val faceShape: String = "round",
    val eyeStyle: String = "round",
    val eyeColor: String = "brown",
    val eyeColorHex: String = "#6D4C41",
    val hairStyle: String = "hat_hair_spiky_brown",
    val hairColor: String = "brown",
    val hairColorHex: String = "#3E2723",
    val hat: String = "cap",
    val top: String = "hoodie",
    val topColor: String = "gray",
    val topColorHex: String = "#546E7A",
    val bottom: String = "cargo_pants",
    val bottomColor: String = "khaki",
    val bottomColorHex: String = "#3E2723"
) {
    companion object {
        fun fromAvatar(avatar: Avatar): AvatarConfig {
            val hatClean = when {
                avatar.hatStyle.isBlank() || avatar.hatStyle.lowercase() == "none" -> "none"
                avatar.hatStyle.lowercase().contains("cap") || avatar.hatStyle.lowercase().contains("baseball") -> "cap"
                else -> avatar.hatStyle.lowercase()
            }
            val topClean = when {
                avatar.outfit.lowercase().contains("hoodie") -> "hoodie"
                avatar.outfit.lowercase().contains("shirt") || avatar.outfit.lowercase().contains("tshirt") -> "tshirt"
                avatar.outfit.lowercase().contains("jacket") -> "jacket"
                avatar.outfit.lowercase().contains("long") -> "long_sleeve"
                avatar.outfit.lowercase().contains("sweater") -> "sweater"
                else -> avatar.outfit.lowercase()
            }
            val topColorClean = when {
                avatar.outfitColor.contains("red", ignoreCase = true) || avatar.outfitColor.equals("#E53935", true) -> "red"
                avatar.outfitColor.contains("blue", ignoreCase = true) || avatar.outfitColor.equals("#1E88E5", true) -> "blue"
                avatar.outfitColor.contains("green", ignoreCase = true) || avatar.outfitColor.equals("#43A047", true) -> "green"
                avatar.outfitColor.contains("purple", ignoreCase = true) || avatar.outfitColor.equals("#7E57C2", true) -> "purple"
                avatar.outfitColor.contains("gray", ignoreCase = true) || avatar.outfitColor.equals("#546E7A", true) -> "gray"
                else -> avatar.outfitColor.lowercase()
            }
            val bottomClean = when {
                avatar.bottomStyle.lowercase().contains("cargo") -> "cargo_pants"
                avatar.bottomStyle.lowercase().contains("jeans") -> "jeans"
                avatar.bottomStyle.lowercase().contains("shorts") -> "shorts"
                avatar.bottomStyle.lowercase().contains("joggers") -> "joggers"
                else -> avatar.bottomStyle.lowercase()
            }
            val bottomColorClean = when {
                avatar.bottomColor.contains("khaki", ignoreCase = true) || avatar.bottomColor.equals("#3E2723", true) || avatar.bottomColor.equals("#EF6C00", true) -> "khaki"
                avatar.bottomColor.contains("blue", ignoreCase = true) || avatar.bottomColor.equals("#1A237E", true) -> "blue"
                avatar.bottomColor.contains("green", ignoreCase = true) || avatar.bottomColor.equals("#2E7D32", true) -> "green"
                avatar.bottomColor.contains("purple", ignoreCase = true) || avatar.bottomColor.equals("#4527A0", true) -> "purple"
                avatar.bottomColor.contains("gray", ignoreCase = true) || avatar.bottomColor.equals("#37474F", true) -> "gray"
                else -> avatar.bottomColor.lowercase()
            }

            return AvatarConfig(
                gender = if (avatar.gender.contains("Girl", ignoreCase = true)) "girl" else "boy",
                skinTone = if (avatar.skinColor.isBlank()) "body_01" else avatar.skinColor,
                skinColorHex = avatar.skinColor,
                faceShape = "round",
                eyeStyle = "round",
                eyeColor = "brown",
                eyeColorHex = avatar.eyeColor,
                hairStyle = if (avatar.hairStyle.isBlank()) "hat_hair_spiky_brown" else avatar.hairStyle,
                hairColor = "brown",
                hairColorHex = avatar.hairColor,
                hat = hatClean,
                top = topClean,
                topColor = topColorClean,
                topColorHex = avatar.outfitColor,
                bottom = bottomClean,
                bottomColor = bottomColorClean,
                bottomColorHex = avatar.bottomColor
            )
        }
    }
}
