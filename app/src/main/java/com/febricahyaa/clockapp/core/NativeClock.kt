package com.febricahyaa.clockapp.core

import java.time.LocalDate

/** Kotlin facade: Compose/application code never needs to know about JNI details. */
object NativeClock {
    fun isNativeReady(): Boolean = ChronaNativeBridge.isNativeReady()

    fun remainingMillis(endMillis: Long, nowMillis: Long): Long =
        ChronaNativeBridge.remainingMillis(endMillis, nowMillis)

    fun remainingSeconds(endMillis: Long, nowMillis: Long): Long =
        ChronaNativeBridge.remainingSeconds(endMillis, nowMillis)

    fun elapsedMillis(startMillis: Long, nowMillis: Long): Long =
        ChronaNativeBridge.elapsedMillis(startMillis, nowMillis)

    fun monotonicMillis(): Long = ChronaNativeBridge.monotonicMillis()

    fun solarTimes(latitude: Double, longitude: Double, date: LocalDate): SolarTimes =
        ChronaNativeBridge.solarTimes(latitude, longitude, date).let { values ->
            SolarTimes(values.getOrElse(0) { 0.0 }.toInt(), values.getOrElse(1) { 0.0 }.toInt(), values.getOrElse(2) { 0.0 } > 0.5)
        }

    fun moonState(epochMillis: Long): MoonState =
        ChronaNativeBridge.moonState(epochMillis).let { values ->
            MoonState(values.getOrElse(0) { 0.0 }, values.getOrElse(1) { 0.0 }.toInt(), values.getOrElse(2) { 0.0 })
        }

    fun springProgress(elapsedMs: Double, durationMs: Double, dampingRatio: Double = 0.85, frequencyHz: Double = 2.6): Double =
        ChronaNativeBridge.springProgress(elapsedMs, durationMs, dampingRatio, frequencyHz)

    fun cubicBezier(t: Double, p0: Double, p1: Double, p2: Double, p3: Double): Double =
        ChronaNativeBridge.cubicBezier(t, p0, p1, p2, p3)
}

data class SolarTimes(val sunriseMinutesUtc: Int, val sunsetMinutesUtc: Int, val valid: Boolean)
data class MoonState(val illumination: Double, val phaseIndex: Int, val ageDays: Double)
