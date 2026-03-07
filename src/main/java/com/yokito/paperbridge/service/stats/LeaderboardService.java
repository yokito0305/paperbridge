package com.yokito.paperbridge.service.stats;

import com.yokito.paperbridge.model.stats.LeaderboardCategory;
import com.yokito.paperbridge.model.stats.LeaderboardEntry;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class LeaderboardService {

    private final StatsFormatter statsFormatter;
    private final Function<UUID, OfflinePlayer> offlinePlayerProvider;

    public LeaderboardService(StatsFormatter statsFormatter) {
        this(statsFormatter, Bukkit::getOfflinePlayer);
    }

    LeaderboardService(StatsFormatter statsFormatter, Function<UUID, OfflinePlayer> offlinePlayerProvider) {
        this.statsFormatter = statsFormatter;
        this.offlinePlayerProvider = offlinePlayerProvider;
    }

    public List<LeaderboardEntry> getLeaderboard(LeaderboardCategory category, Collection<UUID> playerIds, int limit) {
        Comparator<LeaderboardEntry> byScoreDesc = Comparator.comparingLong(LeaderboardEntry::rawValue).reversed();
        Comparator<LeaderboardEntry> byNameAsc = Comparator.comparing(entry -> entry.playerName().toLowerCase());

        return playerIds.stream()
                .map(offlinePlayerProvider)
                .filter(this::isEligiblePlayer)
                .map(player -> toEntry(category, player))
                .sorted(byScoreDesc.thenComparing(byNameAsc))
                .limit(limit)
                .toList();
    }

    private LeaderboardEntry toEntry(LeaderboardCategory category, OfflinePlayer player) {
        long rawValue = category.extractRawValue(player);
        return new LeaderboardEntry(
                player.getUniqueId(),
                resolvePlayerName(player),
                rawValue,
                category.formatDisplayValue(statsFormatter, rawValue)
        );
    }

    private boolean isEligiblePlayer(OfflinePlayer player) {
        return player.hasPlayedBefore() || player.isOnline();
    }

    private String resolvePlayerName(OfflinePlayer player) {
        String playerName = player.getName();
        return playerName != null ? playerName : player.getUniqueId().toString();
    }
}
