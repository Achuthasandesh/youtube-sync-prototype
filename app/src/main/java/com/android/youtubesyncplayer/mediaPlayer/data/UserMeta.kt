package com.android.youtubesyncplayer.mediaPlayer.data

data class UserMeta(
    val userId: String = "",
    val userName: String = "",
    val isReadyToPlay: Boolean = false,
    val isServer: Boolean = false,
    val videoId: String = "",
    val startTime: Long = 0L,
)
