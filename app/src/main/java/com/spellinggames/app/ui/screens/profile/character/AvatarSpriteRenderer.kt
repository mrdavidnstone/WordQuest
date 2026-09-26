package com.spellinggames.app.ui.screens.profile.character

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.spellinggames.app.ui.components.avatar.SpriteRepository

/**
 * Renders avatar with exact Z-scale layer ordering:
 * 1. Base body (sprites/base_body/...)
 * 2. Shoes (sprites/shoes/...) — Rendered before pants in Z-scale
 * 3. Bottom clothing / Pants (sprites/bottom/{color}/{style}.png) — Laid on top of shoes
 * 4. Top clothing (sprites/top/{color}/{style}.png)
 * 5. Hair / Hat Hair (sprites/special/hat_hair_spiky_brown.png or sprites/hair/...)
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
            val bodyPath = "sprites/base_body/$bodyFile"
            val bodyBitmap = SpriteRepository.loadSpriteBitmap(context, bodyPath, 0, 0)
            Image(
                bitmap = bodyBitmap,
                contentDescription = "Base Body",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        // 2. SHOES (Rendered before pants in Z-scale)
        if (showShoes) {
            val shoesPath = "sprites/shoes/shoes_blue.png"
            val shoesBitmap = SpriteRepository.loadSpriteBitmap(context, shoesPath, 6, 7)
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
            val bottomPath = "sprites/bottom/${character.bottomColor}/$bottomFile"
            val bottomBitmap = SpriteRepository.loadSpriteBitmap(context, bottomPath, 5, 9)
            Image(
                bitmap = bottomBitmap,
                contentDescription = "Bottom Layer (Pants over Shoes)",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        // 4. TOP CLOTHING (Supports game-specific top overrides e.g. Lab Coat)
        if (showTop) {
            val topPath = if (!overrideTopPath.isNullOrBlank()) {
                overrideTopPath
            } else {
                val topFile = if (character.top.endsWith(".png")) character.top else "${character.top}.png"
                "sprites/top/${character.topColor}/$topFile"
            }
            val topBitmap = SpriteRepository.loadSpriteBitmap(context, topPath, 3, 3)
            Image(
                bitmap = topBitmap,
                contentDescription = "Top Layer",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        // 5. HAIR (If hat equipped, default to special/hat_hair_spiky_brown.png)
        if (showHair) {
            val hairPath = if (character.hat != "none" && character.hat.isNotBlank()) {
                "sprites/special/hat_hair_spiky_brown.png"
            } else {
                val hairFile = if (character.hair.endsWith(".png")) character.hair else "${character.hair}.png"
                "sprites/hair/$hairFile"
            }
            val hairBitmap = SpriteRepository.loadSpriteBitmap(context, hairPath, 1, 2)
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
            val hatPath = "sprites/hats/$hatFile"
            val hatBitmap = SpriteRepository.loadSpriteBitmap(context, hatPath, 2, 7)
            Image(
                bitmap = hatBitmap,
                contentDescription = "Hat Layer",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}
