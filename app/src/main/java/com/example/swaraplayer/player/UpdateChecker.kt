package com.example.swaraplayer.player

import android.util.Log
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

    fun checkForUpdate(currentVersion: String = "v0.0.1"): UpdateInfo {
        return try {
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

                val tagName = json.optString("tag_name", "v0.0.1")
                val body = json.optString("body", "Bug fixes and performance improvements.")
                val htmlUrl = json.optString("html_url", "https://github.com/kannan-ai/SwaraPlayer/releases")

                val isNewer = tagName.trim() != currentVersion.trim() && tagName.isNotBlank()

                UpdateInfo(
                    isUpdateAvailable = isNewer,
                    latestVersion = tagName,
                    releaseNotes = body,
                    downloadUrl = htmlUrl,
                )
            } else {
                UpdateInfo(
                    isUpdateAvailable = false,
                    latestVersion = currentVersion,
                    releaseNotes = "You are running the latest version.",
                    downloadUrl = "https://github.com/kannan-ai/SwaraPlayer/releases",
                )
            }
        } catch (e: Exception) {
            Log.e("UpdateChecker", "Failed to check GitHub releases", e)
            UpdateInfo(
                isUpdateAvailable = false,
                latestVersion = currentVersion,
                releaseNotes = "Unable to check for updates. Please check internet connection.",
                downloadUrl = "https://github.com/kannan-ai/SwaraPlayer/releases",
            )
        }
    }
}