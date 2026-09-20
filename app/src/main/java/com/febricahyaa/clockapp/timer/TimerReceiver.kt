/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.timer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.febricahyaa.clockapp.data.TimerRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TimerReceiver : BroadcastReceiver() {
    @Inject lateinit var repository: TimerRepository

    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val snapshot = repository.load()
                val recovery = TimerDurabilityPolicy.recover(snapshot, System.currentTimeMillis())
                if (recovery !is TimerDurabilityPolicy.Recovery.Expired) return@launch

                val pending = TimerDurabilityPolicy.markCompletionPending(snapshot)
                repository.save(pending)
                TimerRunningNotification.cancel(appContext)

                runCatching {
                    ContextCompat.startForegroundService(
                        appContext,
                        Intent(appContext, TimerService::class.java),
                    )
                }.onFailure {
                    if (TimerNotification.postFinished(appContext)) {
                        repository.save(TimerDurabilityPolicy.clearCompletionPending(pending))
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
