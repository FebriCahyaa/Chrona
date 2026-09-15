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
    private static final Locale LOCALE = Locale.getDefault();
    private ChronaTimeEngine() {}

    public static String time(long epochMillis, ZoneId zone, boolean use24Hour, boolean showSeconds) {
        ZonedDateTime now = Instant.ofEpochMilli(epochMillis).atZone(zone);
        String pattern;
        if (use24Hour) pattern = showSeconds ? "HH:mm:ss" : "HH:mm";
        else pattern = showSeconds ? "h:mm:ss a" : "h:mm a";
        return DateTimeFormatter.ofPattern(pattern, LOCALE).format(now);
    }

    public static String shortTime(long epochMillis, ZoneId zone, boolean use24Hour) {
        ZonedDateTime now = Instant.ofEpochMilli(epochMillis).atZone(zone);
        return DateTimeFormatter.ofPattern(use24Hour ? "HH:mm" : "h:mm a", LOCALE).format(now);
    }

    public static String date(long epochMillis, ZoneId zone) {
        return DateTimeFormatter.ofPattern("EEE, MMM d", LOCALE)
                .format(Instant.ofEpochMilli(epochMillis).atZone(zone));
    }

    public static String utcOffset(ZoneId zone, long epochMillis) {
        ZonedDateTime now = Instant.ofEpochMilli(epochMillis).atZone(zone);
        int minutes = now.getOffset().getTotalSeconds() / 60;
        if (minutes == 0) return "UTC";
        int abs = Math.abs(minutes);
        return String.format(Locale.US, "UTC%s%02d:%02d", minutes > 0 ? "+" : "-", abs / 60, abs % 60);
    }
}
