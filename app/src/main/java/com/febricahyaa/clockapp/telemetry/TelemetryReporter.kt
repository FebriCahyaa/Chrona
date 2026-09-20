/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.telemetry

interface TelemetryReporter {
    fun recordAppStart()
    fun recordNonFatal(throwable: Throwable, context: String)
    fun recordJank(frameDurationUiNanos: Long, screen: String)
}
