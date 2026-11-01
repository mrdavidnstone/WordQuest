package com.spellarcade.app.ui.components

import android.graphics.BitmapFactory
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spellarcade.app.data.local.SpellingListEntity
import com.spellarcade.app.data.model.AvatarConfig
import com.spellarcade.app.ui.components.avatar.HeroAnimationState
import com.spellarcade.app.ui.components.avatar.HeroSprite
import com.spellarcade.app.ui.screens.games.GameUiState
import com.spellarcade.app.ui.screens.profile.Avatar
import com.spellarcade.app.ui.theme.*

@Composable
fun AvatarDisplay(
    avatar: Avatar,
    modifier: Modifier = Modifier,
    mood: String = "Happy",
    sway: Boolean = true,
    isJumping: Boolean = true
) {
    val config = remember(avatar) {
        AvatarConfig.fromAvatar(avatar)
    }

    val animState = when {
        mood.equals("Victory", true) -> HeroAnimationState.VICTORY
        isJumping -> HeroAnimationState.JUMP
        else -> HeroAnimationState.IDLE
    }

    HeroSprite(
        config = config,
        modifier = modifier,
        animationState = animState,
        sway = sway,
        isJumping = isJumping
    )
}

// --- MODULAR DRAWING FUNCTIONS ---

private fun DrawScope.drawFaceShape(color: Color, shape: String, x: Float, y: Float) {
    val r = 32.dp.toPx()
    when (shape) {
        "Round" -> drawCircle(color, radius = r, center = Offset(x, y))
        "Oval" -> drawOval(color, topLeft = Offset(x - r * 0.85f, y - r * 1.1f), size = Size(r * 1.7f, r * 2.2f))
        "Square" -> {
            drawRoundRect(color, topLeft = Offset(x - r, y - r), size = Size(r * 2, r * 2.1f), cornerRadius = CornerRadius(12.dp.toPx()))
        }
        "Narrow" -> drawOval(color, topLeft = Offset(x - r * 0.75f, y - r * 1.2f), size = Size(r * 1.5f, r * 2.4f))
        "Heart" -> {
            val path = Path().apply {
                moveTo(x, y + r)
                cubicTo(x + r, y + r * 0.5f, x + r * 1.2f, y - r, x, y - r)
                cubicTo(x - r * 1.2f, y - r, x - r, y + r * 0.5f, x, y + r)
                close()
            }
            drawPath(path, color)
        }
        else -> drawCircle(color, radius = r, center = Offset(x, y))
    }
    
    // Pseudo-3D shading gradient overlay for face depth
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.12f)),
            center = Offset(x - 5.dp.toPx(), y - 5.dp.toPx()),
            radius = r * 1.2f
        ),
        radius = r,
        center = Offset(x, y)
    )
    
    // High-contrast chin rim highlight
    drawArc(
        color = Color.White.copy(alpha = 0.1f),
        startAngle = 45f,
        sweepAngle = 90f,
        useCenter = false,
        topLeft = Offset(x - r + 1.dp.toPx(), y - r + 1.dp.toPx()),
        size = Size(r * 2 - 2.dp.toPx(), r * 2 - 2.dp.toPx()),
        style = Stroke(width = 2.dp.toPx())
    )
}

private fun DrawScope.drawEyes(color: Color, style: String, x: Float, y: Float) {
    val eyeSpacing = 12.dp.toPx()
    val eyeY = y - 4.dp.toPx()
    
    fun drawSingleEye(ex: Float) {
        // White
        drawCircle(Color.White, radius = 6.dp.toPx(), center = Offset(ex, eyeY))
        // Iris
        drawCircle(color, radius = 3.5.dp.toPx(), center = Offset(ex, eyeY))
        // Pupil
        drawCircle(Color.Black, radius = 1.8.dp.toPx(), center = Offset(ex, eyeY))
        // Glint
        drawCircle(Color.White, radius = 1.dp.toPx(), center = Offset(ex - 1.5.dp.toPx(), eyeY - 1.5.dp.toPx()))
    }
    
    drawSingleEye(x - eyeSpacing)
    drawSingleEye(x + eyeSpacing)
}

private fun DrawScope.drawEyebrows(style: String, x: Float, y: Float) {
    val browY = y - 14.dp.toPx()
    val browW = 10.dp.toPx()
    val spacing = 12.dp.toPx()
    
    val color = Color.Black.copy(alpha = 0.4f)
    
    drawLine(color, Offset(x - spacing - browW/2, browY), Offset(x - spacing + browW/2, browY), 2.dp.toPx(), StrokeCap.Round)
    drawLine(color, Offset(x + spacing - browW/2, browY), Offset(x + spacing + browW/2, browY), 2.dp.toPx(), StrokeCap.Round)
}

private fun DrawScope.drawNose(style: String, x: Float, y: Float) {
    drawCircle(Color.Black.copy(alpha = 0.1f), radius = 2.dp.toPx(), center = Offset(x, y + 6.dp.toPx()))
}

private fun DrawScope.drawMouth(style: String, x: Float, y: Float) {
    val mouthY = y + 16.dp.toPx()
    val path = Path().apply {
        moveTo(x - 8.dp.toPx(), mouthY)
        quadraticTo(x, mouthY + 8.dp.toPx(), x + 8.dp.toPx(), mouthY)
    }
    drawPath(path, Color.Black.copy(alpha = 0.7f), style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round))
}

private fun DrawScope.drawTorso(skin: Color, color: Color, style: String, x: Float, y: Float) {
    val torsoY = y + 22.dp.toPx()
    
    // Draw different shapes based on the chosen outfit style to make them uniquely functional/visible
    when (style) {
        "Explorer" -> {
            drawRoundRect(color, topLeft = Offset(x - 20.dp.toPx(), torsoY), size = Size(40.dp.toPx(), 45.dp.toPx()), cornerRadius = CornerRadius(6.dp.toPx()))
            drawRect(Color.White.copy(alpha = 0.4f), topLeft = Offset(x - 6.dp.toPx(), torsoY), size = Size(12.dp.toPx(), 10.dp.toPx()))
            drawRoundRect(Color.Black.copy(alpha = 0.15f), topLeft = Offset(x - 16.dp.toPx(), torsoY + 15.dp.toPx()), size = Size(10.dp.toPx(), 12.dp.toPx()), cornerRadius = CornerRadius(2.dp.toPx()))
            drawRoundRect(Color.Black.copy(alpha = 0.15f), topLeft = Offset(x + 6.dp.toPx(), torsoY + 15.dp.toPx()), size = Size(10.dp.toPx(), 12.dp.toPx()), cornerRadius = CornerRadius(2.dp.toPx()))
        }
        "Knight" -> {
            drawRoundRect(color, topLeft = Offset(x - 20.dp.toPx(), torsoY), size = Size(40.dp.toPx(), 45.dp.toPx()), cornerRadius = CornerRadius(4.dp.toPx()))
            drawCircle(Color.LightGray, radius = 8.dp.toPx(), center = Offset(x - 18.dp.toPx(), torsoY + 4.dp.toPx()))
            drawCircle(Color.LightGray, radius = 8.dp.toPx(), center = Offset(x + 18.dp.toPx(), torsoY + 4.dp.toPx()))
            drawRect(Color.White.copy(alpha = 0.5f), topLeft = Offset(x - 3.dp.toPx(), torsoY + 12.dp.toPx()), size = Size(6.dp.toPx(), 20.dp.toPx()))
            drawRect(Color.White.copy(alpha = 0.5f), topLeft = Offset(x - 10.dp.toPx(), torsoY + 17.dp.toPx()), size = Size(20.dp.toPx(), 6.dp.toPx()))
        }
        "Wizard" -> {
            val path = Path().apply {
                moveTo(x - 16.dp.toPx(), torsoY)
                lineTo(x + 16.dp.toPx(), torsoY)
                lineTo(x + 24.dp.toPx(), torsoY + 45.dp.toPx())
                lineTo(x - 24.dp.toPx(), torsoY + 45.dp.toPx())
                close()
            }
            drawPath(path, color)
            val collarPath = Path().apply {
                moveTo(x - 8.dp.toPx(), torsoY)
                lineTo(x, torsoY + 14.dp.toPx())
                lineTo(x + 8.dp.toPx(), torsoY)
            }
            drawPath(collarPath, Color(0xFFFBC02D), style = Stroke(3.dp.toPx()))
        }
        "Archer" -> {
            drawRoundRect(color, topLeft = Offset(x - 20.dp.toPx(), torsoY), size = Size(40.dp.toPx(), 45.dp.toPx()), cornerRadius = CornerRadius(8.dp.toPx()))
            drawLine(Color(0xFF5D4037), start = Offset(x - 20.dp.toPx(), torsoY + 5.dp.toPx()), end = Offset(x + 20.dp.toPx(), torsoY + 35.dp.toPx()), strokeWidth = 5.dp.toPx())
        }
        "School" -> {
            drawRoundRect(color, topLeft = Offset(x - 20.dp.toPx(), torsoY), size = Size(40.dp.toPx(), 45.dp.toPx()), cornerRadius = CornerRadius(8.dp.toPx()))
            val collarPath = Path().apply {
                moveTo(x - 10.dp.toPx(), torsoY)
                lineTo(x, torsoY + 12.dp.toPx())
                lineTo(x + 10.dp.toPx(), torsoY)
                close()
            }
            drawPath(collarPath, Color.White)
            drawLine(Color.Red, start = Offset(x, torsoY + 8.dp.toPx()), end = Offset(x, torsoY + 22.dp.toPx()), strokeWidth = 3.dp.toPx())
        }
        "Sports" -> {
            drawRoundRect(color, topLeft = Offset(x - 18.dp.toPx(), torsoY), size = Size(36.dp.toPx(), 45.dp.toPx()), cornerRadius = CornerRadius(12.dp.toPx()))
            drawLine(Color.White.copy(alpha = 0.6f), start = Offset(x - 8.dp.toPx(), torsoY), end = Offset(x - 8.dp.toPx(), torsoY + 45.dp.toPx()), strokeWidth = 2.5.dp.toPx())
            drawLine(Color.White.copy(alpha = 0.6f), start = Offset(x + 8.dp.toPx(), torsoY), end = Offset(x + 8.dp.toPx(), torsoY + 45.dp.toPx()), strokeWidth = 2.5.dp.toPx())
        }
        "Scientist" -> {
            drawRoundRect(color, topLeft = Offset(x - 20.dp.toPx(), torsoY), size = Size(40.dp.toPx(), 45.dp.toPx()), cornerRadius = CornerRadius(6.dp.toPx()))
            drawRoundRect(Color(0xFFF5F5F5), topLeft = Offset(x - 20.dp.toPx(), torsoY), size = Size(12.dp.toPx(), 45.dp.toPx()), cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()))
            drawRoundRect(Color(0xFFF5F5F5), topLeft = Offset(x + 8.dp.toPx(), torsoY), size = Size(12.dp.toPx(), 45.dp.toPx()), cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()))
            drawRect(Color.LightGray, topLeft = Offset(x - 16.dp.toPx(), torsoY + 15.dp.toPx()), size = Size(6.dp.toPx(), 8.dp.toPx()))
        }
        else -> {
            drawRoundRect(color, topLeft = Offset(x - 20.dp.toPx(), torsoY), size = Size(40.dp.toPx(), 45.dp.toPx()), cornerRadius = CornerRadius(10.dp.toPx()))
            drawRoundRect(Color.Black.copy(alpha = 0.1f), topLeft = Offset(x - 12.dp.toPx(), torsoY + 25.dp.toPx()), size = Size(24.dp.toPx(), 15.dp.toPx()), cornerRadius = CornerRadius(4.dp.toPx()))
        }
    }
}

private fun DrawScope.drawArms(skin: Color, color: Color, style: String, x: Float, y: Float) {
    val shoulderY = y + 28.dp.toPx()
    val armW = 12.dp.toPx()
    
    // Left Arm
    drawLine(color, Offset(x - 18.dp.toPx(), shoulderY), Offset(x - 32.dp.toPx(), y + 60.dp.toPx()), armW, StrokeCap.Round)
    drawCircle(skin, radius = 7.dp.toPx(), center = Offset(x - 32.dp.toPx(), y + 60.dp.toPx()))
    
    // Right Arm (Hands down as well, mirror of left arm)
    drawLine(color, Offset(x + 18.dp.toPx(), shoulderY), Offset(x + 32.dp.toPx(), y + 60.dp.toPx()), armW, StrokeCap.Round)
    drawCircle(skin, radius = 7.dp.toPx(), center = Offset(x + 32.dp.toPx(), y + 60.dp.toPx()))
}

private fun DrawScope.drawBottoms(color: Color, style: String, x: Float, y: Float) {
    val hipY = y + 65.dp.toPx()
    val legW = 14.dp.toPx()
    
    when (style) {
        "Shorts" -> {
            // Draw brief shorts top
            drawRect(color, topLeft = Offset(x - 14.dp.toPx(), hipY), size = Size(28.dp.toPx(), 12.dp.toPx()))
            // Shorts legs shorter length
            drawLine(color, Offset(x - 8.dp.toPx(), hipY), Offset(x - 9.dp.toPx(), y + 80.dp.toPx()), legW, StrokeCap.Round)
            drawLine(color, Offset(x + 8.dp.toPx(), hipY), Offset(x + 9.dp.toPx(), y + 80.dp.toPx()), legW, StrokeCap.Round)
        }
        "Skirt" -> {
            // Flowing flared out style
            val path = Path().apply {
                moveTo(x - 12.dp.toPx(), hipY)
                lineTo(x + 12.dp.toPx(), hipY)
                lineTo(x + 24.dp.toPx(), y + 88.dp.toPx())
                lineTo(x - 24.dp.toPx(), y + 88.dp.toPx())
                close()
            }
            drawPath(path, color)
        }
        "Cargo Pants" -> {
            // Baggy pants with pocket outlines
            drawLine(color, Offset(x - 10.dp.toPx(), hipY), Offset(x - 12.dp.toPx(), y + 100.dp.toPx()), 17.dp.toPx(), StrokeCap.Round)
            drawLine(color, Offset(x + 10.dp.toPx(), hipY), Offset(x + 12.dp.toPx(), y + 100.dp.toPx()), 17.dp.toPx(), StrokeCap.Round)
            // Left cargo flap
            drawRoundRect(Color.Black.copy(alpha = 0.1f), topLeft = Offset(x - 20.dp.toPx(), y + 76.dp.toPx()), size = Size(7.dp.toPx(), 10.dp.toPx()), cornerRadius = CornerRadius(2.dp.toPx()))
            // Right cargo flap
            drawRoundRect(Color.Black.copy(alpha = 0.1f), topLeft = Offset(x + 13.dp.toPx(), y + 76.dp.toPx()), size = Size(7.dp.toPx(), 10.dp.toPx()), cornerRadius = CornerRadius(2.dp.toPx()))
        }
        "Robe Bottom" -> {
            // Full extension robe downwards
            drawRoundRect(color, topLeft = Offset(x - 18.dp.toPx(), hipY), size = Size(36.dp.toPx(), 35.dp.toPx()), cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()))
        }
        "Trousers" -> {
            // Smart straight line trousers
            drawLine(color, Offset(x - 9.dp.toPx(), hipY), Offset(x - 10.dp.toPx(), y + 100.dp.toPx()), 13.dp.toPx(), StrokeCap.Square)
            drawLine(color, Offset(x + 9.dp.toPx(), hipY), Offset(x + 10.dp.toPx(), y + 100.dp.toPx()), 13.dp.toPx(), StrokeCap.Square)
        }
        else -> {
            // Standard "Jeans" style
            drawLine(color, Offset(x - 10.dp.toPx(), hipY), Offset(x - 12.dp.toPx(), y + 100.dp.toPx()), 14.dp.toPx(), StrokeCap.Round)
            drawLine(color, Offset(x + 10.dp.toPx(), hipY), Offset(x + 12.dp.toPx(), y + 100.dp.toPx()), 14.dp.toPx(), StrokeCap.Round)
        }
    }
}

private fun DrawScope.drawShoes(color: Color, style: String, x: Float, y: Float) {
    val shoeY = y + 100.dp.toPx()
    drawRoundRect(color, Offset(x - 20.dp.toPx(), shoeY), Size(16.dp.toPx(), 10.dp.toPx()), CornerRadius(4.dp.toPx()))
    drawRect(Color.White.copy(alpha = 0.3f), Offset(x - 20.dp.toPx(), shoeY + 7.dp.toPx()), Size(16.dp.toPx(), 3.dp.toPx()))
    
    drawRoundRect(color, Offset(x + 4.dp.toPx(), shoeY), Size(16.dp.toPx(), 10.dp.toPx()), CornerRadius(4.dp.toPx()))
    drawRect(Color.White.copy(alpha = 0.3f), Offset(x + 4.dp.toPx(), shoeY + 7.dp.toPx()), Size(16.dp.toPx(), 3.dp.toPx()))
}

private fun DrawScope.drawBackHair(color: Color, style: String, x: Float, y: Float) {
    if (style == "Long Straight") {
        drawRoundRect(color, topLeft = Offset(x - 30.dp.toPx(), y - 10.dp.toPx()), size = Size(60.dp.toPx(), 75.dp.toPx()), cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()))
    } else if (style == "Long Wavy") {
        val path = Path().apply {
            moveTo(x - 30.dp.toPx(), y - 10.dp.toPx())
            lineTo(x + 30.dp.toPx(), y - 10.dp.toPx())
            quadraticTo(x + 35.dp.toPx(), y + 15.dp.toPx(), x + 28.dp.toPx(), y + 35.dp.toPx())
            quadraticTo(x + 33.dp.toPx(), y + 55.dp.toPx(), x + 26.dp.toPx(), y + 75.dp.toPx())
            lineTo(x - 26.dp.toPx(), y + 75.dp.toPx())
            quadraticTo(x - 33.dp.toPx(), y + 55.dp.toPx(), x - 28.dp.toPx(), y + 35.dp.toPx())
            quadraticTo(x - 35.dp.toPx(), y + 15.dp.toPx(), x - 30.dp.toPx(), y - 10.dp.toPx())
            close()
        }
        drawPath(path, color)
    } else if (style == "Ponytail") {
        // High ponytail bubble coming down
        drawCircle(color, radius = 16.dp.toPx(), center = Offset(x + 22.dp.toPx(), y + 10.dp.toPx()))
        drawLine(color, Offset(x, y - 15.dp.toPx()), Offset(x + 20.dp.toPx(), y + 5.dp.toPx()), 8.dp.toPx(), StrokeCap.Round)
    } else if (style == "Braids") {
        // Multi part braided strands hanging left and right
        drawRoundRect(color, Offset(x - 32.dp.toPx(), y), Size(10.dp.toPx(), 60.dp.toPx()), CornerRadius(6.dp.toPx()))
        drawRoundRect(color, Offset(x + 22.dp.toPx(), y), Size(10.dp.toPx(), 60.dp.toPx()), CornerRadius(6.dp.toPx()))
    } else if (style == "Bun" || style == "Space Buns") {
        // Top or side bun back extension indicators
        drawCircle(color, radius = 12.dp.toPx(), center = Offset(x - 24.dp.toPx(), y - 28.dp.toPx()))
        drawCircle(color, radius = 12.dp.toPx(), center = Offset(x + 24.dp.toPx(), y - 28.dp.toPx()))
    } else if (style == "Top Knot") {
        val rHead = 32.dp.toPx()
        drawCircle(color, radius = 12.dp.toPx(), center = Offset(x, y - rHead - 8.dp.toPx()))
    } else if (style == "Pigtails") {
        drawCircle(color, radius = 15.dp.toPx(), center = Offset(x - 32.dp.toPx(), y + 10.dp.toPx()))
        drawCircle(color, radius = 15.dp.toPx(), center = Offset(x + 32.dp.toPx(), y + 10.dp.toPx()))
    } else if (style.contains("Long") || style == "Wavy" || style == "Curly") {
        drawRect(color, topLeft = Offset(x - 30.dp.toPx(), y - 10.dp.toPx()), size = Size(60.dp.toPx(), 70.dp.toPx()))
    }
}

private fun DrawScope.drawFrontHair(color: Color, style: String, x: Float, y: Float) {
    val r = 32.dp.toPx()
    // Base hair cap
    if (style != "Bald") {
        drawArc(color, -180f, 180f, true, Offset(x - r, y - r - 4.dp.toPx()), Size(r * 2, r * 1.5f))
    }
    
    // Custom logic for every unique hairstyle option so they're completely operational
    when (style) {
        "Messy" -> {
            drawCircle(color, radius = 10.dp.toPx(), center = Offset(x - 15.dp.toPx(), y - r))
            drawCircle(color, radius = 12.dp.toPx(), center = Offset(x + 5.dp.toPx(), y - r - 5.dp.toPx()))
        }
        "Spiky" -> {
            val path = Path().apply {
                moveTo(x - r, y - 10.dp.toPx())
                lineTo(x - 20.dp.toPx(), y - r - 15.dp.toPx())
                lineTo(x, y - r - 5.dp.toPx())
                lineTo(x + 20.dp.toPx(), y - r - 15.dp.toPx())
                lineTo(x + r, y - 10.dp.toPx())
            }
            drawPath(path, color)
        }
        "Short" -> {
            drawRoundRect(color, topLeft = Offset(x - r, y - r - 2.dp.toPx()), size = Size(r * 2, 16.dp.toPx()), cornerRadius = CornerRadius(4.dp.toPx()))
        }
        "Side Part" -> {
            drawArc(Color.Black.copy(alpha = 0.2f), -130f, 40f, true, Offset(x - r, y - r - 4.dp.toPx()), Size(r * 2, r * 1.5f))
            drawCircle(color, radius = 14.dp.toPx(), center = Offset(x - 10.dp.toPx(), y - r))
        }
        "Buzz Cut" -> {
            drawArc(color.copy(alpha = 0.7f), -180f, 180f, true, Offset(x - r + 2.dp.toPx(), y - r), Size(r * 2 - 4.dp.toPx(), r * 1.3f))
        }
        "Curly Fade" -> {
            drawCircle(color, radius = 8.dp.toPx(), center = Offset(x - 15.dp.toPx(), y - r - 4.dp.toPx()))
            drawCircle(color, radius = 9.dp.toPx(), center = Offset(x, y - r - 6.dp.toPx()))
            drawCircle(color, radius = 8.dp.toPx(), center = Offset(x + 15.dp.toPx(), y - r - 4.dp.toPx()))
        }
        "Undercut" -> {
            drawRoundRect(color, topLeft = Offset(x - 12.dp.toPx(), y - r - 8.dp.toPx()), size = Size(28.dp.toPx(), 18.dp.toPx()), cornerRadius = CornerRadius(4.dp.toPx()))
        }
        "Afro", "Afro Puffs" -> {
            drawCircle(color, radius = 22.dp.toPx(), center = Offset(x, y - r + 4.dp.toPx()))
            drawCircle(color, radius = 16.dp.toPx(), center = Offset(x - 16.dp.toPx(), y - r + 10.dp.toPx()))
            drawCircle(color, radius = 16.dp.toPx(), center = Offset(x + 16.dp.toPx(), y - r + 10.dp.toPx()))
        }
        "Bob" -> {
            drawRoundRect(color, topLeft = Offset(x - r - 4.dp.toPx(), y - r), size = Size(8.dp.toPx(), 40.dp.toPx()), cornerRadius = CornerRadius(4.dp.toPx()))
            drawRoundRect(color, topLeft = Offset(x + r - 4.dp.toPx(), y - r), size = Size(8.dp.toPx(), 40.dp.toPx()), cornerRadius = CornerRadius(4.dp.toPx()))
        }
        "Pixie" -> {
            drawRoundRect(color, topLeft = Offset(x - r, y - r + 4.dp.toPx()), size = Size(r * 2, 8.dp.toPx()), cornerRadius = CornerRadius(2.dp.toPx()))
        }
    }
}

private fun DrawScope.drawHats(hairColor: Color, hat: String, x: Float, y: Float) {
    val r = 32.dp.toPx()
    val hatY = y - r - 2.dp.toPx()
    
    when (hat) {
        "Baseball Cap" -> {
            // Cap dome
            drawArc(Color(0xFFE53935), -180f, 180f, true, Offset(x - r + 2.dp.toPx(), hatY - 6.dp.toPx()), Size(r * 2 - 4.dp.toPx(), r * 1.3f))
            // Visor bill extending outwards
            drawRoundRect(Color(0xFFC62828), topLeft = Offset(x - 4.dp.toPx(), hatY + 12.dp.toPx()), size = Size(r * 1.4f, 8.dp.toPx()), cornerRadius = CornerRadius(4.dp.toPx()))
        }
        "Wizard Hat" -> {
            // Tall conical spire
            val path = Path().apply {
                moveTo(x - r - 6.dp.toPx(), hatY + 8.dp.toPx())
                lineTo(x + r + 6.dp.toPx(), hatY + 8.dp.toPx())
                lineTo(x + 4.dp.toPx(), hatY - 45.dp.toPx())
                lineTo(x - 4.dp.toPx(), hatY - 45.dp.toPx())
                close()
            }
            drawPath(path, Color(0xFF311B92))
            // Hat Brim line
            drawRoundRect(Color(0xFFFBC02D), topLeft = Offset(x - r - 10.dp.toPx(), hatY + 4.dp.toPx()), size = Size(r * 2 + 20.dp.toPx(), 7.dp.toPx()), cornerRadius = CornerRadius(3.dp.toPx()))
        }
        "Noble Crown" -> {
            // Golden crown spikes
            val path = Path().apply {
                moveTo(x - 24.dp.toPx(), hatY + 12.dp.toPx())
                lineTo(x - 24.dp.toPx(), hatY - 10.dp.toPx())
                lineTo(x - 12.dp.toPx(), hatY + 2.dp.toPx())
                lineTo(x, hatY - 16.dp.toPx())
                lineTo(x + 12.dp.toPx(), hatY + 2.dp.toPx())
                lineTo(x + 24.dp.toPx(), hatY - 10.dp.toPx())
                lineTo(x + 24.dp.toPx(), hatY + 12.dp.toPx())
                close()
            }
            drawPath(path, Color(0xFFFBC02D))
            // Jewels ruby indicators
            drawCircle(Color.Red, radius = 2.5.dp.toPx(), center = Offset(x, hatY - 2.dp.toPx()))
            drawCircle(Color.Blue, radius = 2.dp.toPx(), center = Offset(x - 14.dp.toPx(), hatY + 4.dp.toPx()))
            drawCircle(Color.Blue, radius = 2.dp.toPx(), center = Offset(x + 14.dp.toPx(), hatY + 4.dp.toPx()))
        }
        "Straw Hat" -> {
            // Rounded tan cap with huge horizontal wide brim
            drawArc(Color(0xFFE6EE9C), -180f, 180f, true, Offset(x - r + 6.dp.toPx(), hatY), Size(r * 2 - 12.dp.toPx(), r))
            drawRoundRect(Color(0xFFD4E157), topLeft = Offset(x - r - 16.dp.toPx(), hatY + r * 0.4f), size = Size(r * 2 + 32.dp.toPx(), 6.dp.toPx()), cornerRadius = CornerRadius(2.dp.toPx()))
            // Red band ribbon
            drawRect(Color.Red, topLeft = Offset(x - r + 7.dp.toPx(), hatY + r * 0.22f), size = Size(r * 2 - 14.dp.toPx(), 5.dp.toPx()))
        }
        "Cowboy Hat" -> {
            // Pinched brown crown hat shape
            val path = Path().apply {
                moveTo(x - r + 6.dp.toPx(), hatY + 10.dp.toPx())
                quadraticTo(x - r + 12.dp.toPx(), hatY - 14.dp.toPx(), x, hatY - 8.dp.toPx())
                quadraticTo(x + r - 12.dp.toPx(), hatY - 14.dp.toPx(), x + r - 6.dp.toPx(), hatY + 10.dp.toPx())
                close()
            }
            drawPath(path, Color(0xFF5D4037))
            // Upturned sides brim line
            drawRoundRect(Color(0xFF4E342E), topLeft = Offset(x - r - 12.dp.toPx(), hatY + 8.dp.toPx()), size = Size(r * 2 + 24.dp.toPx(), 6.dp.toPx()), cornerRadius = CornerRadius(3.dp.toPx()))
        }
        "Beret" -> {
            // Slanted fashion flat round disk
            val path = Path().apply {
                moveTo(x - 22.dp.toPx(), hatY + 6.dp.toPx())
                quadraticTo(x - 10.dp.toPx(), hatY - 12.dp.toPx(), x + r, hatY)
                quadraticTo(x + 10.dp.toPx(), hatY + 14.dp.toPx(), x - 22.dp.toPx(), hatY + 6.dp.toPx())
                close()
            }
            drawPath(path, Color(0xFF212121))
        }
        "Explorer Helmet" -> {
            drawArc(Color(0xFFD7B594), -180f, 180f, true, Offset(x - r + 3.dp.toPx(), hatY - 4.dp.toPx()), Size(r * 2 - 6.dp.toPx(), r * 1.3f))
            drawRoundRect(Color(0xFFBCAAA4), topLeft = Offset(x - r - 6.dp.toPx(), hatY + 10.dp.toPx()), size = Size(r * 2 + 12.dp.toPx(), 5.dp.toPx()), cornerRadius = CornerRadius(2.dp.toPx()))
        }
        "Headband" -> {
            drawRoundRect(Color(0xFFE53935), topLeft = Offset(x - r - 1.dp.toPx(), hatY + 16.dp.toPx()), size = Size(r * 2 + 2.dp.toPx(), 8.dp.toPx()), cornerRadius = CornerRadius(1.dp.toPx()))
        }
    }
}

private fun DrawScope.drawAccessories(acc: String, x: Float, y: Float) {
    if (acc == "Glasses" || acc == "Sunglasses") {
        val color = if (acc == "Glasses") Color.Black.copy(alpha = 0.6f) else Color(0xFF212121)
        val eyeSpacing = 12.dp.toPx()
        val eyeY = y - 4.dp.toPx()
        val glassSize = 14.dp.toPx()
        
        drawRoundRect(color, Offset(x - eyeSpacing - glassSize/2, eyeY - glassSize/2), Size(glassSize, glassSize), CornerRadius(4.dp.toPx()), style = Stroke(2.dp.toPx()))
        drawRoundRect(color, Offset(x + eyeSpacing - glassSize/2, eyeY - glassSize/2), Size(glassSize, glassSize), CornerRadius(4.dp.toPx()), style = Stroke(2.dp.toPx()))
        drawLine(color, Offset(x - 4.dp.toPx(), eyeY), Offset(x + 4.dp.toPx(), eyeY), 2.dp.toPx())
    }
}

@Composable
fun ActiveListBadge(
    listName: String?,
    allLists: List<SpellingListEntity> = emptyList(),
    onListSelected: (Long) -> Unit = {},
    modifier: Modifier = Modifier,
    label: String = ""
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Surface(
            onClick = { if (allLists.isNotEmpty()) showMenu = true },
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0D234A).copy(alpha = 0.8f),
            border = BorderStroke(2.dp, Color(0xFF00B0FF).copy(alpha = 0.8f)),
            enabled = allLists.isNotEmpty()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📚", fontSize = 20.sp)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = listName ?: "No lesson selected",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                if (allLists.isNotEmpty()) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Switch List",
                        tint = Color(0xFF00B0FF)
                    )
                }
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            allLists.forEach { list ->
                DropdownMenuItem(
                    text = {
                        Text(
                            list.name,
                            fontWeight = if (list.name == listName) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        showMenu = false
                        onListSelected(list.id)
                    },
                    leadingIcon = {
                        if (list.name == listName) Icon(Icons.Default.Check, null)
                        else Spacer(Modifier.size(24.dp))
                    }
                )
            }
        }
    }
}

@Composable
fun ReadyOverlay(
    title: String,
    emoji: String,
    description: String,
    uiState: GameUiState,
    onSwitchList: (Long) -> Unit,
    onStart: () -> Unit,
    onBack: () -> Unit,
    customHeader: (@Composable ColumnScope.() -> Unit)? = null,
    extraContent: @Composable ColumnScope.() -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Back Button to return to game selection screen
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .align(Alignment.TopStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Games",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Center Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 52.dp, bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (customHeader != null) {
                customHeader()
            } else {
                Surface(
                    modifier = Modifier.size(90.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(emoji, fontSize = 48.sp)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    title.uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                description,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(Modifier.height(14.dp))

            ActiveListBadge(
                listName = uiState.listName,
                allLists = uiState.allLists,
                onListSelected = onSwitchList
            )

            Spacer(Modifier.height(14.dp))
            
            extraContent()

            Spacer(Modifier.height(18.dp))
            
            if (!uiState.hasWords) {
                Text(
                    "This list has no words. Add some words or pick another list.",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = onBack) { Text("Go Back") }
            } else {
                Button(
                    onClick = onStart,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("PLAY GAME", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun GameCard(
    title: String,
    emoji: String,
    description: String,
    color: Color,
    onClick: () -> Unit,
    enabled: Boolean = true,
    iconAsset: String? = null
) {
    val context = LocalContext.current
    val iconBitmap = remember(context, iconAsset) {
        if (!iconAsset.isNullOrBlank()) {
            val pathsToTry = listOf(
                iconAsset,
                "games/lab_meltdown/$iconAsset"
            )
            for (path in pathsToTry) {
                try {
                    context.assets.open(path).use { stream ->
                        BitmapFactory.decodeStream(stream)?.asImageBitmap()?.let { return@remember it }
                    }
                } catch (_: Exception) {}
            }
            null
        } else null
    }

    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                if (iconBitmap != null) {
                    Image(
                        bitmap = iconBitmap,
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize().padding(2.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(emoji, fontSize = 28.sp)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = color)
        }
    }
}

@Composable
fun EmptyState(
    emoji: String,
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(24.dp))
            Button(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
fun FeedbackBanner(
    isCorrect: Boolean,
    message: String,
    visible: Boolean
) {
    if (!visible) return
    val bg = if (isCorrect) FreshGreen else Coral
    val icon = if (isCorrect) "⭐" else "💪"
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = bg.copy(alpha = 0.9f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(icon, fontSize = 24.sp)
            Spacer(Modifier.width(12.dp))
            Text(
                message,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun ScoreChip(score: Int, total: Int) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SunnyYellow.copy(alpha = 0.3f)
    ) {
        Text(
            "$score / $total",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun GameHeader(
    title: String,
    onBack: () -> Unit,
    score: Int? = null,
    total: Int? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 0.dp, vertical = 0.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(Modifier.weight(1f))
            if (score != null && total != null) {
                ScoreChip(score, total)
                Spacer(Modifier.width(8.dp))
            }
        }
    }
}

@Composable
fun PulsingStar() {
    val infiniteTransition = rememberInfiniteTransition(label = "star")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    Text(
        "⭐",
        fontSize = 48.sp,
        modifier = Modifier.scale(scale)
    )
}
