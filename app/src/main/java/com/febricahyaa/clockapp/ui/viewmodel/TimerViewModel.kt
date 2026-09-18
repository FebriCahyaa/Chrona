/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.febricahyaa.clockapp.core.config.AppDefaults
import com.febricahyaa.clockapp.data.TimerRepository
import com.febricahyaa.clockapp.model.TimerSnapshot
import com.febricahyaa.clockapp.time.ChronaTimeEngine
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
            scheduler.cancel()
            timeEngine.configureTimer(safe * 1_000L)
            persistCurrentState()
        }
    }

    fun reset() {
        viewModelScope.launch {
            scheduler.cancel()
            timeEngine.resetTimer()
            persistCurrentState()
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
            scheduler.schedule(endAtEpochMillis)
            persistCurrentState(endAtEpochMillis = endAtEpochMillis)
        }
    }

    private fun pause() {
        viewModelScope.launch {
            timeEngine.pauseTimer()
            scheduler.cancel()
            persistCurrentState()
        }
    }

    private suspend fun restore() {
        val snapshot = repository.load()
        val remaining = if (snapshot.running) {
            remainingSeconds(snapshot.endAtEpochMillis)
        } else {
            snapshot.remainingSeconds
        }.coerceAtLeast(0)

        if (snapshot.running && remaining > 0) {
            scheduler.schedule(snapshot.endAtEpochMillis)
            timeEngine.restoreTimer(
                durationMillis = snapshot.totalSeconds.coerceAtLeast(1) * 1_000L,
                remainingMillis = remaining * 1_000L,
                running = true,
            )
        } else {
            if (snapshot.running) {
                repository.save(
                    snapshot.copy(
                        running = false,
                        remainingSeconds = 0,
                        endAtEpochMillis = 0L,
                        completionPending = true,
                    ),
                )
            }

            timeEngine.restoreTimer(
                durationMillis = snapshot.totalSeconds.coerceAtLeast(1) * 1_000L,
                remainingMillis = remaining,
                running = false,
            )
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

    private fun remainingSeconds(endAtEpochMillis: Long): Int {
        if (endAtEpochMillis <= 0L) return 0
        val remainingMillis = endAtEpochMillis - timeEngine.currentEpochMillis()
        return if (remainingMillis <= 0L) {
            0
        } else {
            ((remainingMillis + 999L) / 1_000L).toInt()
        }
    }
}
