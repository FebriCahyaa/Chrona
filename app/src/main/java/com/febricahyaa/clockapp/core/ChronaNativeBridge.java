package com.febricahyaa.clockapp.core;

import androidx.annotation.Keep;
import java.time.ZoneId;
import java.time.ZonedDateTime;

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

    public static boolean isNativeReady() {
        return NATIVE_READY;
    }

    /** Returns hour/minute/second angles for the device's current zone. */
    public static float[] anglesForEpochMillis(long epochMillis) {
        if (!NATIVE_READY) return ClockTimeMath.anglesForEpochMillis(epochMillis);
        int offsetMinutes = ZonedDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault()).getOffset().getTotalSeconds() / 60;
        return nativeAnglesForEpochMillis(epochMillis, offsetMinutes);
    }

    private static native float[] nativeAnglesForEpochMillis(long epochMillis, int offsetMinutes);

    public static long remainingSeconds(long endMillis, long nowMillis) {
        if (NATIVE_READY) return nativeRemainingSeconds(endMillis, nowMillis);
        return Math.max(0L, (endMillis - nowMillis) / 1_000L);
    }

    public static long elapsedMillis(long startMillis, long nowMillis) {
        if (NATIVE_READY) return nativeElapsedMillis(startMillis, nowMillis);
        return Math.max(0L, nowMillis - startMillis);
    }

    private static native long nativeRemainingSeconds(long endMillis, long nowMillis);
    private static native long nativeElapsedMillis(long startMillis, long nowMillis);
}
