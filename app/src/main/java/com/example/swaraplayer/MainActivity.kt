package com.example.swaraplayer

import android.Manifest
import android.app.PictureInPictureParams
import android.content.ComponentName
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.swaraplayer.data.VideoFile
import com.example.swaraplayer.player.PlayerViewModel
import com.example.swaraplayer.service.MediaPlaybackService
import com.example.swaraplayer.ui.AppSettingsScreen
import com.example.swaraplayer.ui.ExpandedPlayerScreen
import com.example.swaraplayer.ui.MusicPlayerScreen
import com.example.swaraplayer.ui.ProVideoPlayer
import com.example.swaraplayer.ui.VideoLibraryScreen
import com.example.swaraplayer.ui.components.AppBottomNavigationBar
import com.example.swaraplayer.ui.components.AppNavTab
import com.example.swaraplayer.ui.theme.LocalAppColors
import com.example.swaraplayer.ui.theme.SwaraPlayerTheme
import com.example.swaraplayer.ui.theme.getThemeColors
import com.google.common.util.concurrent.MoreExecutors

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

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Enable full cutout support for notch/punch-hole displays across all smartphone brands
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        // Request Permissions for Media & Notifications
        val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            viewModel.scanLocalVideos()
            viewModel.scanLocalAudioTracks()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.POST_NOTIFICATIONS,
                ),
            )
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        }

        // Bind MediaController for MediaSessionService
        try {
            val sessionToken = SessionToken(this, ComponentName(this, MediaPlaybackService::class.java))
            val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
            controllerFuture.addListener({
                try {
                    val controller = controllerFuture.get()
                    controller?.let { viewModel.bindAudioPlayer(it) }
                } catch (_: Exception) {}
            }, MoreExecutors.directExecutor())
        } catch (_: Exception) {}

        setContent {
            val currentThemeMode by viewModel.appThemeMode.collectAsState()
            val themeColors = remember(currentThemeMode) { getThemeColors(currentThemeMode) }
            val updateInfo by viewModel.updateInfoState.collectAsState()

            CompositionLocalProvider(LocalAppColors provides themeColors) {
                SwaraPlayerTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = themeColors.background,
                    ) {
                        var activeVideo by remember { mutableStateOf<VideoFile?>(null) }
                        var isExpandedMusicPlayerOpen by remember { mutableStateOf(false) }
                        var currentNavTab by remember { mutableStateOf(AppNavTab.LOCAL) }

                        // Intercept phone's hardware back button when non-home tab is active
                        if (currentNavTab != AppNavTab.LOCAL && activeVideo == null && !isExpandedMusicPlayerOpen) {
                            BackHandler(enabled = true) {
                                currentNavTab = AppNavTab.LOCAL
                            }
                        }

                        // Intercept phone's hardware back button when expanded music player is open
                        if (isExpandedMusicPlayerOpen) {
                            BackHandler(enabled = true) {
                                isExpandedMusicPlayerOpen = false
                            }
                        }

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

                        // Global GitHub Release Update Dialog Observer
                        if (updateInfo != null) {
                            AlertDialog(
                                onDismissRequest = { viewModel.clearUpdateInfo() },
                                containerColor = themeColors.surface,
                                title = {
                                    Text(
                                        text = if (updateInfo!!.isUpdateAvailable) "New Version Available (${updateInfo!!.latestVersion})" else "Swara Player Up-To-Date",
                                        color = themeColors.textPrimary,
                                        fontWeight = FontWeight.Bold,
                                    )
                                },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(updateInfo!!.releaseNotes, color = themeColors.textSecondary, fontSize = 13.sp)
                                    }
                                },
                                confirmButton = {
                                    if (updateInfo!!.isUpdateAvailable) {
                                        Button(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo!!.downloadUrl))
                                                startActivity(intent)
                                                viewModel.clearUpdateInfo()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accentOrange),
                                        ) {
                                            Text("Download Update", color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        TextButton(onClick = { viewModel.clearUpdateInfo() }) {
                                            Text("OK", color = themeColors.accentOrange, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                },
                                dismissButton = {
                                    if (updateInfo!!.isUpdateAvailable) {
                                        TextButton(onClick = { viewModel.clearUpdateInfo() }) {
                                            Text("Later", color = themeColors.textSecondary)
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                            )
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
                        } else if (isExpandedMusicPlayerOpen) {
                            ExpandedPlayerScreen(
                                viewModel = viewModel,
                                onBack = { isExpandedMusicPlayerOpen = false },
                            )
                        } else {
                            Scaffold(
                                bottomBar = {
                                    AppBottomNavigationBar(
                                        currentTab = currentNavTab,
                                        onTabSelected = { currentNavTab = it },
                                    )
                                },
                                containerColor = themeColors.background,
                            ) { innerPadding ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding),
                                ) {
                                    when (currentNavTab) {
                                        AppNavTab.LOCAL, AppNavTab.SEARCH -> {
                                            VideoLibraryScreen(
                                                viewModel = viewModel,
                                                onSelectVideo = { video -> activeVideo = video },
                                                onNavigateBack = { finish() },
                                            )
                                        }
                                        AppNavTab.MUSIC -> {
                                            MusicPlayerScreen(
                                                viewModel = viewModel,
                                                onOpenExpandedPlayer = { isExpandedMusicPlayerOpen = true },
                                                onOpenSettings = { currentNavTab = AppNavTab.SETTINGS },
                                                onNavigateBack = { currentNavTab = AppNavTab.LOCAL },
                                            )
                                        }
                                        AppNavTab.SETTINGS -> {
                                            AppSettingsScreen(
                                                viewModel = viewModel,
                                                onBack = { currentNavTab = AppNavTab.LOCAL },
                                            )
                                        }
                                    }
                                }
                            }
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
        // Only trigger Picture-in-Picture mode if video playback is actively playing
        if (viewModel.isPlaying.value) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build()
            enterPictureInPictureMode(params)
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration,
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isInPipMode.value = isInPictureInPictureMode
    }
}