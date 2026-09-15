package com.febricahyaa.clockapp.core;

import java.time.LocalDate;

/** Deterministic Java fallback for devices where the native library cannot load. */
public final class ClockTimeMath {
    private ClockTimeMath() {}

    public static float[] anglesForEpochMillis(long epochMillis) {
        final long day = Math.floorMod(epochMillis, 86_400_000L);
        final double seconds = day / 1000.0;
        final double hour = (seconds / 3600.0) % 12.0;
        final double minute = (seconds / 60.0) % 60.0;
        final double second = seconds % 60.0;
        return new float[] {(float) (hour * 30.0), (float) (minute * 6.0), (float) (second * 6.0)};
    }

    public static double[] solarTimes(double latitude, double longitude, LocalDate date) {
        return new double[] {0.0, 0.0, 0.0};
    }

    public static double[] moonState(long epochMillis) {
        double phase = Math.floorMod(epochMillis - 946684800000L, 2_551_443_939L) / 2_551_443_939.0;
        return new double[] {0.5 * (1.0 - Math.cos(2.0 * Math.PI * phase)), Math.floor(phase * 8.0) % 8.0, phase * 29.530588853};
    }

    public static double springProgress(double elapsedMs, double durationMs, double dampingRatio, double frequencyHz) {
        if (durationMs <= 0.0) return 1.0;
        double t = Math.max(0.0, Math.min(1.0, elapsedMs / durationMs));
        return 1.0 - Math.exp(-8.0 * t) * (1.0 + 8.0 * t);
    }

    public static double cubicBezier(double t, double p0, double p1, double p2, double p3) {
        t = Math.max(0.0, Math.min(1.0, t));
        double u = 1.0 - t;
        return u*u*u*p0 + 3.0*u*u*t*p1 + 3.0*u*t*t*p2 + t*t*t*p3;
    }
}
