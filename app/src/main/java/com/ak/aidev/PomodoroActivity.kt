package com.ak.aidev

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ak.aidev.ui.theme.AiDevTheme
import kotlinx.coroutines.delay

class PomodoroActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AiDevTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PomodoroTimer()
                }
            }
        }
    }
}

@Composable
fun PomodoroTimer() {
    var totalTime by remember { mutableStateOf(25 * 60) }
    var remainingTime by remember { mutableStateOf(totalTime) }
    var isRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning, remainingTime) {
        if (isRunning && remainingTime > 0) {
            delay(1000)
            remainingTime--
        } else if (remainingTime == 0) {
            isRunning = false
            // Here you can add logic for what happens when the timer finishes
        }
    }

    val progress by animateFloatAsState(
        targetValue = if (totalTime > 0) remainingTime.toFloat() / totalTime.toFloat() else 0f,
        label = "progress"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(300.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = Color.LightGray,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 25f, cap = StrokeCap.Round),
                )
                drawArc(
                    color = Color(0xFFFF5722),
                    startAngle = -90f,
                    sweepAngle = 360 * progress,
                    useCenter = false,
                    style = Stroke(width = 25f, cap = StrokeCap.Round),
                )
            }
            Text(
                text = "${remainingTime / 60}:${String.format("%02d", remainingTime % 60)}",
                fontSize = 64.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = { isRunning = true }) {
                Text("Start")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = { isRunning = false }) {
                Text("Pause")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = {
                isRunning = false
                remainingTime = totalTime
            }) {
                Text("Reset")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Button(onClick = { totalTime = 25 * 60; remainingTime = totalTime }) {
                Text("Pomodoro (25 min)")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = { totalTime = 5 * 60; remainingTime = totalTime }) {
                Text("Short Break (5 min)")
            }
        }
    }
}
