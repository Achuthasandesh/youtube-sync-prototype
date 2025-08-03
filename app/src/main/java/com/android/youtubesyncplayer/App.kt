package com.android.youtubesyncplayer

import android.app.Application
import com.android.youtubesyncplayer.trustedTime.TrustedTimeImpl

class App: Application() {
    companion object {
        lateinit var instance: App
    }

    private var trustedTimeImpl: TrustedTimeImpl? = null

    init{
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        trustedTimeImpl = TrustedTimeImpl(
            context = this
        )
    }

    fun getTrustedTimeInMillis(): Long {
        return trustedTimeImpl?.getCurrentTimeMillis() ?: System.currentTimeMillis()
    }
}