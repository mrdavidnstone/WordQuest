package com.spellarcade.app.ui.components.avatar

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

/**
 * Android Rendering Engine for 2D Modular Hero Sprite System.
 * Generates transparent 1024x1024 RGBA canvas layers using unified anchor coordinates.
 */
object AvatarPrototypeGenerator {

    const val CANVAS_W = 1024
    const val CANVAS_H = 1024

    // Master Anchor Reference Center
    private const val CX = 512f
    private const val CY = 460f

    fun generateLayer(
        layerType: AvatarLayerType,
        spec: AvatarLayerSpec,
        isGirl: Boolean
    ): ImageBitmap {
        val bitmap = Bitmap.createBitmap(CANVAS_W, CANVAS_H, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isSubpixelText = true
            isFilterBitmap = true
        }

        val parsedTint = try {
            if (!spec.tintHex.isNullOrBlank()) Color.parseColor(spec.tintHex)
            else Color.parseColor("#FFD180")
        } catch (_: Exception) {
            Color.parseColor("#FFD180")
        }

        when (layerType) {
            AvatarLayerType.BODY -> drawBodyLayer(canvas, paint, CX, CY, parsedTint)
            AvatarLayerType.FACE -> drawFaceLayer(canvas, paint, CX, CY, parsedTint, spec.fallbackKey)
            AvatarLayerType.EYES -> drawEyesLayer(canvas, paint, CX, CY - 30f, parsedTint)
            AvatarLayerType.HAIR_FRONT -> drawHairFrontLayer(canvas, paint, CX, CY, parsedTint)
            AvatarLayerType.HAIR_BACK -> drawHairBackLayer(canvas, paint, CX, CY, parsedTint)
            AvatarLayerType.TOP -> drawTopLayer(canvas, paint, CX, CY + 160f, parsedTint)
            AvatarLayerType.BOTTOM -> drawBottomLayer(canvas, paint, CX, CY + 360f, parsedTint)
            AvatarLayerType.HAT -> drawHatLayer(canvas, paint, CX, CY - 160f, parsedTint)
        }

        return bitmap.asImageBitmap()
    }

    private fun drawBodyLayer(canvas: Canvas, paint: Paint, cx: Float, cy: Float, tint: Int) {
        // 1. Neck (Junction between head at cy and chest at cy + 160)
        paint.color = darkenColor(tint, 0.85f)
        canvas.drawRect(cx - 40f, cy + 120f, cx + 40f, cy + 200f, paint)

        // 2. Base Arms & Hands (Under outfit)
        paint.color = tint
        canvas.drawOval(RectF(cx - 240f, cy + 220f, cx - 170f, cy + 290f), paint)
        canvas.drawOval(RectF(cx + 170f, cy + 220f, cx + 240f, cy + 290f), paint)

        // 3. Base Legs Anchor / Boots Baseline (cy + 360 to cy + 520)
        paint.color = darkenColor(tint, 0.75f)
        canvas.drawRect(cx - 100f, cy + 420f, cx - 40f, cy + 520f, paint)
        canvas.drawRect(cx + 40f, cy + 420f, cx + 100f, cy + 520f, paint)
    }

    private fun drawFaceLayer(canvas: Canvas, paint: Paint, cx: Float, cy: Float, tint: Int, shapeKey: String) {
        val headWidth = 360f
        val headHeight = 370f

        paint.color = tint
        // Head Oval
        canvas.drawOval(RectF(cx - headWidth / 2f, cy - headHeight / 2f, cx + headWidth / 2f, cy + headHeight / 2f), paint)

        // Left & Right Ears
        canvas.drawOval(RectF(cx - headWidth / 2f - 25f, cy - 20f, cx - headWidth / 2f + 15f, cy + 40f), paint)
        canvas.drawOval(RectF(cx + headWidth / 2f - 15f, cy - 20f, cx + headWidth / 2f + 25f, cy + 40f), paint)

        // Nose
        paint.color = darkenColor(tint, 0.85f)
        canvas.drawOval(RectF(cx - 12f, cy + 20f, cx + 12f, cy + 40f), paint)

        // Smile Mouth
        paint.color = Color.parseColor("#D32F2F")
        canvas.drawArc(RectF(cx - 40f, cy + 50f, cx + 40f, cy + 100f), 0f, 180f, true, paint)
    }

    private fun drawEyesLayer(canvas: Canvas, paint: Paint, cx: Float, cy: Float, eyeTint: Int) {
        val eyeSpacing = 75f
        val eyeWidth = 60f
        val eyeHeight = 55f

        listOf(cx - eyeSpacing, cx + eyeSpacing).forEach { ex ->
            // Sclera (White)
            paint.color = Color.WHITE
            canvas.drawOval(RectF(ex - eyeWidth / 2f, cy - eyeHeight / 2f, ex + eyeWidth / 2f, cy + eyeHeight / 2f), paint)

            paint.color = Color.DKGRAY
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            canvas.drawOval(RectF(ex - eyeWidth / 2f, cy - eyeHeight / 2f, ex + eyeWidth / 2f, cy + eyeHeight / 2f), paint)
            paint.style = Paint.Style.FILL

            // Iris
            paint.color = if (eyeTint != Color.GRAY) eyeTint else Color.parseColor("#6D4C41")
            canvas.drawOval(RectF(ex - 18f, cy - 18f, ex + 18f, cy + 18f), paint)

            // Pupil
            paint.color = Color.BLACK
            canvas.drawOval(RectF(ex - 10f, cy - 10f, ex + 10f, cy + 10f), paint)

            // Glint Highlight
            paint.color = Color.WHITE
            canvas.drawOval(RectF(ex - 12f, cy - 12f, ex - 4f, cy - 4f), paint)
        }

        // Eyebrows
        paint.color = Color.parseColor("#3E2723")
        paint.strokeWidth = 7f
        canvas.drawLine(cx - eyeSpacing - 30f, cy - 40f, cx - eyeSpacing + 25f, cy - 45f, paint)
        canvas.drawLine(cx + eyeSpacing - 25f, cy - 45f, cx + eyeSpacing + 30f, cy - 40f, paint)
    }

    private fun drawHairFrontLayer(canvas: Canvas, paint: Paint, cx: Float, cy: Float, tint: Int) {
        val hairColor = if (tint != Color.GRAY) tint else Color.parseColor("#3E2723")
        paint.color = hairColor

        // Spiky Boy Hair Top
        val path = Path().apply {
            moveTo(cx - 170f, cy - 80f)
            cubicTo(cx - 190f, cy - 180f, cx - 120f, cy - 240f, cx - 70f, cy - 210f)
            cubicTo(cx - 40f, cy - 260f, cx + 20f, cy - 270f, cx + 60f, cy - 220f)
            cubicTo(cx + 120f, cy - 250f, cx + 180f, cy - 180f, cx + 170f, cy - 80f)
            cubicTo(cx + 120f, cy - 120f, cx - 120f, cy - 120f, cx - 170f, cy - 80f)
            close()
        }
        canvas.drawPath(path, paint)

        // Sideburns
        canvas.drawOval(RectF(cx - 185f, cy - 120f, cx - 140f, cy - 30f), paint)
        canvas.drawOval(RectF(cx + 140f, cy - 120f, cx + 185f, cy - 30f), paint)
    }

    private fun drawHairBackLayer(canvas: Canvas, paint: Paint, cx: Float, cy: Float, tint: Int) {
        paint.color = darkenColor(tint, 0.85f)
        canvas.drawRoundRect(RectF(cx - 210f, cy - 100f, cx + 210f, cy + 260f), 50f, 50f, paint)
    }

    private fun drawTopLayer(canvas: Canvas, paint: Paint, cx: Float, cy: Float, tint: Int) {
        val topColor = if (tint != Color.GRAY) tint else Color.parseColor("#1E88E5")
        paint.color = topColor

        // Torso / Blue Hoodie Body
        canvas.drawRoundRect(RectF(cx - 160f, cy, cx + 160f, cy + 220f), 40f, 40f, paint)

        // Sleeves
        canvas.drawRoundRect(RectF(cx - 230f, cy, cx - 150f, cy + 200f), 30f, 30f, paint)
        canvas.drawRoundRect(RectF(cx + 150f, cy, cx + 230f, cy + 200f), 30f, 30f, paint)

        // Front Pocket
        paint.color = darkenColor(topColor, 0.82f)
        canvas.drawRoundRect(RectF(cx - 90f, cy + 120f, cx + 90f, cy + 200f), 20f, 20f, paint)

        // Hoodie Strings
        paint.color = Color.WHITE
        paint.strokeWidth = 5f
        canvas.drawLine(cx - 20f, cy + 10f, cx - 25f, cy + 80f, paint)
        canvas.drawLine(cx + 20f, cy + 10f, cx + 25f, cy + 80f, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawBottomLayer(canvas: Canvas, paint: Paint, cx: Float, cy: Float, tint: Int) {
        val bottomColor = if (tint != Color.GRAY) tint else Color.parseColor("#1A237E")
        paint.color = bottomColor

        // Left Leg Jeans
        canvas.drawRoundRect(RectF(cx - 120f, cy, cx - 15f, cy + 160f), 20f, 20f, paint)
        // Right Leg Jeans
        canvas.drawRoundRect(RectF(cx + 15f, cy, cx + 120f, cy + 160f), 20f, 20f, paint)

        // Waistband
        paint.color = darkenColor(bottomColor, 0.8f)
        canvas.drawRect(cx - 130f, cy, cx + 130f, cy + 30f, paint)

        // Shoes (Sneakers)
        paint.color = Color.parseColor("#3E2723")
        canvas.drawRoundRect(RectF(cx - 135f, cy + 140f, cx - 20f, cy + 185f), 15f, 15f, paint)
        canvas.drawRoundRect(RectF(cx + 20f, cy + 140f, cx + 135f, cy + 185f), 15f, 15f, paint)

        // Sneaker soles
        paint.color = Color.WHITE
        canvas.drawRect(cx - 135f, cy + 175f, cx - 20f, cy + 185f, paint)
        canvas.drawRect(cx + 20f, cy + 175f, cx + 135f, cy + 185f, paint)
    }

    private fun drawHatLayer(canvas: Canvas, paint: Paint, cx: Float, cy: Float, tint: Int) {
        val capColor = Color.parseColor("#E53935")
        paint.color = capColor

        // Baseball Cap Dome
        canvas.drawArc(RectF(cx - 170f, cy - 60f, cx + 170f, cy + 120f), 180f, 180f, true, paint)

        // Visor Bill
        paint.color = darkenColor(capColor, 0.8f)
        canvas.drawRoundRect(RectF(cx - 190f, cy + 20f, cx + 70f, cy + 50f), 15f, 15f, paint)

        // Front Cap Emblem Button
        paint.color = Color.WHITE
        canvas.drawCircle(cx, cy - 55f, 10f, paint)
    }

    private fun darkenColor(color: Int, factor: Float): Int {
        val a = Color.alpha(color)
        val r = (Color.red(color) * factor).toInt().coerceIn(0, 255)
        val g = (Color.green(color) * factor).toInt().coerceIn(0, 255)
        val b = (Color.blue(color) * factor).toInt().coerceIn(0, 255)
        return Color.argb(a, r, g, b)
    }
}
