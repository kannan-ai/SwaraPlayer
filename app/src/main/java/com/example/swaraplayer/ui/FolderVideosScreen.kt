package com.example.swaraplayer.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swaraplayer.ui.components.LibraryTopBar
import com.example.swaraplayer.ui.components.VideoThumbnailImage
import com.example.swaraplayer.ui.theme.LocalAppColors
import com.example.swaraplayer.ui.theme.VideoItem

@Composable
fun FolderVideosScreen(
    folderName: String,
    videos: List<VideoItem>,
    isGridView: Boolean = true,
    onToggleViewMode: () -> Unit,
    onVideoClick: (VideoItem) -> Unit,
    onBack: () -> Unit,
) {
    val colors = LocalAppColors.current

    Scaffold(
        topBar = {
            LibraryTopBar(
                title = folderName,
                showBackButton = true,
                isGridView = isGridView,
                onToggleViewMode = onToggleViewMode,
                onBack = onBack,
                onHelp = {},
                onSortSelect = {},
            )
        },
        containerColor = colors.background,
    ) { innerPadding ->
        if (isGridView) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                items(videos) { video ->
                    VideoGridItem(video = video, onClick = { onVideoClick(video) })
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                items(videos) { video ->
                    VideoListItem(video = video, onClick = { onVideoClick(video) })
                }
            }
        }
    }
}

@Composable
fun VideoGridItem(video: VideoItem, onClick: () -> Unit) {
    val colors = LocalAppColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surface),
        ) {
            VideoThumbnailImage(
                uri = video.uri,
                id = video.id,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )

            // Capsule "NEW" Badge Pill for Unopened Videos with Dark Outline Border
            if (video.isUnopened) {
                Surface(
                    color = colors.badgeRed,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp),
                ) {
                    Text(
                        text = "✨ NEW",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }

            // Bottom Edge Watch Progress Line for partially watched videos
            if (!video.isUnopened) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth(0.35f)
                        .height(2.5.dp)
                        .background(colors.accentOrange, RoundedCornerShape(topEnd = 2.dp)),
                )
            }

            // Duration Pill
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .background(colors.background.copy(alpha = 0.85f), RoundedCornerShape(3.dp))
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
fun VideoListItem(video: VideoItem, onClick: () -> Unit) {
    val colors = LocalAppColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .height(54.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surface),
            ) {
                VideoThumbnailImage(
                    uri = video.uri,
                    id = video.id,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )

                if (video.isUnopened) {
                    Surface(
                        color = colors.badgeRed,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp),
                    ) {
                        Text(
                            text = "✨ NEW",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                        )
                    }
                }

                if (!video.isUnopened) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth(0.35f)
                            .height(2.dp)
                            .background(colors.accentOrange, RoundedCornerShape(topEnd = 2.dp)),
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(2.dp)
                        .background(colors.background.copy(alpha = 0.85f), RoundedCornerShape(2.dp))
                        .padding(horizontal = 3.dp, vertical = 1.dp),
                ) {
                    Text(
                        text = video.durationText,
                        color = colors.textPrimary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.title,
                    color = colors.textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Icon(
            Icons.Default.MoreVert,
            contentDescription = "Options",
            tint = colors.textSecondary,
            modifier = Modifier.size(18.dp),
        )
    }
}