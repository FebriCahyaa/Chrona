/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.time

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
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

    /** Shared wall-clock stream for foreground clock surfaces. */
    val wallClockMillis: StateFlow<Long>

    fun currentEpochMillis(): Long

    fun currentElapsedRealtimeMillis(): Long

    /**
     * Enables or disables high-frequency foreground rendering work.
     *
     * Timer and stopwatch semantics remain monotonic even while the app is
     * backgrounded; only UI pulse generation is suspended.
     */
    fun setForegroundActive(active: Boolean)

    suspend fun restoreStopwatch(
        elapsedMillis: Long,
        laps: List<StopwatchLap>,
        running: Boolean,
        startedAtElapsedRealtimeMillis: Long = 0L,
    )

    suspend fun startStopwatch()

    suspend fun pauseStopwatch()

    suspend fun resetStopwatch()

    suspend fun recordLap()

    suspend fun restoreTimer(
        durationMillis: Long,
        remainingMillis: Long,
        running: Boolean,
    )

    suspend fun configureTimer(durationMillis: Long)

    suspend fun startTimer(durationMillis: Long)

    suspend fun pauseTimer()

    suspend fun resetTimer()
}

class DefaultChronaTimeEngine(
    private val scope: CoroutineScope,
    private val monotonicClock: ChronaMonotonicClock = AndroidChronaMonotonicClock,
    private val wallClock: ChronaWallClock = AndroidChronaWallClock,
) : ChronaTimeEngine {

    private val stateMutex = Mutex()

    private val _state = MutableStateFlow(
        ChronaTimeState(
            elapsedRealtimeMillis = monotonicClock.elapsedRealtime(),
        ),
    )

    override val state: StateFlow<ChronaTimeState> = _state.asStateFlow()

    override val wallClockMillis: StateFlow<Long> = flow {
        while (kotlinx.coroutines.currentCoroutineContext().isActive) {
            emit(wallClock.epochMillis())
            delay(WALL_CLOCK_PULSE_MILLIS)
        }
    }.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = WALL_CLOCK_STOP_TIMEOUT_MILLIS),
        initialValue = wallClock.epochMillis(),
    )

    override fun currentEpochMillis(): Long = wallClock.epochMillis()

    override fun currentElapsedRealtimeMillis(): Long = monotonicClock.elapsedRealtime()

    override fun setForegroundActive(active: Boolean) {
        synchronized(tickerLock) {
            foregroundActive = active
            if (!active) {
                tickerJob?.cancel()
                tickerJob = null
            } else {
                ensureTickerLocked()
            }
        }
    }

    private var stopwatchStartedAt: Long? = null
    private var stopwatchAccumulatedMillis = 0L
    private var stopwatchLaps = emptyList<StopwatchLap>()

    private var timerEndAt: Long? = null
    private var timerDurationMillis = 0L
    private var timerRemainingMillis = 0L

    private val tickerLock = Any()
    private var tickerJob: Job? = null
    @Volatile
    private var foregroundActive = true

    override suspend fun restoreStopwatch(
        elapsedMillis: Long,
        laps: List<StopwatchLap>,
        running: Boolean,
        startedAtElapsedRealtimeMillis: Long,
    ) {
        stateMutex.withLock {
            val now = monotonicClock.elapsedRealtime()
            stopwatchAccumulatedMillis = elapsedMillis.coerceAtLeast(0L)
            stopwatchLaps = laps.toList()
            stopwatchStartedAt = if (running && startedAtElapsedRealtimeMillis > 0L) {
                startedAtElapsedRealtimeMillis
            } else {
                null
            }
            refreshStateLocked(now)
        }
        if (running) ensureTicker()
    }

    override suspend fun startStopwatch() {
        stateMutex.withLock {
            val now = monotonicClock.elapsedRealtime()
            if (stopwatchStartedAt == null) {
                stopwatchStartedAt = now
            }
            refreshStateLocked(now)
        }
        ensureTicker()
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

    override suspend fun restoreTimer(
        durationMillis: Long,
        remainingMillis: Long,
        running: Boolean,
    ) {
        stateMutex.withLock {
            val now = monotonicClock.elapsedRealtime()
            timerDurationMillis = durationMillis.coerceAtLeast(0L)
            timerRemainingMillis = remainingMillis.coerceIn(0L, timerDurationMillis)
            timerEndAt = if (running && timerRemainingMillis > 0L) {
                now + timerRemainingMillis
            } else {
                null
            }
            refreshStateLocked(now)
        }
        if (running) ensureTicker()
    }

    override suspend fun configureTimer(durationMillis: Long) {
        require(durationMillis > 0L) {
            "Timer duration must be greater than zero."
        }

        stateMutex.withLock {
            timerEndAt = null
            timerDurationMillis = durationMillis
            timerRemainingMillis = durationMillis
            refreshStateLocked(monotonicClock.elapsedRealtime())
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
        ensureTicker()
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
            timerRemainingMillis = timerDurationMillis
            refreshStateLocked(monotonicClock.elapsedRealtime())
        }
    }

    private fun ensureTicker() {
        synchronized(tickerLock) {
            ensureTickerLocked()
        }
    }

    private fun ensureTickerLocked() {
        if (!foregroundActive || tickerJob?.isActive == true) return

        tickerJob = scope.launch(Dispatchers.Default) {
            while (isActive) {
                val pulseMillis = stateMutex.withLock {
                    val now = monotonicClock.elapsedRealtime()
                    refreshStateLocked(now)
                    when {
                        ChronaTickerEligibilityPolicy.shouldTick(
                            foreground = foregroundActive,
                            active = stopwatchStartedAt != null,
                        ) -> STOPWATCH_PULSE_MILLIS
                        ChronaTickerEligibilityPolicy.shouldTick(
                            foreground = foregroundActive,
                            active = timerEndAt != null,
                        ) -> TIMER_PULSE_MILLIS
                        else -> null
                    }
                }

                pulseMillis ?: break
                delay(pulseMillis)
            }
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
        synchronized(tickerLock) {
            tickerJob?.cancel()
            tickerJob = null
        }
    }

    private companion object {
        const val WALL_CLOCK_PULSE_MILLIS = 250L
        const val WALL_CLOCK_STOP_TIMEOUT_MILLIS = 5_000L
        const val IDLE_PULSE_MILLIS = 250L
        const val TIMER_PULSE_MILLIS = 100L
        const val STOPWATCH_PULSE_MILLIS = 50L
    }
}
