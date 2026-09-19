/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.update

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import com.febricahyaa.clockapp.ClockApplication

class GitHubUpdateWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val application = applicationContext as? ClockApplication ?: return Result.failure()
        val onboarding = application.container.onboardingRepository.preferences.first()
        if (!onboarding.completed) return Result.success()

        return runCatching {
            application.container.updateRepository.checkLatest()
        }.fold(
            onSuccess = { Result.success() },
            onFailure = { error ->
                if (AppUpdateRetryPolicy.shouldRetry(error)) Result.retry() else Result.failure()
            },
        )
    }
}
