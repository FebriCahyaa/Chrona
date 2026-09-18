/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.core

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

/** Deterministic Kotlin fallback for devices where the native library cannot load. */
object ClockTimeMath {
    private const val DAY_MILLIS = 86_400_000L
    private const val SYNODIC_MONTH_MILLIS = 2_551_443_939L
    private const val J2000_MILLIS = 946_728_000_000L

    @JvmStatic
    fun anglesForEpochMillis(epochMillis: Long): FloatArray =
        anglesForEpochMillis(epochMillis, ZoneId.systemDefault())

    @JvmStatic
    fun anglesForEpochMillis(epochMillis: Long, zone: ZoneId): FloatArray {
        requireNotNull(zone) { "zone must not be null" }
        val offsetMillis = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), zone)
            .offset
            .totalSeconds * 1_000L
        val localMillis = epochMillis + offsetMillis
        val dayMillis = Math.floorMod(localMillis, DAY_MILLIS)
        val seconds = dayMillis / 1000.0
        val hour = (seconds / 3600.0) % 12.0
        val minute = (seconds / 60.0) % 60.0
        val second = seconds % 60.0
        return floatArrayOf(
            (hour * 30.0).toFloat(),
            (minute * 6.0).toFloat(),
            (second * 6.0).toFloat(),
        )
    }

    /** Returns sunrise/sunset minute-of-day in UTC plus validity flag. */
    @JvmStatic
    fun solarTimes(latitude: Double, longitude: Double, date: LocalDate): DoubleArray {
        if (!latitude.isFinite() || !longitude.isFinite() ||
            latitude !in -90.0..90.0 || longitude !in -180.0..180.0
        ) {
            return doubleArrayOf(0.0, 0.0, 0.0)
        }

        val n = date.dayOfYear
        val lngHour = longitude / 15.0
        val riseApprox = n + ((6.0 - lngHour) / 24.0)
        val setApprox = n + ((18.0 - lngHour) / 24.0)
        val rise = solarUtcHour(latitude, lngHour, riseApprox, sunrise = true)
        val set = solarUtcHour(latitude, lngHour, setApprox, sunrise = false)
        if (!rise.isFinite() || !set.isFinite()) return doubleArrayOf(0.0, 0.0, 0.0)
        return doubleArrayOf(toMinutes(rise).toDouble(), toMinutes(set).toDouble(), 1.0)
    }

    private fun solarUtcHour(
        latitude: Double,
        longitudeHour: Double,
        approximate: Double,
        sunrise: Boolean,
    ): Double {
        val meanAnomaly = (0.9856 * approximate) - 3.289
        var trueLongitude = meanAnomaly +
            (1.916 * kotlin.math.sin(Math.toRadians(meanAnomaly))) +
            (0.020 * kotlin.math.sin(Math.toRadians(2.0 * meanAnomaly))) +
            282.634
        trueLongitude = normalizeDegrees(trueLongitude)

        var rightAscension = Math.toDegrees(
            kotlin.math.atan(0.91764 * kotlin.math.tan(Math.toRadians(trueLongitude)))
        )
        rightAscension = normalizeDegrees(rightAscension)
        val longitudeQuadrant = kotlin.math.floor(trueLongitude / 90.0) * 90.0
        val raQuadrant = kotlin.math.floor(rightAscension / 90.0) * 90.0
        rightAscension += longitudeQuadrant - raQuadrant
        rightAscension /= 15.0

        val sinDeclination = 0.39782 * kotlin.math.sin(Math.toRadians(trueLongitude))
        val cosDeclination = kotlin.math.cos(kotlin.math.asin(sinDeclination))
        val cosHourAngle = (
            kotlin.math.cos(Math.toRadians(90.833)) -
                sinDeclination * kotlin.math.sin(Math.toRadians(latitude))
            ) / (cosDeclination * kotlin.math.cos(Math.toRadians(latitude)))
        if (cosHourAngle > 1.0 || cosHourAngle < -1.0) return Double.NaN

        var localHourAngle = Math.toDegrees(kotlin.math.acos(cosHourAngle))
        if (sunrise) localHourAngle = 360.0 - localHourAngle
        localHourAngle /= 15.0

        var utc = localHourAngle + rightAscension - (0.06571 * approximate) - 6.622 - longitudeHour
        utc %= 24.0
        if (utc < 0.0) utc += 24.0
        return utc
    }

    private fun toMinutes(hours: Double): Int {
        var minutes = kotlin.math.round(hours * 60.0).toInt() % 1_440
        if (minutes < 0) minutes += 1_440
        return minutes
    }

    private fun normalizeDegrees(value: Double): Double {
        val result = value % 360.0
        return if (result < 0.0) result + 360.0 else result
    }

    @JvmStatic
    fun moonState(epochMillis: Long): DoubleArray {
        val phase = Math.floorMod(epochMillis - J2000_MILLIS, SYNODIC_MONTH_MILLIS) /
            SYNODIC_MONTH_MILLIS.toDouble()
        val illumination = 0.5 * (1.0 - kotlin.math.cos(2.0 * Math.PI * phase))
        val phaseIndex = kotlin.math.floor(phase * 8.0 + 0.5).toInt() % 8
        return doubleArrayOf(illumination, phaseIndex.toDouble(), phase * 29.530588853)
    }

    @JvmStatic
    fun springProgress(
        elapsedMs: Double,
        durationMs: Double,
        dampingRatio: Double,
        frequencyHz: Double,
    ): Double {
        if (durationMs <= 0.0 || !durationMs.isFinite()) return 1.0
        val t = (elapsedMs / durationMs).coerceIn(0.0, 1.0)
        val zeta = dampingRatio.coerceIn(0.05, 2.0)
        val omega = (2.0 * Math.PI * frequencyHz).coerceAtLeast(0.01)
        val seconds = (t * durationMs) / 1000.0
        if (zeta < 1.0) {
            val wd = omega * kotlin.math.sqrt(1.0 - zeta * zeta)
            val response = 1.0 - kotlin.math.exp(-zeta * omega * seconds) * (
                kotlin.math.cos(wd * seconds) +
                    (zeta * omega / wd) * kotlin.math.sin(wd * seconds)
                )
            return response.coerceIn(0.0, 1.0)
        }
        val response = 1.0 - (1.0 + omega * seconds) * kotlin.math.exp(-omega * seconds)
        return response.coerceIn(0.0, 1.0)
    }

    @JvmStatic
    fun cubicBezier(t: Double, p0: Double, p1: Double, p2: Double, p3: Double): Double {
        val clamped = t.coerceIn(0.0, 1.0)
        val u = 1.0 - clamped
        return u * u * u * p0 +
            3.0 * u * u * clamped * p1 +
            3.0 * u * clamped * clamped * p2 +
            clamped * clamped * clamped * p3
    }
}
