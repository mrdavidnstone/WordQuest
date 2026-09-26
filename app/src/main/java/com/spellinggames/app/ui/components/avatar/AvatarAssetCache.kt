package com.spellinggames.app.ui.components.avatar

import android.content.Context
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap

object AvatarAssetCache {

    private val cacheSize = (Runtime.getRuntime().maxMemory() / 1024 / 8).toInt()
    private val bitmapCache = object : LruCache<String, ImageBitmap>(cacheSize.coerceAtLeast(1024 * 16)) {
        override fun sizeOf(key: String, value: ImageBitmap): Int {
            return value.width * value.height * 4 / 1024
        }
    }

    private val prototypeCache = ConcurrentHashMap<String, ImageBitmap>()

    fun getLayerBitmap(context: Context, spec: AvatarLayerSpec, isGirl: Boolean): ImageBitmap {
        val cacheKey = spec.assetPath
        
        // 1. Check LRU Cache
        bitmapCache.get(cacheKey)?.let { return it }

        // 2. Try loading from Context Assets
        try {
            context.assets.open(spec.assetPath).use { stream: InputStream ->
                val bitmap = BitmapFactory.decodeStream(stream)
                if (bitmap != null) {
                    val imageBitmap = bitmap.asImageBitmap()
                    bitmapCache.put(cacheKey, imageBitmap)
                    return imageBitmap
                }
            }
        } catch (e: Exception) {
            // Asset file not on disk yet
        }

        // 3. Try slicing layer from sprite.png
        val spriteLayer = SpriteSheetProcessor.getLayerBitmap(context, spec, isGirl)
        if (spriteLayer != null) {
            bitmapCache.put(cacheKey, spriteLayer)
            return spriteLayer
        }

        // 4. Fallback to Prototype Generator (1024x1024 RGBA transparent canvas layer)
        val protoKey = "${spec.layerType.name}_${spec.fallbackKey}_${spec.tintHex}_$isGirl"
        return prototypeCache.getOrPut(protoKey) {
            val protoBitmap = AvatarPrototypeGenerator.generateLayer(
                layerType = spec.layerType,
                spec = spec,
                isGirl = isGirl
            )
            bitmapCache.put(cacheKey, protoBitmap)
            protoBitmap
        }
    }

    fun clear() {
        bitmapCache.evictAll()
        prototypeCache.clear()
    }
}
