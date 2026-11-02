package com.example.swaraplayer.player

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val isUpdateAvailable: Boolean,
    val latestVersion: String,
    val releaseNotes: String,
    val downloadUrl: String,
)

object UpdateChecker {
    private const val GITHUB_RELEASES_API = "https://api.github.com/repos/kannan-ai/SwaraPlayer/releases/latest"

    suspend fun checkForUpdate(currentVersion: String = "v0.0.2"): UpdateInfo = withContext(Dispatchers.IO) {
        try {
            val url = URL(GITHUB_RELEASES_API)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("User-Agent", "SwaraPlayer-Android")
                connectTimeout = 8000
                readTimeout = 8000
            }

            if (connection.responseCode == 200) {
                val jsonStr = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(jsonStr)

                val tagName = json.optString("tag_name", "v0.0.2")
                val body = json.optString("body", "Swara Player - Bug fixes and performance improvements.")
                val htmlUrl = json.optString("html_url", "https://github.com/kannan-ai/SwaraPlayer/releases")

                val isNewer = isNewerVersion(current = currentVersion, latest = tagName)

                UpdateInfo(
                    isUpdateAvailable = isNewer,
                    latestVersion = tagName,
                    releaseNotes = body,
                    downloadUrl = if (htmlUrl.isNotBlank()) htmlUrl else "https://github.com/kannan-ai/SwaraPlayer/releases",
                )
            } else {
                UpdateInfo(
                    isUpdateAvailable = false,
                    latestVersion = currentVersion,
                    releaseNotes = "You are running the latest version of Swara Player ($currentVersion).",
                    downloadUrl = "https://github.com/kannan-ai/SwaraPlayer/releases",
                )
            }
        } catch (e: Exception) {
            Log.e("UpdateChecker", "Failed to check GitHub releases", e)
            UpdateInfo(
                isUpdateAvailable = false,
                latestVersion = currentVersion,
                releaseNotes = "Swara Player $currentVersion (Unable to reach GitHub. Check internet connection).",
                downloadUrl = "https://github.com/kannan-ai/SwaraPlayer/releases",
            )
        }
    }

    private fun cleanVersion(version: String): String {
        return version.replace(Regex("(?i)^v"), "").trim()
    }

    private fun isNewerVersion(current: String, latest: String): Boolean {
        val cleanCurrent = cleanVersion(current)
        val cleanLatest = cleanVersion(latest)

        if (cleanCurrent == cleanLatest || cleanLatest.isBlank()) return false

        val currentParts = cleanCurrent.split(".").mapNotNull { it.toIntOrNull() }
        val latestParts = cleanLatest.split(".").mapNotNull { it.toIntOrNull() }

        val maxLength = maxOf(currentParts.size, latestParts.size)
        for (i in 0 until maxLength) {
            val currVal = currentParts.getOrElse(i) { 0 }
            val latVal = latestParts.getOrElse(i) { 0 }
            if (latVal > currVal) return true
            if (latVal < currVal) return false
        }
        return false
    }
}