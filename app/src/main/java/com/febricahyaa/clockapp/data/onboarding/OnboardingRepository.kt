/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.data.onboarding

import android.content.Context
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

private val Context.chronaOnboardingDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "chrona_onboarding")

/** Small, transactional bootstrap state kept separately from feature settings. */
interface OnboardingRepository {
    val completed: Flow<Boolean>
    suspend fun complete()
}

class DataStoreOnboardingRepository(context: Context) : OnboardingRepository {
    private val appContext = context.applicationContext

    override val completed: Flow<Boolean> = appContext.chronaOnboardingDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences())
            else throw error
        }
        .map { preferences -> preferences[KEY_COMPLETED] ?: false }

    override suspend fun complete() {
        appContext.chronaOnboardingDataStore.edit { preferences ->
            preferences[KEY_COMPLETED] = true
        }
    }

    private companion object {
        val KEY_COMPLETED = booleanPreferencesKey("completed")
    }
}
