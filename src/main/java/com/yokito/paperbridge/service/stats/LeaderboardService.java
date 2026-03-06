package com.yokito.paperbridge.service.stats;

import com.yokito.paperbridge.model.stats.LeaderboardCategory;
import com.yokito.paperbridge.model.stats.LeaderboardEntry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class LeaderboardService {

    private static final List<Material> MINEABLE_BLOCKS = List.of(Material.values()).stream()
            .filter(Material::isBlock)
            .filter(material -> !material.isAir())
            .toList();

    private final StatsFormatter statsFormatter;

    public LeaderboardService(StatsFormatter statsFormatter) {
        this.statsFormatter = statsFormatter;
    }

    public List<LeaderboardEntry> getLeaderboard(LeaderboardCategory category, Collection<UUID> playerIds, int limit) {
        Comparator<LeaderboardEntry> byScoreDesc = Comparator.comparingLong(LeaderboardEntry::rawValue).reversed();
        Comparator<LeaderboardEntry> byNameAsc = Comparator.comparing(entry -> entry.playerName().toLowerCase());

        return playerIds.stream()
                .map(Bukkit::getOfflinePlayer)
                .filter(this::isEligiblePlayer)
                .map(player -> toEntry(category, player))
                .sorted(byScoreDesc.thenComparing(byNameAsc))
                .limit(limit)
                .toList();
    }

    private LeaderboardEntry toEntry(LeaderboardCategory category, OfflinePlayer player) {
        long rawValue = getRawValue(category, player);
        return new LeaderboardEntry(
                player.getUniqueId(),
                resolvePlayerName(player),
                rawValue,
                statsFormatter.formatLeaderboardValue(category, rawValue)
        );
    }

    private boolean isEligiblePlayer(OfflinePlayer player) {
        return player.hasPlayedBefore() || player.isOnline();
    }

    private String resolvePlayerName(OfflinePlayer player) {
        String playerName = player.getName();
        return playerName != null ? playerName : player.getUniqueId().toString();
    }

    private long getRawValue(LeaderboardCategory category, OfflinePlayer player) {
        return switch (category) {
            case DEATHS -> player.getStatistic(Statistic.DEATHS);
            case PLAYTIME -> player.getStatistic(Statistic.PLAY_ONE_MINUTE);
            case KILLS -> player.getStatistic(Statistic.PLAYER_KILLS) + player.getStatistic(Statistic.MOB_KILLS);
            case MINED -> getTotalMinedBlocks(player);
        };
    }

    private long getTotalMinedBlocks(OfflinePlayer player) {
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
}
