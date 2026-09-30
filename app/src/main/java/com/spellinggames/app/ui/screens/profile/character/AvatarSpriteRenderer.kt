package com.spellinggames.app.ui.screens.profile.character

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.spellinggames.app.ui.components.avatar.SpriteRepository

private fun loadLayerBitmap(
    context: Context,
    baseDir: String,
    fileName: String,
    gender: String,
    fallbackRow: Int = 0,
    fallbackCol: Int = 0
): ImageBitmap {
    val genderDir = gender.lowercase()
    val genderPath = "$baseDir/$genderDir/$fileName"

    // 1. Try loading from gender-specific subdirectory first
    try {
        context.assets.open(genderPath).use { stream ->
            val bitmap = BitmapFactory.decodeStream(stream)
            if (bitmap != null) {
                return bitmap.asImageBitmap()
            }
        }
    } catch (_: Exception) {}

    // 2. Fallback to base directory
    val basePath = "$baseDir/$fileName"
    return SpriteRepository.loadSpriteBitmap(context, basePath, fallbackRow, fallbackCol)
}

/**
 * Renders avatar with exact Z-scale layer ordering:
 * 1. Base body (sprites/base_body/...)
 * 2. Shoes (sprites/shoes/...) — Rendered before pants in Z-scale
 * 3. Bottom clothing / Pants (sprites/bottom/{color}/{style}.png) — Laid on top of shoes
 * 4. Top clothing (sprites/top/{color}/{style}.png)
 * 5. Hair (sprites/hair/...)
 * 6. Hats (sprites/hats/...)
 *
 * All layers use exact 285x335 canvas overlay alignment.
 * Supports debug layer hiding via layerVisibility map and game top overrides (e.g. Lab Coat).
 */
@Composable
fun AvatarSpriteRenderer(
    character: CharacterState,
    modifier: Modifier = Modifier,
    layerVisibility: Map<CharacterLayerType, Boolean> = emptyMap(),
    overrideTopPath: String? = null
) {
    val context = LocalContext.current

    val showBody = layerVisibility[CharacterLayerType.BODY] ?: true
    val showShoes = layerVisibility[CharacterLayerType.SHOES] ?: true
    val showBottom = layerVisibility[CharacterLayerType.BOTTOM] ?: true
    val showTop = layerVisibility[CharacterLayerType.TOP] ?: true
    val showHair = layerVisibility[CharacterLayerType.HAIR] ?: true
    val showHat = layerVisibility[CharacterLayerType.HAT] ?: true

    Box(
        modifier = modifier.aspectRatio(285f / 335f),
        contentAlignment = Alignment.Center
    ) {
        // 1. BASE BODY (Solid complete body: head, face, eyes, neck, torso, arms, hands, legs, feet)
        if (showBody) {
            val bodyFile = if (character.skin.endsWith(".png")) character.skin else "${character.skin}.png"
            val bodyBitmap = loadLayerBitmap(context, "sprites/base_body", bodyFile, character.gender, 0, 0)
            Image(
                bitmap = bodyBitmap,
                contentDescription = "Base Body",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        // 2. SHOES (Rendered before pants in Z-scale)
        if (showShoes) {
            val shoesBitmap = loadLayerBitmap(context, "sprites/shoes", "shoes_blue.png", character.gender, 6, 7)
            Image(
                bitmap = shoesBitmap,
                contentDescription = "Shoes Layer",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        // 3. BOTTOM CLOTHING / PANTS (Laid ON TOP of Shoes in Z-scale)
        if (showBottom) {
            val bottomFile = if (character.bottom.endsWith(".png")) character.bottom else "${character.bottom}.png"
            val bottomBitmap = loadLayerBitmap(context, "sprites/bottom/${character.bottomColor}", bottomFile, character.gender, 5, 9)
            Image(
                bitmap = bottomBitmap,
                contentDescription = "Bottom Layer (Pants over Shoes)",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        // 4. TOP CLOTHING (Supports game-specific top overrides e.g. Lab Coat)
        if (showTop) {
            val topBitmap = if (!overrideTopPath.isNullOrBlank()) {
                SpriteRepository.loadSpriteBitmap(context, overrideTopPath, 3, 3)
            } else {
                val cleanTopName = when {
                    character.top.contains("shirt", ignoreCase = true) -> "tshirt"
                    character.top.endsWith(".png") -> character.top.removeSuffix(".png")
                    else -> character.top
                }
                loadLayerBitmap(context, "sprites/top/${character.topColor}", "$cleanTopName.png", character.gender, 3, 3)
            }
            Image(
                bitmap = topBitmap,
                contentDescription = "Top Layer",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        // 5. HAIR (Pulls special hair from sprites/hair/hide_special_hair if equipped hat is from need_hair_overrides / show_special_hair)
        if (showHair) {
            val isShowSpecialHat = character.hat.contains("need_hair_overrides", ignoreCase = true) ||
                                  character.hat.contains("needs_special_hair", ignoreCase = true) ||
                                  character.hat.contains("show_special_hair", ignoreCase = true) ||
                                  character.hat.contains("wizard", ignoreCase = true)

            val hairFile = if (character.hair.endsWith(".png")) character.hair else "${character.hair}.png"
            val hairFileName = hairFile.substringAfterLast("/")

            val hairBitmap = if (isShowSpecialHat) {
                loadSpecialHideHairBitmap(context, hairFileName, character.gender)
            } else {
                loadHairLayerBitmap(context, hairFileName, character.gender, character.hairColor)
            }

            Image(
                bitmap = hairBitmap,
                contentDescription = "Hair Layer",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        // 6. HAT (if equipped)
        if (showHat && character.hat != "none" && character.hat.isNotBlank()) {
            val hatFile = if (character.hat.endsWith(".png")) character.hat else "${character.hat}.png"
            val isSpecialHatDir = character.hat.contains("need_hair_overrides", ignoreCase = true) ||
                                  character.hat.contains("needs_special_hair", ignoreCase = true) ||
                                  character.hat.contains("show_special_hair", ignoreCase = true)

            val hatBitmap = if (isSpecialHatDir) {
                SpriteRepository.loadSpriteBitmap(context, "sprites/hats/$hatFile", 2, 7)
            } else {
                loadLayerBitmap(context, "sprites/hats", hatFile, character.gender, 2, 7)
            }

            Image(
                bitmap = hatBitmap,
                contentDescription = "Hat Layer",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

private fun loadHairLayerBitmap(
    context: Context,
    fileName: String,
    gender: String,
    hairColor: String
): ImageBitmap {
    val g = gender.lowercase()
    val gs = "${g}s"
    val color = hairColor.lowercase()

    val pathsToTry = listOf(
        // Color + Gender specific paths
        "sprites/hair/$color/$g/$fileName",
        "sprites/hair/$color/$gs/$fileName",
        "sprites/hair/$color/both/$fileName",
        "sprites/hair/$color/$fileName",
        "sprites/hair/$g/$color/$fileName",
        "sprites/hair/both/$color/$fileName",
        // Gender specific paths
        "sprites/hair/both/$fileName",
        "sprites/hair/$g/$fileName",
        "sprites/hair/$gs/$fileName",
        "sprites/hair/$fileName"
    )

    for (path in pathsToTry) {
        try {
            context.assets.open(path).use { stream ->
                val bitmap = BitmapFactory.decodeStream(stream)
                if (bitmap != null) {
                    return bitmap.asImageBitmap()
                }
            }
        } catch (_: Exception) {}
    }

    return loadLayerBitmap(context, "sprites/hair", fileName, gender, 1, 2)
}

private fun loadSpecialHideHairBitmap(
    context: Context,
    hairFileName: String,
    gender: String
): ImageBitmap {
    val g = gender.lowercase()
    val pathsToTry = listOf(
        "sprites/hair/hide_special_hair/$g/$hairFileName",
        "sprites/hair/hide_special_hair/$hairFileName",
        "sprites/hair/$g/$hairFileName",
        "sprites/hair/$hairFileName",
        "sprites/hair/hide_special_hair/special_hair.png",
        "sprites/hair/hide_special_hair/hat_hair.png",
        "sprites/special/hat_hair_spiky_brown.png"
    )

    for (path in pathsToTry) {
        try {
            context.assets.open(path).use { stream ->
                val bitmap = BitmapFactory.decodeStream(stream)
                if (bitmap != null) {
                    return bitmap.asImageBitmap()
                }
            }
        } catch (_: Exception) {}
    }

    // Fall back to standard hair
    return loadLayerBitmap(context, "sprites/hair", hairFileName, gender, 1, 2)
}
