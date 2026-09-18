/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.viewmodel

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.febricahyaa.clockapp.data.StopwatchRepository
import com.febricahyaa.clockapp.model.StopwatchSnapshot
import com.febricahyaa.clockapp.time.ChronaTimeEngine
import com.febricahyaa.clockapp.time.StopwatchLap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** UI projection of the shared Chrona stopwatch engine. */
data class StopwatchUiState(
    val elapsedMillis: Long = 0L,
    val isRunning: Boolean = false,
    val laps: List<Long> = emptyList(),
)

/**
 * Persistent stopwatch backed by the application-wide ChronaTimeEngine.
 *
 * The repository remains responsible for persistence while the engine owns
 * the monotonic running interval and shared timing ticker.
 */
class StopwatchViewModel(
    private val repository: StopwatchRepository,
    private val timeEngine: ChronaTimeEngine,
) : ViewModel() {

    private val _state = MutableStateFlow(StopwatchUiState())
    val state: StateFlow<StopwatchUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            restore()
            timeEngine.state.collect { snapshot ->
                val stopwatch = snapshot.stopwatch
                _state.value = StopwatchUiState(
                    elapsedMillis = stopwatch.elapsedMillis,
                    isRunning = stopwatch.isRunning,
                    laps = stopwatch.laps.map(StopwatchLap::elapsedMillis),
                )
            }
        }
    }

    fun toggleRun() {
        viewModelScope.launch {
            if (timeEngine.state.value.stopwatch.isRunning) {
                timeEngine.pauseStopwatch()
            } else {
                timeEngine.startStopwatch()
            }
            persistCurrentState()
        }
    }

    fun lap() {
        viewModelScope.launch {
            if (!timeEngine.state.value.stopwatch.isRunning) return@launch
            timeEngine.recordLap()
            persistCurrentState()
        }
    }

    fun reset() {
        viewModelScope.launch {
            timeEngine.resetStopwatch()
            persistCurrentState()
        }
    }

    private suspend fun restore() {
        val snapshot = repository.load()
        val now = SystemClock.elapsedRealtime()
        val canResume = snapshot.running &&
            snapshot.startElapsedRealtimeMillis > 0L &&
            snapshot.startElapsedRealtimeMillis <= now

        val resumeStartElapsedRealtimeMillis =
            if (canResume) now - snapshot.elapsedMillis.coerceAtLeast(0L) else 0L

        timeEngine.restoreStopwatch(
            elapsedMillis = snapshot.elapsedMillis,
            laps = snapshot.laps.mapIndexed { index, elapsedMillis ->
                StopwatchLap(index + 1, elapsedMillis.coerceAtLeast(0L))
            },
            running = canResume,
            startedAtElapsedRealtimeMillis = resumeStartElapsedRealtimeMillis,
        )

        if (snapshot.running && !canResume) {
            repository.save(
                snapshot.copy(
                    running = false,
                    startElapsedRealtimeMillis = 0L,
                    savedElapsedRealtimeMillis = now,
                ),
            )
        }
    }

    private suspend fun persistCurrentState() {
        val stopwatch = timeEngine.state.value.stopwatch
        val now = SystemClock.elapsedRealtime()

        _state.value = StopwatchUiState(
            elapsedMillis = stopwatch.elapsedMillis,
            isRunning = stopwatch.isRunning,
            laps = stopwatch.laps.map(StopwatchLap::elapsedMillis),
        )

        repository.save(
            StopwatchSnapshot(
                elapsedMillis = stopwatch.elapsedMillis,
                running = stopwatch.isRunning,
                startElapsedRealtimeMillis = if (stopwatch.isRunning) {
                    now - stopwatch.elapsedMillis
                } else {
                    0L
                },
                savedElapsedRealtimeMillis = now,
                laps = stopwatch.laps.map(StopwatchLap::elapsedMillis),
            ),
        )
    }
}
