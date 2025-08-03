package com.android.youtubesyncplayer.helper

import com.android.youtubesyncplayer.App

object AppHelper {

    fun getAppContext(): App {
        return App.instance
    }

    fun getTrustedTimeInMillis(): Long {
        return App.instance.getTrustedTimeInMillis()
    }
}