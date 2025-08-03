package com.android.youtubesyncplayer.mediaPlayer.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.android.youtubesyncplayer.mediaPlayer.domain.MediaAction
import com.android.youtubesyncplayer.mediaPlayer.domain.MediaState
import kotlinx.coroutines.flow.StateFlow

@Composable
fun MediaViewNavigationSideEffect(
    state: StateFlow<MediaState>,
    navController: NavHostController
) {
    val stateValue by state.collectAsStateWithLifecycle()
    LaunchedEffect(stateValue.navigationAction) {
        stateValue.navigationAction?.let {
            when(it){
                is MediaAction.NavigateAction.ToVideoScreen -> {
                    navController.navigate("VIDEO_SCREEN") {
                        launchSingleTop = true
                    }
                }
            }
        }
    }

}