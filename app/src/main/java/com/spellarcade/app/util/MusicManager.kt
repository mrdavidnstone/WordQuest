package com.spellarcade.app.util

import android.content.Context
import android.media.MediaPlayer
import android.util.Log

object MusicManager {
    private const val TAG = "MusicManager"
    private var mediaPlayer: MediaPlayer? = null
    private var currentAssetPath: String? = null
    private var isPaused = false

    fun playMusic(context: Context, assetPath: String) {
        val appContext = context.applicationContext

        // If already playing the requested track and actively playing, do nothing
        if (currentAssetPath == assetPath && mediaPlayer != null && mediaPlayer?.isPlaying == true) {
            return
        }

        // If resuming the same track after a pause
        if (currentAssetPath == assetPath && mediaPlayer != null && isPaused) {
            try {
                mediaPlayer?.start()
                isPaused = false
                Log.d(TAG, "Resumed playing track: $assetPath")
                return
            } catch (e: Exception) {
                Log.e(TAG, "Error resuming music: ${e.message}")
            }
        }

        // Clean up previous player before starting a new track
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
            Log.d(TAG, "Started playing music track: $assetPath")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play music track: $assetPath, error: ${e.message}", e)
        }
    }

    fun pauseMusic() {
        if (mediaPlayer != null && mediaPlayer?.isPlaying == true) {
            try {
                mediaPlayer?.pause()
                isPaused = true
                Log.d(TAG, "Music paused")
            } catch (e: Exception) {
                Log.e(TAG, "Error pausing music: ${e.message}")
            }
        }
    }

    fun resumeMusic() {
        if (mediaPlayer != null && isPaused) {
            try {
                mediaPlayer?.start()
                isPaused = false
                Log.d(TAG, "Music resumed")
            } catch (e: Exception) {
                Log.e(TAG, "Error resuming music: ${e.message}")
            }
        }
    }

    fun stopMusic() {
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
