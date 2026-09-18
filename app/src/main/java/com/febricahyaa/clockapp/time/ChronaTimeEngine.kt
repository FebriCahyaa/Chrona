/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.time

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.max

interface ChronaTimeEngine : AutoCloseable {

    val state: StateFlow<ChronaTimeState>

    suspend fun startStopwatch()

    suspend fun pauseStopwatch()

    suspend fun resetStopwatch()

    suspend fun recordLap()

    suspend fun startTimer(durationMillis: Long)

    suspend fun pauseTimer()

    suspend fun resetTimer()
}

class DefaultChronaTimeEngine(
    private val scope: CoroutineScope,
    private val monotonicClock: ChronaMonotonicClock = AndroidChronaMonotonicClock,
) : ChronaTimeEngine {

    private val stateMutex = Mutex()

    private val _state = MutableStateFlow(
        ChronaTimeState(
            elapsedRealtimeMillis = monotonicClock.elapsedRealtime(),
        ),
    )

    override val state: StateFlow<ChronaTimeState> = _state.asStateFlow()

    private var stopwatchStartedAt: Long? = null
    private var stopwatchAccumulatedMillis = 0L
    private var stopwatchLaps = emptyList<StopwatchLap>()

    private var timerEndAt: Long? = null
    private var timerDurationMillis = 0L
    private var timerRemainingMillis = 0L

    private val tickerJob: Job = scope.launch(Dispatchers.Default) {
        runTicker()
    }

    override suspend fun startStopwatch() {
        stateMutex.withLock {
            val now = monotonicClock.elapsedRealtime()
            if (stopwatchStartedAt == null) {
                stopwatchStartedAt = now
            }
            refreshStateLocked(now)
        }
    }

    override suspend fun pauseStopwatch() {
        stateMutex.withLock {
            val now = monotonicClock.elapsedRealtime()
            stopwatchStartedAt?.let { startedAt ->
                stopwatchAccumulatedMillis = max(
                    0L,
                    stopwatchAccumulatedMillis + (now - startedAt),
                )
            }
            stopwatchStartedAt = null
            refreshStateLocked(now)
        }
    }

    override suspend fun resetStopwatch() {
        stateMutex.withLock {
            stopwatchStartedAt = null
            stopwatchAccumulatedMillis = 0L
            stopwatchLaps = emptyList()
            refreshStateLocked(monotonicClock.elapsedRealtime())
        }
    }

    override suspend fun recordLap() {
        stateMutex.withLock {
            val now = monotonicClock.elapsedRealtime()
            val elapsed = calculateStopwatchElapsed(now)
            if (elapsed <= 0L) return@withLock

            stopwatchLaps = stopwatchLaps + StopwatchLap(
                index = stopwatchLaps.size + 1,
                elapsedMillis = elapsed,
            )
            refreshStateLocked(now)
        }
    }

    override suspend fun startTimer(durationMillis: Long) {
        require(durationMillis > 0L) {
            "Timer duration must be greater than zero."
        }

        stateMutex.withLock {
            val now = monotonicClock.elapsedRealtime()
            timerDurationMillis = durationMillis
            timerRemainingMillis = durationMillis
            timerEndAt = now + durationMillis
            refreshStateLocked(now)
        }
    }

    override suspend fun pauseTimer() {
        stateMutex.withLock {
            val now = monotonicClock.elapsedRealtime()
            timerEndAt?.let { endAt ->
                timerRemainingMillis = max(0L, endAt - now)
            }
            timerEndAt = null
            refreshStateLocked(now)
        }
    }

    override suspend fun resetTimer() {
        stateMutex.withLock {
            timerEndAt = null
            timerDurationMillis = 0L
            timerRemainingMillis = 0L
            refreshStateLocked(monotonicClock.elapsedRealtime())
        }
    }

    private suspend fun runTicker() {
        while (scope.coroutineContext.isActive) {
            stateMutex.withLock {
                refreshStateLocked(monotonicClock.elapsedRealtime())
            }

            val now = monotonicClock.elapsedRealtime()
            val nextSecondBoundary = 1_000L - Math.floorMod(now, 1_000L)
            delay(nextSecondBoundary.coerceAtLeast(1L))
        }
    }

    private fun refreshStateLocked(now: Long) {
        val stopwatchElapsed = calculateStopwatchElapsed(now)

        val timerRemaining = timerEndAt?.let { endAt ->
            max(0L, endAt - now)
        } ?: timerRemainingMillis

        val timerRunning = timerEndAt != null && timerRemaining > 0L

        if (timerEndAt != null && !timerRunning) {
            timerEndAt = null
        }

        timerRemainingMillis = timerRemaining

        _state.value = ChronaTimeState(
            elapsedRealtimeMillis = now,
            stopwatch = StopwatchState(
                isRunning = stopwatchStartedAt != null,
                elapsedMillis = stopwatchElapsed,
                laps = stopwatchLaps,
            ),
            timer = TimerState(
                isRunning = timerRunning,
                durationMillis = timerDurationMillis,
                remainingMillis = timerRemainingMillis,
            ),
        )
    }

    private fun calculateStopwatchElapsed(now: Long): Long {
        val startedAt = stopwatchStartedAt ?: return stopwatchAccumulatedMillis
        return max(0L, stopwatchAccumulatedMillis + (now - startedAt))
    }

    override fun close() {
        tickerJob.cancel()
    }
}
