package com.example.swaraplayer.player

import android.app.Application
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.palette.graphics.Palette
import com.example.swaraplayer.data.Album
import com.example.swaraplayer.data.Artist
import com.example.swaraplayer.data.AspectRatioMode
import com.example.swaraplayer.data.AudioPreset
import com.example.swaraplayer.data.MediaFile
import com.example.swaraplayer.data.OrientationMode
import com.example.swaraplayer.data.SanitizedMetadata
import com.example.swaraplayer.data.SleepTimerMode
import com.example.swaraplayer.data.SortOrder
import com.example.swaraplayer.data.SubtitleStyle
import com.example.swaraplayer.data.TrackOption
import com.example.swaraplayer.data.VideoFile
import com.example.swaraplayer.data.VideoFolder
import com.example.swaraplayer.data.VideoPositionRepository
import com.example.swaraplayer.ui.components.formatTime
import com.example.swaraplayer.ui.theme.AppThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val positionRepo = VideoPositionRepository(application)

    val videosList = MutableStateFlow<List<VideoFile>>(emptyList())
    val foldersList = MutableStateFlow<List<VideoFolder>>(emptyList())
    val selectedFolder = MutableStateFlow<VideoFolder?>(null)
    val isLoading = MutableStateFlow(true)

    val searchQuery = MutableStateFlow("")
    val sortOrder = MutableStateFlow(SortOrder.DATE_NEWEST)
    val currentEqualizerPreset = MutableStateFlow(AudioPreset.FLAT)
    val appThemeMode = MutableStateFlow(AppThemeMode.OLED)

    val updateInfoState = MutableStateFlow<UpdateInfo?>(null)
    val isCheckingUpdate = MutableStateFlow(false)

    // Video Playback State
    val isPlaying = MutableStateFlow(false)
    val currentPosition = MutableStateFlow(0L)
    val duration = MutableStateFlow(0L)
    val playbackSpeed = MutableStateFlow(1.0f)
    val aspectRatioMode = MutableStateFlow(AspectRatioMode.FIT)
    val orientationMode = MutableStateFlow(OrientationMode.SENSOR)
    val sleepTimerMode = MutableStateFlow(SleepTimerMode.OFF)
    val isNightModeEnabled = MutableStateFlow(false)
    val isHardwareDecoding = MutableStateFlow(true)
    val volumeBoost = MutableStateFlow(1.0f) // 1.0f .. 2.0f
    val audioDelayMs = MutableStateFlow(0L) // Audio-Video Sync delay in ms
    val isScreenLocked = MutableStateFlow(false)
    val isBackgroundAudioOnly = MutableStateFlow(false)

    // Audio / Music Player State
    val audioList = MutableStateFlow<List<MediaFile>>(emptyList())
    val artists = MutableStateFlow<List<Artist>>(emptyList())
    val albums = MutableStateFlow<List<Album>>(emptyList())
    val currentAudioTrack = MutableStateFlow<MediaFile?>(null)
    val isAudioPlaying = MutableStateFlow(false)
    val audioCurrentPosition = MutableStateFlow(0L)
    val audioDuration = MutableStateFlow(0L)
    val dominantColor = MutableStateFlow(Color(0xFF1E1E2C))

    private var audioPlayerRef: Player? = null

    val subtitleStyle = MutableStateFlow(SubtitleStyle())
    val audioTracksList = MutableStateFlow<List<TrackOption>>(emptyList())
    val subtitleTracksList = MutableStateFlow<List<TrackOption>>(emptyList())

    // A-B Repeat Loop Markers
    val abPointA = MutableStateFlow<Long?>(null)
    val abPointB = MutableStateFlow<Long?>(null)

    init {
        // Active slider progress polling ticker loop
        viewModelScope.launch {
            while (isActive) {
                audioPlayerRef?.let { p ->
                    if (p.isPlaying) {
                        audioCurrentPosition.value = p.currentPosition.coerceAtLeast(0L)
                        audioDuration.value = p.duration.coerceAtLeast(0L)
                    }
                }
                delay(500)
            }
        }
    }

    fun bindAudioPlayer(player: Player) {
        this.audioPlayerRef = player
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isAudioPlaying.value = playing
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    audioDuration.value = player.duration.coerceAtLeast(0L)
                }
            }
        })
    }

    fun scanLocalVideos() {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading.value = true
            val files = mutableListOf<VideoFile>()
            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_MODIFIED,
                MediaStore.Video.Media.DATA,
            )

            val queryUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            getApplication<Application>().contentResolver.query(
                queryUri,
                projection,
                null,
                null,
                "${MediaStore.Video.Media.DATE_MODIFIED} DESC",
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val folderCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                val durCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val contentUri = ContentUris.withAppendedId(queryUri, id)
                    files.add(
                        VideoFile(
                            id = id,
                            uri = contentUri,
                            title = cursor.getString(nameCol) ?: "Video",
                            folderName = cursor.getString(folderCol) ?: "Internal Storage",
                            durationMs = cursor.getLong(durCol),
                            sizeBytes = cursor.getLong(sizeCol),
                            dateModified = cursor.getLong(dateCol),
                            path = cursor.getString(dataCol) ?: "",
                        ),
                    )
                }
            }
            videosList.value = files

            // Group videos by folder and calculate exact unopened video count
            val folderMap = files.groupBy { it.folderName }
            val folders = folderMap.map { (name, videoFiles) ->
                val unopenedCount = videoFiles.count { file ->
                    positionRepo.getPosition(file.uri.toString()) == 0L
                }
                VideoFolder(
                    name = name,
                    videoCount = videoFiles.size,
                    newVideoCount = if (unopenedCount > 0) unopenedCount else videoFiles.size,
                    totalSizeBytes = videoFiles.sumOf { it.sizeBytes },
                    previewVideoUri = videoFiles.firstOrNull()?.uri,
                )
            }.sortedBy { it.name }
            foldersList.value = folders

            scanLocalAudioTracks()
            isLoading.value = false
        }
    }

    fun scanLocalAudioTracks() {
        viewModelScope.launch(Dispatchers.IO) {
            val audioFiles = mutableListOf<MediaFile>()
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATA,
            )

            val queryUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            getApplication<Application>().contentResolver.query(
                queryUri,
                projection,
                null,
                null,
                "${MediaStore.Audio.Media.TITLE} ASC",
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val albumId = cursor.getLong(albumIdCol)
                    val contentUri = ContentUris.withAppendedId(queryUri, id)
                    val artUri = ContentUris.withAppendedId(
                        ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumId),
                        0,
                    )

                    audioFiles.add(
                        MediaFile(
                            id = id,
                            title = cursor.getString(titleCol) ?: "Audio Track",
                            artist = cursor.getString(artistCol) ?: "Unknown Artist",
                            album = cursor.getString(albumCol) ?: "Unknown Album",
                            durationMs = cursor.getLong(durCol),
                            uri = contentUri,
                            albumArtUri = artUri,
                            albumId = albumId,
                            sizeBytes = cursor.getLong(sizeCol),
                            path = cursor.getString(dataCol) ?: "",
                        ),
                    )
                }
            }
            audioList.value = audioFiles

            artists.value = audioFiles.groupBy { it.artist }.map { (name, tracks) ->
                Artist(name = name, trackCount = tracks.size, tracks = tracks)
            }

            albums.value = audioFiles.groupBy { it.album }.map { (albumTitle, tracks) ->
                val first = tracks.first()
                Album(id = first.id, title = albumTitle, artist = first.artist, albumArtUri = first.albumArtUri, trackCount = tracks.size, tracks = tracks)
            }

            if (currentAudioTrack.value == null && audioFiles.isNotEmpty()) {
                currentAudioTrack.value = audioFiles.first()
                audioDuration.value = audioFiles.first().durationMs
            }
        }
    }

    fun playAudioTrack(track: MediaFile, context: Context? = null) {
        currentAudioTrack.value = track
        audioDuration.value = track.durationMs
        audioCurrentPosition.value = 0L
        isAudioPlaying.value = true

        audioPlayerRef?.let { p ->
            p.setMediaItem(MediaItem.fromUri(track.uri))
            p.prepare()
            p.play()
        }

        if (context != null) {
            extractPaletteColor(context, track.albumId)
        }
    }

    fun extractPaletteColor(context: Context, albumId: Long) {
        val artworkUri = ContentUris.withAppendedId(
            Uri.parse("content://media/external/audio/albumart"),
            albumId,
        )
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Robust Scoped Storage thumbnail loader for Android 10+
                    context.contentResolver.loadThumbnail(
                        artworkUri,
                        Size(120, 120),
                        null,
                    )
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, artworkUri)
                }

                val palette = Palette.from(bitmap).generate()
                val swatch = palette.dominantSwatch ?: palette.vibrantSwatch ?: palette.darkVibrantSwatch
                dominantColor.value = Color(swatch?.rgb ?: android.graphics.Color.parseColor("#1E1E2C"))
            } catch (_: Exception) {
                dominantColor.value = Color(0xFF1E1E2C)
            }
        }
    }

    fun toggleAudioPlayPause() {
        audioPlayerRef?.let { p ->
            if (p.isPlaying) p.pause() else p.play()
        }
        isAudioPlaying.value = !isAudioPlaying.value
    }

    fun skipToNextAudioTrack() {
        val tracks = audioList.value
        if (tracks.isEmpty()) return
        val curr = currentAudioTrack.value
        val nextIndex = if (curr != null) (tracks.indexOf(curr) + 1) % tracks.size else 0
        playAudioTrack(tracks[nextIndex])
    }

    fun skipToPrevAudioTrack() {
        val tracks = audioList.value
        if (tracks.isEmpty()) return
        val curr = currentAudioTrack.value
        val prevIndex = if (curr != null) (tracks.indexOf(curr) - 1 + tracks.size) % tracks.size else 0
        playAudioTrack(tracks[prevIndex])
    }

    fun seekAudioTo(positionMs: Long) {
        val target = positionMs.coerceIn(0L, audioDuration.value)
        audioCurrentPosition.value = target
        audioPlayerRef?.seekTo(target)
    }

    fun checkForAppUpdates() {
        viewModelScope.launch(Dispatchers.IO) {
            isCheckingUpdate.value = true
            val info = UpdateChecker.checkForUpdate(currentVersion = "v0.0.1")
            updateInfoState.value = info
            isCheckingUpdate.value = false
        }
    }

    fun clearUpdateInfo() {
        updateInfoState.value = null
    }

    fun selectFolder(folder: VideoFolder?) {
        selectedFolder.value = folder
    }

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun updateSortOrder(order: SortOrder) {
        sortOrder.value = order
    }

    fun cycleAspectRatio() {
        val modes = AspectRatioMode.entries.toTypedArray()
        val nextIdx = (aspectRatioMode.value.ordinal + 1) % modes.size
        aspectRatioMode.value = modes[nextIdx]
    }

    fun cycleOrientationMode() {
        val modes = OrientationMode.entries.toTypedArray()
        val nextIdx = (orientationMode.value.ordinal + 1) % modes.size
        orientationMode.value = modes[nextIdx]
    }

    fun cycleSleepTimer() {
        val modes = SleepTimerMode.entries.toTypedArray()
        val nextIdx = (sleepTimerMode.value.ordinal + 1) % modes.size
        sleepTimerMode.value = modes[nextIdx]
    }

    fun toggleABRepeat(posMs: Long) {
        if (abPointA.value == null) {
            abPointA.value = posMs
        } else if (abPointB.value == null) {
            abPointB.value = posMs
        } else {
            abPointA.value = null
            abPointB.value = null
        }
    }

    /**
     * Saves position only if within valid threshold (> 5 sec and < 95% of duration).
     */
    fun savePosition(uriStr: String, posMs: Long) {
        val totalMs = duration.value
        if (posMs < 5000L || (totalMs > 0 && posMs > totalMs * 0.95f)) {
            // Ignore saving if at the very start or near the end
            viewModelScope.launch {
                positionRepo.savePosition(uriStr, 0L)
            }
            return
        }
        viewModelScope.launch {
            positionRepo.savePosition(uriStr, posMs)
        }
    }

    suspend fun getSavedPosition(uriStr: String): Long {
        return positionRepo.getPosition(uriStr)
    }

    companion object {
        fun sanitizeTrackMetadata(file: MediaFile?): SanitizedMetadata {
            if (file == null) return SanitizedMetadata("Unknown Track", "Unknown Artist", "Unknown Album", "MP3", "320 kbps", "44100 Hz", "00:00", "")

            var title = file.title.replace(Regex("(?i)\\.(mp3|m4a|flac|wav|aac|ogg)$"), "").replace('_', ' ')
            val parts = title.split('-').map { it.trim() }

            val cleanTitle = if (parts.size >= 2) parts.last() else title
            val cleanTitleFinal = cleanTitle.replace(Regex("(?i)\\b(320kbps|128kbps|official|lyrics|hd|4k|mp3)\\b"), "")
                .replace(Regex("[\\[\\]()]"), " ").trim()

            val artist = if (file.artist.isNotBlank() && file.artist != "<unknown>") file.artist
            else if (parts.size >= 2) parts.first() else "Unknown Artist"

            return SanitizedMetadata(
                cleanTitle = if (cleanTitleFinal.isNotBlank()) cleanTitleFinal else file.title,
                cleanArtist = artist,
                cleanAlbum = file.album,
                fileFormat = if (file.path.contains('.')) file.path.substringAfterLast('.').uppercase() else "MP3",
                bitrateFormatted = "${file.bitrateKbps} kbps",
                sampleRateFormatted = "${file.sampleRateHz} Hz",
                durationFormatted = formatTime(file.durationMs),
                filePath = file.path,
            )
        }
    }
}