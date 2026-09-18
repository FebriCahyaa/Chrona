/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.febricahyaa.clockapp.core.config.AppDefaults
import com.febricahyaa.clockapp.data.TimerRepository
import com.febricahyaa.clockapp.model.TimerSnapshot
import com.febricahyaa.clockapp.time.ChronaTimeEngine
import com.febricahyaa.clockapp.timer.TimerDurabilityPolicy
import com.febricahyaa.clockapp.timer.TimerSchedulerGateway
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** UI projection of the shared Chrona timer engine. */
data class TimerUiState(
    val totalSeconds: Int = AppDefaults.DEFAULT_TIMER_SECONDS,
    val remainingSeconds: Int = AppDefaults.DEFAULT_TIMER_SECONDS,
    val isRunning: Boolean = false,
)

/**
 * Persistent countdown timer backed by [ChronaTimeEngine].
 *
 * AlarmManager remains the durable expiry mechanism. ChronaTimeEngine is the
 * monotonic source of truth for foreground rendering, so UI updates never
 * depend on a fixed-delay counter.
 */
class TimerViewModel(
    private val repository: TimerRepository,
    private val scheduler: TimerSchedulerGateway,
    private val timeEngine: ChronaTimeEngine,
) : ViewModel() {

    private val _state = MutableStateFlow(TimerUiState())
    val state: StateFlow<TimerUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            restore()

            var previousRunning = false
            timeEngine.state.collect { snapshot ->
                val timer = snapshot.timer
                val nextState = TimerUiState(
                    totalSeconds = (timer.durationMillis / 1_000L)
                        .toInt()
                        .coerceAtLeast(1),
                    remainingSeconds = ((timer.remainingMillis + 999L) / 1_000L)
                        .toInt()
                        .coerceAtLeast(0),
                    isRunning = timer.isRunning,
                )
                _state.value = nextState

                if (previousRunning && !timer.isRunning && timer.remainingMillis <= 0L) {
                    repository.save(
                        TimerSnapshot(
                            totalSeconds = nextState.totalSeconds,
                            remainingSeconds = 0,
                            running = false,
                            endAtEpochMillis = 0L,
                            completionPending = true,
                        ),
                    )
                }

                previousRunning = timer.isRunning
            }
        }
    }

    fun setPreset(totalSeconds: Int) {
        val safe = totalSeconds.coerceAtLeast(1)
        viewModelScope.launch {
            timeEngine.configureTimer(safe * 1_000L)
            persistCurrentState()
            scheduler.cancel()
        }
    }

    fun reset() {
        viewModelScope.launch {
            timeEngine.resetTimer()
            persistCurrentState()
            scheduler.cancel()
        }
    }

    fun toggle() {
        val current = _state.value
        when {
            current.isRunning -> pause()
            current.remainingSeconds > 0 -> start()
        }
    }

    private fun start() {
        val durationMillis = timeEngine.state.value.timer.remainingMillis.coerceAtLeast(1_000L)
        val endAtEpochMillis = timeEngine.currentEpochMillis() + durationMillis

        viewModelScope.launch {
            timeEngine.startTimer(durationMillis)
            persistCurrentState(endAtEpochMillis = endAtEpochMillis)
            scheduler.schedule(endAtEpochMillis)
        }
    }

    private fun pause() {
        viewModelScope.launch {
            timeEngine.pauseTimer()
            persistCurrentState()
            scheduler.cancel()
        }
    }

    private suspend fun restore() {
        val snapshot = repository.load()
        when (val recovery = TimerDurabilityPolicy.recover(
            snapshot,
            timeEngine.currentEpochMillis(),
        )) {
            is TimerDurabilityPolicy.Recovery.RestoreRunning -> {
                scheduler.schedule(snapshot.endAtEpochMillis)
                timeEngine.restoreTimer(
                    durationMillis = snapshot.totalSeconds.coerceAtLeast(1) * 1_000L,
                    remainingMillis = recovery.remainingMillis,
                    running = true,
                )
            }
            is TimerDurabilityPolicy.Recovery.RestorePaused -> {
                timeEngine.restoreTimer(
                    durationMillis = snapshot.totalSeconds.coerceAtLeast(1) * 1_000L,
                    remainingMillis = recovery.remainingMillis,
                    running = false,
                )
            }
            TimerDurabilityPolicy.Recovery.Expired -> {
                repository.save(
                    TimerDurabilityPolicy.markCompletionPending(snapshot),
                )
                timeEngine.restoreTimer(
                    durationMillis = snapshot.totalSeconds.coerceAtLeast(1) * 1_000L,
                    remainingMillis = 0L,
                    running = false,
                )
            }
        }
    }

    private suspend fun persistCurrentState(
        endAtEpochMillis: Long = if (_state.value.isRunning) {
            timeEngine.currentEpochMillis() + (_state.value.remainingSeconds * 1_000L)
        } else {
            0L
        },
    ) {
        val timer = timeEngine.state.value.timer
        val remainingSeconds = ((timer.remainingMillis + 999L) / 1_000L)
            .toInt()
            .coerceAtLeast(0)
        val totalSeconds = (timer.durationMillis / 1_000L)
            .toInt()
            .coerceAtLeast(1)

        _state.value = TimerUiState(
            totalSeconds = totalSeconds,
            remainingSeconds = remainingSeconds,
            isRunning = timer.isRunning,
        )

        repository.save(
            TimerSnapshot(
                totalSeconds = totalSeconds,
                remainingSeconds = remainingSeconds,
                running = timer.isRunning,
                endAtEpochMillis = if (timer.isRunning) endAtEpochMillis else 0L,
                completionPending = false,
            ),
        )
    }

}
