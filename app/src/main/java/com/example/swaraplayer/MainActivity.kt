package com.example.swaraplayer

import android.Manifest
import android.app.PictureInPictureParams
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.swaraplayer.data.VideoFile
import com.example.swaraplayer.player.PlayerViewModel
import com.example.swaraplayer.ui.AppSettingsScreen
import com.example.swaraplayer.ui.ProVideoPlayer
import com.example.swaraplayer.ui.VideoLibraryScreen
import com.example.swaraplayer.ui.theme.LocalAppColors
import com.example.swaraplayer.ui.theme.SwaraPlayerTheme
import com.example.swaraplayer.ui.theme.getThemeColors

class MainActivity : ComponentActivity() {

    private val viewModel: PlayerViewModel by viewModels()
    private val isInPipMode = mutableStateOf(false)

    // Storage Access Framework (SAF) Launcher for External Subtitles (.srt, .vtt, .ass)
    private val externalSubtitleLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            } catch (_: Exception) {}
            Toast.makeText(this, "External Subtitle Loaded: ${uri.lastPathSegment}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Enable full cutout support for notch/punch-hole displays across all smartphone brands
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        // Request Permissions
        val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            viewModel.scanLocalVideos()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        setContent {
            val currentThemeMode by viewModel.appThemeMode.collectAsState()
            val themeColors = remember(currentThemeMode) { getThemeColors(currentThemeMode) }

            CompositionLocalProvider(LocalAppColors provides themeColors) {
                SwaraPlayerTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = themeColors.background,
                    ) {
                        var activeVideo by remember { mutableStateOf<VideoFile?>(null) }
                        var isSettingsOpen by remember { mutableStateOf(false) }

                        // Handle External Intent ("Open With" / "Share With" from other apps)
                        LaunchedEffect(intent) {
                            val videoUri: Uri? = when (intent?.action) {
                                Intent.ACTION_SEND -> {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                                    } else {
                                        @Suppress("DEPRECATION")
                                        intent.getParcelableExtra(Intent.EXTRA_STREAM)
                                    }
                                }
                                else -> intent?.data
                            }

                            videoUri?.let { uri ->
                                activeVideo = VideoFile(
                                    id = 0,
                                    uri = uri,
                                    title = uri.lastPathSegment ?: "External Video",
                                    folderName = "External",
                                    durationMs = 0,
                                    sizeBytes = 0,
                                    dateModified = System.currentTimeMillis(),
                                )
                            }
                        }

                        if (activeVideo != null) {
                            ProVideoPlayer(
                                video = activeVideo!!,
                                viewModel = viewModel,
                                onImportExternalSubtitle = {
                                    externalSubtitleLauncher.launch(arrayOf("*/*"))
                                },
                                onBack = { activeVideo = null },
                            )
                        } else if (isSettingsOpen) {
                            AppSettingsScreen(
                                viewModel = viewModel,
                                onBack = { isSettingsOpen = false },
                            )
                        } else {
                            VideoLibraryScreen(
                                viewModel = viewModel,
                                onSelectVideo = { video -> activeVideo = video },
                                onOpenSettings = { isSettingsOpen = true },
                                onNavigateBack = { finish() },
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val params = PictureInPictureParams.Builder()
            .setAspectRatio(Rational(16, 9))
            .build()
        enterPictureInPictureMode(params)
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration,
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isInPipMode.value = isInPictureInPictureMode
    }
}