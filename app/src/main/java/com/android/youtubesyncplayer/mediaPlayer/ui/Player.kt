package com.android.youtubesyncplayer.mediaPlayer.ui

import android.os.CountDownTimer
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import com.android.youtubesyncplayer.helper.AppHelper
import com.android.youtubesyncplayer.mediaPlayer.domain.MediaAction
import com.android.youtubesyncplayer.mediaPlayer.domain.MediaState
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Composable
fun YouTubePlayerCompose(
    mediaState: MediaState,
    dispatch: (action: MediaAction) -> Unit,
    onBackPress: () -> Unit
) {
    val tag = "YouTubePlayerCompose"
    val context = LocalContext.current
    val lifecycleOwner = context as? LifecycleOwner
        ?: throw IllegalStateException("Not in a LifecycleOwner context!")

    DisposableEffect(mediaState.videoId) {
        if(mediaState.videoId == null) onBackPress()
        onDispose {  }
    }

    BackHandler {
        onBackPress()
    }

    var player by remember { mutableStateOf<YouTubePlayer?>(null) }
    var playerState by remember {
        mutableStateOf(State.UNKNOWN)
    }
    var timerCount by remember { mutableStateOf(0L) }
    var timeRemaining by remember { mutableStateOf(0F) }

    LaunchedEffect(mediaState.clientReady,playerState) {
        if(mediaState.clientReady && playerState == State.READY){
            mediaState.server?.let {
                dispatch(
                    MediaAction.VideoAction.ServerReadyAction(mediaState.videoId?:"")
                )
            }
        }
    }

    LaunchedEffect(mediaState.videoStartTime) {
        mediaState.videoStartTime?.let { ts ->
            val delay = ts - AppHelper.getTrustedTimeInMillis()
            if (delay > 0) {
                kotlinx.coroutines.delay(delay)
            }
            player?.seekTo(0f)
            player?.play()
        }
    }

//    DisposableEffect(mediaState.videoStartTime) {
//        val currentTime = AppHelper.getTrustedTimeInMillis()
//        if(mediaState.videoStartTime > currentTime && player != null) {
//            timerCount = mediaState.videoStartTime - currentTime
//            object : CountDownTimer(timerCount,1000){
//                override fun onFinish() {
//                    player?.play()
//                }
//
//                override fun onTick(p0: Long) {
//                    timeRemaining = p0.toFloat()
//                    Log.d(tag, "STARTS IN : ${p0/1000}")
//
//                    Log.d(tag, "TT : ${p0/1000} :::: ${AppHelper.getTrustedTimeInMillis()}")
//                }
//            }.start()
//        }
//        onDispose { }
//    }
    Column{
        AndroidView(
            modifier = Modifier,
            factory = { ctx ->
                YouTubePlayerView(ctx).apply {
                    lifecycleOwner.lifecycle.addObserver(this)

                    addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                        override fun onReady(youTubePlayer: YouTubePlayer) {
                            youTubePlayer.cueVideo(mediaState.videoId?:"", startSeconds = 0f)
                            player = youTubePlayer
                        }
                        override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                            when (state) {
                                PlayerConstants.PlayerState.UNKNOWN -> {

                                }
                                PlayerConstants.PlayerState.UNSTARTED -> {
                                    initiatePlay(youTubePlayer)
                                    mediaState.clientSocket?.apply {
                                        dispatch(
                                            MediaAction.VideoAction.ClientReadyAction(mediaState.videoId?:"")
                                        )
                                    }
                                    playerState = State.READY
                                }


                                PlayerConstants.PlayerState.VIDEO_CUED -> {
                                    initiatePlay(youTubePlayer)
                                    mediaState.clientSocket?.apply {
                                        dispatch(
                                            MediaAction.VideoAction.ClientReadyAction(mediaState.videoId?:"")
                                        )
                                    }
                                    playerState = State.READY
                                }
                                else -> {}
                            }
                        }
                    })
                }
            },
            update = { _ ->
            }
        )

//        Spacer(Modifier.size(24.dp))
//
//        if(timeRemaining> 0 && timerCount > 0) {
//            val progress = 1f - (timeRemaining / timerCount)
//
//            DrawCanvasProgress(
//                progress = progress,
//                secondsRemaining = (timeRemaining / 1000f).coerceAtLeast(0f)
//            )
//        }
    }

}

private fun initiatePlay(youTubePlayer: YouTubePlayer) {
    youTubePlayer.seekTo(10F)
    youTubePlayer.play()
    youTubePlayer.seekTo(0F)
    youTubePlayer.play()
    youTubePlayer.pause()
}

enum class State{
    READY,
    UNKNOWN
}

@Composable
fun DrawCanvasProgress(
    progress: Float,
    secondsRemaining: Float
){
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500, easing = LinearEasing)
    )

    Column(
        modifier = Modifier
            .background(Color.White)
            .padding(16.dp)
    ) {
        Canvas(
            modifier = Modifier.size(180.dp),
        ) {
            val sweepAngleMax = 300f
            val startAngle = 120f
            val strokeWidth = 35f

            // Background arc (full duration)
            drawArc(
                brush = SolidColor(Color.Gray),
                startAngle = startAngle,
                sweepAngle = sweepAngleMax,
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )

            // Foreground arc (progress portion)
            drawArc(
                brush = SolidColor(Color.Black),
                startAngle = startAngle,
                sweepAngle = sweepAngleMax * animatedProgress,
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )

            // Draw the countdown text (seconds remaining)
            drawIntoCanvas {
                val paint = Paint().asFrameworkPaint()
                paint.isAntiAlias = true
                paint.textSize = 55f
                paint.textAlign = android.graphics.Paint.Align.CENTER

                val text = secondsRemaining.toInt().toString()

                it.nativeCanvas.drawText(
                    text,
                    size.width / 2,
                    size.height / 2,
                    paint
                )
            }
        }
    }
}