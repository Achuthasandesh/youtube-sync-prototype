package com.android.youtubesyncplayer.core.uiComponents

import android.os.CountDownTimer
import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.youtubesyncplayer.helper.AppHelper

@Composable
fun TextButton(
    modifier: Modifier = Modifier,
    text: String,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current
    Button(
        colors = colors,
        onClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        enabled = enabled,
        modifier = modifier
    ) {
        Text(text)
    }
}


@Composable
@Preview(showBackground = true)
fun Test(){
    val timerCount = 10000L
    val timeRemaining = remember { mutableFloatStateOf(0F) }

    DisposableEffect(Unit) {
        object : CountDownTimer(timerCount,1000){
            override fun onFinish() {

            }

            override fun onTick(p0: Long) {
                timeRemaining.floatValue = p0.toFloat()
            }
        }.start()

        onDispose { }

    }

    DrawCanvasProgress(
        timerCount.toFloat(),
        timeRemaining.floatValue
    )
}

@Composable
fun DrawCanvasProgress(
    totalTimer: Float,
    remainingTimer: Float
){
    val dragProgress by animateFloatAsState(
        targetValue = (totalTimer/remainingTimer)/100
    )
    Column(modifier = Modifier.background(Color.White).padding(16.dp)){
        Canvas(
            modifier = Modifier
                .size(400.dp)
        ){
            drawArc(
                brush = SolidColor(Color.Gray),
                startAngle = 120F,
                sweepAngle = 300F,
                useCenter = false,
                style = Stroke(35f, cap = StrokeCap.Round)
            )

            val convertedValue = dragProgress * 300F

            drawArc(
                brush = SolidColor(Color.Black),
                startAngle = 120F,
                sweepAngle = convertedValue,
                useCenter = false,
                style = Stroke(35f, cap = StrokeCap.Round)
            )

            drawIntoCanvas {
                val paint = Paint().asFrameworkPaint()
                paint.apply {
                    isAntiAlias = true
                    textSize = 55F
                    textAlign = android.graphics.Paint.Align.CENTER
                }

                it.nativeCanvas.drawText(
                    "${ ((totalTimer - dragProgress)/100).toInt() }%",
                    size.width/2,
                    size.height/4,
                    paint
                )
            }
        }

//        Slider(
//            modifier = Modifier.fillMaxWidth(),
//            value = progress,
//            onValueChange = { value->
//                progress = value
//            }
//        )
    }
}


@Preview
@Composable
fun Test2() {
    val timerCount = 10000L  // total duration in millis
    val timeRemaining = remember { mutableStateOf(timerCount.toFloat()) }

    DisposableEffect(Unit) {
        val timer = object : CountDownTimer(timerCount, 16) {  // 16ms for smoother ticks (~60fps)
            override fun onFinish() {
                timeRemaining.value = 0f
            }

            override fun onTick(millisUntilFinished: Long) {
                timeRemaining.value = millisUntilFinished.toFloat()
            }
        }
        timer.start()
        onDispose {
            timer.cancel()
        }
    }

    // normalized progress fraction (0f to 1f)
    val progress = 1f - (timeRemaining.value / timerCount.toFloat())

    DrawCanvasProgress2(
        progress = progress,
        secondsRemaining = (timeRemaining.value / 1000f).coerceAtLeast(0f)
    )
}

@Composable
fun DrawCanvasProgress2(
    progress: Float,
    secondsRemaining: Float
) {
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
            modifier = Modifier.size(400.dp),
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
