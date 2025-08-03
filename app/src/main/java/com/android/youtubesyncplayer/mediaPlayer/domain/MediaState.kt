package com.android.youtubesyncplayer.mediaPlayer.domain

import com.android.youtubesyncplayer.mediaPlayer.data.UserMeta
import com.android.youtubesyncplayer.mediaPlayer.server.MediaWebSocketServer


data class MediaState(
    val createServer: Boolean = false,
    val server: MediaWebSocketServer? = null,
    val serverUrl: String? = null,
    val isServer: Boolean = true,
    val playVideo: Boolean = false,
    val joinServer: Boolean = false,
    val clientSocket: okhttp3.WebSocket? = null,
    val navigationAction: MediaAction.NavigateAction? = null,
    val videoId: String? = null,
    val serverReady: Boolean = false,
    val clientReady: Boolean = false,
    val connectedUsers: List<UserMeta> = emptyList(),
    val totalServerConnections: Int = 0,
    val videoStartTime: Long = 0L,
)
