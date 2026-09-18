package com.example.swaraplayer.data

import android.net.Uri

data class VideoFile(
    val id: Long,
    val uri: Uri,
    val title: String,
    val folderName: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val resolution: String = "1080p",
    val dateModified: Long,
    val path: String = "",
)

data class VideoFolder(
    val name: String,
    val videoCount: Int,
    val newVideoCount: Int = 0,
    val totalSizeBytes: Long,
    val previewVideoUri: Uri?,
)

data class VideoMetadata(
    val title: String,
    val resolution: String,
    val frameRate: Float = 60f,
    val videoCodec: String = "H.264 / HEVC",
    val audioCodec: String = "AAC / AC3",
    val audioChannels: Int = 2,
    val sampleRate: Int = 48000,
    val sizeFormatted: String,
    val durationFormatted: String,
    val path: String,
)

data class SubtitleStyle(
    val fontSizeDp: Int = 18,
    val colorHex: String = "#FFFFFF",
    val opacity: Float = 1.0f,
    val backgroundColorHex: String = "#80000000",
    val edgeType: Int = 1, // 0: None, 1: Outline, 2: Drop Shadow
)

data class TrackOption(
    val groupIndex: Int,
    val trackIndex: Int,
    val name: String,
    val language: String,
    val isSelected: Boolean,
)

enum class AspectRatioMode(val label: String) {
    FIT("Fit"),
    FILL("Fill"),
    ZOOM("Zoom 100%"),
    RATIO_16_9("16:9"),
    RATIO_4_3("4:3"),
}

enum class OrientationMode(val label: String) {
    SENSOR("Auto Rotate"),
    LANDSCAPE("Landscape"),
    PORTRAIT("Portrait"),
}

enum class SortOrder(val label: String) {
    NAME_ASC("Name (A-Z)"),
    NAME_DESC("Name (Z-A)"),
    DATE_NEWEST("Date (Newest)"),
    DATE_OLDEST("Date (Oldest)"),
    SIZE_LARGEST("Size (Largest)"),
    DURATION_LONGEST("Duration (Longest)"),
}

enum class AudioPreset(val label: String) {
    FLAT("Flat"),
    BASS_BOOST("Bass Boost"),
    VOCAL("Vocal"),
    TREBLE_BOOST("Treble Boost"),
    ROCK("Rock"),
    POP("Pop"),
}