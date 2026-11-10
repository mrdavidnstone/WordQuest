package com.spellarcade.app.data.model

import com.spellarcade.app.ui.screens.profile.Avatar

enum class Gender {
    BOY,
    GIRL
}

data class HeroConfiguration(
    val gender: Gender = Gender.BOY,
    val skinTone: String = "body_01",
    val faceShape: String = "round",
    val eyeStyle: String = "round",
    val eyeColor: String = "brown",
    val hairStyle: String = "hat_hair_spiky_brown",
    val hairColor: String = "brown",
    val hat: String = "cap",
    val topStyle: String = "hoodie",
    val topColor: String = "gray",
    val bottomStyle: String = "cargo_pants",
    val bottomColor: String = "khaki"
) {
    fun toAvatar(): Avatar {
        return Avatar(
            gender = if (gender == Gender.GIRL) "Girl" else "Boy",
            skinColor = skinTone,
            faceShape = faceShape,
            eyeStyle = eyeStyle,
            eyeColor = eyeColor,
            eyebrowsStyle = "Natural",
            noseStyle = "Small",
            mouthStyle = "Smile",
            hairStyle = hairStyle,
            hairColor = hairColor,
            outfit = topStyle,
            outfitColor = topColor,
            bottomStyle = bottomStyle,
            bottomColor = bottomColor,
            hatStyle = hat,
            shoeStyle = "shoes_blue",
            shoeColor = "#3E2723",
            accessory = "None"
        )
    }

    companion object {
        val BOY_PROTOTYPE = HeroConfiguration(
            gender = Gender.BOY,
            skinTone = "body_01",
            faceShape = "round",
            eyeStyle = "round",
            eyeColor = "brown",
            hairStyle = "hat_hair_spiky_brown",
            hairColor = "brown",
            hat = "cap",
            topStyle = "hoodie",
            topColor = "gray",
            bottomStyle = "cargo_pants",
            bottomColor = "khaki"
        )

        val GIRL_PROTOTYPE = HeroConfiguration(
            gender = Gender.GIRL,
            skinTone = "body_02",
            faceShape = "oval",
            eyeStyle = "almond",
            eyeColor = "green",
            hairStyle = "long_wavy",
            hairColor = "auburn",
            hat = "none",
            topStyle = "hoodie",
            topColor = "purple",
            bottomStyle = "jeans",
            bottomColor = "blue"
        )

        fun fromAvatar(avatar: Avatar): HeroConfiguration {
            return HeroConfiguration(
                gender = if (avatar.gender.equals("Girl", ignoreCase = true)) Gender.GIRL else Gender.BOY,
                skinTone = if (avatar.skinColor.isBlank()) "body_01" else avatar.skinColor,
                faceShape = "round",
                eyeStyle = "round",
                eyeColor = "brown",
                hairStyle = if (avatar.hairStyle.isBlank()) "hat_hair_spiky_brown" else avatar.hairStyle,
                hairColor = "brown",
                hat = if (avatar.hatStyle.isBlank()) "cap" else avatar.hatStyle,
                topStyle = if (avatar.outfit.isBlank()) "hoodie" else avatar.outfit,
                topColor = if (avatar.outfitColor.isBlank()) "gray" else avatar.outfitColor,
                bottomStyle = if (avatar.bottomStyle.isBlank()) "cargo_pants" else avatar.bottomStyle,
                bottomColor = if (avatar.bottomColor.isBlank()) "khaki" else avatar.bottomColor
            )
        }
    }
}
