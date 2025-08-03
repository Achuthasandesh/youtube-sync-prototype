package com.android.youtubesyncplayer.mediaPlayer.domain

import android.content.Context
import com.android.youtubesyncplayer.core.Action
import com.android.youtubesyncplayer.mediaPlayer.server.MediaWebSocketServer
import okhttp3.WebSocket

sealed interface MediaAction: Action {

    sealed interface ServerAction: MediaAction {
        data class CreateServer(val port: Int): ServerAction
        data class UpdateServerDataAtState(val server: MediaWebSocketServer, val port: Int):
            ServerAction
        data object StopServer: ServerAction
        data object StopServerAtState: ServerAction
        data object JoinServer: ServerAction
        data class ConnectToServer(val ip: String, val onSuccess: () -> Unit, val onFailure: (String) -> Unit):
            ServerAction
        data class UpdateWebSocketClientAtState(val webSocket: WebSocket,val url: String): ServerAction
        data class RegisterServerNsd(val context: Context): ServerAction
        data object UnregisterServerNsd: ServerAction
        data object CancelSearch: ServerAction
        data class UpdateConnectionCount(val count: Int): ServerAction
    }


    sealed interface NavigateAction: MediaAction {
        data object ToVideoScreen: NavigateAction
    }

    sealed interface VideoAction: MediaAction {
        data class UpdateVideoId(val videoId: String): VideoAction
        data class UpdateVideoIdAtState(val videoId: String): VideoAction
        data object PlayVideoAtState: VideoAction
        data object StopVideoAtState: VideoAction
        data object UpdateNavigationState: VideoAction
        data class ServerReadyAction(val videoId: String): VideoAction
        data class UpdateServerReadyAtState(val startTime: Long): VideoAction
        data class ClientReadyAction(val videoId: String): VideoAction
        data object UpdateClientReadyAtState: VideoAction

    }
}
