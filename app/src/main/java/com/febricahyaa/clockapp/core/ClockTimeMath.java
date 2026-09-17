/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.core;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/** Deterministic Java fallback for devices where the native library cannot load. */
public final class ClockTimeMath {
    private static final long DAY_MILLIS = 86_400_000L;
    private static final long SYNODIC_MONTH_MILLIS = 2_551_443_939L;
    private static final long J2000_MILLIS = 946_728_000_000L;

    private ClockTimeMath() {
        throw new AssertionError("No instances");
    }

    public static float[] anglesForEpochMillis(long epochMillis) {
        return anglesForEpochMillis(epochMillis, ZoneId.systemDefault());
    }

    static float[] anglesForEpochMillis(long epochMillis, ZoneId zone) {
        if (zone == null) {
            throw new IllegalArgumentException("zone must not be null");
        }
        final int offsetMillis = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), zone)
                .getOffset()
                .getTotalSeconds() * 1_000;
        final long localMillis = epochMillis + offsetMillis;
        final long dayMillis = Math.floorMod(localMillis, DAY_MILLIS);
        final double seconds = dayMillis / 1000.0;
        final double hour = (seconds / 3600.0) % 12.0;
        final double minute = (seconds / 60.0) % 60.0;
        final double second = seconds % 60.0;
        return new float[]{(float) (hour * 30.0), (float) (minute * 6.0), (float) (second * 6.0)};
    }

    /** Returns sunrise/sunset minute-of-day in UTC plus validity flag. */
    public static double[] solarTimes(double latitude, double longitude, LocalDate date) {
        if (date == null
                || !Double.isFinite(latitude)
                || !Double.isFinite(longitude)
                || latitude < -90.0
                || latitude > 90.0
                || longitude < -180.0
                || longitude > 180.0) {
            return new double[]{0.0, 0.0, 0.0};
        }

        final int n = date.getDayOfYear();
        final double lngHour = longitude / 15.0;
        final double riseApprox = n + ((6.0 - lngHour) / 24.0);
        final double setApprox = n + ((18.0 - lngHour) / 24.0);

        final double rise = solarUtcHour(latitude, lngHour, riseApprox, true);
        final double set = solarUtcHour(latitude, lngHour, setApprox, false);
        if (!Double.isFinite(rise) || !Double.isFinite(set)) {
            return new double[]{0.0, 0.0, 0.0};
        }
        return new double[]{toMinutes(rise), toMinutes(set), 1.0};
    }

    private static double solarUtcHour(double latitude, double longitudeHour, double approximate, boolean sunrise) {
        final double meanAnomaly = (0.9856 * approximate) - 3.289;
        double trueLongitude = meanAnomaly
                + (1.916 * Math.sin(Math.toRadians(meanAnomaly)))
                + (0.020 * Math.sin(Math.toRadians(2.0 * meanAnomaly)))
                + 282.634;
        trueLongitude = normalizeDegrees(trueLongitude);

        double rightAscension = Math.toDegrees(Math.atan(0.91764 * Math.tan(Math.toRadians(trueLongitude))));
        rightAscension = normalizeDegrees(rightAscension);
        final double longitudeQuadrant = Math.floor(trueLongitude / 90.0) * 90.0;
        final double raQuadrant = Math.floor(rightAscension / 90.0) * 90.0;
        rightAscension += longitudeQuadrant - raQuadrant;
        rightAscension /= 15.0;

        final double sinDeclination = 0.39782 * Math.sin(Math.toRadians(trueLongitude));
        final double cosDeclination = Math.cos(Math.asin(sinDeclination));
        final double cosHourAngle = (Math.cos(Math.toRadians(90.833))
                - sinDeclination * Math.sin(Math.toRadians(latitude)))
                / (cosDeclination * Math.cos(Math.toRadians(latitude)));

        if (cosHourAngle > 1.0 || cosHourAngle < -1.0) {
            return Double.NaN;
        }

        double localHourAngle = Math.toDegrees(Math.acos(cosHourAngle));
        if (sunrise) {
            localHourAngle = 360.0 - localHourAngle;
        }
        localHourAngle /= 15.0;

        double utc = localHourAngle + rightAscension - (0.06571 * approximate) - 6.622 - longitudeHour;
        utc %= 24.0;
        if (utc < 0.0) utc += 24.0;
        return utc;
    }

    private static int toMinutes(double hours) {
        int minutes = (int) Math.round(hours * 60.0) % 1_440;
        if (minutes < 0) minutes += 1_440;
        return minutes;
    }

    private static double normalizeDegrees(double value) {
        double result = value % 360.0;
        return result < 0.0 ? result + 360.0 : result;
    }

    public static double[] moonState(long epochMillis) {
        final double phase = Math.floorMod(epochMillis - J2000_MILLIS, SYNODIC_MONTH_MILLIS)
                / (double) SYNODIC_MONTH_MILLIS;
        final double illumination = 0.5 * (1.0 - Math.cos(2.0 * Math.PI * phase));
        final int phaseIndex = (int) Math.floor(phase * 8.0 + 0.5) % 8;
        return new double[]{illumination, phaseIndex, phase * 29.530588853};
    }

    public static double springProgress(
            double elapsedMs,
            double durationMs,
            double dampingRatio,
            double frequencyHz
    ) {
        if (durationMs <= 0.0 || !Double.isFinite(durationMs)) {
            return 1.0;
        }
        final double t = Math.max(0.0, Math.min(1.0, elapsedMs / durationMs));
        final double zeta = Math.max(0.05, Math.min(2.0, dampingRatio));
        final double omega = Math.max(0.01, 2.0 * Math.PI * frequencyHz);
        final double seconds = (t * durationMs) / 1000.0;

        if (zeta < 1.0) {
            final double wd = omega * Math.sqrt(1.0 - zeta * zeta);
            final double response = 1.0 - Math.exp(-zeta * omega * seconds)
                    * (Math.cos(wd * seconds)
                    + (zeta * omega / wd) * Math.sin(wd * seconds));
            return Math.max(0.0, Math.min(1.0, response));
        }

        final double response = 1.0 - (1.0 + omega * seconds) * Math.exp(-omega * seconds);
        return Math.max(0.0, Math.min(1.0, response));
    }

    public static double cubicBezier(double t, double p0, double p1, double p2, double p3) {
        final double clamped = Math.max(0.0, Math.min(1.0, t));
        final double u = 1.0 - clamped;
        return u * u * u * p0
                + 3.0 * u * u * clamped * p1
                + 3.0 * u * clamped * clamped * p2
                + clamped * clamped * clamped * p3;
    }
}
