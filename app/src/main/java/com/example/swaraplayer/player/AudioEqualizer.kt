package com.example.swaraplayer.player

import android.media.audiofx.Equalizer
import android.util.Log
import com.example.swaraplayer.data.AudioPreset

class SuperAudioEqualizer(initialAudioSessionId: Int = 0) {
    private var equalizer: Equalizer? = null
    private var currentSessionId: Int = 0

    init {
        if (initialAudioSessionId != 0) {
            attach(initialAudioSessionId)
        }
    }

    /**
     * Attaches or re-attaches the Equalizer to a specific audio session ID.
     */
    fun attach(audioSessionId: Int) {
        if (audioSessionId == 0 || audioSessionId == currentSessionId) return
        release()
        try {
            currentSessionId = audioSessionId
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = false // Disabled by default until non-FLAT preset applied
            }
        } catch (e: IllegalArgumentException) {
            Log.e("SuperAudioEqualizer", "Invalid audio session ID: $audioSessionId", e)
        } catch (e: UnsupportedOperationException) {
            Log.e("SuperAudioEqualizer", "Equalizer unsupported on this device/session", e)
        } catch (e: RuntimeException) {
            Log.e("SuperAudioEqualizer", "Failed to initialize Equalizer on session $audioSessionId", e)
        }
    }

    /**
     * Applies smooth, natural EQ curves mapped dynamically to the device's actual frequency bands.
     */
    fun applyPreset(preset: AudioPreset) {
        val eq = equalizer ?: return

        if (preset == AudioPreset.FLAT) {
            try {
                if (eq.enabled) {
                    eq.enabled = false
                }
            } catch (_: Exception) {}
            return
        }

        try {
            if (!eq.enabled) {
                eq.enabled = true
            }

            val numBands = eq.numberOfBands.toInt()
            val maxLevel = eq.bandLevelRange[1].toFloat() // usually +1500 mB (+15 dB)
            val minLevel = eq.bandLevelRange[0].toFloat() // usually -1500 mB (-15 dB)

            // Safe Gain Cap factor (55% of maximum hardware gain to prevent distortion)
            val safeMax = maxLevel * 0.55f
            val safeMin = minLevel * 0.35f

            // Smooth relative gain curves for 5 frequency bands (-1.0f to +1.0f)
            val curve5Band = when (preset) {
                AudioPreset.BASS_BOOST -> floatArrayOf(0.85f, 0.60f, 0.10f, 0f, 0.15f)
                AudioPreset.VOCAL -> floatArrayOf(-0.3f, 0f, 0.80f, 0.50f, 0f)
                AudioPreset.TREBLE_BOOST -> floatArrayOf(0f, 0f, 0.15f, 0.65f, 0.90f)
                AudioPreset.ROCK -> floatArrayOf(0.75f, 0.45f, -0.10f, 0.45f, 0.80f)
                AudioPreset.POP -> floatArrayOf(-0.15f, 0.35f, 0.70f, 0.40f, -0.15f)
                else -> floatArrayOf(0f, 0f, 0f, 0f, 0f)
            }

            for (i in 0 until numBands) {
                val ratio = if (numBands == 5) {
                    curve5Band[i]
                } else {
                    // Interpolate ratio for non-5-band equalizers
                    val normIdx = (i.toFloat() / (numBands - 1)) * 4f
                    val idxFloor = normIdx.toInt().coerceIn(0, 3)
                    val frac = normIdx - idxFloor
                    curve5Band[idxFloor] * (1f - frac) + curve5Band[idxFloor + 1] * frac
                }

                val targetLevelMb = if (ratio >= 0) {
                    (ratio * safeMax).toInt().toShort()
                } else {
                    ((-ratio) * safeMin).toInt().toShort()
                }

                eq.setBandLevel(i.toShort(), targetLevelMb)
            }
        } catch (e: Exception) {
            Log.e("SuperAudioEqualizer", "Error applying preset $preset", e)
        }
    }

    fun isAttached(): Boolean = equalizer != null

    fun release() {
        try {
            equalizer?.release()
        } catch (e: Exception) {
            Log.w("SuperAudioEqualizer", "Error releasing Equalizer", e)
        }
        equalizer = null
        currentSessionId = 0
    }
}