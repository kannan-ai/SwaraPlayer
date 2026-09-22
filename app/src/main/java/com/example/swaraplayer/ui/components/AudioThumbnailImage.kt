package com.example.swaraplayer.ui.components

import android.content.ContentUris
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AudioThumbnailImage(
    albumId: Long,
    modifier: Modifier = Modifier,
    audioUri: Uri? = null,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val context = LocalContext.current
    var bitmap by remember(albumId, audioUri) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(albumId, audioUri) {
        withContext(Dispatchers.IO) {
            val artworkUri = ContentUris.withAppendedId(
                Uri.parse("content://media/external/audio/albumart"),
                albumId,
            )
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    bitmap = context.contentResolver.loadThumbnail(artworkUri, Size(300, 300), null)
                } else {
                    @Suppress("DEPRECATION")
                    bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, artworkUri)
                }
            } catch (_: Exception) {
                // Fallback: Extract embedded ID3 artwork from audio file
                if (audioUri != null) {
                    try {
                        val retriever = MediaMetadataRetriever()
                        try {
                            retriever.setDataSource(context, audioUri)
                            val embeddedArt = retriever.embeddedPicture
                            if (embeddedArt != null) {
                                bitmap = BitmapFactory.decodeByteArray(embeddedArt, 0, embeddedArt.size)
                            }
                        } finally {
                            retriever.release()
                        }
                    } catch (_: Exception) {}
                }
            }
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = "Album Art",
            contentScale = contentScale,
            modifier = modifier,
        )
    } else {
        Box(
            modifier = modifier.background(Color(0xFF2C2C2E)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = Color.Cyan,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}