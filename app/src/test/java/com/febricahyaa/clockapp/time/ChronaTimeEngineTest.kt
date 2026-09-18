package com.febricahyaa.clockapp.time

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChronaTimeEngineTest {
    @Test
    fun stopwatchUsesMonotonicElapsedTime() = runBlocking {
        val clock = FakeMonotonicClock(10_000L)
        val engine = newEngine(clock)
        try {
            engine.startStopwatch()
            clock.now = 12_345L
            engine.pauseStopwatch()

            assertEquals(2_345L, engine.state.value.stopwatch.elapsedMillis)
            assertFalse(engine.state.value.stopwatch.isRunning)
        } finally {
            engine.close()
        }
    }

    @Test
    fun timerUsesMonotonicEndBoundary() = runBlocking {
        val clock = FakeMonotonicClock(50_000L)
        val engine = newEngine(clock)
        try {
            engine.startTimer(5_000L)
            clock.now = 52_500L
            engine.pauseTimer()

            assertEquals(2_500L, engine.state.value.timer.remainingMillis)
            assertFalse(engine.state.value.timer.isRunning)
        } finally {
            engine.close()
        }
    }

    @Test
    fun explicitClockReadsRemainIndependentAndInjectable() {
        val mono = FakeMonotonicClock(9_000L)
        val wall = FakeWallClock(1_000_000L)
        val engine = newEngine(mono, wall)
        try {
            assertEquals(9_000L, engine.currentElapsedRealtimeMillis())
            assertEquals(1_000_000L, engine.currentEpochMillis())
            assertTrue(engine.wallClockMillis.value >= 1_000_000L)
        } finally {
            engine.close()
        }
    }

    private fun newEngine(
        monotonic: FakeMonotonicClock,
        wall: FakeWallClock = FakeWallClock(1_000_000L),
    ): ChronaTimeEngine = DefaultChronaTimeEngine(
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined),
        monotonicClock = monotonic,
        wallClock = wall,
    )

    private class FakeMonotonicClock(var now: Long) : ChronaMonotonicClock {
        override fun elapsedRealtime(): Long = now
    }

    private class FakeWallClock(private var now: Long) : ChronaWallClock {
        override fun epochMillis(): Long = now
    }
}
