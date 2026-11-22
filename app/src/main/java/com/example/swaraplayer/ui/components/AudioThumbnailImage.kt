package com.example.swaraplayer.ui.components

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.LruCache
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.swaraplayer.ui.theme.LocalAppColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Thread-safe LRU Bitmap Cache for audio artwork thumbnails (sized to 1/8th max heap).
 */
object AudioThumbnailCache {
    private val maxMemoryKb = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSizeKb = maxMemoryKb / 8

    private val lruCache = object : LruCache<String, Bitmap>(cacheSizeKb) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount / 1024
        }
    }

    fun get(key: String): Bitmap? {
        synchronized(lruCache) {
            return lruCache.get(key)
        }
    }

    fun put(key: String, bitmap: Bitmap) {
        synchronized(lruCache) {
            if (lruCache.get(key) == null) {
                lruCache.put(key, bitmap)
            }
        }
    }
}

object AudioThumbnailLoader {

    /**
     * Loads artwork on Dispatchers.IO with downsampling to prevent OutOfMemoryError.
     */
    suspend fun loadArtwork(context: Context, albumId: Long, audioUri: Uri?): Bitmap? = withContext(Dispatchers.IO) {
        if (albumId > 0) {
            val artworkUri = ContentUris.withAppendedId(
                Uri.parse("content://media/external/audio/albumart"),
                albumId,
            )
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val bitmap = context.contentResolver.loadThumbnail(artworkUri, Size(256, 256), null)
                    if (bitmap != null) return@withContext bitmap
                } else {
                    @Suppress("DEPRECATION")
                    val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, artworkUri)
                    if (bitmap != null) return@withContext bitmap
                }
            } catch (_: Exception) {}
        }

        // Fallback: Downsample embedded ID3 artwork
        if (audioUri != null) {
            try {
                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(context, audioUri)
                    val embeddedArt = retriever.embeddedPicture
                    if (embeddedArt != null) {
                        return@withContext decodeSampledBitmap(embeddedArt, 256, 256)
                    }
                } finally {
                    retriever.release()
                }
            } catch (_: Exception) {}
        }

        return@withContext null
    }

    private fun decodeSampledBitmap(data: ByteArray, reqWidth: Int, reqHeight: Int): Bitmap? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(data, 0, data.size, options)
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false
        options.inPreferredConfig = Bitmap.Config.RGB_565
        return try {
            BitmapFactory.decodeByteArray(data, 0, data.size, options)
        } catch (_: Throwable) {
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}

@Composable
fun AudioThumbnailImage(
    albumId: Long,
    modifier: Modifier = Modifier,
    audioUri: Uri? = null,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val context = LocalContext.current
    val colors = LocalAppColors.current

    val cacheKey = remember(albumId, audioUri) {
        if (albumId > 0) "album_$albumId" else "uri_${audioUri?.toString() ?: "none"}"
    }

    // Check LRU Cache instantly on main thread
    var bitmap by remember(cacheKey) { mutableStateOf(AudioThumbnailCache.get(cacheKey)) }

    LaunchedEffect(cacheKey) {
        if (bitmap != null) return@LaunchedEffect

        val loadedBitmap = AudioThumbnailLoader.loadArtwork(context, albumId, audioUri)
        if (loadedBitmap != null) {
            AudioThumbnailCache.put(cacheKey, loadedBitmap)
            bitmap = loadedBitmap
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
            modifier = modifier.background(colors.surface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = colors.accentOrange,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}