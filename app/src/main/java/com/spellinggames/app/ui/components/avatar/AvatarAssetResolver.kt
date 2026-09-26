package com.spellinggames.app.ui.components.avatar

import com.spellinggames.app.data.model.AvatarConfig

enum class AvatarLayerType {
    HAIR_BACK,
    BODY,
    BOTTOM,
    TOP,
    FACE,
    EYES,
    HAIR_FRONT,
    HAT
}

data class AvatarLayerSpec(
    val layerType: AvatarLayerType,
    val assetPath: String,
    val fallbackKey: String,
    val tintHex: String? = null
)

object AvatarAssetResolver {

    /**
     * Resolves an AvatarConfig into a list of AvatarLayerSpecs ordered according
     * to the 2D Modular Hero Sprite System specification:
     * 1. Hair Back (if present)
     * 2. Body / Skin
     * 3. Bottom
     * 4. Top
     * 5. Face
     * 6. Eyes
     * 7. Hair (Front)
     * 8. Hat
     */
    fun resolveLayers(config: AvatarConfig): List<AvatarLayerSpec> {
        val g = config.gender.lowercase()
        val layers = mutableListOf<AvatarLayerSpec>()

        // 1. HAIR BACK (For long hair styles like long_wavy, ponytail, braids)
        if (config.hairStyle in listOf("long_wavy", "ponytail", "braids")) {
            layers.add(
                AvatarLayerSpec(
                    layerType = AvatarLayerType.HAIR_BACK,
                    assetPath = "avatar/$g/hair/hair_back_${config.hairStyle}_${config.hairColor}.png",
                    fallbackKey = "hair_back_${config.hairStyle}",
                    tintHex = config.hairColorHex
                )
            )
        }

        // 2. BODY / SKIN
        val skinFile = if (config.skinTone.isNotBlank()) config.skinTone else "skin_01"
        layers.add(
            AvatarLayerSpec(
                layerType = AvatarLayerType.BODY,
                assetPath = "avatar/$g/body/body_${g}_${skinFile}.png",
                fallbackKey = "body_$skinFile",
                tintHex = config.skinColorHex
            )
        )

        // 3. BOTTOM
        val bottomName = if (config.bottom.isNotBlank()) config.bottom else "jeans"
        val bottomColor = if (config.bottomColor.isNotBlank()) config.bottomColor else "blue"
        layers.add(
            AvatarLayerSpec(
                layerType = AvatarLayerType.BOTTOM,
                assetPath = "avatar/$g/bottoms/bottom_${bottomName}_${bottomColor}.png",
                fallbackKey = "bottom_${bottomName}_${bottomColor}",
                tintHex = config.bottomColorHex
            )
        )

        // 4. TOP
        val topName = if (config.top.isNotBlank()) config.top else "hoodie"
        val topColor = if (config.topColor.isNotBlank()) config.topColor else "blue"
        layers.add(
            AvatarLayerSpec(
                layerType = AvatarLayerType.TOP,
                assetPath = "avatar/$g/tops/top_${topName}_${topColor}.png",
                fallbackKey = "top_${topName}_${topColor}",
                tintHex = config.topColorHex
            )
        )

        // 5. FACE
        val faceShape = if (config.faceShape.isNotBlank()) config.faceShape else "round"
        layers.add(
            AvatarLayerSpec(
                layerType = AvatarLayerType.FACE,
                assetPath = "avatar/$g/face/face_${faceShape}.png",
                fallbackKey = "face_${faceShape}",
                tintHex = config.skinColorHex
            )
        )

        // 6. EYES
        val eyeStyle = if (config.eyeStyle.isNotBlank()) config.eyeStyle else "round"
        val eyeColor = if (config.eyeColor.isNotBlank()) config.eyeColor else "brown"
        layers.add(
            AvatarLayerSpec(
                layerType = AvatarLayerType.EYES,
                assetPath = "avatar/$g/eyes/eyes_${eyeStyle}_${eyeColor}.png",
                fallbackKey = "eyes_${eyeStyle}_${eyeColor}",
                tintHex = config.eyeColorHex
            )
        )

        // 7. HAIR FRONT
        val hairStyle = if (config.hairStyle.isNotBlank()) config.hairStyle else "spiky"
        val hairColor = if (config.hairColor.isNotBlank()) config.hairColor else "dark_brown"
        layers.add(
            AvatarLayerSpec(
                layerType = AvatarLayerType.HAIR_FRONT,
                assetPath = "avatar/$g/hair/hair_${hairStyle}_${hairColor}.png",
                fallbackKey = "hair_${hairStyle}_${hairColor}",
                tintHex = config.hairColorHex
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
}
