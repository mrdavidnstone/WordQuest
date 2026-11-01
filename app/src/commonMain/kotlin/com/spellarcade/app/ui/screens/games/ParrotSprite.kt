package com.spellarcade.app.ui.screens.games

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.animation.core.animateFloat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// The sprite is drawn twice: a head layer (top 46%) that can tilt/shake on its own,
// and a body layer (from 42% down) that breathes and hops. The small overlap hides the seam.
private val HeadClip = GenericShape { size, _ ->
    addRect(Rect(0f, 0f, size.width, size.height * 0.46f))
}
private val BodyClip = GenericShape { size, _ ->
    addRect(Rect(0f, size.height * 0.42f, size.width, size.height))
}

private suspend fun Animatable<Float, *>.hop(heightPx: Float, times: Int = 1) {
    repeat(times) {
        animateTo(-heightPx, tween(170, easing = FastOutSlowInEasing))
        animateTo(0f, tween(210, easing = LinearOutSlowInEasing))
    }
}

private suspend fun Animatable<Float, *>.shake() {
    for (angle in listOf(-9f, 7f, -9f, 7f, 0f)) animateTo(angle, tween(90))
}

/**
 * Animated pirate parrot.
 *
 * Idle: breathing + slow head tilt (always on).
 * @param cheerCount   bump this on every correct hit -> single hop
 * @param missCount    bump this on every wrong hit -> head shake ("no")
 * @param celebrate    true while the word is solved -> two hops
 */
@Composable
fun ParrotSprite(
    bitmap: ImageBitmap,
    modifier: Modifier = Modifier,
    height: Dp = 72.dp,
    cheerCount: Int = 0,
    missCount: Int = 0,
    celebrate: Boolean = false
) {
    val hopPx = with(LocalDensity.current) { (height * 0.16f).toPx() }
    val hopY = remember { Animatable(0f) }
    val headShake = remember { Animatable(0f) }

    LaunchedEffect(cheerCount) { if (cheerCount > 0) hopY.hop(hopPx) }
    LaunchedEffect(missCount) { if (missCount > 0) headShake.shake() }
    LaunchedEffect(celebrate) { if (celebrate) hopY.hop(hopPx * 1.2f, times = 2) }

    val idle = rememberInfiniteTransition(label = "parrot_idle")
    val breath by idle.animateFloat(
        initialValue = 1f,
        targetValue = 1.012f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breath"
    )
    val tilt by idle.animateFloat(
        initialValue = -2f,
        targetValue = -2f,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = 6000
                (-2f) at 0
                3f at 2100 using FastOutSlowInEasing
                3f at 3600
                (-3f) at 4800 using FastOutSlowInEasing
                (-2f) at 6000
            }
        ),
        label = "tilt"
    )

    val aspect = bitmap.width.toFloat() / bitmap.height.toFloat()

    Box(
        modifier = modifier
            .height(height)
            .aspectRatio(aspect)
            .graphicsLayer {
                translationY = hopY.value
                scaleY = breath
                transformOrigin = TransformOrigin(0.5f, 1f)
            }
    ) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = Modifier.fillMaxSize().clip(BodyClip),
            contentScale = ContentScale.FillBounds
        )
        Image(
            bitmap = bitmap,
            contentDescription = "Pirate parrot",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = tilt + headShake.value
                    transformOrigin = TransformOrigin(0.62f, 0.44f)
                }
                .clip(HeadClip),
            contentScale = ContentScale.FillBounds
        )
    }
}
