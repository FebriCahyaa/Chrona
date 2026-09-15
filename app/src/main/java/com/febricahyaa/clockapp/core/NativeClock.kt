package com.febricahyaa.clockapp.core

import java.time.LocalDate

/**
 * Typed Kotlin facade for Chrona's native engine.
 * UI/features depend on this class instead of JNI details or raw arrays.
 */
object NativeClock {
    const val PHASE_COUNT = 8

    fun isNativeReady(): Boolean = ChronaNativeBridge.isNativeReady()

    fun remainingMillis(endMillis: Long, nowMillis: Long): Long =
        ChronaNativeBridge.remainingMillis(endMillis, nowMillis)

    fun remainingSeconds(endMillis: Long, nowMillis: Long): Long =
        ChronaNativeBridge.remainingSeconds(endMillis, nowMillis)

    fun elapsedMillis(startMillis: Long, nowMillis: Long): Long =
        ChronaNativeBridge.elapsedMillis(startMillis, nowMillis)

    fun monotonicMillis(): Long = ChronaNativeBridge.monotonicMillis()

    fun clockAngles(epochMillis: Long): ClockAngles =
        ChronaNativeBridge.anglesForEpochMillis(epochMillis).let { values ->
            ClockAngles(
                hour = values[0],
                minute = values[1],
                second = values[2],
            )
        }

    fun solarTimes(latitude: Double, longitude: Double, date: LocalDate): SolarTimes =
        ChronaNativeBridge.solarTimes(latitude, longitude, date).let { values ->
            SolarTimes(
                sunriseMinutesUtc = values[0].toInt(),
                sunsetMinutesUtc = values[1].toInt(),
                valid = values[2] > 0.5,
            )
        }

    fun moonState(epochMillis: Long): MoonState =
        ChronaNativeBridge.moonState(epochMillis).let { values ->
            MoonState(
                illumination = values[0].coerceIn(0.0, 1.0),
                phaseIndex = values[1].toInt().mod(PHASE_COUNT),
                ageDays = values[2].coerceIn(0.0, 29.530588853),
            )
        }

    fun springProgress(
        elapsedMs: Double,
        durationMs: Double,
        dampingRatio: Double = 0.85,
        frequencyHz: Double = 2.6,
    ): Double = ChronaNativeBridge.springProgress(
        elapsedMs,
        durationMs,
        dampingRatio,
        frequencyHz,
    )

    fun cubicBezier(
        t: Double,
        p0: Double,
        p1: Double,
        p2: Double,
        p3: Double,
    ): Double = ChronaNativeBridge.cubicBezier(t, p0, p1, p2, p3)
}

data class ClockAngles(
    val hour: Float,
    val minute: Float,
    val second: Float,
)

data class SolarTimes(
    val sunriseMinutesUtc: Int,
    val sunsetMinutesUtc: Int,
    val valid: Boolean,
)

data class MoonState(
    val illumination: Double,
    val phaseIndex: Int,
    val ageDays: Double,
)
