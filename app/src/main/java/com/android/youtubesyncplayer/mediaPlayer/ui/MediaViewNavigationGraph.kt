package com.android.youtubesyncplayer.mediaPlayer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.android.youtubesyncplayer.mediaPlayer.domain.MediaAction
import com.android.youtubesyncplayer.mediaPlayer.domain.MediaState
import kotlinx.coroutines.flow.StateFlow

@Composable
fun MediaViewNavigationGraph(
    navHostController: NavHostController,
    stateFlow: StateFlow<MediaState>,
    dispatch: (action: MediaAction) -> Unit
) {
    val state by stateFlow.collectAsStateWithLifecycle()
    val url = remember{mutableStateOf("")}
    NavHost(navController = navHostController, startDestination = "HOME") {
        composable("HOME"){
            MediaView(
                state,
                dispatch
            )
        }

        composable("VIDEO_SCREEN") {
            Box(Modifier.fillMaxSize().padding(50.dp)){
                YouTubePlayerCompose(
                    state,
                    dispatch,
                    onBackPress = {
                        navHostController.popBackStack()
                        dispatch(MediaAction.VideoAction.UpdateNavigationState)
                    }
                )
            }
        }
    }

}