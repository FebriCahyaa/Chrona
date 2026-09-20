/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.update

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import com.febricahyaa.clockapp.data.onboarding.OnboardingRepository
import com.febricahyaa.clockapp.data.update.AppUpdateRepository
import kotlinx.coroutines.flow.first

@HiltWorker
class GitHubUpdateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val onboardingRepository: OnboardingRepository,
    private val updateRepository: AppUpdateRepository,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val onboarding = onboardingRepository.preferences.first()
        if (!onboarding.completed) return Result.success()

        return runCatching { updateRepository.checkLatest() }.fold(
            onSuccess = { Result.success() },
            onFailure = { error ->
                if (AppUpdateRetryPolicy.shouldRetry(error)) Result.retry() else Result.failure()
            },
        )
    }
}
