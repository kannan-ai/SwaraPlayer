package com.example.swaraplayer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.swaraplayer.data.MediaFile
import com.example.swaraplayer.player.PlayerViewModel
import com.example.swaraplayer.ui.components.MiniPlayerBar
import com.example.swaraplayer.ui.components.formatTime
import com.example.swaraplayer.ui.theme.LocalAppColors

@Composable
fun MusicPlayerScreen(
    viewModel: PlayerViewModel,
    onOpenExpandedPlayer: () -> Unit,
    onOpenSettings: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val colors = LocalAppColors.current
    val audioTracks by viewModel.audioList.collectAsState()
    val currentTrack by viewModel.currentAudioTrack.collectAsState()
    val isPlaying by viewModel.isAudioPlaying.collectAsState()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.background)
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.textPrimary,
                    )
                }
                Text(
                    text = "Music Library",
                    color = colors.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                )
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = colors.textPrimary)
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = colors.textPrimary)
                }
            }
        },
        bottomBar = {
            if (currentTrack != null) {
                Box(modifier = Modifier.padding(12.dp)) {
                    MiniPlayerBar(
                        currentTrack = currentTrack,
                        isPlaying = isPlaying,
                        onPlayPause = { viewModel.toggleAudioPlayPause() },
                        onSkipNext = { viewModel.skipToNextAudioTrack() },
                        onClick = onOpenExpandedPlayer,
                    )
                }
            }
        },
        containerColor = colors.background,
    ) { innerPadding ->
        if (audioTracks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No audio tracks found",
                    color = colors.textSecondary,
                    fontSize = 14.sp,
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                items(audioTracks) { track ->
                    AudioTrackListItem(
                        track = track,
                        isSelected = currentTrack?.id == track.id,
                        onClick = { viewModel.playAudioTrack(track) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AudioTrackListItem(
    track: MediaFile,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalAppColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface),
                contentAlignment = Alignment.Center,
            ) {
                if (track.albumArtUri != null) {
                    AsyncImage(
                        model = track.albumArtUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = if (isSelected) colors.accentOrange else colors.folderIconTint,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    color = if (isSelected) colors.accentOrange else colors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${track.artist} • ${track.album}",
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = formatTime(track.durationMs),
                color = colors.textSecondary,
                fontSize = 11.sp,
                modifier = Modifier.padding(end = 8.dp),
            )
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Options",
                tint = colors.textSecondary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}