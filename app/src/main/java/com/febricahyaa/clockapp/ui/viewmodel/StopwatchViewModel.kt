/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.viewmodel

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.febricahyaa.clockapp.core.config.AppDefaults
import com.febricahyaa.clockapp.data.StopwatchRepository
import com.febricahyaa.clockapp.model.StopwatchSnapshot
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** UI snapshot of the persistent stopwatch state. */
data class StopwatchUiState(
    val elapsedMillis: Long = 0L,
    val isRunning: Boolean = false,
    val laps: List<Long> = emptyList(),
)

/**
 * Persistent stopwatch. The running state is reconstructed from elapsedRealtime
 * after ordinary process death; a reboot causes the saved running state to be
 * safely restored as paused because elapsedRealtime resets at boot.
 */
class StopwatchViewModel(private val repository: StopwatchRepository) : ViewModel() {

    private val _state = MutableStateFlow(StopwatchUiState())
    val state: StateFlow<StopwatchUiState> = _state.asStateFlow()

    private var tickJob: Job? = null
    private var baseElapsedMillis = 0L
    private var startElapsedRealtimeMillis = 0L

    init {
        restore()
    }

    fun toggleRun() {
        if (_state.value.isRunning) pause() else start()
    }

    fun lap() {
        if (!_state.value.isRunning) return
        val elapsed = currentElapsed()
        _state.value = _state.value.copy(
            elapsedMillis = elapsed,
            laps = _state.value.laps + elapsed,
        )
        persist()
    }

    fun reset() {
        tickJob?.cancel()
        baseElapsedMillis = 0L
        startElapsedRealtimeMillis = 0L
        _state.value = StopwatchUiState()
        persist()
    }

    private fun start() {
        baseElapsedMillis = _state.value.elapsedMillis
        startElapsedRealtimeMillis = SystemClock.elapsedRealtime()
        _state.value = _state.value.copy(isRunning = true)
        persist()
        launchTicker()
    }

    private fun pause() {
        val elapsed = currentElapsed()
        tickJob?.cancel()
        baseElapsedMillis = elapsed
        startElapsedRealtimeMillis = 0L
        _state.value = _state.value.copy(elapsedMillis = elapsed, isRunning = false)
        persist()
    }

    private fun restore() {
        val snapshot = repository.load()
        val now = SystemClock.elapsedRealtime()
        val canResume = snapshot.running &&
            snapshot.startElapsedRealtimeMillis > 0L &&
            snapshot.startElapsedRealtimeMillis <= now

        if (canResume) {
            baseElapsedMillis = snapshot.elapsedMillis
            startElapsedRealtimeMillis = snapshot.startElapsedRealtimeMillis
            _state.value = StopwatchUiState(currentElapsed(), true, snapshot.laps)
            launchTicker()
        } else {
            baseElapsedMillis = snapshot.elapsedMillis
            startElapsedRealtimeMillis = 0L
            _state.value = StopwatchUiState(snapshot.elapsedMillis, false, snapshot.laps)
            if (snapshot.running) {
                repository.save(snapshot.copy(running = false, startElapsedRealtimeMillis = 0L, savedElapsedRealtimeMillis = now))
            }
        }
    }

    private fun launchTicker() {
        tickJob?.cancel()
        tickJob = viewModelScope.launch {
            var lastPersistSecond = -1L
            while (_state.value.isRunning) {
                val elapsed = currentElapsed()
                _state.value = _state.value.copy(elapsedMillis = elapsed, isRunning = true)
                val elapsedSecond = elapsed / 1_000L
                if (elapsedSecond != lastPersistSecond && elapsedSecond % 5L == 0L) {
                    lastPersistSecond = elapsedSecond
                    persist()
                }
                delay(AppDefaults.STOPWATCH_TICK_INTERVAL_MS)
            }
        }
    }

    private fun currentElapsed(): Long =
        if (startElapsedRealtimeMillis <= 0L) baseElapsedMillis
        else baseElapsedMillis + (SystemClock.elapsedRealtime() - startElapsedRealtimeMillis).coerceAtLeast(0L)

    private fun persist() {
        val currentElapsed = currentElapsed()
        _state.value = _state.value.copy(elapsedMillis = currentElapsed)
        val now = SystemClock.elapsedRealtime()
        repository.save(
            StopwatchSnapshot(
                elapsedMillis = currentElapsed,
                running = _state.value.isRunning,
                startElapsedRealtimeMillis = if (_state.value.isRunning) startElapsedRealtimeMillis else 0L,
                savedElapsedRealtimeMillis = now,
                laps = _state.value.laps,
            ),
        )
    }

    override fun onCleared() {
        tickJob?.cancel()
        super.onCleared()
    }
}
