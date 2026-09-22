package com.example.swaraplayer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swaraplayer.ui.theme.LocalAppColors

@Composable
fun GesturesHelpDialog(onDismiss: () -> Unit) {
    val colors = LocalAppColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = {
            Text("Touch Gestures Guide", color = colors.textPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                HorizontalDivider(color = colors.textSecondary.copy(alpha = 0.2f))

                GestureRow("Single Tap", "Toggle Controls Overlay")
                GestureRow("Double Tap (Left)", "Quick Rewind -10s (-20s, -30s...)")
                GestureRow("Double Tap (Right)", "Quick Fast Forward +10s (+20s...)")
                GestureRow("Double Tap (Center)", "Play / Pause Toggle")
                GestureRow("Long Press Hold", "3.0x Fast Forward Speed Boost")
                GestureRow("Drag Left Side", "Screen Brightness Adjustment")
                GestureRow("Drag Right Side", "Volume & +200% Audio Boost")
                GestureRow("Drag Horizontally", "1:1 Timeline Scrubbing")
                GestureRow("Pinch & Pan", "1.0x to 4.0x Zoom & Smooth Pan")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got It", color = colors.accentOrange, fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(16.dp),
    )
}

@Composable
private fun GestureRow(gesture: String, description: String) {
    val colors = LocalAppColors.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(gesture, color = colors.accentOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(description, color = colors.textSecondary, fontSize = 11.sp, modifier = Modifier.padding(start = 8.dp))
    }
}