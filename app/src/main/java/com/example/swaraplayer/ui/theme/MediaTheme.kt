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

val LightThemeColors = AppThemeColors(
    background = Color(0xFFF2F2F7),
    surface = Color(0xFFFFFFFF),
    folderIconTint = Color(0xFFE57A22), // Consistent vibrant Orange across ALL themes!
    textPrimary = Color(0xFF000000),
    textSecondary = Color(0xFF6C6C70),
    accentOrange = Color(0xFFE57A22),
    badgeRed = Color(0xFFFF2D55),
)

val DarkThemeColors = AppThemeColors(
    background = Color(0xFF1C1C1E),
    surface = Color(0xFF2C2C2E),
    folderIconTint = Color(0xFFE57A22), // Consistent vibrant Orange across ALL themes!
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFF8E8E93),
    accentOrange = Color(0xFFE57A22),
    badgeRed = Color(0xFFFF2D55),
)

enum class AppThemeMode(val label: String) {
    SYSTEM("System Default"),
    LIGHT("Light Mode"),
    DARK("Dark Mode"),
}

fun getThemeColors(mode: AppThemeMode, isOledEnabled: Boolean = true, isSystemDark: Boolean = true): AppThemeColors {
    val isDark = when (mode) {
        AppThemeMode.SYSTEM -> isSystemDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    if (!isDark) {
        return LightThemeColors
    }

    return if (isOledEnabled) {
        AppThemeColors(
            background = Color(0xFF000000), // Pure OLED pitch black
            surface = Color(0xFF000000),    // Pure OLED pitch black
            folderIconTint = Color(0xFFE57A22), // Consistent vibrant Orange across ALL themes!
            textPrimary = Color(0xFFFFFFFF),
            textSecondary = Color(0xFF8E8E93),
            accentOrange = Color(0xFFE57A22),
            badgeRed = Color(0xFFFF2D55),
        )
    } else {
        DarkThemeColors
    }
}

val LocalAppColors = staticCompositionLocalOf {
    getThemeColors(AppThemeMode.DARK, isOledEnabled = true, isSystemDark = true)
}

data class VideoItem(
    val id: Long,
    val title: String,
    val durationText: String,
    val uri: Uri,
    val thumbnailUri: String? = null,
    val isUnopened: Boolean = false,
)

data class FolderItem(
    val name: String,
    val videoCount: Int,
    val newVideoCount: Int = 0,
    val previewVideoUri: Uri? = null,
    val isHighlighted: Boolean = false,
)