package com.example.swaraplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SideHUDBar(value: Float, icon: ImageVector, isLeft: Boolean) {
    val isBoost = value > 1.0f
    val activeColor = if (isBoost) Color(0xFFFF9800) else Color(0xFF00E5FF)

    Box(
        modifier = Modifier
            .fillMaxHeight(0.45f)
            .width(44.dp)
            .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(22.dp))
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val fillRatio = if (isBoost) (value - 1.0f) else value

            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(110.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(fillRatio.coerceIn(0f, 1f))
                        .background(activeColor, CircleShape),
                )
            }
            Spacer(Modifier.height(10.dp))
            Icon(icon, contentDescription = null, tint = activeColor, modifier = Modifier.size(18.dp))
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (isBoost) "+${((value - 1f) * 100).toInt()}%" else "${(value * 100).toInt()}%",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun ScrubbingOverlay(targetTimeMs: Long, deltaMs: Long) {
    Box(
        modifier = Modifier
            .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(16.dp))
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val deltaSec = deltaMs / 1000
            val prefix = if (deltaSec >= 0) "+${deltaSec}s" else "${deltaSec}s"
            Text(
                prefix,
                color = if (deltaSec >= 0) Color.Green else Color.Red,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(formatTime(targetTimeMs), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

fun formatTime(ms: Long): String {
    val totalSecs = (ms / 1000).coerceAtLeast(0)
    val mins = totalSecs / 60
    val secs = totalSecs % 60
    val hrs = mins / 60
    return if (hrs > 0) {
        String.format("%d:%02d:%02d", hrs, mins % 60, secs)
    } else {
        String.format("%02d:%02d", mins, secs)
    }
}