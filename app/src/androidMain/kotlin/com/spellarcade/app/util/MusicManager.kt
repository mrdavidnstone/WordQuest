package com.spellarcade.app.util

import android.media.MediaPlayer
import android.util.Log
import com.spellarcade.app.SpellArcadeApp

actual object MusicManager {
    private const val TAG = "MusicManager"
    private var mediaPlayer: MediaPlayer? = null
    private var currentAssetPath: String? = null
    private var isPaused = false

    actual fun playMusic(assetPath: String) {
        val appContext = SpellArcadeApp.instance?.applicationContext ?: return

        if (currentAssetPath == assetPath && mediaPlayer != null && mediaPlayer?.isPlaying == true) {
            return
        }

        if (currentAssetPath == assetPath && mediaPlayer != null && isPaused) {
            try {
                mediaPlayer?.start()
                isPaused = false
                return
            } catch (e: Exception) {
                Log.e(TAG, "Error resuming music: ${e.message}")
            }
        }

        stopMusic()

        try {
            val descriptor = appContext.assets.openFd(assetPath)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
                descriptor.close()
                isLooping = true
                setVolume(0.6f, 0.6f)
                prepare()
                start()
            }
            currentAssetPath = assetPath
            isPaused = false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play music track: $assetPath, error: ${e.message}", e)
        }
    }

    actual fun pauseMusic() {
        if (mediaPlayer != null && mediaPlayer?.isPlaying == true) {
            try {
                mediaPlayer?.pause()
                isPaused = true
            } catch (e: Exception) {
                Log.e(TAG, "Error pausing music: ${e.message}")
            }
        }
    }

    actual fun resumeMusic() {
        if (mediaPlayer != null && isPaused) {
            try {
                mediaPlayer?.start()
                isPaused = false
            } catch (e: Exception) {
                Log.e(TAG, "Error resuming music: ${e.message}")
            }
        }
    }

    actual fun stopMusic() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping music: ${e.message}")
        } finally {
            mediaPlayer = null
            currentAssetPath = null
            isPaused = false
        }
    }
}
