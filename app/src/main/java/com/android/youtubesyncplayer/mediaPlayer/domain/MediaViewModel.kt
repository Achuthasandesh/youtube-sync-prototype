package com.android.youtubesyncplayer.mediaPlayer.domain

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.android.youtubesyncplayer.core.Action
import com.android.youtubesyncplayer.helper.AppHelper
import com.android.youtubesyncplayer.mediaPlayer.client.MediaWebSocketClient
import com.android.youtubesyncplayer.mediaPlayer.data.UserMeta
import com.android.youtubesyncplayer.mediaPlayer.server.MediaWebSocketServer
import com.android.youtubesyncplayer.nsd.NsdServerRegistrar
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException


class MediaViewModel: ViewModel() {
    private val tag = "MediaViewModel"
    private val _mediaState: MutableStateFlow<MediaState> = MutableStateFlow(MediaState())
    val state: StateFlow<MediaState> = _mediaState

    private val gson = Gson()

    private var nsdServerRegistrar: NsdServerRegistrar? =null


    fun dispatch(action: Action) {
        handleSideEffect(action)
        handleReducer(action)
    }


    private fun handleSideEffect(action: Action){
        viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            withContext(Dispatchers.IO){
                when (action) {
                    is MediaAction.ServerAction.CreateServer -> {
                        state.value.server?.let {
                            MediaAction.ServerAction.UpdateServerDataAtState(
                                it,
                                action.port
                            )
                        } ?: run {
                            val server = MediaWebSocketServer(
                                action.port,
                                onMessageReceived = { msg->
                                    try{
                                        val videoMeta = Gson().fromJson(msg, UserMeta::class.java)
                                        videoMeta?.let {
                                            Log.d(tag, "SERVER RECEIVED: $msg")
                                            if(it.isReadyToPlay) handleReducer(MediaAction.VideoAction.UpdateClientReadyAtState)
                                        }
                                    }catch(e: Exception) {
                                        Log.e(tag, "Error parsing video metadata: ${e.message}")
                                    }

                                },
                                onConnectionOpened = {
                                    handleReducer(MediaAction.ServerAction.UpdateConnectionCount(it))
                                    Log.d(tag, "COUNT : $it")
                                },
                                onConnectionError = {},
                                onConnectionClosed = {
                                    handleReducer(MediaAction.ServerAction.UpdateConnectionCount(it))
                                }
                            )
                            server.start()
                            handleReducer(
                                MediaAction.ServerAction.UpdateServerDataAtState(
                                    server,
                                    action.port
                                )
                            )
                        }
                    }

                    is MediaAction.ServerAction.StopServer -> {
                        state.value.server?.stop()
                        handleReducer(MediaAction.ServerAction.StopServerAtState)
                    }

                    is MediaAction.ServerAction.ConnectToServer -> {
                        try{
                            val url = action.ip
                            val request = Request.Builder().url(url).build()
                            val client = OkHttpClient()

                            val listener = MediaWebSocketClient(
                                onMessageReceived = { msg->
                                    try{
                                        val videoMeta = Gson().fromJson(msg, UserMeta::class.java)
                                        videoMeta?.let {
                                            Log.d(tag, "CLIENT RECEIVED: $msg")
                                            if(it.isReadyToPlay) handleReducer(MediaAction.VideoAction.UpdateServerReadyAtState(startTime = it.startTime))
                                        }?: run {
                                            handleReducer(MediaAction.VideoAction.UpdateVideoIdAtState(msg))
                                        }
                                    } catch(e: Exception) {
                                        handleReducer(MediaAction.VideoAction.UpdateVideoIdAtState(msg))
                                    }

                                },
                                onConnectionError = {
                                    action.onFailure(it)
                                    Log.d(tag, "CONNECTION ERROR: $it")
                                },
                                onConnectionClosed = {
                                    Log.d(tag, "CLOSED")
                                },
                                onConnectionOpened = {
                                    action.onSuccess()
                                    Log.d(tag, "CONNECTION REOPENED")
                                }
                            )
                            val socket = client.newWebSocket(request, listener)
                            handleReducer(MediaAction.ServerAction.UpdateWebSocketClientAtState(socket, url = url))
                        }catch (e: Exception){
                            Log.d(tag, "${e.message}")
                            action.onFailure(e.message?:"Unknown error")
                        }

                    }

                    is MediaAction.VideoAction.UpdateVideoId -> {
                        state.value.server?.apply {
                            broadcastToClients(action.videoId)
                        }
                        handleReducer(MediaAction.VideoAction.UpdateVideoIdAtState(action.videoId))
                    }

                    is MediaAction.ServerAction.RegisterServerNsd -> {
                        registerServerNsd(action.context)
                    }

                    is MediaAction.ServerAction.UnregisterServerNsd -> {
                        unregisterServerNsd()
                    }

                    is MediaAction.ServerAction.CancelSearch -> {
                        unregisterServerNsd()
                    }

                    is MediaAction.VideoAction.ServerReadyAction -> {
                        val startTime = AppHelper.getTrustedTimeInMillis() + 5000L
                        state.value.server?.broadcastToClients(
                            gson.toJson(
                                UserMeta(
                                    videoId = action.videoId,
                                    isReadyToPlay = true,
                                    startTime = startTime
                                )
                            )
                        )
                        handleReducer(MediaAction.VideoAction.UpdateServerReadyAtState(startTime))
                    }

                    is MediaAction.VideoAction.ClientReadyAction -> {
                        state.value.clientSocket?.send(
                            gson.toJson(
                                UserMeta(
                                    videoId = state.value.videoId?:"",
                                    isReadyToPlay = true
                                )
                            )
                        )
                        handleReducer(MediaAction.VideoAction.UpdateClientReadyAtState)
                    }

                    else -> {
                        // Handle other actions if needed
                    }
                }
            }
        }

    }

    private fun registerServerNsd(context: Context) {
        nsdServerRegistrar = NsdServerRegistrar(context, "MyServer", 8887)
        nsdServerRegistrar?.registerService()
    }

    private fun unregisterServerNsd() {
        nsdServerRegistrar?.unregisterService()
        nsdServerRegistrar = null
    }

    private fun handleReducer(action: Action){
        when (action) {
            is MediaAction.ServerAction.CreateServer -> {
                _mediaState.value = _mediaState.value.copy(createServer = true)
            }

            is MediaAction.ServerAction.UpdateServerDataAtState -> {
                _mediaState.value = _mediaState.value.copy(
                    server = action.server,
                    serverUrl = "${getLocalIpAddress() ?:""}:${action.port}",
                    isServer = true,
                    joinServer = false
                )
            }

            is MediaAction.ServerAction.StopServerAtState -> {
                _mediaState.value = _mediaState.value.copy(
                    server = null,
                    serverUrl = null,
                    createServer = false,
                    isServer = false,
                    connectedUsers = emptyList()
                )
            }

            is MediaAction.ServerAction.JoinServer -> {
                _mediaState.value = _mediaState.value.copy(joinServer = true)
            }

            is MediaAction.ServerAction.UpdateWebSocketClientAtState -> {
                _mediaState.value = _mediaState.value.copy(clientSocket = action.webSocket, serverUrl = action.url, isServer = false)
            }

            is MediaAction.VideoAction.UpdateVideoIdAtState -> {
                _mediaState.value = _mediaState.value.copy(
                    videoId = action.videoId,
                    navigationAction = MediaAction.NavigateAction.ToVideoScreen,
                    serverReady = false,
                    clientReady = false
                )
            }

            is MediaAction.VideoAction.UpdateNavigationState -> {
                _mediaState.value = _mediaState.value.copy(
                    navigationAction = null
                )
            }

            is MediaAction.VideoAction.UpdateServerReadyAtState -> {
                _mediaState.value = _mediaState.value.copy(
                    serverReady = true,
                    videoStartTime = action.startTime
                )
            }

            is MediaAction.VideoAction.UpdateClientReadyAtState -> {
                _mediaState.value = _mediaState.value.copy(clientReady = true)
            }

            is MediaAction.ServerAction.CancelSearch -> {
                _mediaState.value = _mediaState.value.copy(joinServer = false, serverUrl = null, clientSocket = null)
            }

            is MediaAction.ServerAction.UpdateConnectionCount -> {
                _mediaState.value = _mediaState.value.copy(
                    totalServerConnections = action.count,
//                    connectedUsers = state.value.server?.getConnectedUsers() ?: emptyList()
                )
            }

            else -> {
                // Handle other actions if needed
            }
        }
    }
}

fun getLocalIpAddress(): String? {
    try {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        for (intf in interfaces) {
            val addrs = intf.inetAddresses
            for (addr in addrs) {
                if (!addr.isLoopbackAddress && addr is InetAddress) {
                    val ip = addr.hostAddress
                    if ((ip?.indexOf(':')?:0) < 0) { // IPv4 check
                        return ip
                    }
                }
            }
        }
    } catch (ex: SocketException) {
        ex.printStackTrace()
    }
    return null
}