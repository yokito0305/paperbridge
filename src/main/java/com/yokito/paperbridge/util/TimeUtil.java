package com.yokito.paperbridge.util;

import javax.annotation.Nonnull;

public class TimeUtil {

    private TimeUtil() {
    }

    @Nonnull
    public static String ticksToTimeString(long ticks) {
        long totalSeconds = ticks / 20;
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        String formatted;

        StringBuilder builder = new StringBuilder();
        if (days > 0) {
            builder.append(days).append(" 天 ");
        }
        if (hours > 0 || days > 0) {
            builder.append(hours).append(" 小時 ");
        }
        builder.append(minutes).append(" 分");
        formatted = builder.toString().trim();
        return formatted == null ? "" : formatted;
    }

    @Nonnull
    public static String secondsToTimeString(long totalSeconds) {
        return ticksToTimeString(totalSeconds * 20);
    }
}
