package com.example.swaraplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swaraplayer.player.PlayerViewModel

@Composable
fun SubtitleStyleDrawer(
    viewModel: PlayerViewModel,
    onImportExternalSubtitle: () -> Unit,
    onDismiss: () -> Unit,
) {
    val style by viewModel.subtitleStyle.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .width(320.dp),
            color = Color(0xFF1E1E2C),
            shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text("Subtitle Customization", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                // Import External Subtitle File (.srt, .vtt, .ass)
                Button(
                    onClick = onImportExternalSubtitle,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                ) {
                    Text("Import External Subtitle (.srt/.vtt)", color = Color.Cyan, fontSize = 12.sp)
                }

                // Font Size Slider
                Text("Font Size: ${style.fontSizeDp} sp", color = Color.Cyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = style.fontSizeDp.toFloat(),
                    onValueChange = { size ->
                        viewModel.subtitleStyle.value = style.copy(fontSizeDp = size.toInt())
                    },
                    valueRange = 12f..32f,
                    colors = SliderDefaults.colors(thumbColor = Color.Cyan, activeTrackColor = Color.Cyan),
                )

                // Text Color Options
                Text("Text Color", color = Color.Cyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("#FFFFFF" to "White", "#FFFF00" to "Yellow", "#00FFFF" to "Cyan").forEach { (hex, name) ->
                        FilterChip(
                            selected = style.colorHex == hex,
                            onClick = {
                                viewModel.subtitleStyle.value = style.copy(colorHex = hex)
                            },
                            label = { Text(name) },
                        )
                    }
                }

                // Background Opacity Slider
                Text("Background Opacity", color = Color.Cyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = style.opacity,
                    onValueChange = { op ->
                        viewModel.subtitleStyle.value = style.copy(opacity = op)
                    },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(thumbColor = Color.Cyan, activeTrackColor = Color.Cyan),
                )

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan),
                ) {
                    Text("Apply & Close", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}