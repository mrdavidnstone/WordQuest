package com.spellarcade.app.ui.components.avatar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.spellarcade.app.data.model.HeroConfiguration

object SpriteSheetProcessor {

    private const val MASTER_SIZE = 1024
    private const val CX = 512f
    private const val CY = 460f

    private var masterBitmap: Bitmap? = null

    private val cache = object : LruCache<String, ImageBitmap>(32 * 1024 * 1024) {
        override fun sizeOf(key: String, value: ImageBitmap): Int {
            return value.width * value.height * 4
        }
    }

    private fun loadMasterBitmap(context: Context): Bitmap? {
        if (masterBitmap == null || masterBitmap?.isRecycled == true) {
            try {
                context.assets.open("sprite.png").use { stream ->
                    masterBitmap = BitmapFactory.decodeStream(stream)
                }
            } catch (_: Exception) {
                masterBitmap = null
            }
        }
        return masterBitmap
    }

    /**
     * Crops sprite piece from sprite.png using specified pixel bounds
     * on a normalized 1000 x 850 reference grid.
     */
    fun cropRegion(context: Context, left: Float, top: Float, right: Float, bottom: Float): ImageBitmap? {
        val master = loadMasterBitmap(context) ?: return null
        val cacheKey = "crop_${left}_${top}_${right}_${bottom}"

        cache.get(cacheKey)?.let { return it }

        val w = master.width.toFloat()
        val h = master.height.toFloat()

        // Reference grid dimensions from specification sheet (1000 x 850)
        val refW = 1000f
        val refH = 850f

        val cropX = ((left / refW) * w).toInt().coerceIn(0, master.width - 1)
        val cropY = ((top / refH) * h).toInt().coerceIn(0, master.height - 1)
        val cropW = (((right - left) / refW) * w).toInt().coerceIn(1, master.width - cropX)
        val cropH = (((bottom - top) / refH) * h).toInt().coerceIn(1, master.height - cropY)

        try {
            val cropped = Bitmap.createBitmap(master, cropX, cropY, cropW, cropH)
            val transparentPiece = removeBackground(cropped)
            val imageBitmap = transparentPiece.asImageBitmap()
            cache.put(cacheKey, imageBitmap)
            return imageBitmap
        } catch (_: Exception) {
            return null
        }
    }

    /**
     * Crops a card content region from the sprite2 grid sheet.
     * Grid has 7 rows and 9-10 columns per row.
     */
    fun cropGridCard(context: Context, row: Int, col: Int, totalCols: Int = 9): ImageBitmap? {
        val master = loadMasterBitmap(context) ?: return null
        val cacheKey = "grid_r${row}_c${col}_t${totalCols}"

        cache.get(cacheKey)?.let { return it }

        val w = master.width
        val h = master.height

        val cellWidth = w / totalCols.toFloat()
        val cellHeight = h / 7.0f

        val headerOffset = cellHeight * 0.12f

        val cropX = (col * cellWidth).toInt().coerceIn(0, w - 1)
        val cropY = (row * cellHeight + headerOffset).toInt().coerceIn(0, h - 1)
        val cropW = cellWidth.toInt().coerceIn(1, w - cropX)
        val cropH = (cellHeight - headerOffset).toInt().coerceIn(1, h - cropY)

        try {
            val cropped = Bitmap.createBitmap(master, cropX, cropY, cropW, cropH)
            val transparentPiece = removeBackground(cropped)
            val imageBitmap = transparentPiece.asImageBitmap()
            cache.put(cacheKey, imageBitmap)
            return imageBitmap
        } catch (_: Exception) {
            return null
        }
    }

    /**
     * Crops sprite piece and embeds it onto a 1024x1024 transparent master canvas sheet centered at targetY.
     */
    fun cropLayerSheet(
        context: Context,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        targetY: Float,
        scaleFactor: Float = 2.8f
    ): ImageBitmap? {
        val master = loadMasterBitmap(context) ?: return null
        val cacheKey = "sheet_${left}_${top}_${right}_${bottom}_${targetY}_${scaleFactor}"

        cache.get(cacheKey)?.let { return it }

        val w = master.width.toFloat()
        val h = master.height.toFloat()

        val refW = 1000f
        val refH = 850f

        val cropX = ((left / refW) * w).toInt().coerceIn(0, master.width - 1)
        val cropY = ((top / refH) * h).toInt().coerceIn(0, master.height - 1)
        val cropW = (((right - left) / refW) * w).toInt().coerceIn(1, master.width - cropX)
        val cropH = (((bottom - top) / refH) * h).toInt().coerceIn(1, master.height - cropY)

        try {
            val cropped = Bitmap.createBitmap(master, cropX, cropY, cropW, cropH)
            val transparentPiece = removeBackground(cropped)

            val scaledWidth = (transparentPiece.width * scaleFactor).toInt().coerceAtLeast(1)
            val scaledHeight = (transparentPiece.height * scaleFactor).toInt().coerceAtLeast(1)
            val scaledPiece = Bitmap.createScaledBitmap(transparentPiece, scaledWidth, scaledHeight, true)

            val masterCanvasBitmap = Bitmap.createBitmap(MASTER_SIZE, MASTER_SIZE, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(masterCanvasBitmap)

            val destX = CX - (scaledWidth / 2f)
            val destY = targetY - (scaledHeight / 2f)

            canvas.drawBitmap(scaledPiece, destX, destY, null)

            val imageBitmap = masterCanvasBitmap.asImageBitmap()
            cache.put(cacheKey, imageBitmap)
            return imageBitmap
        } catch (_: Exception) {
            return null
        }
    }

    /**
     * Extracts individual layer piece as a 1024x1024 transparent sheet based on exact source locations.
     */
    fun getLayerBitmap(context: Context, spec: AvatarLayerSpec, isGirl: Boolean): ImageBitmap? {
        val key = spec.fallbackKey.lowercase()

        return when {
            // SKIN / BODY (Skin Tone 1: x 285-365, y 45-190)
            key.contains("body_skin_01") || key.contains("skin_1") -> cropLayerSheet(context, 285f, 45f, 365f, 190f, CY, 2.8f)

            // FACE (Round Face: x 645-735, y 45-190)
            key.contains("face_round") || key.contains("round") -> cropLayerSheet(context, 645f, 45f, 735f, 190f, CY, 2.8f)

            // EYES (Round / Brown Eyes: x 915-985, y 55-105)
            key.contains("eyes_round") || key.contains("round_brown") -> cropLayerSheet(context, 915f, 55f, 985f, 105f, CY - 30f, 2.8f)

            // HAIR FRONT (Spiky Style 1: x 285-375, y 245-405)
            key.contains("hair_spiky") || key.contains("dark_brown") -> cropLayerSheet(context, 285f, 245f, 375f, 405f, CY - 100f, 2.8f)

            // HAT (Red Baseball Cap: x 735-825, y 440-570)
            key.contains("hat_baseball") || key.contains("baseball_red") -> cropLayerSheet(context, 735f, 440f, 825f, 570f, CY - 160f, 2.8f)

            // TOP (Hoodie Gray: x 145-190, y 625-760)
            key.contains("top_hoodie_gray") || key.contains("gray") -> cropLayerSheet(context, 145f, 625f, 190f, 760f, CY + 180f, 3.2f)

            // BOTTOM (Cargo Pants Tan: x 880-930, y 625-855)
            key.contains("bottom_cargopants") || key.contains("cargo") || key.contains("tan") -> cropLayerSheet(context, 880f, 625f, 930f, 855f, CY + 360f, 3.2f)

            else -> cropLayerSheet(context, 285f, 45f, 365f, 190f, CY, 2.8f)
        }
    }

    /**
     * Extracts individual option thumbnail directly from sprite card grid.
     */
    fun getOptionThumbnail(context: Context, key: String): ImageBitmap? {
        val k = key.lowercase()
        return when {
            k.contains("skin_1") -> cropRegion(context, 285f, 45f, 365f, 190f)
            k.contains("round") -> cropRegion(context, 645f, 45f, 735f, 190f)
            k.contains("oval") -> cropRegion(context, 735f, 45f, 820f, 190f)
            k.contains("square") -> cropRegion(context, 825f, 45f, 910f, 190f)
            k.contains("spiky") -> cropRegion(context, 285f, 245f, 375f, 405f)
            k.contains("baseball") -> cropRegion(context, 735f, 440f, 825f, 570f)
            k.contains("hoodie") || k.contains("gray") -> cropRegion(context, 145f, 625f, 190f, 760f)
            k.contains("cargo") || k.contains("tan") -> cropRegion(context, 880f, 625f, 930f, 855f)
            else -> cropRegion(context, 285f, 45f, 365f, 190f)
        }
    }

    /**
     * Extracts full hero 3D Boy bitmap from sprite.png.
     */
    fun getHeroBitmap(context: Context, config: HeroConfiguration): ImageBitmap? {
        return cropRegion(context, 10f, 15f, 105f, 350f)
    }

    private fun removeBackground(src: Bitmap): Bitmap {
        val width = src.width
        val height = src.height
        val pixels = IntArray(width * height)
        src.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val color = pixels[i]
            val r = (color shr 16) and 0xFF
            val g = (color shr 8) and 0xFF
            val b = color and 0xFF

            // Remove white background and checkered canvas pixels
            if ((r >= 235 && g >= 238 && b >= 238) || (r in 200..215 && g in 200..215 && b in 200..215)) {
                pixels[i] = 0 // Transparent ARGB
            }
        }

        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }
}
