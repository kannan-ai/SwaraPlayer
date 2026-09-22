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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swaraplayer.data.VideoMetadata
import com.example.swaraplayer.ui.theme.LocalAppColors

@Composable
fun VideoMetadataDialog(metadata: VideoMetadata, onDismiss: () -> Unit) {
    val colors = LocalAppColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = { Text("Video Information", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
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
            TextButton(onClick = onDismiss) { Text("OK", color = colors.accentOrange, fontWeight = FontWeight.Bold) }
        },
        shape = RoundedCornerShape(16.dp),
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    val colors = LocalAppColors.current

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = colors.textSecondary, fontSize = 12.sp)
        Text(value, color = colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}