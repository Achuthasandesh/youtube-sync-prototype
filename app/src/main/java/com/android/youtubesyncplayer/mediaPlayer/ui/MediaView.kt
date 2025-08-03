package com.android.youtubesyncplayer.mediaPlayer.ui

import android.net.nsd.NsdServiceInfo
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.youtubesyncplayer.core.uiComponents.TextButton
import com.android.youtubesyncplayer.mediaPlayer.domain.MediaAction
import com.android.youtubesyncplayer.mediaPlayer.domain.MediaState
import com.android.youtubesyncplayer.nsd.NsdDiscoveryHelper
import com.android.youtubesyncplayer.theme.SUCCESS
import java.net.InetAddress

@Composable
fun MediaView(
    state: MediaState,
    dispatch: (action: MediaAction) -> Unit,
){
    val context = LocalContext.current
    val nsdHelper = remember { NsdDiscoveryHelper(context) }
    val discoveredServers = nsdHelper.discoveredServices

    LaunchedEffect(state.joinServer) {
        if (state.joinServer) {
            nsdHelper.startDiscovery()
        } else {
            nsdHelper.stopDiscovery()
        }
    }

    LaunchedEffect(state.createServer) {
        if (state.createServer) {
            dispatch(MediaAction.ServerAction.RegisterServerNsd(context))
        } else {
            dispatch(MediaAction.ServerAction.UnregisterServerNsd)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val url = remember { mutableStateOf("") }
        val errorMsg= remember { mutableStateOf("") }


        TextField(
            value = url.value,
            onValueChange = { value ->
                url.value = value
            },
            label = {
                Text("Paste Youtube Link/ Video ID")
            },
            modifier = Modifier
                .fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge
        )

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){
            TextButton(
                onClick = {
                    dispatch(MediaAction.VideoAction.UpdateVideoId(
                        extractYoutubeVideoId(url.value) ?:""))
                },
                enabled = url.value.isNotBlank(),
                text = "PLAY VIDEO"
            )
        }

        Spacer(Modifier.size(40.dp))


        if(!state.createServer){
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                TextButton(
                    onClick = {
                        errorMsg.value = ""
                        dispatch(MediaAction.ServerAction.CreateServer(8887))
                    },
                    text = "CREATE SERVER"
                )
                if(!state.joinServer) {
                    TextButton(
                        onClick = {
                            dispatch(MediaAction.ServerAction.JoinServer)
                        },
                        text = "JOIN SERVER"
                    )
                }
            }

            if(state.joinServer){
                errorMsg.value = ""
                NSDSelector(
                    discoveredServers = discoveredServers,
                    dispatch = dispatch,
                    onSuccess = {
                        errorMsg.value = ""
                    },
                    onError = {
                        errorMsg.value = it
                    },
                    serverUrl = state.serverUrl
                )
            }
        }

        if(state.isServer){
            state.serverUrl?.let {
                Text("SERVER STARTED AT : ${state.serverUrl}",color = SUCCESS)
                Text("TOTAL CONNECTIONS : ${state.totalServerConnections}",color = SUCCESS)
            }
        }


        if(state.createServer){
            errorMsg.value = ""
            TextButton(
                colors = ButtonDefaults.buttonColors().copy(
                    containerColor =Color.Red
                ),
                onClick = {
                    dispatch(MediaAction.ServerAction.StopServer)
                },
                text = "STOP SERVER",
            )
        }

        if(errorMsg.value.isNotBlank()){
            Text("ERROR: ${errorMsg.value}", color = MaterialTheme.colorScheme.error, maxLines = 5)
        }
    }
}

fun extractYoutubeVideoId(input: String): String? {
    val trimmed = input.trim()
    if (trimmed.matches(Regex("^[0-9A-Za-z_-]{11}$"))) {
        return trimmed
    }

    val regex = Regex("""(?:youtu\.be/|youtube\.com/(?:embed/|v/|watch\?v=))([\w-]{11})""")
    val matchResult = regex.find(trimmed)
    return matchResult?.groups?.get(1)?.value
}

@Composable
fun NSDSelector(
    discoveredServers: List<NsdServiceInfo>,
    dispatch: (MediaAction) -> Unit,
    onError: (String)-> Unit,
    onSuccess: () -> Unit,
    serverUrl: String?
) {
    if(serverUrl.isNullOrBlank()){
        if(discoveredServers.isEmpty()){
            Text("Searching for servers...", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn {
                itemsIndexed(items = discoveredServers) { _,serviceInfo ->
                    val host: String = if (Build.VERSION.SDK_INT >= 34) {
                        try {
                            val method = serviceInfo.javaClass.getMethod("getHostAddresses")
                            val addresses = method.invoke(serviceInfo) as? List<InetAddress>
                            addresses?.firstOrNull()?.hostAddress
                        } catch (e: Exception) {
                            serviceInfo.host?.hostAddress
                        }
                    } else {
                        serviceInfo.host?.hostAddress
                    } ?: "Unknown Host"
                    val port = serviceInfo.port

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                val serverAddress = "ws://$host:$port"
                                dispatch(
                                    MediaAction.ServerAction.ConnectToServer(
                                        serverAddress,
                                        onSuccess = onSuccess,
                                        onFailure = { err -> onError(err) }
                                    ))
                            }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = serviceInfo.serviceName, style = MaterialTheme.typography.bodyLarge)
                            Text(text = "IP: $host", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "Port: $port", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }


        Spacer(Modifier.height(16.dp))

        Text(text = "Or enter server IP manually:", style = MaterialTheme.typography.bodyMedium)
        val manualServerIp = remember { mutableStateOf("") }

        TextField(
            value = manualServerIp.value,
            onValueChange = { manualServerIp.value = it },
            label = { Text("Server IP:Port") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceBetween){
            TextButton(
                onClick = {
                    val serverAddress = manualServerIp.value.trim()
                    if (serverAddress.isNotBlank()) {
                        dispatch(
                            MediaAction.ServerAction.ConnectToServer(
                                ip = serverAddress,
                                onSuccess = onSuccess,
                                onFailure = { err -> onError(err)}
                            )
                        )
                    }
                },
                modifier = Modifier,
                enabled = manualServerIp.value.isNotBlank(),
                text = "Connect Manually"
            )

            TextButton(
                colors = ButtonDefaults.buttonColors().copy(
                    containerColor =Color.Red
                ),
                onClick = {
                    dispatch(MediaAction.ServerAction.CancelSearch)
                },
                modifier = Modifier,
                text = "Cancel Search"
            )
        }
    } else {
        Text("Connected to server at $serverUrl", style = MaterialTheme.typography.bodyLarge, color = SUCCESS)
        TextButton(
            text = "DISCONNECT",
            onClick = {
                dispatch(MediaAction.ServerAction.StopServer)
            },
        )
    }
}


@Preview(showBackground = true)
@Composable
fun Test(){
    MediaView(
        state = MediaState(),
        dispatch = { _ -> }
    )
}