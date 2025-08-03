package com.android.youtubesyncplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.android.youtubesyncplayer.mediaPlayer.domain.MediaViewModel
import com.android.youtubesyncplayer.mediaPlayer.ui.MediaViewNavigationGraph
import com.android.youtubesyncplayer.mediaPlayer.ui.MediaViewNavigationSideEffect
import com.android.youtubesyncplayer.theme.YoutubeSyncPlayerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val viewModel: MediaViewModel = viewModel()
            YoutubeSyncPlayerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(Modifier.fillMaxSize().padding(innerPadding)){
                        MainContentScreen(
                            navigationGraph = {
                                MediaViewNavigationGraph(
                                    navHostController = navController,
                                    stateFlow = viewModel.state,
                                    dispatch = viewModel::dispatch
                                )
                            }
                        ) {
                            MediaViewNavigationSideEffect(
                                viewModel.state,
                                navController
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainContentScreen(
    navigationGraph: (@Composable () -> Unit),
    registerNavigationSideEffect: (@Composable () -> Unit),
) {
    navigationGraph()
    registerNavigationSideEffect()
}