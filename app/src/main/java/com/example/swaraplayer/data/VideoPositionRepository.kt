package com.example.swaraplayer.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.videoDataStore by preferencesDataStore("video_resume_positions")

class VideoPositionRepository(private val context: Context) {

    suspend fun savePosition(uriStr: String, positionMs: Long) {
        val key = longPreferencesKey("pos_${uriStr.hashCode()}")
        context.videoDataStore.edit { prefs ->
            prefs[key] = positionMs
        }
    }

    suspend fun getPosition(uriStr: String): Long {
        val key = longPreferencesKey("pos_${uriStr.hashCode()}")
        val prefs = context.videoDataStore.data.first()
        return prefs[key] ?: 0L
    }

    suspend fun markAsOpened(uriStr: String) {
        val key = longPreferencesKey("pos_${uriStr.hashCode()}")
        context.videoDataStore.edit { prefs ->
            if ((prefs[key] ?: 0L) == 0L) {
                prefs[key] = 1L // Mark as opened
            }
        }
    }

    suspend fun isOpened(uriStr: String): Boolean {
        val key = longPreferencesKey("pos_${uriStr.hashCode()}")
        val prefs = context.videoDataStore.data.first()
        return (prefs[key] ?: 0L) > 0L
    }

    suspend fun recordPlayback(uriStr: String) {
        val keyTime = longPreferencesKey("time_${uriStr.hashCode()}")
        val keyOpened = longPreferencesKey("pos_${uriStr.hashCode()}")
        context.videoDataStore.edit { prefs ->
            prefs[keyTime] = System.currentTimeMillis()
            if ((prefs[keyOpened] ?: 0L) == 0L) {
                prefs[keyOpened] = 1L
            }
        }
    }

    suspend fun getLastPlayedTimestamp(uriStr: String): Long {
        val keyTime = longPreferencesKey("time_${uriStr.hashCode()}")
        val prefs = context.videoDataStore.data.first()
        return prefs[keyTime] ?: 0L
    }
}