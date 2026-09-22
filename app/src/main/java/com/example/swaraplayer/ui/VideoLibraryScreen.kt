package com.example.swaraplayer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swaraplayer.data.VideoFile
import com.example.swaraplayer.player.PlayerViewModel
import com.example.swaraplayer.ui.components.GesturesHelpDialog
import com.example.swaraplayer.ui.components.LibraryTopBar
import com.example.swaraplayer.ui.components.VideoThumbnailImage
import com.example.swaraplayer.ui.components.formatTime
import com.example.swaraplayer.ui.theme.FolderItem
import com.example.swaraplayer.ui.theme.LocalAppColors
import com.example.swaraplayer.ui.theme.VideoItem

@Composable
fun VideoLibraryScreen(
    viewModel: PlayerViewModel,
    onSelectVideo: (VideoFile) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val colors = LocalAppColors.current
    val allVideos by viewModel.videosList.collectAsState()
    val foldersList by viewModel.foldersList.collectAsState()
    val selectedFolder by viewModel.selectedFolder.collectAsState()
    val isGridView by viewModel.isGridViewMode.collectAsState()
    val query by viewModel.searchQuery.collectAsState()

    var showGesturesDialog by remember { mutableStateOf(false) }

    // Intercept phone's back button when searching
    if (query.isNotBlank()) {
        BackHandler(enabled = true) {
            viewModel.updateSearchQuery("")
        }
    }

    // Filter videos by search query if typing
    val filteredVideos = remember(allVideos, query) {
        if (query.isBlank()) allVideos else allVideos.filter { it.title.contains(query, ignoreCase = true) || it.folderName.contains(query, ignoreCase = true) }
    }

    // Map domain models to UI items
    val recentVideosUI = remember(filteredVideos) {
        filteredVideos.take(8).map { video ->
            VideoItem(
                id = video.id,
                title = video.title,
                durationText = formatTime(video.durationMs),
                uri = video.uri,
                thumbnailUri = video.uri.toString(),
            )
        }
    }

    val folderItemsUI = remember(foldersList, query) {
        val filtered = if (query.isBlank()) foldersList else foldersList.filter { it.name.contains(query, ignoreCase = true) }
        filtered.map { folder ->
            FolderItem(
                name = folder.name,
                videoCount = folder.videoCount,
                newVideoCount = folder.newVideoCount,
                previewVideoUri = folder.previewVideoUri,
                isHighlighted = folder.name.contains("series", ignoreCase = true) || folder.name.contains("Download", ignoreCase = true),
            )
        }
    }

    // Intercept phone's back button inside sub-folder view
    if (selectedFolder != null) {
        BackHandler(enabled = true) {
            viewModel.selectFolder(null)
        }

        val folderVideos = remember(selectedFolder, filteredVideos) {
            filteredVideos.filter { it.folderName == selectedFolder!!.name }.mapIndexed { index, video ->
                VideoItem(
                    id = video.id,
                    title = video.title,
                    durationText = formatTime(video.durationMs),
                    uri = video.uri,
                    thumbnailUri = video.uri.toString(),
                    isUnopened = index < selectedFolder!!.newVideoCount,
                )
            }
        }
        FolderVideosScreen(
            folderName = selectedFolder!!.name,
            videos = folderVideos,
            isGridView = isGridView,
            onToggleViewMode = { viewModel.toggleViewMode() },
            onVideoClick = { videoItem ->
                allVideos.find { it.id == videoItem.id }?.let { onSelectVideo(it) }
            },
            onBack = { viewModel.selectFolder(null) },
        )
        return
    }

    Scaffold(
        topBar = {
            LibraryTopBar(
                title = "Video Library",
                showBackButton = false,
                isGridView = isGridView,
                onToggleViewMode = { viewModel.toggleViewMode() },
                onBack = onNavigateBack,
                onHelp = { showGesturesDialog = true },
                onSortSelect = { order -> viewModel.updateSortOrder(order) },
            )
        },
        containerColor = colors.background,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Recently Played Section
            if (recentVideosUI.isNotEmpty()) {
                item {
                    Text(
                        text = "Recently Played",
                        color = colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 10.dp),
                    )
                }

                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(recentVideosUI) { videoItem ->
                            RecentVideoCard(
                                video = videoItem,
                                onClick = {
                                    allVideos.find { it.id == videoItem.id }?.let { onSelectVideo(it) }
                                },
                            )
                        }
                    }
                }
            }

            // Folders Header
            item {
                Text(
                    text = "Folders",
                    color = colors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 12.dp),
                )
            }

            // Folders Grid or List View
            if (isGridView) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 1200.dp),
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            userScrollEnabled = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                        ) {
                            items(folderItemsUI) { folderItem ->
                                FolderGridItem(
                                    folder = folderItem,
                                    onClick = {
                                        foldersList.find { it.name == folderItem.name }?.let { viewModel.selectFolder(it) }
                                    },
                                )
                            }
                        }
                    }
                }
            } else {
                items(folderItemsUI) { folderItem ->
                    FolderListItem(
                        folder = folderItem,
                        onClick = {
                            foldersList.find { it.name == folderItem.name }?.let { viewModel.selectFolder(it) }
                        },
                    )
                }
            }
        }
    }

    // Gestures Interactive Help Guide Dialog
    if (showGesturesDialog) {
        GesturesHelpDialog(onDismiss = { showGesturesDialog = false })
    }
}

@Composable
fun RecentVideoCard(video: VideoItem, onClick: () -> Unit) {
    val colors = LocalAppColors.current

    Column(
        modifier = Modifier
            .width(170.dp)
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surface),
        ) {
            VideoThumbnailImage(
                uri = video.uri,
                id = video.id,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(radius = 12.dp), // Blurred thumbnail for recently played / history items
            )

            // Frosted Dark Overlay with Centered Cyan Play Icon
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = null,
                    tint = Color.Cyan,
                    modifier = Modifier.size(32.dp),
                )
            }

            // Duration Pill
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .background(colors.background.copy(alpha = 0.8f), RoundedCornerShape(2.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp),
            ) {
                Text(
                    text = video.durationText,
                    color = colors.textPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = video.title,
                color = colors.textPrimary,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Icon(
                Icons.Default.MoreVert,
                contentDescription = "Options",
                tint = colors.textSecondary,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
fun FolderGridItem(folder: FolderItem, onClick: () -> Unit) {
    val colors = LocalAppColors.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            // Clean Folder Silhouette Icon (without thumbnail overlay)
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = folder.name,
                tint = if (folder.isHighlighted) colors.accentOrange else colors.folderIconTint,
                modifier = Modifier.size(68.dp),
            )

            // Red Notification Badge showing EXACT number of new unopened files with high-contrast dark border
            if (folder.newVideoCount > 0) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(colors.badgeRed, CircleShape)
                        .border(1.5.dp, Color(0xFF12121D), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (folder.newVideoCount > 99) "99+" else folder.newVideoCount.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = folder.name,
            color = if (folder.isHighlighted) colors.accentOrange else colors.textPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Text(
            text = "${folder.videoCount} videos",
            color = colors.textSecondary,
            fontSize = 10.sp,
        )
    }
}

@Composable
fun FolderListItem(folder: FolderItem, onClick: () -> Unit) {
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
            Box(contentAlignment = Alignment.TopEnd) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = folder.name,
                    tint = if (folder.isHighlighted) colors.accentOrange else colors.folderIconTint,
                    modifier = Modifier.size(48.dp),
                )

                if (folder.newVideoCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(colors.badgeRed, CircleShape)
                            .border(1.5.dp, Color(0xFF12121D), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (folder.newVideoCount > 99) "99+" else folder.newVideoCount.toString(),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = folder.name,
                    color = if (folder.isHighlighted) colors.accentOrange else colors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${folder.videoCount} videos",
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                )
            }
        }

        Icon(
            Icons.Default.MoreVert,
            contentDescription = "Options",
            tint = colors.textSecondary,
            modifier = Modifier.size(20.dp),
        )
    }
}