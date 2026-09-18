package com.example.swaraplayer.player

import android.app.Application
import android.content.ContentUris
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.swaraplayer.data.AspectRatioMode
import com.example.swaraplayer.data.AudioPreset
import com.example.swaraplayer.data.OrientationMode
import com.example.swaraplayer.data.SortOrder
import com.example.swaraplayer.data.SubtitleStyle
import com.example.swaraplayer.data.TrackOption
import com.example.swaraplayer.data.VideoFile
import com.example.swaraplayer.data.VideoFolder
import com.example.swaraplayer.data.VideoPositionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
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

    val updateInfoState = MutableStateFlow<UpdateInfo?>(null)
    val isCheckingUpdate = MutableStateFlow(false)

    val isPlaying = MutableStateFlow(false)
    val currentPosition = MutableStateFlow(0L)
    val duration = MutableStateFlow(0L)
    val playbackSpeed = MutableStateFlow(1.0f)
    val aspectRatioMode = MutableStateFlow(AspectRatioMode.FIT)
    val orientationMode = MutableStateFlow(OrientationMode.SENSOR)
    val volumeBoost = MutableStateFlow(1.0f) // 1.0f .. 2.0f
    val audioDelayMs = MutableStateFlow(0L) // Audio-Video Sync delay in ms
    val isScreenLocked = MutableStateFlow(false)
    val isBackgroundAudioOnly = MutableStateFlow(false)

    val subtitleStyle = MutableStateFlow(SubtitleStyle())
    val audioTracksList = MutableStateFlow<List<TrackOption>>(emptyList())
    val subtitleTracksList = MutableStateFlow<List<TrackOption>>(emptyList())

    // A-B Repeat Loop Markers
    val abPointA = MutableStateFlow<Long?>(null)
    val abPointB = MutableStateFlow<Long?>(null)

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

            isLoading.value = false
        }
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
}