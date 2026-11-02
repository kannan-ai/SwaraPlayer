package com.example.swaraplayer.player

import android.media.audiofx.LoudnessEnhancer
import android.util.Log

class SuperAudioEnhancer(initialAudioSessionId: Int = 0) {
    private var enhancer: LoudnessEnhancer? = null
    private var currentSessionId: Int = 0

    init {
        if (initialAudioSessionId != 0) {
            attach(initialAudioSessionId)
        }
    }

    /**
     * Attaches or re-attaches the LoudnessEnhancer to a specific audio session ID.
     */
    fun attach(audioSessionId: Int) {
        if (audioSessionId == 0 || audioSessionId == currentSessionId) return
        release()
        try {
            currentSessionId = audioSessionId
            enhancer = LoudnessEnhancer(audioSessionId).apply {
                enabled = true
            }
        } catch (e: IllegalArgumentException) {
            Log.e("SuperAudioEnhancer", "Invalid audio session ID: $audioSessionId", e)
        } catch (e: UnsupportedOperationException) {
            Log.e("SuperAudioEnhancer", "LoudnessEnhancer unsupported on this device/session", e)
        } catch (e: RuntimeException) {
            Log.e("SuperAudioEnhancer", "Failed to initialize LoudnessEnhancer on session $audioSessionId", e)
        }
    }

    /**
     * Sets super audio boost ratio cleanly with safe max gain limits.
     * @param boostRatio Range: 1.0f (0 mB / +0 dB) to 2.0f (+1500 mB / +15.0 dB max safe boost without clipping)
     */
    fun setBoostRatio(boostRatio: Float) {
        val e = enhancer ?: return
        try {
            if (boostRatio > 1.0f) {
                // 1.0f -> 0 mB (+0 dB), 2.0f -> 1500 mB (+15.0 dB safe max gain)
                val gainMb = ((boostRatio - 1.0f) * 1500).toInt().coerceIn(0, 1500)
                e.setTargetGain(gainMb)
                if (!e.enabled) {
                    e.enabled = true
                }
            } else {
                e.setTargetGain(0)
                if (e.enabled) {
                    e.enabled = false
                }
            }
        } catch (e: Exception) {
            Log.e("SuperAudioEnhancer", "Error setting gain target for boostRatio $boostRatio", e)
        }
    }

    fun isAttached(): Boolean = enhancer != null

    fun release() {
        try {
            enhancer?.release()
        } catch (e: Exception) {
            Log.w("SuperAudioEnhancer", "Error releasing LoudnessEnhancer", e)
        }
        enhancer = null
        currentSessionId = 0
    }
}