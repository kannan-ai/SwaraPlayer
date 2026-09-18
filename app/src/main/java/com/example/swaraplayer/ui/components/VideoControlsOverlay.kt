package com.example.swaraplayer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.Cast
import androidx.compose.material.icons.outlined.ClosedCaption
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.exoplayer.ExoPlayer
import com.example.swaraplayer.data.OrientationMode
import com.example.swaraplayer.player.PlayerViewModel

@Composable
fun VideoControlsOverlay(
    title: String,
    player: ExoPlayer,
    viewModel: PlayerViewModel,
    isVisible: Boolean,
    onToggleControls: () -> Unit,
    onToggleSettings: () -> Unit,
    onToggleInfo: () -> Unit,
    onBack: () -> Unit,
) {
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPos by viewModel.currentPosition.collectAsState()
    val totalDuration by viewModel.duration.collectAsState()
    val speed by viewModel.playbackSpeed.collectAsState()
    val orientationMode by viewModel.orientationMode.collectAsState()
    val isLocked by viewModel.isScreenLocked.collectAsState()

    val accentOrange = Color(0xFFE57A22)

    if (isLocked) {
        // Locked State: Display Floating Unlock Button Only
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.TopStart) {
            IconButton(
                onClick = { viewModel.isScreenLocked.value = false },
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    .size(48.dp),
            ) {
                Icon(Icons.Default.Lock, contentDescription = "Unlock Screen", tint = accentOrange)
            }
        }
        return
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { onToggleControls() })
                },
        ) {
            // Top Bar without Voice Button
            PlayerTopBar(
                title = title,
                onBack = onBack,
                onEdit = onToggleInfo,
                onShare = {},
                onCast = {},
                onToggleCC = onToggleSettings,
                onMoreOptions = onToggleSettings,
                modifier = Modifier.align(Alignment.TopCenter),
            )

            // Bottom Bar with Rotation Lock & Track Seekbar
            PlayerBottomBar(
                currentPositionMs = currentPos,
                durationMs = totalDuration,
                isPlaying = isPlaying,
                isLocked = false,
                isOrientationLocked = orientationMode != OrientationMode.SENSOR,
                playbackSpeed = "${speed}x",
                onSeek = { targetMs -> player.seekTo(targetMs) },
                onPlayPause = {
                    if (isPlaying) player.pause() else player.play()
                },
                onNext = { player.seekTo((player.currentPosition + 10000).coerceAtMost(player.duration)) },
                onPrev = { player.seekTo((player.currentPosition - 10000).coerceAtLeast(0)) },
                onToggleLock = { viewModel.isScreenLocked.value = true },
                onToggleOrientationLock = { viewModel.cycleOrientationMode() },
                onAspectRatioClick = { viewModel.cycleAspectRatio() },
                onSpeedClick = onToggleSettings,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
fun PlayerTopBar(
    title: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onCast: () -> Unit,
    onToggleCC: () -> Unit,
    onMoreOptions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.45f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
            )
        }

        Text(
            text = title,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
        )

        IconButton(onClick = onEdit) {
            Icon(Icons.Outlined.Edit, "Edit", tint = Color.White, modifier = Modifier.size(20.dp))
        }
        IconButton(onClick = onShare) {
            Icon(Icons.Outlined.Share, "Share", tint = Color.White, modifier = Modifier.size(20.dp))
        }
        IconButton(onClick = onCast) {
            Icon(Icons.Outlined.Cast, "Cast", tint = Color.White, modifier = Modifier.size(20.dp))
        }
        IconButton(onClick = onToggleCC) {
            Icon(Icons.Outlined.ClosedCaption, "Subtitles", tint = Color.White, modifier = Modifier.size(20.dp))
        }
        IconButton(onClick = onMoreOptions) {
            Icon(Icons.Default.MoreVert, "More", tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerBottomBar(
    currentPositionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    isLocked: Boolean,
    isOrientationLocked: Boolean,
    playbackSpeed: String,
    onSeek: (Long) -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onToggleLock: () -> Unit,
    onToggleOrientationLock: () -> Unit,
    onAspectRatioClick: () -> Unit,
    onSpeedClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accentOrange = Color(0xFFE57A22)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.45f))
            .padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        // Slider Seekbar
        Slider(
            value = if (durationMs > 0) currentPositionMs.toFloat() / durationMs else 0f,
            onValueChange = { frac -> onSeek((frac * durationMs).toLong()) },
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp),
            colors = SliderDefaults.colors(
                thumbColor = accentOrange,
                activeTrackColor = accentOrange,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f),
            ),
            thumb = {
                SliderDefaults.Thumb(
                    interactionSource = remember { MutableInteractionSource() },
                    colors = SliderDefaults.colors(thumbColor = accentOrange),
                    modifier = Modifier.size(10.dp),
                )
            },
            track = { sliderState ->
                SliderDefaults.Track(
                    sliderState = sliderState,
                    modifier = Modifier.height(2.dp),
                    colors = SliderDefaults.colors(
                        activeTrackColor = accentOrange,
                        inactiveTrackColor = Color.White.copy(alpha = 0.3f),
                    ),
                )
            },
        )

        // Time indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = formatTime(currentPositionMs),
                color = Color.White,
                fontSize = 11.sp,
            )
            Text(
                text = "-${formatTime((durationMs - currentPositionMs).coerceAtLeast(0L))}",
                color = Color.White,
                fontSize = 11.sp,
            )
        }

        // Action controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Left lock tools
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onToggleLock) {
                    Icon(
                        imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = "Screen Lock",
                        tint = if (isLocked) accentOrange else Color.White,
                    )
                }
                IconButton(onClick = onToggleOrientationLock) {
                    Icon(
                        imageVector = Icons.Default.ScreenRotation,
                        contentDescription = "Orientation Lock",
                        tint = if (isOrientationLocked) accentOrange else Color.White,
                    )
                }
            }

            // Center playback cluster
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPrev) {
                    Icon(Icons.Default.SkipPrevious, "Previous", tint = Color.White, modifier = Modifier.size(28.dp))
                }
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable(onClick = onPlayPause),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.Black,
                        modifier = Modifier.size(28.dp),
                    )
                }
                IconButton(onClick = onNext) {
                    Icon(Icons.Default.SkipNext, "Next", tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }

            // Right aspect & speed tools
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onAspectRatioClick) {
                    Icon(Icons.Default.AspectRatio, "Aspect Ratio", tint = Color.White)
                }
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onSpeedClick)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = playbackSpeed,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}