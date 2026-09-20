/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.telemetry

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OssTelemetryModule {
    @Provides
    @Singleton
    fun provideTelemetryReporter(): TelemetryReporter = NoOpTelemetryReporter
}

private object NoOpTelemetryReporter : TelemetryReporter {
    override fun recordAppStart() = Unit
    override fun recordNonFatal(throwable: Throwable, context: String) = Unit
    override fun recordJank(frameDurationUiNanos: Long, screen: String) = Unit
}
