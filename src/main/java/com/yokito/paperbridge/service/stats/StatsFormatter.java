package com.yokito.paperbridge.service.stats;

import com.yokito.paperbridge.util.TimeUtil;

import java.util.Locale;

import javax.annotation.Nonnull;

public class StatsFormatter {

    @Nonnull
    public String formatPlayTime(long ticks) {
        return TimeUtil.ticksToTimeString(ticks);
    }

    @Nonnull
    public String formatDamage(long rawDamage) {
        double hearts = rawDamage / 10.0;
        String formatted;
        if (hearts >= 1000) {
            formatted = String.format(Locale.US, "%.2fk 顆心", hearts / 1000.0);
            return formatted == null ? "" : formatted;
        }
        formatted = String.format(Locale.US, "%.1f 顆心", hearts);
        return formatted == null ? "" : formatted;
    }

    @Nonnull
    public String formatDistance(long totalCm) {
        double meters = totalCm / 100.0;
        String formatted;
        if (meters >= 1000) {
            formatted = String.format(Locale.US, "%.2f km", meters / 1000.0);
            return formatted == null ? "" : formatted;
        }
        formatted = String.format(Locale.US, "%.1f m", meters);
        return formatted == null ? "" : formatted;
    }
}
