package com.android.youtubesyncplayer.mediaPlayer.client

import android.util.Log
import okhttp3.*
import okio.ByteString

class MediaWebSocketClient(
    private val onMessageReceived: (String) -> Unit,
    private val onConnectionOpened: () -> Unit,
    private val onConnectionClosed: () -> Unit,
    private val onConnectionError: (String) -> Unit
) : WebSocketListener() {

    private val TAG = "MyWebSocketListener"

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d(TAG, "WebSocket connection opened")
        onConnectionOpened()
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d(TAG, "Message received: $text")
        onMessageReceived(text)
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        Log.d(TAG, "Bytes received: ${bytes.hex()}")
        onMessageReceived(bytes.utf8())
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Log.d(TAG, "WebSocket closing: $code / $reason")
        webSocket.close(1000, null)
        onConnectionClosed()
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        Log.d(TAG, "WebSocket closed: $code / $reason")
        onConnectionClosed()
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e(TAG, "WebSocket connection failed", t)
        onConnectionError(t.message ?: "Unknown error occurred")
    }
}
