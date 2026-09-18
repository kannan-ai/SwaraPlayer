package com.example.swaraplayer.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun DoubleTapRippleIndicator(
    isForward: Boolean,
    seconds: Int,
    onAnimationEnd: () -> Unit,
) {
    var animState by remember { mutableStateOf(false) }

    val scaleAnim by animateFloatAsState(
        targetValue = if (animState) 1.2f else 0.6f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "rippleScale",
    )

    val alphaAnim by animateFloatAsState(
        targetValue = if (animState) 0.85f else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "rippleAlpha",
    )

    LaunchedEffect(Unit) {
        animState = true
        delay(600)
        animState = false
        delay(200)
        onAnimationEnd()
    }

    Box(
        modifier = Modifier
            .size(140.dp)
            .scale(scaleAnim)
            .background(Color.Cyan.copy(alpha = alphaAnim * 0.25f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp),
        ) {
            Icon(
                imageVector = if (isForward) Icons.Default.FastForward else Icons.Default.FastRewind,
                contentDescription = null,
                tint = Color.Cyan.copy(alpha = alphaAnim),
                modifier = Modifier.size(36.dp),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (isForward) "+${seconds}s" else "-${seconds}s",
                color = Color.White.copy(alpha = alphaAnim),
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
            )
        }
    }
}