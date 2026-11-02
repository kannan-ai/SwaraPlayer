# 🎬🎵 Swara Player — Modern Standalone Audio & Video Player for Android

**Swara Player** is a production-grade, standalone **Dual Video & Music Player Android App** built with **Jetpack Compose**, **Material 3**, and **AndroidX Media3 (ExoPlayer)**. It provides a complete local media solution supporting all major video formats (MP4, MKV, WebM, 3GP, AVI, MOV, FLV, TS) and audio formats (MP3, FLAC, M4A, WAV, AAC, OGG) with gesture controls, +15dB audio amplification, 5-band equalizer, album art palette background extraction, Picture-in-Picture, SAF subtitles, and OLED Pure Black theme.

---

## 🌟 Key Features

### 📺 Standalone Video Player Engine
- ⚡ **AndroidX Media3 ExoPlayer**: Low-latency video playback for local files and adaptive HLS/DASH streams.
- 👆 **Advanced Intuitive Gestures**:
  - **Vertical Left Drag**: Screen Brightness adjustment.
  - **Vertical Right Drag**: Volume adjustment with **Safe Super Audio Boost** (`LoudnessEnhancer`).
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
- 🎨 **Palette Album Art Colors**: Dynamic gradient background extraction from song album art thumbnails.
- 🎛️ **Super Audio Equalizer**: 5-band equalizer with 8 presets (Flat, Bass Boost, Vocal, Treble Boost, Rock, Pop).
- 🎚️ **X-Ray Audio Metadata Inspector**: Inspect sample rate, bitrate, file format, and track channel specifications.

### 📁 Local Storage & System Interoperability
- 📂 **Nested Subfolder Navigation**: Stack-based folder navigation for video directories with unopened red badges.
- 🔍 **Global Media Search**: Real-time search filter for local videos, songs, artists, and albums.
- 📤 **System Intent Interoperability**: Handles `video/*` and `audio/*` intents from Gallery, Files, and messaging apps.
- 🚀 **Real APK Sharing & App Shortcuts**: Share installed `SwaraPlayer.apk` directly or launch via home-screen long-press shortcuts.

---

## 🛠️ Architecture & Tech Stack
- **UI Framework**: 100% Declarative Jetpack Compose + Material 3.
- **Playback Engine**: AndroidX Media3 ExoPlayer (`1.3.1`) + `MediaSessionService`.
- **Audio DSP**: Android `LoudnessEnhancer` and `Equalizer` audio effects.
- **Data Persistence**: Jetpack DataStore Preferences for resume positions and watch history.
- **Image Pipeline**: Coil Compose + Coil Video Frame Fetcher + AndroidX Palette.

---

## 📄 License
This project is open-source and released under the **MIT License**.
```