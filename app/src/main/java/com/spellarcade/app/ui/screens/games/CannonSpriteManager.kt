package com.spellarcade.app.ui.screens.games

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import org.json.JSONObject
import kotlin.math.abs
import kotlin.math.roundToInt

data class CannonSpriteData(
    val bitmap: ImageBitmap,
    val flipX: Boolean,
    val muzzle: Offset
)

class CannonSpriteManager private constructor(context: Context) {
    val pivot = Offset(154f, 298f)
    private val spriteMap = mutableMapOf<String, CannonSpriteData>()

    init {
        try {
            val jsonString = context.assets.open("games/pirate_blaster/animations/cannon_aiming/animation.json").bufferedReader().use { it.readText() }
            val json = JSONObject(jsonString)
            val frames = json.getJSONObject("frames")
            val bitmapCache = mutableMapOf<String, ImageBitmap>()

            for (key in frames.keys()) {
                val f = frames.getJSONObject(key)
                val file = "games/pirate_blaster/animations/cannon_aiming/" + f.getString("file")
                val flipX = f.optBoolean("flip_x", false)
                val m = f.getJSONArray("muzzle_px")
                val muzzle = Offset(m.getDouble(0).toFloat(), m.getDouble(1).toFloat())

                val bmp = bitmapCache.getOrPut(file) {
                    context.assets.open(file).use { stream ->
                        BitmapFactory.decodeStream(stream)?.asImageBitmap() ?: ImageBitmap(1, 1)
                    }
                }

                spriteMap[key] = CannonSpriteData(bmp, flipX, muzzle)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getSprite(yawIndex: Int, pitchIndex: Int): CannonSpriteData? {
        val yaws = listOf("far_left", "mid_left", "center", "mid_right", "far_right")
        val pitches = listOf("high", "upper_mid", "center", "lower_mid", "low")
        val key = "${yaws.getOrNull(yawIndex) ?: "center"}/${pitches.getOrNull(pitchIndex) ?: "center"}"
        return spriteMap[key]
    }

    companion object {
        private var instance: CannonSpriteManager? = null
        fun getInstance(context: Context): CannonSpriteManager {
            return instance ?: synchronized(this) {
                instance ?: CannonSpriteManager(context.applicationContext).also { instance = it }
            }
        }
    }
}

fun getAimIndices(aimX: Float, aimY: Float, currentYaw: Int, currentPitch: Int): Pair<Int, Int> {
    val rawYaw = (aimX.coerceIn(-1f, 1f) * 2f) + 2f // 0..4
    val rawPitch = 2f - (aimY.coerceIn(-1f, 1f) * 2f) // 0..4
    val hyst = 0.08f
    var yawI = currentYaw
    var pitchI = currentPitch
    if (abs(rawYaw - yawI) > 0.5f + hyst) yawI = rawYaw.roundToInt().coerceIn(0, 4)
    if (abs(rawPitch - pitchI) > 0.5f + hyst) pitchI = rawPitch.roundToInt().coerceIn(0, 4)
    return yawI to pitchI
}
