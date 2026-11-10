package com.spellarcade.app.util

expect object MusicManager {
    fun playMusic(assetPath: String)
    fun pauseMusic()
    fun resumeMusic()
    fun stopMusic()
}
