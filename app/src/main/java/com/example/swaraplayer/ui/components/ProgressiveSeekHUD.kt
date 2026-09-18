package com.example.swaraplayer.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun Modifier.progressiveTapSeek(
    enabled: Boolean = true,
    onSingleTap: () -> Unit,
    onSeekAccumulated: (forward: Boolean, totalSeconds: Int) -> Unit,
    onSeekEnd: () -> Unit,
): Modifier {
    if (!enabled) return this

    val coroutineScope = rememberCoroutineScope()
    var tapJob by remember { mutableStateOf<Job?>(null) }
    var tapCount by remember { mutableIntStateOf(0) }
    var isForward by remember { mutableStateOf(true) }

    return this.pointerInput(Unit) {
        val tapTimeout = 650L // Window to register subsequent taps

        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false)
            val up = waitForUpOrCancellation()

            if (up != null) {
                val screenWidth = size.width
                val tapX = up.position.x
                val tappedForward = tapX > (screenWidth / 2f)

                tapJob?.cancel()

                if (tapCount == 0) {
                    // First tap: wait to see if a double tap follows
                    tapJob = coroutineScope.launch {
                        delay(250L) // Wait for potential second tap
                        if (tapCount == 0) {
                            onSingleTap()
                        }
                    }
                    tapCount = 1
                    isForward = tappedForward

                    // Reset tapCount if no second tap arrives
                    coroutineScope.launch {
                        delay(250L)
                        if (tapCount == 1) {
                            tapCount = 0
                        }
                    }
                } else if ((tapCount >= 1) && (isForward == tappedForward)) {
                    // Second tap or subsequent consecutive taps
                    tapCount++
                    val accumulatedSeconds = (tapCount - 1) * 10
                    onSeekAccumulated(isForward, accumulatedSeconds)

                    // Keep window open for continuous chain (+20s, +30s, etc.)
                    tapJob = coroutineScope.launch {
                        delay(tapTimeout)
                        tapCount = 0
                        onSeekEnd()
                    }
                } else {
                    // Tapped the opposite side mid-chain: reset and switch direction
                    tapCount = 1
                    isForward = tappedForward
                    onSeekEnd()
                }
            }
        }
    }
}

@Composable
fun ProgressiveSeekHUD(text: String, isForward: Boolean, modifier: Modifier = Modifier) {
    var animState by remember { mutableStateOf(false) }

    val scaleAnim by animateFloatAsState(
        targetValue = if (animState) 1.15f else 0.85f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "seekHudScale",
    )

    val alphaAnim by animateFloatAsState(
        targetValue = if (animState) 0.9f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "seekHudAlpha",
    )

    LaunchedEffect(text) {
        animState = true
    }

    Box(
        modifier = modifier
            .padding(horizontal = 48.dp)
            .size(96.dp)
            .scale(scaleAnim)
            .background(Color.Black.copy(alpha = alphaAnim * 0.75f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = if (isForward) Icons.Default.FastForward else Icons.Default.FastRewind,
                contentDescription = null,
                tint = Color.Cyan.copy(alpha = alphaAnim),
                modifier = Modifier.size(34.dp),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = text,
                color = Color.White.copy(alpha = alphaAnim),
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
            )
        }
    }
}