package com.yokito.paperbridge.util;

public class TimeUtil {

    private TimeUtil() {
    }

    public static String ticksToTimeString(long ticks) {
        long totalSeconds = ticks / 20;
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;

        StringBuilder builder = new StringBuilder();
        if (days > 0) {
            builder.append(days).append(" 天 ");
        }
        if (hours > 0 || days > 0) {
            builder.append(hours).append(" 小時 ");
        }
        builder.append(minutes).append(" 分鐘");
        return builder.toString().trim();
    }

    public static String secondsToTimeString(long totalSeconds) {
        return ticksToTimeString(totalSeconds * 20);
    }
}
