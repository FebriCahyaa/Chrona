package com.febricahyaa.clockapp.core;

import static org.junit.Assert.assertEquals;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.Test;

public class ClockTimeMathTest {
    @Test
    public void midnightProducesZeroAnglesInUtc() {
        ZonedDateTime utc = ZonedDateTime.of(2026, 9, 15, 0, 0, 0, 0, ZoneId.of("UTC"));
        float[] angles = ClockTimeMath.anglesForEpochMillis(utc.toInstant().toEpochMilli());
        assertEquals(0f, angles[0], 0.001f);
        assertEquals(0f, angles[1], 0.001f);
        assertEquals(0f, angles[2], 0.001f);
    }

    @Test
    public void sixThirtyProducesExpectedHourAndMinuteAnglesInDeviceZoneFallback() {
        ZonedDateTime local = ZonedDateTime.of(2026, 9, 15, 6, 30, 0, 0, ZoneId.systemDefault());
        float[] angles = ClockTimeMath.anglesForEpochMillis(local.toInstant().toEpochMilli());
        assertEquals(195f, angles[0], 0.001f);
        assertEquals(180f, angles[1], 0.001f);
    }
}
