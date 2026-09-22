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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SpeakerGroup
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swaraplayer.data.SleepTimerMode
import com.example.swaraplayer.player.PlayerViewModel
import com.example.swaraplayer.ui.components.AudioThumbnailImage
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
    val dominantColor by viewModel.dominantColor.collectAsState()
    val sleepTimer by viewModel.sleepTimerMode.collectAsState()

    var showXRayDialog by remember { mutableStateOf(false) }

    val track = currentTrack ?: return
    val metadata = remember(track) { PlayerViewModel.sanitizeTrackMetadata(track) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(dominantColor, colors.background),
                ),
            ),
    ) {
        Scaffold(
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Collapse",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp),
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "NOW PLAYING",
                            color = Color.LightGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                        )
                        Text(
                            text = "Phone Speaker",
                            color = colors.accentOrange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }

                    IconButton(onClick = { showXRayDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "X-Ray Audio Inspector",
                            tint = colors.accentOrange,
                        )
                    }
                }
            },
            bottomBar = {
                // Bottom Output Speaker Bar
                Surface(
                    color = Color(0xFF23232C),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorite", tint = Color.LightGray)

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SpeakerGroup, contentDescription = null, tint = colors.accentOrange, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Phone Speaker", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = "Queue", tint = colors.accentOrange)
                    }
                }
            },
            containerColor = Color.Transparent,
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                // High-Res Artwork Display via Multi-Tier AudioThumbnailImage
                Surface(
                    color = colors.surface.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(24.dp),
                    shadowElevation = 12.dp,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(24.dp)),
                ) {
                    AudioThumbnailImage(
                        albumId = track.albumId,
                        audioUri = track.uri,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                // Quick Action Pills Row (Equalizer & Sleep Timer)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(vertical = 4.dp),
                ) {
                    Surface(
                        color = Color(0xFF2E2E38),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.clickable {},
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Default.Equalizer, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Equalizer", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    Surface(
                        color = Color(0xFF2E2E38),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.clickable { viewModel.cycleSleepTimer() },
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (sleepTimer == SleepTimerMode.OFF) "Sleep Timer" else sleepTimer.label,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }

                // Track Title & Artist Info (Sanitized)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 4.dp),
                ) {
                    Text(
                        text = metadata.cleanTitle,
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${metadata.cleanArtist} • ${metadata.cleanAlbum}",
                        color = colors.accentOrange,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
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
                            inactiveTrackColor = Color.White.copy(alpha = 0.25f),
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(formatTime(currentPos), color = Color.LightGray, fontSize = 11.sp)
                        Text(formatTime(duration), color = Color.LightGray, fontSize = 11.sp)
                    }
                }

                // Playback Action Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Shuffle, contentDescription = "Shuffle", tint = Color.LightGray)
                    }
                    IconButton(onClick = { viewModel.skipToPrevAudioTrack() }) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                    // Large Circular Orange Play/Pause Button
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
                            tint = Color.Black,
                            modifier = Modifier.size(36.dp),
                        )
                    }
                    IconButton(onClick = { viewModel.skipToNextAudioTrack() }) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Repeat, contentDescription = "Repeat", tint = Color.LightGray)
                    }
                }
            }
        }
    }

    // Audio X-Ray Modal Dialog Inspector
    if (showXRayDialog) {
        AudioXRayDialog(
            metadata = metadata,
            onDismiss = { showXRayDialog = false },
        )
    }
}