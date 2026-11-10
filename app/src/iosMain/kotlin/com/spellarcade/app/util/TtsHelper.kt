package com.spellarcade.app.util

actual class TtsHelper actual constructor() {
    actual fun speak(text: String, slow: Boolean) {}
    actual fun shutdown() {}
}
