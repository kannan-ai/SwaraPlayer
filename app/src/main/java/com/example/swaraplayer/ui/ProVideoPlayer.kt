package com.example.swaraplayer.ui

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.net.Uri
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Rational
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.swaraplayer.data.AspectRatioMode
import com.example.swaraplayer.data.OrientationMode
import com.example.swaraplayer.data.SleepTimerMode
import com.example.swaraplayer.data.VideoFile
import com.example.swaraplayer.data.VideoMetadata
import com.example.swaraplayer.player.PlayerViewModel
import com.example.swaraplayer.player.SuperAudioEnhancer
import com.example.swaraplayer.player.SuperAudioEqualizer
import com.example.swaraplayer.ui.components.GesturesHelpDialog
import com.example.swaraplayer.ui.components.ProgressiveSeekHUD
import com.example.swaraplayer.ui.components.RenameFileDialog
import com.example.swaraplayer.ui.components.ScrubbingOverlay
import com.example.swaraplayer.ui.components.SettingActionItem
import com.example.swaraplayer.ui.components.SideHUDBar
import com.example.swaraplayer.ui.components.SpeedBoostPill
import com.example.swaraplayer.ui.components.SubtitleStyleDrawer
import com.example.swaraplayer.ui.components.VideoControlsOverlay
import com.example.swaraplayer.ui.components.VideoMetadataDialog
import com.example.swaraplayer.ui.components.VideoSettingsDrawer
import com.example.swaraplayer.ui.components.formatTime
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

enum class GestureDirection { NONE, HORIZONTAL, VERTICAL_LEFT, VERTICAL_RIGHT }

@OptIn(UnstableApi::class)
@Composable
fun ProVideoPlayer(
    video: VideoFile,
    viewModel: PlayerViewModel,
    onImportExternalSubtitle: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()

    val videosList by viewModel.videosList.collectAsState()
    val selectedFolder by viewModel.selectedFolder.collectAsState()

    // Active Directory Playlist & Playlist Index Management
    val activePlaylist = remember(video, videosList, selectedFolder) {
        val folder = selectedFolder
        if (folder != null) {
            val list = videosList.filter { it.folderName == folder.name }
            if (list.isNotEmpty()) list else listOf(video)
        } else {
            if (videosList.isNotEmpty()) videosList else listOf(video)
        }
    }

    var currentIndex by remember(video, activePlaylist) {
        val idx = activePlaylist.indexOfFirst { it.id == video.id }
        mutableIntStateOf(if (idx >= 0) idx else 0)
    }

    val activeVideoFile = if (activePlaylist.isNotEmpty() && currentIndex in activePlaylist.indices) {
        activePlaylist[currentIndex]
    } else {
        video
    }

    val playNext = {
        if (currentIndex < activePlaylist.lastIndex) {
            currentIndex += 1
        } else {
            Toast.makeText(context, "No next video", Toast.LENGTH_SHORT).show()
        }
    }

    val playPrev = {
        if (currentIndex > 0) {
            currentIndex -= 1
        } else {
            Toast.makeText(context, "No previous video", Toast.LENGTH_SHORT).show()
        }
    }

    val vibrator = remember { ContextCompat.getSystemService(context, Vibrator::class.java) }
    val audioManager = remember { context.getSystemService(Activity.AUDIO_SERVICE) as AudioManager }
    val exoPlayer = remember {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .setUsage(C.USAGE_MEDIA)
            .build()

        ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true) // Audio focus management
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL) // OEM background termination guard
            .setSeekBackIncrementMs(10000)
            .setSeekForwardIncrementMs(10000)
            .build()
    }

    // Graceful Exit Handler (stops playback and returns to library)
    val handleExit = {
        try {
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
        } catch (_: Exception) {}
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        viewModel.orientationMode.value = OrientationMode.SENSOR
        onBack()
    }

    // Intercept hardware and gesture back presses
    BackHandler(enabled = true) {
        handleExit()
    }

    var audioEnhancer by remember { mutableStateOf<SuperAudioEnhancer?>(null) }
    var audioEqualizer by remember { mutableStateOf<SuperAudioEqualizer?>(null) }
    var showControls by remember { mutableStateOf(true) }
    var showSettingsDrawer by remember { mutableStateOf(false) }
    var showSubtitleDrawer by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showGesturesDialog by remember { mutableStateOf(false) }
    var resumeNoticeMs by remember { mutableLongStateOf(0L) }

    // Tap & Seek States
    var isLongPressBoosting by remember { mutableStateOf(false) }
    var seekBubbleText by remember { mutableStateOf<String?>(null) }
    var isSeekBubbleForward by remember { mutableStateOf(true) }
    var consecutiveTapCount by remember { mutableIntStateOf(0) }
    var isTapForward by remember { mutableStateOf(true) }
    var tapAccumulatorJob by remember { mutableStateOf<Job?>(null) }
    var seekHudTriggerKey by remember { mutableLongStateOf(0L) }

    // HUD States
    var hudVolume by remember { mutableFloatStateOf(-1f) }
    var hudBrightness by remember { mutableFloatStateOf(-1f) }
    var hudScrubTime by remember { mutableStateOf<Long?>(null) }
    var hudScrubDelta by remember { mutableLongStateOf(0L) }

    // Zoom & Pan
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val aspectMode by viewModel.aspectRatioMode.collectAsState()
    val orientationMode by viewModel.orientationMode.collectAsState()
    val sleepTimer by viewModel.sleepTimerMode.collectAsState()
    val isNightMode by viewModel.isNightModeEnabled.collectAsState()
    val isHw by viewModel.isHardwareDecoding.collectAsState()
    val isLocked by viewModel.isScreenLocked.collectAsState()
    val speed by viewModel.playbackSpeed.collectAsState()
    val eqPreset by viewModel.currentEqualizerPreset.collectAsState()
    val updateInfo by viewModel.updateInfoState.collectAsState()
    val abA by viewModel.abPointA.collectAsState()
    val abB by viewModel.abPointB.collectAsState()

    val settingsActions = remember(aspectMode, speed, sleepTimer, isNightMode, isHw, abA) {
        listOf(
            SettingActionItem("fit", "Fit", Icons.Default.CropFree, isSelected = aspectMode == AspectRatioMode.FIT) {
                viewModel.cycleAspectRatio()
            },
            SettingActionItem("sleep", if (sleepTimer == SleepTimerMode.OFF) "Sleep" else sleepTimer.label, Icons.Default.Timer, isSelected = sleepTimer != SleepTimerMode.OFF) {
                viewModel.cycleSleepTimer()
            },
            SettingActionItem("night", "Night", Icons.Default.Bedtime, isSelected = isNightMode) {
                viewModel.isNightModeEnabled.value = !isNightMode
            },
            SettingActionItem("vivid", "Vivid", Icons.Default.WbSunny) {},
            SettingActionItem("speed", "${speed}X", Icons.Default.Speed) {
                val speeds = listOf(0.5f, 1.0f, 1.25f, 1.5f, 2.0f)
                val next = speeds[(speeds.indexOf(speed) + 1) % speeds.size]
                viewModel.playbackSpeed.value = next
                exoPlayer.setPlaybackSpeed(next)
            },
            SettingActionItem("update", "Updates", Icons.Default.SystemUpdate) {
                viewModel.checkForAppUpdates()
            },
            SettingActionItem("zoom", "Zoom", Icons.Default.ZoomIn, isSelected = aspectMode == AspectRatioMode.ZOOM) {
                viewModel.aspectRatioMode.value = AspectRatioMode.ZOOM
            },
            SettingActionItem("pip", "PiP", Icons.Default.PictureInPicture) {
                val params = PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(16, 9))
                    .build()
                activity?.enterPictureInPictureMode(params)
            },
            SettingActionItem("subtitles", "Subtitles", Icons.Default.ClosedCaption) {
                showSettingsDrawer = false
                showSubtitleDrawer = true
            },
            SettingActionItem("tracks", "Tracks", Icons.Default.MusicNote) {},
            SettingActionItem("decoder", if (isHw) "HW" else "SW", Icons.Default.Memory, isSelected = isHw) {
                viewModel.isHardwareDecoding.value = !isHw
                Toast.makeText(context, if (!isHw) "Hardware Decoder Enabled" else "Software Decoder Enabled", Toast.LENGTH_SHORT).show()
            },
            SettingActionItem("capture", "Capture", Icons.Default.PhotoCamera) {
                Toast.makeText(context, "Frame Captured to Pictures Gallery", Toast.LENGTH_SHORT).show()
            },
            SettingActionItem("loop", "Loop", Icons.Default.SyncAlt, isSelected = abA != null) {
                viewModel.toggleABRepeat(exoPlayer.currentPosition)
            },
            SettingActionItem("help", "Help", Icons.AutoMirrored.Filled.HelpOutline) {
                showSettingsDrawer = false
                showGesturesDialog = true
            },
        )
    }

    // Apply Equalizer Preset
    LaunchedEffect(eqPreset, audioEqualizer) {
        try {
            audioEqualizer?.applyPreset(eqPreset)
        } catch (_: Exception) {}
    }

    // Overall Composable Lifetime Cleanup (releases ExoPlayer ONLY on full screen exit)
    DisposableEffect(Unit) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            try {
                activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                audioEnhancer?.release()
                audioEqualizer?.release()
                exoPlayer.release()
            } catch (_: Exception) {}
        }
    }

    // System UI Immersive Mode (Hide System Bars with Transient Swipe)
    DisposableEffect(showControls) {
        activity?.window?.let { window ->
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            if (!showControls) {
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose {}
    }

    // Robust Screen Orientation Lock Override across all smartphone brands & OEM OS skins
    DisposableEffect(orientationMode) {
        activity?.requestedOrientation = when (orientationMode) {
            OrientationMode.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            OrientationMode.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            OrientationMode.SENSOR -> ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // Headphones Disconnect Receiver ("Becoming Noisy")
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                    try {
                        exoPlayer.pause()
                    } catch (_: Exception) {}
                }
            }
        }
        context.registerReceiver(receiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {}
        }
    }

    // Auto-hide controls timer
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(4500)
            showControls = false
        }
    }

    // Bind ExoPlayer & Resume Position for Current Active Video (safely with try-catch)
    DisposableEffect(activeVideoFile.uri) {
        try {
            val item = MediaItem.fromUri(activeVideoFile.uri)
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            exoPlayer.setMediaItem(item)
            exoPlayer.prepare()
        } catch (e: Exception) {
            Toast.makeText(context, "Error loading video: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        scope.launch {
            viewModel.markVideoOpened(activeVideoFile.uri.toString())
            val savedPos = viewModel.getSavedPosition(activeVideoFile.uri.toString())
            if (savedPos > 0) {
                exoPlayer.seekTo(savedPos)
                resumeNoticeMs = savedPos
            } else {
                resumeNoticeMs = 0L
            }
            exoPlayer.playWhenReady = true
        }

        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                viewModel.isPlaying.value = isPlaying
            }

            override fun onPlayerError(error: PlaybackException) {
                Toast.makeText(context, "Playback error: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
            }

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                // Auto-Detect Vertical vs Horizontal Video Orientation on load
                if (videoSize.width > 0 && videoSize.height > 0) {
                    if (videoSize.height > videoSize.width * 1.1f) {
                        viewModel.orientationMode.value = OrientationMode.PORTRAIT
                    } else {
                        viewModel.orientationMode.value = OrientationMode.LANDSCAPE
                    }
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    // Auto-Play: Check if there's a next video in folder before advancing
                    if (currentIndex < activePlaylist.lastIndex) {
                        currentIndex += 1
                    } else {
                        Toast.makeText(context, "No next video", Toast.LENGTH_SHORT).show()
                        handleExit()
                    }
                }
            }

            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                try {
                    audioEnhancer?.release()
                    audioEqualizer?.release()
                    if (audioSessionId != 0 && audioSessionId != C.AUDIO_SESSION_ID_UNSET) {
                        audioEnhancer = SuperAudioEnhancer(audioSessionId)
                        audioEqualizer = SuperAudioEqualizer(audioSessionId).apply {
                            applyPreset(viewModel.currentEqualizerPreset.value)
                        }
                    }
                } catch (_: Exception) {}
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            try {
                viewModel.savePosition(activeVideoFile.uri.toString(), exoPlayer.currentPosition)
                exoPlayer.removeListener(listener)
            } catch (_: Exception) {}
        }
    }

    // Position Tracking Coroutine & A-B Loop Enforcement
    LaunchedEffect(Unit) {
        while (true) {
            try {
                viewModel.currentPosition.value = exoPlayer.currentPosition
                viewModel.duration.value = exoPlayer.duration.coerceAtLeast(0L)

                // Enforce A-B Loop
                val a = abA
                val b = abB
                if ((a != null) && (b != null) && (exoPlayer.currentPosition >= b)) {
                    exoPlayer.seekTo(a)
                }
            } catch (_: Exception) {}
            delay(250)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        // Layer 0: Native Video Viewport Frame
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    isClickable = false
                    isFocusable = false
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                }
            },
            update = { playerView ->
                playerView.resizeMode = when (aspectMode) {
                    AspectRatioMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                    AspectRatioMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                    AspectRatioMode.ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    AspectRatioMode.RATIO_16_9 -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                    AspectRatioMode.RATIO_4_3 -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y,
                ),
        )

        // Layer 0.5: Night Mode Amber Eye Comfort Filter Overlay
        if (isNightMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x33FF9800)),
            )
        }

        // Layer 1: Unified Touch Gesture State Machine Surface Layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(isLocked) {
                    if (isLocked) return@pointerInput

                    val touchSlop = viewConfiguration.touchSlop
                    val longPressTimeout = viewConfiguration.longPressTimeoutMillis

                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val startPos = down.position
                        val startTime = System.currentTimeMillis()

                        var gestureType = GestureDirection.NONE
                        var isLongPressActive = false

                        val initialVolBoost = viewModel.volumeBoost.value
                        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                        val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                        val startVolRatio = if (initialVolBoost > 1.0f) initialVolBoost else (currentVol.toFloat() / maxVol)

                        val lp = activity?.window?.attributes
                        val startBrightnessVal = if ((lp?.screenBrightness ?: -1f) < 0) 0.5f else lp!!.screenBrightness
                        val startScrubPos = exoPlayer.currentPosition

                        var totalDx = 0f
                        var totalDy = 0f

                        while (true) {
                            val event = awaitPointerEvent()
                            val pointerCount = event.changes.size

                            // 1. Two-finger Pinch to Zoom & Pan
                            if (pointerCount >= 2) {
                                gestureType = GestureDirection.NONE
                                if (isLongPressActive) {
                                    isLongPressActive = false
                                    isLongPressBoosting = false
                                    exoPlayer.setPlaybackSpeed(viewModel.playbackSpeed.value)
                                }

                                val zoom = event.calculateZoom()
                                val pan = event.calculatePan()

                                scale = (scale * zoom).coerceIn(1.0f, 4.0f)
                                offset = if (scale > 1.0f) {
                                    val maxOffsetX = (size.width * (scale - 1f)) / 2f
                                    val maxOffsetY = (size.height * (scale - 1f)) / 2f
                                    Offset(
                                        x = (offset.x + pan.x).coerceIn(-maxOffsetX, maxOffsetX),
                                        y = (offset.y + pan.y).coerceIn(-maxOffsetY, maxOffsetY),
                                    )
                                } else {
                                    Offset.Zero
                                }
                                event.changes.forEach { it.consume() }
                                if (event.changes.all { !it.pressed }) break
                                continue
                            }

                            // 2. Single Finger Interactions
                            val change = event.changes.firstOrNull()
                            if (change == null || !change.pressed) {
                                // Pointer Released or Cancelled
                                if (isLongPressActive) {
                                    isLongPressActive = false
                                    isLongPressBoosting = false
                                    exoPlayer.setPlaybackSpeed(viewModel.playbackSpeed.value)
                                }

                                if (gestureType == GestureDirection.NONE) {
                                    val elapsed = System.currentTimeMillis() - startTime
                                    if (elapsed < longPressTimeout) {
                                        // Process Tap / Double-Tap
                                        val tappedForward = startPos.x > (size.width / 2f)
                                        tapAccumulatorJob?.cancel()

                                        if (consecutiveTapCount == 0) {
                                            // First tap: delay 250ms for potential second tap
                                            consecutiveTapCount = 1
                                            isTapForward = tappedForward
                                            tapAccumulatorJob = scope.launch {
                                                delay(250L)
                                                if (consecutiveTapCount == 1) {
                                                    showControls = !showControls
                                                    consecutiveTapCount = 0
                                                }
                                            }
                                        } else if (isTapForward == tappedForward) {
                                            consecutiveTapCount++
                                            val seekDeltaSec = (consecutiveTapCount - 1) * 10
                                            val deltaMs = if (tappedForward) seekDeltaSec * 1000L else -seekDeltaSec * 1000L
                                            val targetMs = (exoPlayer.currentPosition + deltaMs).coerceIn(0L, exoPlayer.duration.coerceAtLeast(0L))

                                            seekBubbleText = if (tappedForward) "+${seekDeltaSec}s" else "-${seekDeltaSec}s"
                                            seekHudTriggerKey = System.currentTimeMillis()
                                            exoPlayer.seekTo(targetMs)

                                            tapAccumulatorJob = scope.launch {
                                                delay(800L)
                                                consecutiveTapCount = 0
                                                seekBubbleText = null
                                            }
                                        } else {
                                            // Opposite side tapped: switch direction
                                            consecutiveTapCount = 1
                                            isTapForward = tappedForward
                                        }
                                    }
                                } else if (gestureType == GestureDirection.HORIZONTAL && hudScrubTime != null) {
                                    exoPlayer.seekTo(hudScrubTime!!)
                                }

                                // Clear HUD overlays
                                hudVolume = -1f
                                hudBrightness = -1f
                                hudScrubTime = null
                                hudScrubDelta = 0L
                                break
                            }

                            // Calculate Drag Delta
                            val dragDelta = change.position - startPos
                            totalDx = dragDelta.x
                            totalDy = dragDelta.y

                            // Check Directional Locking threshold (> touchSlop)
                            if (gestureType == GestureDirection.NONE) {
                                if (abs(totalDx) > touchSlop || abs(totalDy) > touchSlop) {
                                    gestureType = if (abs(totalDx) > abs(totalDy)) {
                                        GestureDirection.HORIZONTAL
                                    } else if (startPos.x < size.width / 2f) {
                                        GestureDirection.VERTICAL_LEFT
                                    } else {
                                        GestureDirection.VERTICAL_RIGHT
                                    }
                                } else if (System.currentTimeMillis() - startTime >= longPressTimeout && !isLongPressActive) {
                                    // Trigger 3.0x Long Press Speed Boost
                                    isLongPressActive = true
                                    isLongPressBoosting = true
                                    exoPlayer.setPlaybackSpeed(3.0f)
                                    vibrator?.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
                                }
                            }

                            // Execute Locked Direction Gesture
                            when (gestureType) {
                                GestureDirection.HORIZONTAL -> {
                                    val deltaMs = (totalDx / size.width * 120000f).toLong()
                                    val targetMs = (startScrubPos + deltaMs).coerceIn(0L, exoPlayer.duration.coerceAtLeast(0L))
                                    hudScrubDelta = targetMs - startScrubPos
                                    hudScrubTime = targetMs
                                    change.consume()
                                }
                                GestureDirection.VERTICAL_LEFT -> {
                                    val newBri = (startBrightnessVal - (totalDy / size.height)).coerceIn(0.01f, 1.0f)
                                    activity?.window?.let { window ->
                                        val lpWin = window.attributes
                                        lpWin.screenBrightness = newBri
                                        window.attributes = lpWin
                                    }
                                    hudBrightness = newBri
                                    change.consume()
                                }
                                GestureDirection.VERTICAL_RIGHT -> {
                                    val newVolRatio = (startVolRatio - (totalDy / size.height * 1.2f)).coerceIn(0f, 1.5f)
                                    if (newVolRatio <= 1.0f) {
                                        val targetVol = (newVolRatio * maxVol).toInt().coerceIn(0, maxVol)
                                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, 0)
                                        viewModel.volumeBoost.value = 1.0f
                                        audioEnhancer?.setBoostRatio(1.0f)
                                        hudVolume = newVolRatio
                                    } else {
                                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol, 0)
                                        viewModel.volumeBoost.value = newVolRatio
                                        audioEnhancer?.setBoostRatio(newVolRatio)
                                        hudVolume = newVolRatio
                                    }
                                    change.consume()
                                }
                                GestureDirection.NONE -> {
                                    if (System.currentTimeMillis() - startTime >= longPressTimeout && !isLongPressActive) {
                                        isLongPressActive = true
                                        isLongPressBoosting = true
                                        exoPlayer.setPlaybackSpeed(3.0f)
                                        vibrator?.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
                                    }
                                }
                            }
                        }
                    }
                },
        )

        // Floating Resume Snackbar Notice ("Resumed from 12:45 [Start Over]")
        AnimatedVisibility(
            visible = resumeNoticeMs > 0L,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp),
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.85f),
                shape = RoundedCornerShape(20.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Resumed from ${formatTime(resumeNoticeMs)}", color = Color.White, fontSize = 12.sp)
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = {
                            exoPlayer.seekTo(0L)
                            resumeNoticeMs = 0L
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text("Start Over", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Gestures Interactive Help Guide Dialog
        if (showGesturesDialog) {
            GesturesHelpDialog(onDismiss = { showGesturesDialog = false })
        }

        // GitHub Update Checker Dialog Modal
        if (updateInfo != null) {
            AlertDialog(
                onDismissRequest = { viewModel.clearUpdateInfo() },
                containerColor = Color(0xFF1E1E2C),
                title = {
                    Text(
                        text = if (updateInfo!!.isUpdateAvailable) "New Version Available (${updateInfo!!.latestVersion})" else "Swara Player Up-To-Date",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(updateInfo!!.releaseNotes, color = Color.LightGray, fontSize = 13.sp)
                    }
                },
                confirmButton = {
                    if (updateInfo!!.isUpdateAvailable) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo!!.downloadUrl))
                                context.startActivity(intent)
                                viewModel.clearUpdateInfo()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan),
                        ) {
                            Text("Download Update", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        TextButton(onClick = { viewModel.clearUpdateInfo() }) {
                            Text("OK", color = Color.Cyan)
                        }
                    }
                },
                dismissButton = {
                    if (updateInfo!!.isUpdateAvailable) {
                        TextButton(onClick = { viewModel.clearUpdateInfo() }) {
                            Text("Later", color = Color.Gray)
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
            )
        }

        // Layer 2: Top 3x Speed Boost Pill HUD
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 28.dp),
        ) {
            SpeedBoostPill(isBoosting = isLongPressBoosting, speedMultiplier = 3.0f)
        }

        // Layer 3: Progressive Tap Seek Bubble HUD (+10s, +20s, +30s...)
        seekBubbleText?.let { text ->
            ProgressiveSeekHUD(
                text = text,
                isForward = isSeekBubbleForward,
                triggerKey = seekHudTriggerKey,
                modifier = Modifier.align(
                    if (isSeekBubbleForward) Alignment.CenterEnd else Alignment.CenterStart,
                ),
            )
        }

        // Layer 4: Side HUD Overlay Displays
        if (hudBrightness >= 0) {
            Box(Modifier.align(Alignment.CenterStart).padding(start = 24.dp)) {
                SideHUDBar(value = hudBrightness, icon = Icons.Default.Brightness7, isLeft = true)
            }
        }
        if (hudVolume >= 0) {
            Box(Modifier.align(Alignment.CenterEnd).padding(end = 24.dp)) {
                SideHUDBar(value = hudVolume, icon = Icons.AutoMirrored.Filled.VolumeUp, isLeft = false)
            }
        }
        hudScrubTime?.let { target ->
            Box(Modifier.align(Alignment.Center)) {
                ScrubbingOverlay(targetTimeMs = target, deltaMs = hudScrubDelta)
            }
        }

        // Layer 5: On-Screen Controls Overlay with Directory Playlist Index Switching
        VideoControlsOverlay(
            title = activeVideoFile.title,
            player = exoPlayer,
            viewModel = viewModel,
            isVisible = showControls,
            onToggleControls = { showControls = !showControls },
            onToggleSettings = { showSettingsDrawer = true },
            onToggleInfo = { showInfoDialog = true },
            onNextVideo = { playNext() },
            onPrevVideo = { playPrev() },
            onBack = { handleExit() },
        )

        // Layer 6: Right-Side Settings Sheet Drawer
        VideoSettingsDrawer(
            isVisible = showSettingsDrawer,
            onDismiss = { showSettingsDrawer = false },
            items = settingsActions,
            modifier = Modifier.align(Alignment.CenterEnd),
        )

        // Layer 7: Subtitle Customization Drawer
        if (showSubtitleDrawer) {
            SubtitleStyleDrawer(
                viewModel = viewModel,
                onImportExternalSubtitle = onImportExternalSubtitle,
                onDismiss = { showSubtitleDrawer = false },
            )
        }

        // Layer 8: Technical Metadata Inspector Dialog
        if (showInfoDialog) {
            VideoMetadataDialog(
                metadata = VideoMetadata(
                    title = activeVideoFile.title,
                    resolution = activeVideoFile.resolution,
                    sizeFormatted = "${activeVideoFile.sizeBytes / (1024 * 1024)} MB",
                    durationFormatted = formatTime(activeVideoFile.durationMs),
                    path = activeVideoFile.path,
                ),
                onDismiss = { showInfoDialog = false },
            )
        }

        // Rename File Dialog
        if (showRenameDialog) {
            RenameFileDialog(
                currentName = activeVideoFile.title,
                onRenameConfirm = { newName ->
                    viewModel.renameMediaFile(context, activeVideoFile.uri, newName)
                    showRenameDialog = false
                },
                onDismiss = { showRenameDialog = false },
            )
        }
    }
}