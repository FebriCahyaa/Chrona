package com.febricahyaa.clockapp.core;

import androidx.annotation.Keep;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Java platform boundary for Chrona's native C++ engine.
 *
 * <p>Java owns JNI and Android/platform time access. The native layer owns
 * deterministic time/math primitives. All native array results are validated
 * here so Kotlin never receives a malformed native payload.</p>
 */
@Keep
public final class ChronaNativeBridge {
    private static final int SOLAR_RESULT_SIZE = 3;
    private static final int MOON_RESULT_SIZE = 3;
    private static final int ANGLE_RESULT_SIZE = 3;
    private static final int NATIVE_ERROR_FALLBACK = 0;

    private static final boolean NATIVE_READY;

    static {
        NATIVE_READY = loadNativeLibrary();
    }

    private ChronaNativeBridge() {
        throw new AssertionError("No instances");
    }

    private static boolean loadNativeLibrary() {
        try {
            System.loadLibrary("chrona_clock");
            return true;
        } catch (UnsatisfiedLinkError ignored) {
            return false;
        }
    }

    public static boolean isNativeReady() {
        return NATIVE_READY;
    }

    public static float[] anglesForEpochMillis(long epochMillis) {
        if (!NATIVE_READY) {
            return ClockTimeMath.anglesForEpochMillis(epochMillis);
        }

        final ZoneId zone = ZoneId.systemDefault();
        final int offsetMinutes = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), zone)
                .getOffset()
                .getTotalSeconds() / 60;
        return requireFloatArray(
                nativeAnglesForEpochMillis(epochMillis, offsetMinutes),
                ANGLE_RESULT_SIZE,
                "clock angles"
        );
    }

    public static long remainingMillis(long endMillis, long nowMillis) {
        if (NATIVE_READY) {
            return nativeRemainingMillis(endMillis, nowMillis);
        }
        return Math.max(0L, endMillis - nowMillis);
    }

    public static long remainingSeconds(long endMillis, long nowMillis) {
        return remainingMillis(endMillis, nowMillis) / 1_000L;
    }

    public static long elapsedMillis(long startMillis, long nowMillis) {
        if (NATIVE_READY) {
            return nativeElapsedMillis(startMillis, nowMillis);
        }
        return Math.max(0L, nowMillis - startMillis);
    }

    public static long monotonicMillis() {
        if (NATIVE_READY) {
            return nativeMonotonicMillis();
        }
        return android.os.SystemClock.elapsedRealtime();
    }

    /** Returns sunrise/sunset minute-of-day in UTC plus validity flag. */
    public static double[] solarTimes(double latitude, double longitude, LocalDate date) {
        if (!NATIVE_READY) {
            return ClockTimeMath.solarTimes(latitude, longitude, date);
        }
        if (date == null) {
            throw new IllegalArgumentException("date must not be null");
        }
        return requireDoubleArray(
                nativeSolarTimes(latitude, longitude, date.getYear(), date.getMonthValue(), date.getDayOfMonth()),
                SOLAR_RESULT_SIZE,
                "solar times"
        );
    }

    /** Returns illumination [0..1], eight-part phase index, and moon age in days. */
    public static double[] moonState(long epochMillis) {
        if (!NATIVE_READY) {
            return ClockTimeMath.moonState(epochMillis);
        }
        return requireDoubleArray(nativeMoonState(epochMillis), MOON_RESULT_SIZE, "moon state");
    }

    public static double springProgress(
            double elapsedMs,
            double durationMs,
            double dampingRatio,
            double frequencyHz
    ) {
        return NATIVE_READY
                ? nativeSpringProgress(elapsedMs, durationMs, dampingRatio, frequencyHz)
                : ClockTimeMath.springProgress(elapsedMs, durationMs, dampingRatio, frequencyHz);
    }

    public static double cubicBezier(double t, double p0, double p1, double p2, double p3) {
        return NATIVE_READY
                ? nativeCubicBezier(t, p0, p1, p2, p3)
                : ClockTimeMath.cubicBezier(t, p0, p1, p2, p3);
    }

    private static float[] requireFloatArray(float[] value, int expectedSize, String name) {
        if (value == null || value.length != expectedSize) {
            throw new IllegalStateException(
                    "Native " + name + " result mismatch: expected " + expectedSize
                            + ", got " + (value == null ? NATIVE_ERROR_FALLBACK : value.length)
            );
        }
        return value;
    }

    private static double[] requireDoubleArray(double[] value, int expectedSize, String name) {
        if (value == null || value.length != expectedSize) {
            throw new IllegalStateException(
                    "Native " + name + " result mismatch: expected " + expectedSize
                            + ", got " + (value == null ? NATIVE_ERROR_FALLBACK : value.length)
            );
        }
        return value;
    }

    private static native float[] nativeAnglesForEpochMillis(long epochMillis, int offsetMinutes);
    private static native long nativeRemainingMillis(long endMillis, long nowMillis);
    private static native long nativeElapsedMillis(long startMillis, long nowMillis);
    private static native long nativeMonotonicMillis();
    private static native double[] nativeSolarTimes(double latitude, double longitude, int year, int month, int day);
    private static native double[] nativeMoonState(long epochMillis);
    private static native double nativeSpringProgress(
            double elapsedMs,
            double durationMs,
            double dampingRatio,
            double frequencyHz
    );
    private static native double nativeCubicBezier(
            double t,
            double p0,
            double p1,
            double p2,
            double p3
    );
}
