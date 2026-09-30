package com.spellinggames.app.ui.screens.profile.character

import com.spellinggames.app.data.model.Gender
import com.spellinggames.app.data.model.HeroConfiguration
import com.spellinggames.app.ui.screens.profile.Avatar

data class CharacterState(
    val gender: String = "Boy",
    val skin: String = "body_01",
    val face: String = "round",
    val eyes: String = "round_brown",
    val hair: String = "hat_hair_spiky_brown",
    val hairColor: String = "brown",
    val hat: String = "cap",
    val top: String = "hoodie",
    val topColor: String = "gray",
    val bottom: String = "cargo_pants",
    val bottomColor: String = "khaki",
    val shoes: String = "shoes_blue"
) {
    fun toHeroConfiguration(): HeroConfiguration {
        return HeroConfiguration(
            gender = if (gender.equals("Girl", ignoreCase = true)) Gender.GIRL else Gender.BOY,
            skinTone = if (skin.contains("02")) "skin_02" else "skin_01",
            faceShape = "round",
            eyeStyle = "round",
            eyeColor = "brown",
            hairStyle = hair,
            hairColor = hairColor,
            hat = hat,
            topStyle = top,
            topColor = topColor,
            bottomStyle = bottom,
            bottomColor = bottomColor
        )
    }

    companion object {
        val POC_DEFAULT = CharacterState(
            gender = "Boy",
            skin = "body_01",
            face = "round",
            eyes = "round_brown",
            hair = "hat_hair_spiky_brown",
            hairColor = "brown",
            hat = "cap",
            top = "hoodie",
            topColor = "gray",
            bottom = "cargo_pants",
            bottomColor = "khaki",
            shoes = "shoes_blue"
        )

        fun fromAvatar(avatar: Avatar): CharacterState {
            val hatClean = when {
                avatar.hatStyle.lowercase().contains("cap") || avatar.hatStyle.lowercase().contains("baseball") -> "cap"
                avatar.hatStyle.lowercase().contains("none") -> "none"
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
                else -> "gray"
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
                else -> "gray"
            }

            return CharacterState(
                gender = if (avatar.gender.equals("Girl", ignoreCase = true)) "Girl" else "Boy",
                skin = if (avatar.skinColor.contains("02")) "body_02" else "body_01",
                hair = if (avatar.hairStyle.isBlank()) "hat_hair_spiky_brown" else avatar.hairStyle,
                hairColor = "brown",
                hat = if (hatClean.isBlank()) "cap" else hatClean,
                top = if (topClean.isBlank()) "hoodie" else topClean,
                topColor = topColorClean,
                bottom = if (bottomClean.isBlank()) "cargo_pants" else bottomClean,
                bottomColor = bottomColorClean,
                shoes = "shoes_blue"
            )
        }
    }
}
