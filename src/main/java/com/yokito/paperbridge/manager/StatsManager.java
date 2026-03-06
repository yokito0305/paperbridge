package com.yokito.paperbridge.manager;

import com.yokito.paperbridge.PaperBridge;
import com.yokito.paperbridge.utils.TimeUtil;
import github.scarsz.discordsrv.DiscordSRV;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 核心統計數據管理器 (Service Layer)。
 * <p>
 * 負責從 Bukkit 的 Statistic API 抓取玩家的遊戲統計數據，
 * 並提供格式化後的結果供指令或 Discord 互動使用。
 * </p>
 */
public class StatsManager {

    private static final List<Material> MINEABLE_BLOCKS = List.of(Material.values()).stream()
            .filter(Material::isBlock)
            .filter(material -> !material.isAir())
            .toList();

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
        int playTimeTicks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
        int damageTaken = player.getStatistic(Statistic.DAMAGE_TAKEN);
        int damageDealt = player.getStatistic(Statistic.DAMAGE_DEALT);

        long totalDistanceCm = 0L;
        totalDistanceCm += player.getStatistic(Statistic.WALK_ONE_CM);
        totalDistanceCm += player.getStatistic(Statistic.SPRINT_ONE_CM);
        totalDistanceCm += player.getStatistic(Statistic.SWIM_ONE_CM);
        totalDistanceCm += player.getStatistic(Statistic.FLY_ONE_CM);
        totalDistanceCm += player.getStatistic(Statistic.BOAT_ONE_CM);
        totalDistanceCm += player.getStatistic(Statistic.HORSE_ONE_CM);
        totalDistanceCm += player.getStatistic(Statistic.MINECART_ONE_CM);
        totalDistanceCm += player.getStatistic(Statistic.AVIATE_ONE_CM);

        stats.put("deaths", String.valueOf(deaths));
        stats.put("playerKills", String.valueOf(playerKills));
        stats.put("mobKills", String.valueOf(mobKills));
        stats.put("playTime", TimeUtil.ticksToTimeString(playTimeTicks));
        stats.put("damageTaken", formatDamage(damageTaken));
        stats.put("damageDealt", formatDamage(damageDealt));
        stats.put("distanceTraveled", formatDistance(totalDistanceCm));

        return stats;
    }

    public List<LeaderboardEntry> getLeaderboard(LeaderboardCategory category, int limit) {
        Comparator<LeaderboardEntry> byScoreDesc = Comparator.comparingLong(LeaderboardEntry::rawValue).reversed();
        Comparator<LeaderboardEntry> byNameAsc = Comparator.comparing(entry -> entry.playerName().toLowerCase());

        return DiscordSRV.getPlugin().getAccountLinkManager().getLinkedAccounts().values().stream()
                .map(Bukkit::getOfflinePlayer)
                .filter(this::isEligiblePlayer)
                .map(player -> new LeaderboardEntry(
                        player.getUniqueId(),
                        resolvePlayerName(player),
                        category.getRawValue(player),
                        category.formatValue(category.getRawValue(player))
                ))
                .sorted(byScoreDesc.thenComparing(byNameAsc))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private boolean isEligiblePlayer(OfflinePlayer player) {
        return player.hasPlayedBefore() || player.isOnline();
    }

    private String resolvePlayerName(OfflinePlayer player) {
        String playerName = player.getName();
        return playerName != null ? playerName : player.getUniqueId().toString();
    }

    private String formatDamage(int rawDamage) {
        double hearts = rawDamage / 10.0;
        if (hearts >= 1000) {
            return String.format("%.2fk ❤", hearts / 1000.0);
        }
        return String.format("%.1f ❤", hearts);
    }

    private String formatDistance(long totalCm) {
        double meters = totalCm / 100.0;
        if (meters >= 1000) {
            return String.format("%.2f km", meters / 1000.0);
        }
        return String.format("%.1f m", meters);
    }

    public enum LeaderboardCategory {
        DEATHS("deaths", "死亡排名", "死亡次數") {
            @Override
            public long getRawValue(OfflinePlayer player) {
                return player.getStatistic(Statistic.DEATHS);
            }
        },
        PLAYTIME("playtime", "遊戲時數排名", "遊戲時數") {
            @Override
            public long getRawValue(OfflinePlayer player) {
                return player.getStatistic(Statistic.PLAY_ONE_MINUTE);
            }

            @Override
            @Nonnull
            public String formatValue(long rawValue) {
                return Objects.requireNonNull(TimeUtil.ticksToTimeString((int) rawValue));
            }
        },
        KILLS("kills", "擊殺排名", "總擊殺") {
            @Override
            public long getRawValue(OfflinePlayer player) {
                return player.getStatistic(Statistic.PLAYER_KILLS) + player.getStatistic(Statistic.MOB_KILLS);
            }
        },
        MINED("mined", "挖掘數量排名", "挖掘方塊數") {
            @Override
            public long getRawValue(OfflinePlayer player) {
                long totalMinedBlocks = 0L;
                for (Material material : MINEABLE_BLOCKS) {
                    try {
                        totalMinedBlocks += player.getStatistic(Statistic.MINE_BLOCK, material);
                    } catch (IllegalArgumentException ignored) {
                        // MINE_BLOCK 並非對所有方塊類型都有效，忽略不支援的項目。
                    }
                }
                return totalMinedBlocks;
            }
        };

        private final @Nonnull String optionValue;
        private final @Nonnull String displayName;
        private final @Nonnull String metricLabel;

        LeaderboardCategory(@Nonnull String optionValue, @Nonnull String displayName, @Nonnull String metricLabel) {
            this.optionValue = optionValue;
            this.displayName = displayName;
            this.metricLabel = metricLabel;
        }

        @Nonnull
        public String optionValue() {
            return optionValue;
        }

        @Nonnull
        public String displayName() {
            return displayName;
        }

        @Nonnull
        public String metricLabel() {
            return metricLabel;
        }

        public abstract long getRawValue(OfflinePlayer player);

        @Nonnull
        public String formatValue(long rawValue) {
            return Objects.requireNonNull(String.valueOf(rawValue));
        }

        public static LeaderboardCategory fromOption(String optionValue) {
            for (LeaderboardCategory category : values()) {
                if (category.optionValue.equalsIgnoreCase(optionValue)) {
                    return category;
                }
            }
            throw new IllegalArgumentException("Unknown leaderboard category: " + optionValue);
        }
    }

    public record LeaderboardEntry(UUID playerUuid, String playerName, long rawValue, String displayValue) {
    }
}
