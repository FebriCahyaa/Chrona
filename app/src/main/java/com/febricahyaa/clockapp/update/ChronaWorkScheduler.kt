/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.update

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object ChronaWorkScheduler {
    const val UPDATE_WORK_NAME = "chrona-github-release-update-check"
    const val TIMEZONE_WORK_NAME = "chrona-timezone-catalog-maintenance"

    fun scheduleAll(context: Context) {
        val appContext = context.applicationContext
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresCharging(true)
            .build()

        val updateRequest = PeriodicWorkRequestBuilder<GitHubUpdateWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        val timezoneRequest = PeriodicWorkRequestBuilder<TimeZoneCatalogMaintenanceWorker>(7, TimeUnit.DAYS)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(appContext).enqueueUniquePeriodicWork(
            UPDATE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            updateRequest,
        )
        WorkManager.getInstance(appContext).enqueueUniquePeriodicWork(
            TIMEZONE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            timezoneRequest,
        )
    }
}

