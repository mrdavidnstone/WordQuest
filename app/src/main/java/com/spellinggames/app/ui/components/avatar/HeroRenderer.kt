package com.spellinggames.app.ui.components.avatar

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.spellinggames.app.data.model.Gender
import com.spellinggames.app.data.model.HeroConfiguration

/**
 * Dedicated Hero Renderer & Live Preview Component.
 * Receives [HeroConfiguration], resolves layer assets, retrieves cached layers,
 * and renders composited character on a 1024x1024 canvas scaled uniformly.
 */
@Composable
fun HeroPreview(
    configuration: HeroConfiguration,
    modifier: Modifier = Modifier,
    animationState: HeroAnimationState = HeroAnimationState.IDLE,
    sway: Boolean = true,
    isJumping: Boolean = false
) {
    val context = LocalContext.current
    val isGirl = configuration.gender == Gender.GIRL

    val spriteBitmap = remember(configuration) {
        SpriteSheetProcessor.getHeroBitmap(context, configuration)
    }

    // 1. Resolve layers via HeroAssetResolver
    val layerSpecs = remember(configuration) {
        HeroAssetResolver.resolve(configuration)
    }

    // 2. Fetch / Cache bitmaps
    val layerBitmaps = layerSpecs.map { spec ->
        AvatarAssetCache.getLayerBitmap(context, spec, isGirl)
    }

    // 3. Subtle live animation
    val infiniteTransition = rememberInfiniteTransition(label = "hero_preview_anim")

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

    // Game-themed preview background panel
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF071A30)),
        contentAlignment = Alignment.Center
    ) {
        // Character Container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = swayAngle
                    translationY = jumpOffset.dp.toPx()
                    scaleX = breathScale
                    scaleY = breathScale
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width.toInt().coerceAtLeast(1)
                val canvasHeight = size.height.toInt().coerceAtLeast(1)
                val destSize = IntSize(canvasWidth, canvasHeight)

                // Ground Shadow
                drawOval(
                    color = Color.Black.copy(alpha = 0.25f),
                    topLeft = Offset(size.width * 0.2f, size.height * 0.90f),
                    size = Size(size.width * 0.6f, size.height * 0.07f)
                )

                if (spriteBitmap != null) {
                    drawImage(
                        image = spriteBitmap,
                        dstOffset = IntOffset.Zero,
                        dstSize = destSize
                    )
                } else {
                    // Draw composited layers in 1024x1024 z-order
                    layerBitmaps.forEach { bitmap ->
                        drawImage(
                            image = bitmap,
                            dstOffset = IntOffset.Zero,
                            dstSize = destSize
                        )
                    }
                }
            }
        }
    }
}
