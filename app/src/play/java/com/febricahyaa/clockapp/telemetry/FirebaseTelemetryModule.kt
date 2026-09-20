/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.telemetry

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseTelemetryModule {
    @Provides
    @Singleton
    fun provideTelemetryReporter(@ApplicationContext context: Context): TelemetryReporter =
        FirebaseTelemetryReporter(context)
}

private class FirebaseTelemetryReporter(
    private val context: Context,
) : TelemetryReporter {
    private val crashlytics: FirebaseCrashlytics?
        get() = runCatching {
            if (!com.febricahyaa.clockapp.BuildConfig.CRASH_REPORTING_ENABLED) return null
            if (FirebaseApp.getApps(context).isEmpty()) return null
            FirebaseCrashlytics.getInstance()
        }.getOrNull()

    override fun recordAppStart() {
        crashlytics?.log("chrona_app_start")
    }

    override fun recordNonFatal(throwable: Throwable, context: String) {
        crashlytics?.setCustomKey("chrona_error_context", context)
        crashlytics?.recordException(throwable)
    }

    override fun recordJank(frameDurationUiNanos: Long, screen: String) {
        val durationMillis = frameDurationUiNanos / 1_000_000L
        crashlytics?.setCustomKey("chrona_jank_screen", screen)
        crashlytics?.setCustomKey("chrona_jank_duration_ms", durationMillis)
        crashlytics?.log("chrona_jank screen=$screen duration_ms=$durationMillis")
    }
}
