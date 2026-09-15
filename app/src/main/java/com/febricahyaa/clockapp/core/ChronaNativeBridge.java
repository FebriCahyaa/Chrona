package com.febricahyaa.clockapp.core;

import androidx.annotation.Keep;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Java platform boundary for Chrona's native C++ engine.
 * Android APIs remain on Java/Kotlin; deterministic math and time calculations
 * are delegated to the native layer when available.
 */
@Keep
public final class ChronaNativeBridge {
    private static final boolean NATIVE_READY;

    static {
        boolean loaded;
        try {
            System.loadLibrary("chrona_clock");
            loaded = true;
        } catch (UnsatisfiedLinkError error) {
            loaded = false;
        }
        NATIVE_READY = loaded;
    }

    private ChronaNativeBridge() {}

    public static boolean isNativeReady() { return NATIVE_READY; }

    public static float[] anglesForEpochMillis(long epochMillis) {
        if (!NATIVE_READY) return ClockTimeMath.anglesForEpochMillis(epochMillis);
        int offsetMinutes = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault())
                .getOffset().getTotalSeconds() / 60;
        return nativeAnglesForEpochMillis(epochMillis, offsetMinutes);
    }

    public static long remainingMillis(long endMillis, long nowMillis) {
        if (NATIVE_READY) return nativeRemainingMillis(endMillis, nowMillis);
        return Math.max(0L, endMillis - nowMillis);
    }

    public static long remainingSeconds(long endMillis, long nowMillis) {
        return remainingMillis(endMillis, nowMillis) / 1_000L;
    }

    public static long elapsedMillis(long startMillis, long nowMillis) {
        if (NATIVE_READY) return nativeElapsedMillis(startMillis, nowMillis);
        return Math.max(0L, nowMillis - startMillis);
    }

    public static long monotonicMillis() {
        if (NATIVE_READY) return nativeMonotonicMillis();
        return android.os.SystemClock.elapsedRealtime();
    }

    /** Returns sunrise/sunset UTC minute-of-day plus validity flag. */
    public static double[] solarTimes(double latitude, double longitude, LocalDate date) {
        if (!NATIVE_READY) return ClockTimeMath.solarTimes(latitude, longitude, date);
        return nativeSolarTimes(latitude, longitude, date.getYear(), date.getMonthValue(), date.getDayOfMonth());
    }

    /** Returns illumination [0..1], eight-part phase index, and moon age in days. */
    public static double[] moonState(long epochMillis) {
        if (!NATIVE_READY) return ClockTimeMath.moonState(epochMillis);
        return nativeMoonState(epochMillis);
    }

    public static double springProgress(double elapsedMs, double durationMs, double dampingRatio, double frequencyHz) {
        return NATIVE_READY
                ? nativeSpringProgress(elapsedMs, durationMs, dampingRatio, frequencyHz)
                : ClockTimeMath.springProgress(elapsedMs, durationMs, dampingRatio, frequencyHz);
    }

    public static double cubicBezier(double t, double p0, double p1, double p2, double p3) {
        return NATIVE_READY
                ? nativeCubicBezier(t, p0, p1, p2, p3)
                : ClockTimeMath.cubicBezier(t, p0, p1, p2, p3);
    }

    private static native float[] nativeAnglesForEpochMillis(long epochMillis, int offsetMinutes);
    private static native long nativeRemainingMillis(long endMillis, long nowMillis);
    private static native long nativeElapsedMillis(long startMillis, long nowMillis);
    private static native long nativeMonotonicMillis();
    private static native double[] nativeSolarTimes(double latitude, double longitude, int year, int month, int day);
    private static native double[] nativeMoonState(long epochMillis);
    private static native double nativeSpringProgress(double elapsedMs, double durationMs, double dampingRatio, double frequencyHz);
    private static native double nativeCubicBezier(double t, double p0, double p1, double p2, double p3);
}
