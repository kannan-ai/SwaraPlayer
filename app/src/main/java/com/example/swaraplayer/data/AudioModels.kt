package com.example.swaraplayer.data

import android.net.Uri

data class MediaFile(
    val id: Long,
    val title: String,
    val artist: String = "Unknown Artist",
    val album: String = "Unknown Album",
    val durationMs: Long,
    val uri: Uri,
    val albumArtUri: Uri? = null,
    val sizeBytes: Long = 0L,
    val path: String = "",
    val bitrateKbps: Int = 320,
    val sampleRateHz: Int = 44100,
    val mimeType: String = "audio/mp3",
)

data class Artist(
    val name: String,
    val trackCount: Int,
    val albumCount: Int = 1,
    val tracks: List<MediaFile> = emptyList(),
)

data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val albumArtUri: Uri? = null,
    val trackCount: Int,
    val tracks: List<MediaFile> = emptyList(),
)

data class SanitizedMetadata(
    val cleanTitle: String,
    val cleanArtist: String,
    val cleanAlbum: String,
    val fileFormat: String,
    val bitrateFormatted: String,
    val sampleRateFormatted: String,
    val durationFormatted: String,
    val filePath: String,
)