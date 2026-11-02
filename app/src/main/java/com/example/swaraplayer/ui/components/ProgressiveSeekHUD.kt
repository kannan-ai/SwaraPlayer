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
import com.example.swaraplayer.ui.theme.LocalAppColors
import kotlinx.coroutines.delay

@Composable
fun ProgressiveSeekHUD(
    text: String,
    isForward: Boolean,
    modifier: Modifier = Modifier,
    triggerKey: Long = System.currentTimeMillis(),
) {
    val colors = LocalAppColors.current
    var animState by remember { mutableStateOf(false) }

    val scaleAnim by animateFloatAsState(
        targetValue = if (animState) 1.15f else 0.85f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "seekHudScale",
    )

    val alphaAnim by animateFloatAsState(
        targetValue = if (animState) 0.95f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "seekHudAlpha",
    )

    LaunchedEffect(text, triggerKey) {
        animState = false
        delay(15)
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
                tint = colors.accentOrange.copy(alpha = alphaAnim),
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