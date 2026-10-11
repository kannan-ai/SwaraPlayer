package com.example.swaraplayer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swaraplayer.data.MediaFile
import com.example.swaraplayer.data.VideoFile
import com.example.swaraplayer.player.PlayerViewModel
import com.example.swaraplayer.ui.components.AudioThumbnailImage
import com.example.swaraplayer.ui.components.VideoThumbnailImage
import com.example.swaraplayer.ui.components.formatTime
import com.example.swaraplayer.ui.theme.LocalAppColors

@Composable
fun SearchScreen(
    viewModel: PlayerViewModel,
    onSelectVideo: (VideoFile) -> Unit,
    onOpenExpandedPlayer: () -> Unit,
) {
    val context = LocalContext.current
    val colors = LocalAppColors.current

    val searchQuery by viewModel.searchQuery.collectAsState()
    val videosList by viewModel.videosList.collectAsState()
    val audioList by viewModel.audioList.collectAsState()

    var selectedFilterChip by remember { mutableIntStateOf(0) } // 0: All, 1: Videos, 2: Songs
    val filterLabels = listOf("All", "Videos", "Songs")

    val matchingVideos = remember(videosList, searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else videosList.filter { it.title.contains(searchQuery, ignoreCase = true) || it.folderName.contains(searchQuery, ignoreCase = true) }
    }

    val matchingAudio = remember(audioList, searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else audioList.filter { it.title.contains(searchQuery, ignoreCase = true) || it.artist.contains(searchQuery, ignoreCase = true) || it.album.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.background)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                // Full Search TextField
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Search videos, songs, artists...", color = colors.textSecondary, fontSize = 13.sp) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = colors.accentOrange)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = colors.textPrimary)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accentOrange,
                        unfocusedBorderColor = colors.textSecondary.copy(alpha = 0.5f),
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(filterLabels.size) { idx ->
                        val isSelected = selectedFilterChip == idx
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilterChip = idx },
                            label = {
                                Text(
                                    text = filterLabels[idx],
                                    color = if (isSelected) Color.White else colors.textPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.accentOrange,
                                containerColor = colors.surface,
                            ),
                        )
                    }
                }
            }
        },
        containerColor = colors.background,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (searchQuery.isBlank()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = colors.folderIconTint, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Type to search local videos and songs", color = colors.textSecondary, fontSize = 14.sp)
                    }
                }
            } else if ((selectedFilterChip == 0 && matchingVideos.isEmpty() && matchingAudio.isEmpty()) ||
                (selectedFilterChip == 1 && matchingVideos.isEmpty()) ||
                (selectedFilterChip == 2 && matchingAudio.isEmpty())
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No matching results found for '$searchQuery'", color = colors.textSecondary, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    // Matching Videos Results Section
                    if ((selectedFilterChip == 0 || selectedFilterChip == 1) && matchingVideos.isNotEmpty()) {
                        item {
                            Text(
                                text = "Videos (${matchingVideos.size})",
                                color = colors.accentOrange,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp),
                            )
                        }

                        items(matchingVideos) { video ->
                            SearchVideoResultItem(video = video, onClick = { onSelectVideo(video) })
                        }
                    }

                    // Matching Songs Results Section
                    if ((selectedFilterChip == 0 || selectedFilterChip == 2) && matchingAudio.isNotEmpty()) {
                        item {
                            Text(
                                text = "Songs (${matchingAudio.size})",
                                color = colors.accentOrange,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                            )
                        }

                        items(matchingAudio) { song ->
                            SearchSongResultItem(
                                song = song,
                                onClick = {
                                    viewModel.playAudioTrack(song, context)
                                    onOpenExpandedPlayer()
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchVideoResultItem(video: VideoFile, onClick: () -> Unit) {
    val colors = LocalAppColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(100.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black),
        ) {
            VideoThumbnailImage(
                uri = video.uri,
                id = video.id,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color.Cyan, modifier = Modifier.size(24.dp))
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = video.title,
                color = colors.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${video.folderName} • ${formatTime(video.durationMs)}",
                color = colors.textSecondary,
                fontSize = 11.sp,
            )
        }
    }
}

@Composable
private fun SearchSongResultItem(song: MediaFile, onClick: () -> Unit) {
    val colors = LocalAppColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            AudioThumbnailImage(
                albumId = song.albumId,
                audioUri = song.uri,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                color = colors.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${song.artist} • ${formatTime(song.durationMs)}",
                color = colors.textSecondary,
                fontSize = 11.sp,
            )
        }

        Icon(Icons.Default.MusicNote, contentDescription = null, tint = colors.accentOrange, modifier = Modifier.size(20.dp))
    }
}