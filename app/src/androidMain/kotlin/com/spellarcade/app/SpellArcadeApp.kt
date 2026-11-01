package com.spellarcade.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SpellArcadeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        var instance: SpellArcadeApp? = null
            private set
    }
}
