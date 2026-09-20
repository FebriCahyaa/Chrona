/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.chronaSettingsDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "chrona_settings")

val Context.chronaTimerDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "chrona_timer_v2")

val Context.chronaStopwatchDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "chrona_stopwatch_v2")

val Context.chronaSecretsDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "chrona_secrets")

val Context.chronaMigrationDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "chrona_storage_migration")

val Context.chronaTimezoneHealthDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "chrona_timezone_health")
