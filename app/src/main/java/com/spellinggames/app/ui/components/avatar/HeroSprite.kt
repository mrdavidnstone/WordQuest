package com.spellinggames.app.ui.components.avatar

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.spellinggames.app.data.model.AvatarConfig
import com.spellinggames.app.ui.screens.profile.character.AvatarSpriteRenderer
import com.spellinggames.app.ui.screens.profile.character.CharacterState

enum class HeroAnimationState {
    IDLE,
    WALK,
    RUN,
    JUMP,
    VICTORY,
    DEFEAT,
    THINKING,
    CORRECT_ANSWER,
    WRONG_ANSWER,
    WAVE
}

/**
 * 2D Modular Hero Sprite System Renderer for Home Page, Games & Preview Overlays.
 * Renders the hero using the exact 285x335 layer overlay system.
 */
@Composable
fun HeroSprite(
    config: AvatarConfig,
    modifier: Modifier = Modifier,
    animationState: HeroAnimationState = HeroAnimationState.IDLE,
    sway: Boolean = true,
    isJumping: Boolean = false,
    overrideTopPath: String? = null
) {
    val characterState = remember(config) {
        CharacterState(
            skin = if (config.skinTone.contains("02")) "body_02" else "body_01",
            hair = if (config.hairStyle.isBlank()) "hat_hair_spiky_brown" else config.hairStyle,
            hairColor = config.hairColor,
            top = if (config.top.isBlank()) "hoodie" else config.top,
            topColor = if (config.topColor.isBlank()) "gray" else config.topColor,
            bottom = if (config.bottom.isBlank()) "cargo_pants" else config.bottom,
            bottomColor = if (config.bottomColor.isBlank()) "khaki" else config.bottomColor,
            hat = if (config.hat.isBlank()) "cap" else config.hat
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "hero_anim")

    val swayAngle by if (sway && !isJumping && animationState == HeroAnimationState.IDLE) {
        infiniteTransition.animateFloat(
            initialValue = -1.5f,
            targetValue = 1.5f,
            animationSpec = infiniteRepeatable(
                tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "sway"
        )
    } else remember { mutableFloatStateOf(0f) }

    val jumpOffset by if (isJumping || animationState == HeroAnimationState.JUMP || animationState == HeroAnimationState.VICTORY) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -12f,
            animationSpec = infiniteRepeatable(
                tween(600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "jump"
        )
    } else remember { mutableFloatStateOf(0f) }

    val breathScale by if (animationState == HeroAnimationState.IDLE) {
        infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.015f,
            animationSpec = infiniteRepeatable(
                tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "breath"
        )
    } else remember { mutableFloatStateOf(1.0f) }

    Box(
        modifier = modifier.graphicsLayer {
            rotationZ = swayAngle
            translationY = jumpOffset.dp.toPx()
            scaleX = breathScale
            scaleY = breathScale
        }
    ) {
        AvatarSpriteRenderer(
            character = characterState,
            modifier = Modifier.fillMaxSize(),
            overrideTopPath = overrideTopPath
        )
    }
}
