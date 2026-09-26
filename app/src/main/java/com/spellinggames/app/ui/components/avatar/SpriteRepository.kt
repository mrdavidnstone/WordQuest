package com.spellinggames.app.ui.components.avatar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
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

    /**
     * Automatically scans an asset directory for PNG files.
     * Source of truth = Asset File System.
     */
    fun scanDirectory(context: Context, relativeDir: String): List<SpriteOption> {
        val options = mutableListOf<SpriteOption>()
        try {
            val fileList = context.assets.list(relativeDir) ?: emptyArray()
            fileList.filter { it.endsWith(".png", ignoreCase = true) }.forEach { fileName ->
                val cleanName = fileName.removeSuffix(".png")
                    .replace("_", " ")
                    .split(" ")
                    .joinToString(" ") { word -> word.replaceFirstChar { char -> char.uppercase() } }

                options.add(
                    SpriteOption(
                        id = fileName,
                        displayName = cleanName,
                        fullAssetPath = "$relativeDir/$fileName"
                    )
                )
            }
        } catch (_: Exception) {
            // Handle empty or uncreated asset directories gracefully
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
