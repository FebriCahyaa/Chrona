/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.febricahyaa.clockapp.core.config.AppDefaults
import com.febricahyaa.clockapp.data.TimerRepository
import com.febricahyaa.clockapp.model.TimerSnapshot
import com.febricahyaa.clockapp.timer.TimerSchedulerGateway
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** UI snapshot of the persistent timer state. */
data class TimerUiState(
    val totalSeconds: Int = AppDefaults.DEFAULT_TIMER_SECONDS,
    val remainingSeconds: Int = AppDefaults.DEFAULT_TIMER_SECONDS,
    val isRunning: Boolean = false,
)

/**
 * Persistent countdown timer.
 *
 * AlarmManager owns the expiry wake-up so the timer does not depend on a
 * Compose coroutine surviving process death. The ViewModel owns only the
 * visible second-by-second projection while the UI is alive.
 */
class TimerViewModel(
    private val repository: TimerRepository,
    private val scheduler: TimerSchedulerGateway,
) : ViewModel() {

    private val _state = MutableStateFlow(TimerUiState())
    val state: StateFlow<TimerUiState> = _state.asStateFlow()

    private var tickJob: Job? = null
    private var endAtEpochMillis: Long = 0L

    init {
        restore()
    }

    fun setPreset(totalSeconds: Int) {
        scheduler.cancel()
        tickJob?.cancel()
        val safe = totalSeconds.coerceAtLeast(1)
        endAtEpochMillis = 0L
        _state.value = TimerUiState(safe, safe, false)
        persist()
    }

    fun reset() {
        scheduler.cancel()
        tickJob?.cancel()
        endAtEpochMillis = 0L
        _state.value = _state.value.copy(
            remainingSeconds = _state.value.totalSeconds,
            isRunning = false,
        )
        persist()
    }

    fun toggle() {
        if (_state.value.isRunning) pause() else if (_state.value.remainingSeconds > 0) start()
    }

    private fun start() {
        val remainingSeconds = _state.value.remainingSeconds.coerceAtLeast(1)
        endAtEpochMillis = System.currentTimeMillis() + remainingSeconds * 1_000L
        _state.value = _state.value.copy(isRunning = true)
        persist()
        scheduler.schedule(endAtEpochMillis)
        launchTicker()
    }

    private fun pause() {
        val remaining = remainingSeconds(endAtEpochMillis)
        scheduler.cancel()
        tickJob?.cancel()
        endAtEpochMillis = 0L
        _state.value = _state.value.copy(
            remainingSeconds = remaining,
            isRunning = false,
        )
        persist()
    }

    private fun restore() {
        val snapshot = repository.load()
        val remaining = if (snapshot.running) remainingSeconds(snapshot.endAtEpochMillis) else snapshot.remainingSeconds
        if (snapshot.running && remaining > 0) {
            endAtEpochMillis = snapshot.endAtEpochMillis
            _state.value = TimerUiState(snapshot.totalSeconds, remaining, true)
            scheduler.schedule(endAtEpochMillis)
            launchTicker()
        } else {
            endAtEpochMillis = 0L
            _state.value = TimerUiState(snapshot.totalSeconds, remaining.coerceAtLeast(0), false)
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
        }
    }

    private fun launchTicker() {
        tickJob?.cancel()
        tickJob = viewModelScope.launch {
            while (_state.value.isRunning) {
                val remaining = remainingSeconds(endAtEpochMillis)
                if (remaining <= 0) {
                    endAtEpochMillis = 0L
                    _state.value = _state.value.copy(remainingSeconds = 0, isRunning = false)
                    repository.save(
                        TimerSnapshot(
                            totalSeconds = _state.value.totalSeconds,
                            remainingSeconds = 0,
                            running = false,
                            endAtEpochMillis = 0L,
                            completionPending = true,
                        ),
                    )
                    // Do not cancel the AlarmManager operation here. It is the
                    // durable completion signal that wakes TimerReceiver and
                    // hands playback to TimerService. User-driven reset/pause
                    // still cancels the scheduler before clearing this marker.
                    break
                }
                _state.value = _state.value.copy(remainingSeconds = remaining, isRunning = true)

                // Re-align to the next wall-clock second instead of sleeping a
                // fixed interval. The deadline remains the source of truth, so
                // scheduler/process jitter cannot accumulate into timer drift.
                val now = System.currentTimeMillis()
                val remainder = Math.floorMod(now, 1_000L)
                delay((1_000L - remainder).coerceAtLeast(16L))
            }
        }
    }

    private fun persist() {
        val current = _state.value
        repository.save(
            TimerSnapshot(
                totalSeconds = current.totalSeconds,
                remainingSeconds = if (current.isRunning) remainingSeconds(endAtEpochMillis) else current.remainingSeconds,
                running = current.isRunning,
                endAtEpochMillis = if (current.isRunning) endAtEpochMillis else 0L,
                completionPending = false,
            ),
        )
    }

    private fun remainingSeconds(endAt: Long): Int {
        if (endAt <= 0L) return 0
        val remainingMillis = endAt - System.currentTimeMillis()
        return if (remainingMillis <= 0L) 0 else ((remainingMillis + 999L) / 1_000L).toInt()
    }

    override fun onCleared() {
        tickJob?.cancel()
        super.onCleared()
    }
}
