# 🎬🎵 Swara Player — Modern Standalone Audio & Video Player for Android

**Swara Player** is a production-grade, standalone **Dual Video & Music Player Android App** built with **Jetpack Compose**, **Material 3**, and **AndroidX Media3 (ExoPlayer)**. It provides a complete local media solution supporting all major video formats (MP4, MKV, WebM, 3GP, AVI, MOV, FLV, TS) and audio formats (MP3, FLAC, M4A, WAV, AAC, OGG) with gesture controls, +200% audio amplification, 5-band equalizer, album art palette background extraction, Picture-in-Picture, SAF subtitles, and OLED Pure Black theme.

---

## 🌟 Key Features

### 📺 Standalone Video Player Engine
- ⚡ **AndroidX Media3 ExoPlayer**: Low-latency video playback for local files and adaptive HLS/DASH streams.
- 👆 **MX Player-Style Gestures**:
  - **Vertical Left Drag**: Screen Brightness adjustment.
  - **Vertical Right Drag**: Volume adjustment with **+200% Super Audio Boost** (`LoudnessEnhancer`).
  - **Horizontal Drag**: 1:1 Timeline Scrubbing with exact position HUD overlay.
  - **Progressive Tap Seeking**: Continuous double-tap chain seeking (`+10s`, `+20s`, `+30s`...).
  - **3.0x Speed Boost**: Press and hold anywhere on the video for instant 3x fast-forward.
- 🎛️ **360dp 4-Column Settings Drawer**: Right-hand side sheet with circular icon tiles for Fit, Zoom, Night Filter, Sleep Timer, PiP, Subtitles, Decoder, Capture, and Loop.
- 🔒 **Screen Lock Mode & Auto-Rotation Override**: Lock screen touches during video playback.
- 💬 **SAF External Subtitles**: Import `.srt`, `.vtt`, `.ass` caption files with custom typography, opacity, and outline styling.
- 📺 **Picture-in-Picture (PiP)**: Seamless background PiP window switching when exiting the app.
- 📱 **Cutout & Notch Support**: `LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES` for edge-to-edge playback on notch/punch-hole displays.

### 🎵 Standalone Music Player Engine
- 📻 **Dedicated Music Library**: Local tracks, artists, and albums view with 16:9 history carousel.
- 🔊 **Foreground MediaSessionService**: Background audio playback with lock-screen media controls and Bluetooth headset button support.
- 🎨 **AndroidX Palette Cover Art Extraction**: Dynamically extracts cover art color swatches to render animated background gradients.
- 🧹 **Regex Metadata Sanitizer**: Strips raw file extensions (`.mp3`, `.flac`), tags (`320kbps`, `HD`, `Official`), and formats clean titles.
- 🎵 **MiniPlayerBar & Expanded Player**: Docked mini bar at the bottom with slide-up full-screen player.
- 🔍 **Audio X-Ray Inspector**: Technical modal displaying clean Title, Artist, Album, Codec, Bitrate (kbps), Sample Rate (Hz), Duration, and File Path.

### ⚙️ App Themes & Updates
- 🎨 **3 Built-In App Themes**:
  - ☀️ **Light Theme**
  - 🌙 **Dark Theme**
  - 🖤 **OLED Pure Black Theme** (`#000000` AMOLED power-saving black)
- 🚀 **In-App GitHub Release Update Checker**: Connects directly to GitHub API (`kannan-ai/SwaraPlayer`) to notify users of new releases and direct APK downloads.

---

## 📁 Project File Architecture

```
com.example.swaraplayer/
├── data/
│   ├── MediaModels.kt                   # VideoFile, VideoFolder, VideoMetadata, SubtitleStyle, AspectRatioMode, OrientationMode, SortOrder, AudioPreset, SleepTimerMode
│   ├── AudioModels.kt                   # MediaFile, Artist, Album, SanitizedMetadata
│   └── VideoPositionRepository.kt       # DataStore resume position saver
├── player/
│   ├── AudioEnhancer.kt                 # +200% LoudnessEnhancer wrapper
│   ├── AudioEqualizer.kt                # 5-Band Equalizer & Presets wrapper
│   ├── UpdateChecker.kt                 # GitHub API Release Update Checker
│   └── PlayerViewModel.kt               # MediaStore Video & Audio scanner, Palette color extractor, Metadata Sanitizer & playback controller
├── service/
│   └── MediaPlaybackService.kt          # Foreground MediaSessionService with lock-screen notification & audio focus
├── ui/
│   ├── VideoLibraryScreen.kt            # Folders-first Home, Recently Played carousel, 3-column Folders grid with unopened count badges
│   ├── FolderVideosScreen.kt            # 2-column video grid when opening a folder
│   ├── MusicPlayerScreen.kt             # Main Music Library track list + docked MiniPlayerBar
│   ├── ExpandedPlayerScreen.kt          # Slide-up full-screen music player with dynamic Palette background, artwork & Audio X-Ray inspector
│   ├── AppSettingsScreen.kt             # Light, Dark, OLED Pure Black themes & GitHub update checker
│   ├── ProVideoPlayer.kt                # ExoPlayer Viewport + Gestures + System UI & Focus manager
│   └── components/
│       ├── AudioComponents.kt           # MiniPlayerBar & AudioXRayDialog
│       ├── VideoThumbnailImage.kt       # Multi-tier high-clarity video thumbnail generator
│       ├── LibraryTopBar.kt             # Back, Title, Home, Music, Settings, Help, Search & Sort header
│       ├── ProgressiveSeekHUD.kt        # Custom progressive tap seek modifier & spring-animated HUD
│       ├── DoubleTapRippleIndicator.kt  # Expanding spring animated -10s/+10s ripple indicator
│       ├── SpeedBoostPill.kt            # Glowing top 3.0x speed boost pill
│       ├── VideoControlsOverlay.kt      # PlayerTopBar (no voice button) & PlayerBottomBar (orange track, rotation lock, speed chip)
│       ├── GestureHUD.kt                 # Brightness/Volume Side HUDs & Scrubbing overlay
│       ├── VideoSettingsDrawer.kt        # 360dp right-hand side sheet with 4-column setting action grid (orange accent tint)
│       ├── SubtitleStyleDrawer.kt        # Subtitle typography, color, opacity & external file import
│       ├── GesturesHelpDialog.kt        # Touch gesture interactive guide
│       └── VideoMetadataDialog.kt        # Codec, resolution & file specs modal
├── ui/theme/
│   ├── MediaTheme.kt                    # Light, Dark, OLED Pure Black AppThemeColors tokens, LocalAppColors composition local & UI items
│   ├── Color.kt
│   ├── Theme.kt
│   └── Type.kt
├── MainActivity.kt                      # Multi-screen navigation, SAF subtitle picker, Media permissions & intent filter handling
└── AndroidManifest.xml
```

---

## 🛠️ Build & Installation Instructions

### Prerequisites
- Android Studio Ladybug (2024.2.1+) or Android Studio 2026.1+
- JDK 17
- Android SDK API 35 (compileSdk 37, minSdk 26)

### Build APK via Terminal
```bash
git clone https://github.com/kannan-ai/SwaraPlayer.git
cd SwaraPlayer
./gradlew :app:assembleDebug
```
The output APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 📜 License
This project is open-source under the **MIT License**.
