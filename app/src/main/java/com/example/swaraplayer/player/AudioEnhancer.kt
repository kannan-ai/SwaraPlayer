package com.example.swaraplayer.player

import android.media.audiofx.LoudnessEnhancer
import android.util.Log

class SuperAudioEnhancer(audioSessionId: Int) {
    private var enhancer: LoudnessEnhancer? = null

    init {
        try {
            if (audioSessionId != 0) {
                enhancer = LoudnessEnhancer(audioSessionId).apply {
                    enabled = true
                }
            }
        } catch (e: Exception) {
            Log.e("SuperAudioEnhancer", "Failed to initialize LoudnessEnhancer", e)
        }
    }

    /**
     * @param boostRatio Range: 1.0f (System Max 100%) to 2.0f (+200% Super Boost)
     */
    fun setBoostRatio(boostRatio: Float) {
        enhancer?.let { e ->
            if (boostRatio > 1.0f) {
                // 1.0f -> 0mB gain, 2.0f -> 2000mB gain (+20dB)
                val gainMb = ((boostRatio - 1.0f) * 2000).toInt().coerceIn(0, 3000)
                e.setTargetGain(gainMb)
                e.enabled = true
            } else {
                e.setTargetGain(0)
                e.enabled = false
            }
        }
    }

    fun release() {
        try {
            enhancer?.release()
        } catch (_: Exception) {}
        enhancer = null
    }
}