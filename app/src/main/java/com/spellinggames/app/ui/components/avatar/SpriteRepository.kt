package com.spellinggames.app.ui.components.avatar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.InputStream

data class SpriteOption(
    val id: String,          // e.g. "hoodie.png"
    val displayName: String, // e.g. "Hoodie"
    val fullAssetPath: String
)

object SpriteRepository {

    private val bitmapCache = object : LruCache<String, ImageBitmap>(32 * 1024 * 1024) {
        override fun sizeOf(key: String, value: ImageBitmap): Int {
            return value.width * value.height * 4
        }
    }

    private val croppedCache = object : LruCache<String, ImageBitmap>(16 * 1024 * 1024) {
        override fun sizeOf(key: String, value: ImageBitmap): Int {
            return value.width * value.height * 4
        }
    }

    /**
     * Crops transparent borders around an item graphic so it maximizes space
     * and centers vertically & horizontally inside grid item selection boxes.
     */
    fun loadCroppedItemBitmap(context: Context, assetPath: String, fallbackRow: Int = 0, fallbackCol: Int = 0): ImageBitmap {
        croppedCache.get(assetPath)?.let { return it }

        val source = loadSpriteBitmap(context, assetPath, fallbackRow, fallbackCol)
        try {
            val bitmap = source.asAndroidBitmap()
            val width = bitmap.width
            val height = bitmap.height

            var minX = width
            var minY = height
            var maxX = -1
            var maxY = -1

            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            for (y in 0 until height) {
                for (x in 0 until width) {
                    val alpha = (pixels[y * width + x] shr 24) and 0xFF
                    if (alpha > 15) { // Non-transparent pixel threshold
                        if (x < minX) minX = x
                        if (x > maxX) maxX = x
                        if (y < minY) minY = y
                        if (y > maxY) maxY = y
                    }
                }
            }

            if (maxX >= minX && maxY >= minY) {
                val cropWidth = (maxX - minX + 1).coerceAtLeast(1)
                val cropHeight = (maxY - minY + 1).coerceAtLeast(1)
                val cropped = Bitmap.createBitmap(bitmap, minX, minY, cropWidth, cropHeight)
                val result = cropped.asImageBitmap()
                croppedCache.put(assetPath, result)
                return result
            }
        } catch (_: Exception) {}

        croppedCache.put(assetPath, source)
        return source
    }

    /**
     * Automatically scans an asset directory for PNG files.
     * Supports gender-specific subdirectories and special hat directories (sprites/hats/needs_hair_overrides).
     * Filters out hidden special hair directories (sprites/hair/hide_special_hair) from main selection.
     */
    fun scanDirectory(context: Context, relativeDir: String, genderFolder: String? = null): List<SpriteOption> {
        val options = mutableListOf<SpriteOption>()
        val g = genderFolder?.lowercase()

        // Build list of target directories to scan in order of preference
        val targetDirs = mutableListOf<String>()
        if (relativeDir == "sprites/hats") {
            if (!g.isNullOrBlank()) {
                targetDirs.add("sprites/hats/$g")
            }
            targetDirs.add("sprites/hats")

            // Dynamically discover all hat subdirectories (e.g. needs_hair_overrides)
            try {
                val subFolders = context.assets.list("sprites/hats") ?: emptyArray()
                subFolders.filter { !it.contains(".") }.forEach { subFolder ->
                    if (!g.isNullOrBlank()) {
                        targetDirs.add("sprites/hats/$subFolder/$g")
                    }
                    targetDirs.add("sprites/hats/$subFolder")
                }
            } catch (_: Exception) {}
        } else if (relativeDir == "sprites/hair") {
            targetDirs.add("sprites/hair/both")
            if (!g.isNullOrBlank()) {
                targetDirs.add("sprites/hair/$g")
                targetDirs.add("sprites/hair/${g}s") // e.g. boys, girls
            }
            targetDirs.add("sprites/hair")
        } else if (!g.isNullOrBlank()) {
            targetDirs.add("$relativeDir/$g")
            targetDirs.add(relativeDir)
        } else {
            targetDirs.add(relativeDir)
        }

        val scannedPaths = mutableSetOf<String>()

        for (dir in targetDirs) {
            try {
                val fileList = context.assets.list(dir) ?: emptyArray()
                fileList.filter { file ->
                    file.endsWith(".png", ignoreCase = true) && !file.contains("hide_special_hair", ignoreCase = true)
                }.forEach { fileName ->
                    val isSpecialHatDir = dir.contains("needs_hair_overrides") || dir.contains("need_hair_overrides") || dir.contains("needs_special_hair") || dir.contains("show_special_hair")
                    val optionKey = if (isSpecialHatDir) "needs_hair_overrides/$fileName" else fileName
                    if (!scannedPaths.contains(fileName) && !scannedPaths.contains(optionKey)) {
                        scannedPaths.add(fileName)
                        scannedPaths.add(optionKey)
                        val cleanName = fileName.removeSuffix(".png")
                            .replace("_", " ")
                            .split(" ")
                            .joinToString(" ") { word -> word.replaceFirstChar { char -> char.uppercase() } }

                        options.add(
                            SpriteOption(
                                id = optionKey,
                                displayName = cleanName,
                                fullAssetPath = "$dir/$fileName"
                            )
                        )
                    }
                }
            } catch (_: Exception) {
                // Handle empty or uncreated asset directories gracefully
            }
        }
        return options
    }

    /**
     * Automatically scans subfolders (e.g. sprites/top/) to discover available colors.
     */
    fun scanSubfolders(context: Context, relativeDir: String): List<String> {
        return try {
            val list = context.assets.list(relativeDir) ?: emptyArray()
            list.filter { !it.contains(".") }.map { folder ->
                folder.replace("_", " ")
                    .split(" ")
                    .joinToString(" ") { word -> word.replaceFirstChar { char -> char.uppercase() } }
            }
        } catch (_: Exception) {
            listOf("Khaki", "Gray", "Blue", "Green", "Purple", "Red")
        }
    }

    /**
     * Loads and caches a transparent 285x335 sprite layer bitmap.
     * Searches exact path first, then checks directory for style keyword matches.
     */
    fun loadSpriteBitmap(context: Context, assetPath: String, fallbackRow: Int = 0, fallbackCol: Int = 0): ImageBitmap {
        bitmapCache.get(assetPath)?.let { return it }

        // 1. Try loading directly from Assets directory
        try {
            context.assets.open(assetPath).use { stream: InputStream ->
                val bitmap = BitmapFactory.decodeStream(stream)
                if (bitmap != null) {
                    val imageBitmap = bitmap.asImageBitmap()
                    bitmapCache.put(assetPath, imageBitmap)
                    return imageBitmap
                }
            }
        } catch (_: Exception) {
            // Search directory for style keyword match (e.g. hoodie, cargo, jeans, t_shirt, jacket)
            val parentDir = assetPath.substringBeforeLast("/", "")
            val fileName = assetPath.substringAfterLast("/").removeSuffix(".png").lowercase()
            if (parentDir.isNotBlank()) {
                try {
                    val files = context.assets.list(parentDir) ?: emptyArray()
                    val styleKey = when {
                        fileName.contains("hoodie") -> "hoodie"
                        fileName.contains("t_shirt") || fileName.contains("tshirt") || fileName.contains("shirt") -> "t_shirt"
                        fileName.contains("jacket") -> "jacket"
                        fileName.contains("long") -> "long"
                        fileName.contains("sweater") -> "sweater"
                        fileName.contains("cargo") -> "cargo"
                        fileName.contains("jeans") -> "jeans"
                        fileName.contains("shorts") -> "shorts"
                        fileName.contains("joggers") -> "joggers"
                        else -> fileName
                    }
                    val matchedFile = files.firstOrNull {
                        (it.lowercase().contains(styleKey) || styleKey.contains(it.lowercase().removeSuffix(".png"))) && it.endsWith(".png", ignoreCase = true)
                    } ?: files.firstOrNull { it.endsWith(".png", ignoreCase = true) }

                    if (matchedFile != null) {
                        context.assets.open("$parentDir/$matchedFile").use { stream ->
                            val bitmap = BitmapFactory.decodeStream(stream)
                            if (bitmap != null) {
                                val imageBitmap = bitmap.asImageBitmap()
                                bitmapCache.put(assetPath, imageBitmap)
                                return imageBitmap
                            }
                        }
                    }
                } catch (_: Exception) {}
            }
        }

        // 2. Slice 285x335 card from master sprite sheet
        val gridBitmap = SpriteSheetProcessor.cropGridCard(context, fallbackRow, fallbackCol, 9)
        if (gridBitmap != null) {
            bitmapCache.put(assetPath, gridBitmap)
            return gridBitmap
        }

        // 3. Fallback blank 285x335 ARGB_8888 bitmap
        val emptyBitmap = Bitmap.createBitmap(285, 335, Bitmap.Config.ARGB_8888).asImageBitmap()
        bitmapCache.put(assetPath, emptyBitmap)
        return emptyBitmap
    }
}
