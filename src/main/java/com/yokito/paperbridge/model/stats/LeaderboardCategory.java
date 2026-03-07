package com.yokito.paperbridge.model.stats;

import com.yokito.paperbridge.service.stats.StatsFormatter;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;
import java.util.function.ToLongFunction;

public enum LeaderboardCategory {
    DEATHS(
            "deaths",
            "死亡排名",
            "死亡次數",
            player -> player.getStatistic(Statistic.DEATHS),
            (statsFormatter, rawValue) -> String.valueOf(rawValue)
    ),
    PLAYTIME(
            "playtime",
            "遊玩時數排名",
            "遊玩時間",
            player -> player.getStatistic(Statistic.PLAY_ONE_MINUTE),
            (statsFormatter, rawValue) -> statsFormatter.formatPlayTime(rawValue)
    ),
    KILLS(
            "kills",
            "擊殺排名",
            "總擊殺",
            player -> player.getStatistic(Statistic.PLAYER_KILLS) + player.getStatistic(Statistic.MOB_KILLS),
            (statsFormatter, rawValue) -> String.valueOf(rawValue)
    ),
    MINED(
            "mined",
            "挖掘數量排名",
            "挖掘方塊數",
            LeaderboardCategory::getTotalMinedBlocks,
            (statsFormatter, rawValue) -> String.valueOf(rawValue)
    );

    private static final Material[] MATERIALS = Material.values();

    @Nonnull
    private final String optionValue;
    @Nonnull
    private final String displayName;
    @Nonnull
    private final String metricLabel;
    private final ToLongFunction<OfflinePlayer> metricExtractor;
    private final BiFunction<StatsFormatter, Long, String> displayFormatter;

    LeaderboardCategory(
            @Nonnull String optionValue,
            @Nonnull String displayName,
            @Nonnull String metricLabel,
            ToLongFunction<OfflinePlayer> metricExtractor,
            BiFunction<StatsFormatter, Long, String> displayFormatter
    ) {
        this.optionValue = optionValue;
        this.displayName = displayName;
        this.metricLabel = metricLabel;
        this.metricExtractor = metricExtractor;
        this.displayFormatter = displayFormatter;
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

    public long extractRawValue(OfflinePlayer player) {
        return metricExtractor.applyAsLong(player);
    }

    @Nonnull
    public String formatDisplayValue(StatsFormatter statsFormatter, long rawValue) {
        return displayFormatter.apply(statsFormatter, rawValue);
    }

    @Nonnull
    public static LeaderboardCategory fromOption(String optionValue) {
        for (LeaderboardCategory category : values()) {
            if (category.optionValue.equalsIgnoreCase(optionValue)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown leaderboard category: " + optionValue);
    }

    private static long getTotalMinedBlocks(OfflinePlayer player) {
        long totalMinedBlocks = 0L;
        for (Material material : MATERIALS) {
            try {
                totalMinedBlocks += player.getStatistic(Statistic.MINE_BLOCK, material);
            } catch (IllegalArgumentException ignored) {
                // Some blocks do not expose a MINE_BLOCK statistic.
            }
        }
        return totalMinedBlocks;
    }
}
