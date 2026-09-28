package com.spellinggames.app.data.model

object HeroConfigurationValidator {

    val validSkins = listOf("skin_01", "skin_02", "skin_03", "skin_04", "skin_05")
    val validFaces = listOf("round", "oval", "soft_square")
    val validEyeStyles = listOf("round", "almond", "narrow")
    val validEyeColors = listOf("brown", "blue", "green", "gray")

    val validHairBoy = listOf("spiky", "side_part", "curly", "undercut")
    val validHairGirl = listOf("long_wavy", "ponytail", "braids", "bun")
    val validHairColors = listOf("black", "dark_brown", "light_brown", "blonde", "auburn", "purple")

    val validHats = listOf("none", "cap", "baseball", "wizard", "crown", "cowboy", "explorer")

    val validTops = listOf("hoodie", "tshirt", "t_shirt", "shirt", "jacket", "longsleeve", "long_sleeve", "sweater")
    val validTopColors = listOf("blue", "red", "green", "gray", "purple")

    val validBottoms = listOf("jeans", "cargopants", "cargo_pants", "shorts", "joggers")
    val validBottomColors = listOf("blue", "khaki", "green", "gray", "purple")

    fun validate(config: HeroConfiguration): HeroConfiguration {
        val validSkin = if (config.skinTone in validSkins) config.skinTone else "skin_01"
        val validFace = if (config.faceShape in validFaces) config.faceShape else "round"
        val validEyeStyle = if (config.eyeStyle in validEyeStyles) config.eyeStyle else "round"
        val validEyeColor = if (config.eyeColor in validEyeColors) config.eyeColor else "brown"

        val allowedHair = if (config.gender == Gender.GIRL) validHairGirl else validHairBoy
        val validHairStyle = if (config.hairStyle in allowedHair || config.hairStyle.isNotBlank()) config.hairStyle else allowedHair.first()
        val validHairColor = if (config.hairColor in validHairColors) config.hairColor else "dark_brown"

        val validHat = if (config.hat in validHats || config.hat.isNotBlank()) config.hat else "none"

        val normalizedTop = when {
            config.topStyle.contains("shirt", ignoreCase = true) -> "tshirt"
            config.topStyle.contains("hoodie", ignoreCase = true) -> "hoodie"
            config.topStyle.contains("jacket", ignoreCase = true) -> "jacket"
            config.topStyle.contains("long", ignoreCase = true) -> "longsleeve"
            config.topStyle.contains("sweater", ignoreCase = true) -> "sweater"
            config.topStyle in validTops -> config.topStyle
            else -> if (config.topStyle.isNotBlank()) config.topStyle else "hoodie"
        }
        val validTopColor = if (config.topColor in validTopColors) config.topColor else "gray"

        val normalizedBottom = when {
            config.bottomStyle.contains("cargo", ignoreCase = true) -> "cargo_pants"
            config.bottomStyle.contains("jeans", ignoreCase = true) -> "jeans"
            config.bottomStyle.contains("shorts", ignoreCase = true) -> "shorts"
            config.bottomStyle.contains("joggers", ignoreCase = true) -> "joggers"
            config.bottomStyle in validBottoms -> config.bottomStyle
            else -> if (config.bottomStyle.isNotBlank()) config.bottomStyle else "cargo_pants"
        }
        val validBottomColor = if (config.bottomColor in validBottomColors) config.bottomColor else "khaki"

        return config.copy(
            skinTone = validSkin,
            faceShape = validFace,
            eyeStyle = validEyeStyle,
            eyeColor = validEyeColor,
            hairStyle = validHairStyle,
            hairColor = validHairColor,
            hat = validHat,
            topStyle = normalizedTop,
            topColor = validTopColor,
            bottomStyle = normalizedBottom,
            bottomColor = validBottomColor
        )
    }

    fun getRandomConfiguration(gender: Gender = Gender.BOY): HeroConfiguration {
        val allowedHair = if (gender == Gender.GIRL) validHairGirl else validHairBoy

        return HeroConfiguration(
            gender = gender,
            skinTone = validSkins.random(),
            faceShape = validFaces.random(),
            eyeStyle = validEyeStyles.random(),
            eyeColor = validEyeColors.random(),
            hairStyle = allowedHair.random(),
            hairColor = validHairColors.random(),
            hat = validHats.random(),
            topStyle = validTops.random(),
            topColor = validTopColors.random(),
            bottomStyle = validBottoms.random(),
            bottomColor = validBottomColors.random()
        )
    }
}
