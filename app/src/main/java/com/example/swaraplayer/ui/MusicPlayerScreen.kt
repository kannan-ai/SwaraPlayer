package com.example.swaraplayer.ui

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swaraplayer.data.Album
import com.example.swaraplayer.data.Artist
import com.example.swaraplayer.data.MediaFile
import com.example.swaraplayer.player.PlayerViewModel
import com.example.swaraplayer.ui.components.AudioThumbnailImage
import com.example.swaraplayer.ui.components.MiniPlayerBar
import com.example.swaraplayer.ui.components.MoveToFolderDialog
import com.example.swaraplayer.ui.components.SelectionActionBar
import com.example.swaraplayer.ui.components.formatTime
import com.example.swaraplayer.ui.theme.LocalAppColors
import java.util.Locale

@Composable
fun MusicPlayerScreen(
    viewModel: PlayerViewModel,
    onOpenExpandedPlayer: () -> Unit,
    onOpenSettings: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val colors = LocalAppColors.current
    val audioTracks by viewModel.audioList.collectAsState()
    val artistsList by viewModel.artists.collectAsState()
    val albumsList by viewModel.albums.collectAsState()
    val currentTrack by viewModel.currentAudioTrack.collectAsState()
    val isPlaying by viewModel.isAudioPlaying.collectAsState()
    val selectedAudioIds by viewModel.selectedAudioIds.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Songs, 1: Artists, 2: Albums, 3: Folders
    var showMoveDialog by remember { mutableStateOf(false) }
    val tabs = listOf("Songs", "Artists", "Albums", "Folders")

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.background)
                    .statusBarsPadding(),
            ) {
                // Sleek Top Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .height(38.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.size(30.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(18.dp),
                        )
                    }

                    Text(
                        text = if (isSelectionMode) "${selectedAudioIds.size} Selected" else "Music Library",
                        color = colors.textPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                    )

                    IconButton(onClick = {}, modifier = Modifier.size(30.dp)) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(18.dp),
                        )
                    }

                    IconButton(onClick = {}, modifier = Modifier.size(30.dp)) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Sort Options",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                // 4 Tab Switcher Bar (Songs | Artists | Albums | Folders)
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = colors.background,
                    contentColor = colors.accentOrange,
                    indicator = {
                        TabRowDefaults.PrimaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(selectedTab),
                            color = colors.accentOrange,
                            height = 3.dp,
                        )
                    },
                    divider = {},
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    color = if (selectedTab == index) colors.accentOrange else colors.textSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                )
                            },
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (isSelectionMode) {
                Box(modifier = Modifier.padding(12.dp)) {
                    SelectionActionBar(
                        selectedCount = selectedAudioIds.size,
                        onSelectAllToggle = { viewModel.selectAllAudio(audioTracks.map { it.id }) },
                        onClearSelection = { viewModel.clearSelections() },
                        onMoveToFolder = { showMoveDialog = true },
                        onShare = { viewModel.shareSelectedMedia(context) },
                        onDelete = { viewModel.deleteSelectedMedia(context) },
                    )
                }
            } else if (currentTrack != null) {
                Box(modifier = Modifier.padding(12.dp)) {
                    MiniPlayerBar(
                        currentTrack = currentTrack,
                        isPlaying = isPlaying,
                        onPlayPause = { viewModel.toggleAudioPlayPause() },
                        onSkipPrev = { viewModel.skipToPrevAudioTrack() },
                        onSkipNext = { viewModel.skipToNextAudioTrack() },
                        onClick = onOpenExpandedPlayer,
                    )
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
            when (selectedTab) {
                0 -> SongsTabContent(
                    audioTracks = audioTracks,
                    currentTrack = currentTrack,
                    selectedAudioIds = selectedAudioIds,
                    isSelectionMode = isSelectionMode,
                    onToggleSelect = { id -> viewModel.toggleAudioSelection(id) },
                    onTrackClick = { track -> viewModel.playAudioTrack(track, context) },
                )
                1 -> ArtistsTabContent(
                    artists = artistsList,
                    onArtistClick = {},
                )
                2 -> AlbumsTabContent(
                    albums = albumsList,
                    onAlbumClick = {},
                )
                3 -> FoldersTabContent(
                    folders = listOf("Music"),
                    onFolderClick = {},
                )
            }
        }
    }

    if (showMoveDialog) {
        MoveToFolderDialog(
            foldersList = listOf("Music", "Downloads", "Audiobooks", "Podcasts"),
            onMoveConfirm = { targetFolder ->
                Toast.makeText(context, "${selectedAudioIds.size} tracks moved to '$targetFolder'", Toast.LENGTH_SHORT).show()
                viewModel.clearSelections()
                showMoveDialog = false
            },
            onDismiss = { showMoveDialog = false },
        )
    }
}

@Composable
private fun SongsTabContent(
    audioTracks: List<MediaFile>,
    currentTrack: MediaFile?,
    selectedAudioIds: Set<Long>,
    isSelectionMode: Boolean,
    onToggleSelect: (Long) -> Unit,
    onTrackClick: (MediaFile) -> Unit,
) {
    val colors = LocalAppColors.current

    if (audioTracks.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No audio tracks found", color = colors.textSecondary, fontSize = 14.sp)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 80.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item {
                Text(
                    text = "All Songs",
                    color = colors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 12.dp),
                )
            }

            items(audioTracks) { track ->
                AudioTrackListItem(
                    track = track,
                    isPlayingTrack = currentTrack?.id == track.id,
                    isSelected = selectedAudioIds.contains(track.id),
                    isSelectionMode = isSelectionMode,
                    onToggleSelect = onToggleSelect,
                    onClick = { onTrackClick(track) },
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AudioTrackListItem(
    track: MediaFile,
    isPlayingTrack: Boolean,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onToggleSelect: (Long) -> Unit,
    onClick: () -> Unit,
) {
    val colors = LocalAppColors.current
    val sizeMbFormatted = String.format(Locale.US, "%.1f MB", track.sizeBytes / (1024f * 1024f))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) onToggleSelect(track.id) else onClick()
                },
                onLongClick = {
                    onToggleSelect(track.id)
                },
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            // High Quality Cover Art Thumbnail
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surface),
                contentAlignment = Alignment.Center,
            ) {
                AudioThumbnailImage(
                    albumId = track.albumId,
                    audioUri = track.uri,
                    modifier = Modifier.fillMaxSize(),
                )

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = colors.accentOrange,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    color = if (isPlayingTrack) colors.accentOrange else colors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$sizeMbFormatted • ${formatTime(track.durationMs)}",
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "Options",
            tint = colors.textSecondary,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun ArtistsTabContent(
    artists: List<Artist>,
    onArtistClick: (Artist) -> Unit,
) {
    val colors = LocalAppColors.current

    if (artists.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No artists found", color = colors.textSecondary, fontSize = 14.sp)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 80.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(artists) { artist ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onArtistClick(artist) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Circular Cover Art Avatar
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(colors.surface),
                        contentAlignment = Alignment.Center,
                    ) {
                        val firstTrack = artist.tracks.firstOrNull()
                        AudioThumbnailImage(
                            albumId = firstTrack?.albumId ?: 0L,
                            audioUri = firstTrack?.uri,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = artist.name,
                            color = colors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "${artist.trackCount} Songs",
                            color = colors.textSecondary,
                            fontSize = 12.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AlbumsTabContent(
    albums: List<Album>,
    onAlbumClick: (Album) -> Unit,
) {
    val colors = LocalAppColors.current

    if (albums.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No albums found", color = colors.textSecondary, fontSize = 14.sp)
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(albums) { album ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surface)
                        .clickable { onAlbumClick(album) },
                ) {
                    val firstTrack = album.tracks.firstOrNull()
                    AudioThumbnailImage(
                        albumId = album.id,
                        audioUri = firstTrack?.uri,
                        modifier = Modifier.fillMaxSize(),
                    )

                    // Dark Gradient Overlay with Album Title & Artist
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                                ),
                            )
                            .padding(12.dp),
                        contentAlignment = Alignment.BottomStart,
                    ) {
                        Column {
                            Text(
                                text = album.title,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = album.artist,
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FoldersTabContent(
    folders: List<String>,
    onFolderClick: (String) -> Unit,
) {
    val colors = LocalAppColors.current

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(folders) { folder ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
                    .clickable { onFolderClick(folder) }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = colors.accentOrange,
                    modifier = Modifier.size(32.dp),
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = folder,
                    color = colors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}