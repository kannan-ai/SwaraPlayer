package com.example.swaraplayer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.swaraplayer.data.SanitizedMetadata
import com.example.swaraplayer.player.PlayerViewModel
import com.example.swaraplayer.ui.components.AudioXRayDialog
import com.example.swaraplayer.ui.components.formatTime
import com.example.swaraplayer.ui.theme.LocalAppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedPlayerScreen(
    viewModel: PlayerViewModel,
    onBack: () -> Unit,
) {
    val colors = LocalAppColors.current
    val currentTrack by viewModel.currentAudioTrack.collectAsState()
    val isPlaying by viewModel.isAudioPlaying.collectAsState()
    val currentPos by viewModel.audioCurrentPosition.collectAsState()
    val duration by viewModel.audioDuration.collectAsState()

    var showXRayDialog by remember { mutableStateOf(false) }

    val track = currentTrack ?: return

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.background)
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.textPrimary,
                    )
                }
                Text(
                    text = "NOW PLAYING",
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                )
                IconButton(onClick = { showXRayDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "X-Ray Audio Inspector",
                        tint = colors.accentOrange,
                    )
                }
            }
        },
        containerColor = colors.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Album Art Display
            Surface(
                color = colors.surface,
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(24.dp)),
            ) {
                if (track.albumArtUri != null) {
                    AsyncImage(
                        model = track.albumArtUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = colors.folderIconTint,
                            modifier = Modifier.size(96.dp),
                        )
                    }
                }
            }

            // Track Title & Artist Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 12.dp),
            ) {
                Text(
                    text = track.title,
                    color = colors.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${track.artist} • ${track.album}",
                    color = colors.textSecondary,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Seekbar Progress Slider & Time Indicators
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = if (duration > 0) currentPos.toFloat() / duration else 0f,
                    onValueChange = { frac -> viewModel.seekAudioTo((frac * duration).toLong()) },
                    colors = SliderDefaults.colors(
                        thumbColor = colors.accentOrange,
                        activeTrackColor = colors.accentOrange,
                        inactiveTrackColor = colors.textSecondary.copy(alpha = 0.3f),
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(formatTime(currentPos), color = colors.textSecondary, fontSize = 12.sp)
                    Text("-${formatTime((duration - currentPos).coerceAtLeast(0L))}", color = colors.textSecondary, fontSize = 12.sp)
                }
            }

            // Playback Action Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Shuffle, contentDescription = "Shuffle", tint = colors.textSecondary)
                }
                IconButton(onClick = { viewModel.skipToPrevAudioTrack() }) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = colors.textPrimary, modifier = Modifier.size(36.dp))
                }
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(colors.accentOrange)
                        .clickable { viewModel.toggleAudioPlayPause() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp),
                    )
                }
                IconButton(onClick = { viewModel.skipToNextAudioTrack() }) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = colors.textPrimary, modifier = Modifier.size(36.dp))
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Repeat, contentDescription = "Repeat", tint = colors.textSecondary)
                }
            }
        }
    }

    // Audio X-Ray Modal Dialog Inspector
    if (showXRayDialog) {
        AudioXRayDialog(
            metadata = SanitizedMetadata(
                cleanTitle = track.title,
                cleanArtist = track.artist,
                cleanAlbum = track.album,
                fileFormat = track.mimeType,
                bitrateFormatted = "${track.bitrateKbps} kbps",
                sampleRateFormatted = "${track.sampleRateHz} Hz",
                durationFormatted = formatTime(track.durationMs),
                filePath = track.path,
            ),
            onDismiss = { showXRayDialog = false },
        )
    }
}