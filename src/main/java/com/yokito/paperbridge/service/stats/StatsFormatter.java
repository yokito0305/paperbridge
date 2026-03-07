package com.yokito.paperbridge.service.stats;

import com.yokito.paperbridge.util.TimeUtil;

import java.util.Locale;

import javax.annotation.Nonnull;

/**
 * 將原始統計數值格式化為適合 UI 顯示的文字。
 *
 * <p>此類別只關注數值呈現，例如時間、傷害與距離的格式，不負責統計來源的取得。</p>
 */
public class StatsFormatter {

    /**
     * 將 Bukkit ticks 格式化成天/小時/分鐘字串。
     */
    @Nonnull
    public String formatPlayTime(long ticks) {
        return TimeUtil.ticksToTimeString(ticks);
    }

    /**
     * 將原始傷害值轉成以顆心為單位的文字。
     */
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

    /**
     * 將原始公分距離轉成公尺或公里文字。
     */
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
