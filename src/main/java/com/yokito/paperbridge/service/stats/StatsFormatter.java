package com.yokito.paperbridge.service.stats;

import com.yokito.paperbridge.model.stats.LeaderboardCategory;
import com.yokito.paperbridge.util.TimeUtil;

public class StatsFormatter {

    public String formatPlayTime(long ticks) {
        return TimeUtil.ticksToTimeString(ticks);
    }

    public String formatDamage(int rawDamage) {
        double hearts = rawDamage / 10.0;
        if (hearts >= 1000) {
            return String.format("%.2fk ❤", hearts / 1000.0);
        }
        return String.format("%.1f ❤", hearts);
    }

    public String formatDistance(long totalCm) {
        double meters = totalCm / 100.0;
        if (meters >= 1000) {
            return String.format("%.2f km", meters / 1000.0);
        }
        return String.format("%.1f m", meters);
    }

    public String formatLeaderboardValue(LeaderboardCategory category, long rawValue) {
        return switch (category) {
            case PLAYTIME -> formatPlayTime(rawValue);
            default -> String.valueOf(rawValue);
        };
    }
}
