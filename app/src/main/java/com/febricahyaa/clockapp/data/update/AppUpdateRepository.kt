/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.data.update

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import java.io.IOException

private val Context.chronaUpdateDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "chrona_updates")

data class AppUpdateSnapshot(
    val lastCheckedAt: Long = 0L,
    val latestVersion: String? = null,
    val releaseName: String? = null,
    val releaseUrl: String? = null,
    val apkUrl: String? = null,
    val releaseNotes: String? = null,
    val publishedAt: String? = null,
)

data class GitHubReleaseDto(
    val tag_name: String,
    val name: String?,
    val html_url: String,
    val body: String?,
    val published_at: String?,
    val draft: Boolean,
    val prerelease: Boolean,
    val assets: List<GitHubAssetDto> = emptyList(),
)

data class GitHubAssetDto(
    val name: String,
    val browser_download_url: String,
)

interface GitHubReleaseApi {
    @Headers(
        "Accept: application/vnd.github+json",
        "X-GitHub-Api-Version: 2026-03-10",
        "User-Agent: Chrona-Android",
    )
    @GET("repos/FebriCahyaa/Chrona/releases/latest")
    suspend fun getLatestRelease(): Response<GitHubReleaseDto>
}

interface AppUpdateRepository {
    val snapshot: Flow<AppUpdateSnapshot>
    suspend fun checkLatest(): AppUpdateSnapshot
}

class GitHubReleaseRepository(
    context: Context,
    private val api: GitHubReleaseApi = createApi(),
) : AppUpdateRepository {
    private val appContext = context.applicationContext
    private val checkMutex = Mutex()

    override val snapshot: Flow<AppUpdateSnapshot> = appContext.chronaUpdateDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences())
            else throw error
        }
        .map { preferences ->
            AppUpdateSnapshot(
                lastCheckedAt = preferences[KEY_LAST_CHECKED_AT] ?: 0L,
                latestVersion = preferences[KEY_LATEST_VERSION],
                releaseName = preferences[KEY_RELEASE_NAME],
                releaseUrl = preferences[KEY_RELEASE_URL],
                apkUrl = preferences[KEY_APK_URL],
                releaseNotes = preferences[KEY_RELEASE_NOTES],
                publishedAt = preferences[KEY_PUBLISHED_AT],
            )
        }

    override suspend fun checkLatest(): AppUpdateSnapshot = checkMutex.withLock {
        val response = api.getLatestRelease()
        if (!response.isSuccessful) {
            throw GitHubReleaseException(response.code())
        }

        val release = response.body() ?: throw GitHubReleaseException(200)
        if (release.draft || release.prerelease) {
            throw GitHubReleaseException(200)
        }

        val latestVersion = normalizeVersion(release.tag_name)
        require(isSemverLike(latestVersion)) {
            "GitHub latest release has an invalid version tag: ${release.tag_name}"
        }
        val apkUrl = release.assets
            .firstOrNull { it.name.endsWith(".apk", ignoreCase = true) }
            ?.browser_download_url

        val checkedAt = System.currentTimeMillis()
        appContext.chronaUpdateDataStore.edit { preferences ->
            preferences[KEY_LAST_CHECKED_AT] = checkedAt
            preferences[KEY_LATEST_VERSION] = latestVersion
            preferences[KEY_RELEASE_NAME] = release.name ?: "Chrona $latestVersion"
            preferences[KEY_RELEASE_URL] = release.html_url
            apkUrl?.let { preferences[KEY_APK_URL] = it } ?: preferences.remove(KEY_APK_URL)
            release.body?.take(MAX_NOTES_LENGTH)?.let { preferences[KEY_RELEASE_NOTES] = it }
                ?: preferences.remove(KEY_RELEASE_NOTES)
            release.published_at?.let { preferences[KEY_PUBLISHED_AT] = it }
                ?: preferences.remove(KEY_PUBLISHED_AT)
        }

        AppUpdateSnapshot(
            lastCheckedAt = checkedAt,
            latestVersion = latestVersion,
            releaseName = release.name ?: "Chrona $latestVersion",
            releaseUrl = release.html_url,
            apkUrl = apkUrl,
            releaseNotes = release.body?.take(MAX_NOTES_LENGTH),
            publishedAt = release.published_at,
        )
    }

    companion object {
        private const val MAX_NOTES_LENGTH = 8_000
        private const val BASE_URL = "https://api.github.com/"

        private val retrofit by lazy {
            retrofit2.Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                .build()
        }

        private fun createApi(): GitHubReleaseApi = retrofit.create(GitHubReleaseApi::class.java)

        fun normalizeVersion(value: String): String =
            value.trim().removePrefix("v").removePrefix("V")

        fun isSemverLike(value: String): Boolean =
            value.trim().matches(Regex("\\d+(?:\\.\\d+){0,3}(?:[-+][0-9A-Za-z.-]+)?"))

        val KEY_LAST_CHECKED_AT = longPreferencesKey("last_checked_at")
        val KEY_LATEST_VERSION = stringPreferencesKey("latest_version")
        val KEY_RELEASE_NAME = stringPreferencesKey("release_name")
        val KEY_RELEASE_URL = stringPreferencesKey("release_url")
        val KEY_APK_URL = stringPreferencesKey("apk_url")
        val KEY_RELEASE_NOTES = stringPreferencesKey("release_notes")
        val KEY_PUBLISHED_AT = stringPreferencesKey("published_at")
    }
}


class GitHubReleaseException(
    val httpCode: Int,
) : IOException()

object AppVersionComparator {
    fun isNewer(current: String, candidate: String): Boolean {
        val currentParts = parse(current)
        val candidateParts = parse(candidate)
        val max = maxOf(currentParts.size, candidateParts.size)
        for (index in 0 until max) {
            val currentPart = currentParts.getOrElse(index) { 0 }
            val candidatePart = candidateParts.getOrElse(index) { 0 }
            if (candidatePart != currentPart) return candidatePart > currentPart
        }
        return false
    }

    private fun parse(value: String): List<Int> = value
        .trim()
        .removePrefix("v")
        .removePrefix("V")
        .substringBefore('-')
        .substringBefore('+')
        .split('.')
        .map { it.toIntOrNull() ?: 0 }
}
