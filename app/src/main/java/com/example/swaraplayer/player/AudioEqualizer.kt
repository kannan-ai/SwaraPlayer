package com.example.swaraplayer.player

import android.media.audiofx.Equalizer
import android.util.Log
import com.example.swaraplayer.data.AudioPreset

class SuperAudioEqualizer(audioSessionId: Int) {
    private var equalizer: Equalizer? = null

    init {
        try {
            if (audioSessionId != 0) {
                equalizer = Equalizer(0, audioSessionId).apply {
                    enabled = true
                }
            }
        } catch (e: Exception) {
            Log.e("SuperAudioEqualizer", "Failed to initialize Equalizer", e)
        }
    }

    fun applyPreset(preset: AudioPreset) {
        val eq = equalizer ?: return
        try {
            val numBands = eq.numberOfBands.toInt()
            val maxBandLevel = eq.bandLevelRange[1]
            val midLevel = 0.toShort()

            val bandGains: ShortArray = when (preset) {
                AudioPreset.FLAT -> ShortArray(numBands) { midLevel }
                AudioPreset.BASS_BOOST -> ShortArray(numBands) { idx ->
                    if (idx == 0 || idx == 1) maxBandLevel else midLevel
                }
                AudioPreset.VOCAL -> ShortArray(numBands) { idx ->
                    if (idx == 2 || idx == 3) maxBandLevel else midLevel
                }
                AudioPreset.TREBLE_BOOST -> ShortArray(numBands) { idx ->
                    if (idx >= numBands - 2) maxBandLevel else midLevel
                }
                AudioPreset.ROCK -> ShortArray(numBands) { idx ->
                    if (idx == 0 || idx == numBands - 1) maxBandLevel else midLevel
                }
                AudioPreset.POP -> ShortArray(numBands) { idx ->
                    if (idx == 1 || idx == 2) maxBandLevel else midLevel
                }
            }

            for (i in 0 until numBands) {
                eq.setBandLevel(i.toShort(), bandGains[i])
            }
        } catch (e: Exception) {
            Log.e("SuperAudioEqualizer", "Error applying preset $preset", e)
        }
    }

    fun release() {
        try {
            equalizer?.release()
        } catch (_: Exception) {}
        equalizer = null
    }
}