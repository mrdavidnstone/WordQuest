package com.spellarcade.app.util

expect class TtsHelper() {
    fun speak(text: String, slow: Boolean = false)
    fun shutdown()
}
