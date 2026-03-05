package com.yokito.paperbridge.utils;

/**
 * 時間相關工具類。
 * 處理 Minecraft tick 與人類可讀時間格式之間的轉換。
 */
public class TimeUtil {

    private TimeUtil() {
        // 工具類不允許實例化
    }

    /**
     * 將 Minecraft tick 數轉換為人類可讀的時間字串。
     * <p>
     * Minecraft 中 {@code Statistic.PLAY_ONE_MINUTE} 的單位實際上是 tick (1 tick = 1/20 秒)。
     * </p>
     *
     * @param ticks tick 數量
     * @return 格式化的時間字串，例如 "3 天 5 小時 30 分鐘"
     */
    public static String ticksToTimeString(long ticks) {
        long totalSeconds = ticks / 20;
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append(" 天 ");
        }
        if (hours > 0 || days > 0) {
            sb.append(hours).append(" 小時 ");
        }
        sb.append(minutes).append(" 分鐘");

        return sb.toString().trim();
    }

    /**
     * 將秒數轉換為簡短的時間字串。
     *
     * @param totalSeconds 總秒數
     * @return 格式化的時間字串
     */
    public static String secondsToTimeString(long totalSeconds) {
        return ticksToTimeString(totalSeconds * 20);
    }
}
