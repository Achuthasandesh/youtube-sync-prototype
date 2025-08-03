package com.android.youtubesyncplayer.mediaPlayer.server

import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress

class MediaWebSocketServer(
    port: Int,
    private val onMessageReceived: (String?) -> Unit,
    private val onConnectionOpened: (Int) -> Unit,
    private val onConnectionClosed: (Int) -> Unit,
    private val onConnectionError: (String) -> Unit
) : WebSocketServer(InetSocketAddress(port)) {
    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
        onConnectionOpened(connections.size)
    }
    override fun onMessage(conn: WebSocket?, message: String?) {
        println("Received: $message")
        onMessageReceived(message)
    }
    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        onConnectionClosed(connections.size)
    }
    override fun onError(conn: WebSocket?, ex: Exception?) {
        onConnectionError(ex?.message ?: "Unknown error occurred")
    }

    override fun onStart() {
        //
    }

    fun broadcastToClients(message: String) {
        connections.forEach { client ->
            client.send(message)
        }
    }
}