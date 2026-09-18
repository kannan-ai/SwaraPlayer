package com.example.swaraplayer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swaraplayer.data.VideoMetadata

@Composable
fun VideoMetadataDialog(metadata: VideoMetadata, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E2C),
        title = { Text("Video Information", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoRow("File Name", metadata.title)
                InfoRow("Duration", metadata.durationFormatted)
                InfoRow("File Size", metadata.sizeFormatted)
                InfoRow("Resolution", metadata.resolution)
                InfoRow("Video Codec", metadata.videoCodec)
                InfoRow("Audio Codec", metadata.audioCodec)
                InfoRow("Path", metadata.path)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK", color = Color.Cyan) }
        },
        shape = RoundedCornerShape(16.dp),
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray, fontSize = 12.sp)
        Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}