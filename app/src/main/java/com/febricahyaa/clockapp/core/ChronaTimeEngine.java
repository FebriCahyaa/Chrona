/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.core;

import androidx.annotation.Keep;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Java platform time layer. UI and orchestration stay in Kotlin; native math stays in C++. */
@Keep
public final class ChronaTimeEngine {

    private static final String PATTERN_24H_WITH_SECONDS = "HH:mm:ss";
    private static final String PATTERN_24H = "HH:mm";
    private static final String PATTERN_12H_WITH_SECONDS = "h:mm:ss a";
    private static final String PATTERN_12H = "h:mm a";
    private static final String PATTERN_DATE = "EEE, MMM d";
    private static final String UTC_LABEL = "UTC";
    private static final String UTC_OFFSET_FORMAT = "UTC%s%02d:%02d";

    private ChronaTimeEngine() {}

    // Intentionally reads Locale.getDefault() fresh on every call rather
    // than caching it in a static field: a static snapshot taken at class
    // load time would go stale the moment the user changes their system
    // locale in a running process (a config change does not reload this
    // class), silently freezing the clock's format in the old locale.
    private static Locale currentLocale() {
        return Locale.getDefault();
    }

    public static String time(long epochMillis, ZoneId zone, boolean use24Hour, boolean showSeconds) {
        ZonedDateTime now = Instant.ofEpochMilli(epochMillis).atZone(zone);
        String pattern;
        if (use24Hour) pattern = showSeconds ? PATTERN_24H_WITH_SECONDS : PATTERN_24H;
        else pattern = showSeconds ? PATTERN_12H_WITH_SECONDS : PATTERN_12H;
        return DateTimeFormatter.ofPattern(pattern, currentLocale()).format(now);
    }

    public static String shortTime(long epochMillis, ZoneId zone, boolean use24Hour) {
        ZonedDateTime now = Instant.ofEpochMilli(epochMillis).atZone(zone);
        String pattern = use24Hour ? PATTERN_24H : PATTERN_12H;
        return DateTimeFormatter.ofPattern(pattern, currentLocale()).format(now);
    }

    public static String date(long epochMillis, ZoneId zone) {
        return DateTimeFormatter.ofPattern(PATTERN_DATE, currentLocale())
                .format(Instant.ofEpochMilli(epochMillis).atZone(zone));
    }

    public static String utcOffset(ZoneId zone, long epochMillis) {
        ZonedDateTime now = Instant.ofEpochMilli(epochMillis).atZone(zone);
        int minutes = now.getOffset().getTotalSeconds() / 60;
        if (minutes == 0) return UTC_LABEL;
        int abs = Math.abs(minutes);
        return String.format(Locale.US, UTC_OFFSET_FORMAT, minutes > 0 ? "+" : "-", abs / 60, abs % 60);
    }
}
