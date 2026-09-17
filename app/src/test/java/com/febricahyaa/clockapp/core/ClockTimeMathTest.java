/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.core;

import static org.junit.Assert.assertEquals;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.Test;

public class ClockTimeMathTest {
    @Test
    public void midnightProducesZeroAnglesInUtc() {
        ZonedDateTime utc = ZonedDateTime.of(2026, 9, 15, 0, 0, 0, 0, ZoneId.of("UTC"));
        float[] angles = ClockTimeMath.anglesForEpochMillis(utc.toInstant().toEpochMilli(), ZoneId.of("UTC"));
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

    @Test
    public void halfPastSixProducesContinuousHourAngle() {
        ZonedDateTime local = ZonedDateTime.of(2026, 9, 15, 6, 30, 30, 0, ZoneId.of("Asia/Jakarta"));
        float[] angles = ClockTimeMath.anglesForEpochMillis(local.toInstant().toEpochMilli(), ZoneId.of("Asia/Jakarta"));
        assertEquals(195.2542f, angles[0], 0.01f);
        assertEquals(183f, angles[1], 0.01f);
    }

    @Test
    public void invalidSolarCoordinatesReturnInvalidPayload() {
        double[] solar = ClockTimeMath.solarTimes(91.0, 0.0, java.time.LocalDate.of(2026, 9, 15));
        assertEquals(0.0, solar[2], 0.0);
    }

    @Test
    public void cubicBezierHasStableEndpoints() {
        assertEquals(0.0, ClockTimeMath.cubicBezier(0.0, 0.0, 1.0, 1.0, 0.0), 0.0);
        assertEquals(0.0, ClockTimeMath.cubicBezier(1.0, 0.0, 1.0, 1.0, 0.0), 0.0);
    }
}
