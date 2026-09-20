/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.data.onboarding

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

private val Context.chronaOnboardingDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "chrona_onboarding")

/** Persistent bootstrap state kept separately from feature settings. */
data class OnboardingPreferences(
    val completed: Boolean = false,
    val notificationPermissionPrompted: Boolean = false,
    val locationPermissionPrompted: Boolean = false,
)

interface OnboardingRepository {
    val preferences: Flow<OnboardingPreferences>
    suspend fun complete()
    suspend fun markNotificationPermissionPrompted()
    suspend fun markLocationPermissionPrompted()
}

class DataStoreOnboardingRepository @Inject constructor(@ApplicationContext context: Context) : OnboardingRepository {
    private val appContext = context.applicationContext

    override val preferences: Flow<OnboardingPreferences> = appContext.chronaOnboardingDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences())
            else throw error
        }
        .map { values ->
            OnboardingPreferences(
                completed = values[KEY_COMPLETED] ?: false,
                notificationPermissionPrompted = values[KEY_NOTIFICATION_PERMISSION_PROMPTED] ?: false,
                locationPermissionPrompted = values[KEY_LOCATION_PERMISSION_PROMPTED] ?: false,
            )
        }

    override suspend fun complete() {
        appContext.chronaOnboardingDataStore.edit { values ->
            values[KEY_COMPLETED] = true
        }
    }

    override suspend fun markNotificationPermissionPrompted() {
        appContext.chronaOnboardingDataStore.edit { values ->
            values[KEY_NOTIFICATION_PERMISSION_PROMPTED] = true
        }
    }

    override suspend fun markLocationPermissionPrompted() {
        appContext.chronaOnboardingDataStore.edit { values ->
            values[KEY_LOCATION_PERMISSION_PROMPTED] = true
        }
    }

    private companion object {
        val KEY_COMPLETED = booleanPreferencesKey("completed")
        val KEY_NOTIFICATION_PERMISSION_PROMPTED = booleanPreferencesKey("notification_permission_prompted")
        val KEY_LOCATION_PERMISSION_PROMPTED = booleanPreferencesKey("location_permission_prompted")
    }
}
