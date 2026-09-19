/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.core

/** Pure, UI-independent duration parsing and formatting for the Chrona timer keypad. */
data class TimerDuration(
    val hours: Int,
    val minutes: Int,
    val seconds: Int,
) {
    val totalSeconds: Int
        get() = hours * 3_600 + minutes * 60 + seconds
}

object TimerDurationInput {
    const val MAX_TIMER_SECONDS: Int = 99 * 3_600 + 59 * 60 + 59

    fun parse(raw: String): TimerDuration? {
        val padded = raw
            .filter(Char::isDigit)
            .takeLast(6)
            .padStart(6, '0')
        val hours = padded.substring(0, 2).toInt()
        val minutes = padded.substring(2, 4).toInt()
        val seconds = padded.substring(4, 6).toInt()
        return if (minutes <= 59 && seconds <= 59) {
            TimerDuration(hours, minutes, seconds)
        } else {
            null
        }
    }

    fun toSeconds(raw: String): Int = parse(raw)?.totalSeconds ?: 0

    fun toDigits(totalSeconds: Int): String {
        val safe = totalSeconds.coerceIn(0, MAX_TIMER_SECONDS)
        val hours = safe / 3_600
        val minutes = (safe / 60) % 60
        val seconds = safe % 60
        return "%02d%02d%02d".format(hours, minutes, seconds).trimStart('0')
    }

    fun format(totalSeconds: Int): String {
        val safe = totalSeconds.coerceIn(0, MAX_TIMER_SECONDS)
        val hours = safe / 3_600
        val minutes = (safe / 60) % 60
        val seconds = safe % 60
        return "%02dh %02dm %02ds".format(hours, minutes, seconds)
    }
}
