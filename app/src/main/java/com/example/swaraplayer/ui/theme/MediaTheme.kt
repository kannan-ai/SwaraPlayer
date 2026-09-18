package com.example.swaraplayer.ui.theme

import android.net.Uri
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppThemeColors(
    val background: Color,
    val surface: Color,
    val folderIconTint: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accentOrange: Color,
    val badgeRed: Color,
)

val DarkThemeColors = AppThemeColors(
    background = Color(0xFF000000),
    surface = Color(0xFF141414),
    folderIconTint = Color(0xFF4A4A4A),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFF8E8E93),
    accentOrange = Color(0xFFE57A22),
    badgeRed = Color(0xFFFF2D55),
)

val LocalAppColors = staticCompositionLocalOf { DarkThemeColors }

data class VideoItem(
    val id: Long,
    val title: String,
    val durationText: String,
    val uri: Uri,
    val thumbnailUri: String? = null,
)

data class FolderItem(
    val name: String,
    val videoCount: Int,
    val newVideoCount: Int = 0,
    val previewVideoUri: Uri? = null,
    val isHighlighted: Boolean = false,
)