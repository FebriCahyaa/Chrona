/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.update

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.febricahyaa.clockapp.data.chronaTimezoneHealthDataStore
import java.time.zone.ZoneRulesProvider

/**
 * Maintains a lightweight local timezone health snapshot. The runtime source
 * remains Android's bundled TZDB/ICU; this worker deliberately does not fake a
 * remote timezone download. A future remote catalog provider can replace the
 * snapshot without changing the repository/UI contract.
 */
class TimeZoneCatalogMaintenanceWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val version = runCatching {
            ZoneRulesProvider.getVersions("UTC").keys.maxOrNull().orEmpty()
        }.getOrElse { return Result.retry() }
        applicationContext.chronaTimezoneHealthDataStore.edit { values ->
            values[KEY_TIMEZONE_TZDB_VERSION] = version
            values[KEY_TIMEZONE_LAST_CHECKED_AT] = System.currentTimeMillis()
        }
        return Result.success()
    }

    private companion object {
        val KEY_TIMEZONE_TZDB_VERSION = stringPreferencesKey("timezone_tzdb_version")
        val KEY_TIMEZONE_LAST_CHECKED_AT = longPreferencesKey("timezone_last_checked_at")
    }
}
