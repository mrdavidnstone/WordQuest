package com.spellinggames.app.ui.components.avatar

import com.spellinggames.app.data.model.Gender
import com.spellinggames.app.data.model.HeroConfiguration

object HeroAssetResolver {

    /**
     * Resolves a [HeroConfiguration] into ordered asset specs matching Section 14 render order:
     * 1. Body
     * 2. Bottom
     * 3. Top
     * 4. Face
     * 5. Eyes
     * 6. Hair_Back (if applicable)
     * 7. Hair_Front
     * 8. Hat
     */
    fun resolve(config: HeroConfiguration): List<AvatarLayerSpec> {
        val g = if (config.gender == Gender.GIRL) "girl" else "boy"
        val layers = mutableListOf<AvatarLayerSpec>()

        // 1. BODY
        layers.add(
            AvatarLayerSpec(
                layerType = AvatarLayerType.BODY,
                assetPath = "avatar/$g/body/body_${config.skinTone}.png",
                fallbackKey = "body_${config.skinTone}",
                tintHex = getSkinColorHex(config.skinTone)
            )
        )

        // 2. BOTTOM
        layers.add(
            AvatarLayerSpec(
                layerType = AvatarLayerType.BOTTOM,
                assetPath = "avatar/$g/bottoms/bottom_${config.bottomStyle}_${config.bottomColor}.png",
                fallbackKey = "bottom_${config.bottomStyle}_${config.bottomColor}",
                tintHex = getColorHex(config.bottomColor)
            )
        )

        // 3. TOP
        layers.add(
            AvatarLayerSpec(
                layerType = AvatarLayerType.TOP,
                assetPath = "avatar/$g/tops/top_${config.topStyle}_${config.topColor}.png",
                fallbackKey = "top_${config.topStyle}_${config.topColor}",
                tintHex = getColorHex(config.topColor)
            )
        )

        // 4. FACE
        layers.add(
            AvatarLayerSpec(
                layerType = AvatarLayerType.FACE,
                assetPath = "avatar/$g/face/face_${config.faceShape}.png",
                fallbackKey = "face_${config.faceShape}",
                tintHex = getSkinColorHex(config.skinTone)
            )
        )

        // 5. EYES
        layers.add(
            AvatarLayerSpec(
                layerType = AvatarLayerType.EYES,
                assetPath = "avatar/$g/eyes/eyes_${config.eyeStyle}_${config.eyeColor}.png",
                fallbackKey = "eyes_${config.eyeStyle}_${config.eyeColor}",
                tintHex = getColorHex(config.eyeColor)
            )
        )

        // 6. HAIR BACK (For long hair styles)
        if (config.hairStyle in listOf("long_wavy", "ponytail", "braids", "bun")) {
            layers.add(
                AvatarLayerSpec(
                    layerType = AvatarLayerType.HAIR_BACK,
                    assetPath = "avatar/$g/hair/hair_back_${config.hairStyle}_${config.hairColor}.png",
                    fallbackKey = "hair_back_${config.hairStyle}_${config.hairColor}",
                    tintHex = getColorHex(config.hairColor)
                )
            )
        }

        // 7. HAIR FRONT
        layers.add(
            AvatarLayerSpec(
                layerType = AvatarLayerType.HAIR_FRONT,
                assetPath = "avatar/$g/hair/hair_${config.hairStyle}_${config.hairColor}.png",
                fallbackKey = "hair_${config.hairStyle}_${config.hairColor}",
                tintHex = getColorHex(config.hairColor)
            )
        )

        // 8. HAT
        if (config.hat.isNotBlank() && config.hat != "none") {
            layers.add(
                AvatarLayerSpec(
                    layerType = AvatarLayerType.HAT,
                    assetPath = "avatar/$g/hats/hat_${config.hat}.png",
                    fallbackKey = "hat_${config.hat}",
                    tintHex = "#E53935"
                )
            )
        }

        return layers
    }

    private fun getSkinColorHex(skinTone: String): String {
        return when (skinTone) {
            "skin_02" -> "#FFE0B2"
            "skin_03" -> "#D7B594"
            "skin_04" -> "#E0AC69"
            "skin_05" -> "#8D6E63"
            else -> "#FFD180"
        }
    }

    private fun getColorHex(colorName: String): String {
        return when (colorName.lowercase()) {
            "red" -> "#E53935"
            "green" -> "#43A047"
            "gray" -> "#546E7A"
            "purple" -> "#7E57C2"
            "khaki" -> "#3E2723"
            "black" -> "#000000"
            "dark_brown" -> "#3E2723"
            "light_brown" -> "#6D4C41"
            "blonde" -> "#FBC02D"
            "auburn" -> "#E53935"
            "brown" -> "#6D4C41"
            else -> "#1E88E5"
        }
    }
}
