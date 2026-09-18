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
}