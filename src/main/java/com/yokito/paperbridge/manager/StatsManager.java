package com.yokito.paperbridge.manager;

import com.yokito.paperbridge.PaperBridge;
import com.yokito.paperbridge.utils.TimeUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 核心統計數據管理器 (Service Layer)。
 * <p>
 * 負責從 Bukkit 的 Statistic API 抓取玩家的遊戲統計數據，
 * 並提供格式化後的結果供指令或 Discord 互動使用。
 * </p>
 */
public class StatsManager {

    private final PaperBridge plugin;

    public StatsManager(PaperBridge plugin) {
        this.plugin = plugin;
    }

    /**
     * 取得指定玩家的格式化統計數據。
     *
     * @param player 目標玩家 (可以是離線玩家)
     * @return 以 key-value 形式回傳的統計資料 Map
     */
    public Map<String, String> getFormattedStats(OfflinePlayer player) {
        Map<String, String> stats = new LinkedHashMap<>();

        int deaths = player.getStatistic(Statistic.DEATHS);
        int playerKills = player.getStatistic(Statistic.PLAYER_KILLS);
        int mobKills = player.getStatistic(Statistic.MOB_KILLS);
        int playTimeTicks = player.getStatistic(Statistic.PLAY_ONE_MINUTE); // 單位為 tick
        int damageTaken = player.getStatistic(Statistic.DAMAGE_TAKEN);
        int damageDealt = player.getStatistic(Statistic.DAMAGE_DEALT);

        // 移動距離 (多種方式加總，單位為 cm → 轉為 km)
        long totalDistanceCm = 0L;
        totalDistanceCm += player.getStatistic(Statistic.WALK_ONE_CM);
        totalDistanceCm += player.getStatistic(Statistic.SPRINT_ONE_CM);
        totalDistanceCm += player.getStatistic(Statistic.SWIM_ONE_CM);
        totalDistanceCm += player.getStatistic(Statistic.FLY_ONE_CM);
        totalDistanceCm += player.getStatistic(Statistic.BOAT_ONE_CM);
        totalDistanceCm += player.getStatistic(Statistic.HORSE_ONE_CM);
        totalDistanceCm += player.getStatistic(Statistic.MINECART_ONE_CM);
        totalDistanceCm += player.getStatistic(Statistic.AVIATE_ONE_CM); // 鞘翅

        stats.put("deaths", String.valueOf(deaths));
        stats.put("playerKills", String.valueOf(playerKills));
        stats.put("mobKills", String.valueOf(mobKills));
        stats.put("playTime", TimeUtil.ticksToTimeString(playTimeTicks));
        stats.put("damageTaken", formatDamage(damageTaken));
        stats.put("damageDealt", formatDamage(damageDealt));
        stats.put("distanceTraveled", formatDistance(totalDistanceCm));

        return stats;
    }

    /**
     * 將 Minecraft 的傷害值 (十分之一 ❤) 格式化為可讀字串。
     */
    private String formatDamage(int rawDamage) {
        double hearts = rawDamage / 10.0;
        if (hearts >= 1000) {
            return String.format("%.2fk ❤", hearts / 1000.0);
        }
        return String.format("%.1f ❤", hearts);
    }

    /**
     * 將公分距離格式化為公里或公尺。
     */
    private String formatDistance(long totalCm) {
        double meters = totalCm / 100.0;
        if (meters >= 1000) {
            return String.format("%.2f km", meters / 1000.0);
        }
        return String.format("%.1f m", meters);
    }
}
