package com.febricahyaa.clockapp.core;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/** Pure-Java fallback for clock angle calculation and native/JNI verification. */
public final class ClockTimeMath {
    private ClockTimeMath() {}

    public static float[] anglesForEpochMillis(long epochMillis) {
        ZonedDateTime now = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault());
        float second = now.getSecond() + now.getNano() / 1_000_000_000f;
        float minute = now.getMinute() + second / 60f;
        float hour = (now.getHour() % 12) + minute / 60f;
        return new float[] {
            hour * 30f,
            minute * 6f,
            second * 6f
        };
    }
}
