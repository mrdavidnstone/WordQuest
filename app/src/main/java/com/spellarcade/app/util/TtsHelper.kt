package com.spellarcade.app.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class TtsHelper(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isReady = false
    private var pendingText: String? = null
    private var pendingSlow: Boolean = false

    private val TAG = "TtsHelper"

    init {
        Log.d(TAG, "Initializing TTS...")
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            Log.d(TAG, "TTS initialized successfully")
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w(TAG, "Locale.US not supported, trying Locale.ENGLISH")
                val fallbackResult = tts?.setLanguage(Locale.ENGLISH)
                if (fallbackResult != TextToSpeech.LANG_MISSING_DATA && fallbackResult != TextToSpeech.LANG_NOT_SUPPORTED) {
                    isReady = true
                } else {
                    Log.e(TAG, "Locale.ENGLISH also not supported")
                }
            } else {
                isReady = true
            }

            if (isReady) {
                Log.d(TAG, "TTS is ready for speaking")
                pendingText?.let {
                    Log.d(TAG, "Processing pending speech: $it")
                    speak(it, pendingSlow)
                    pendingText = null
                }
            }
        } else {
            Log.e(TAG, "TTS initialization failed with status: $status")
        }
    }

    fun speak(text: String, slow: Boolean = false) {
        if (!isReady) {
            Log.d(TAG, "Speak called but not ready. Queuing: $text")
            pendingText = text
            pendingSlow = slow
            return
        }
        
        Log.d(TAG, "Speaking: $text (slow=$slow)")
        tts?.setSpeechRate(if (slow) 0.5f else 1.0f)
        val result = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "spell_utterance")
        if (result == TextToSpeech.ERROR) {
            Log.e(TAG, "Error calling speak for: $text")
        }
    }

    fun shutdown() {
        Log.d(TAG, "Shutting down TTS")
        tts?.stop()
        tts?.shutdown()
    }
}
