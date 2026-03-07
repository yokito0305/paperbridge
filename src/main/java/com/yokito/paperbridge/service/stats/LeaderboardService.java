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

/**
 * 產生指定排行榜類別的排序結果。
 *
 * <p>此服務只負責排行榜流程控制：載入玩家、過濾有效對象、排序、截斷結果，以及把原始值轉成
 * `LeaderboardEntry`。各類別如何取值則由 {@link LeaderboardCategory} 自己決定。</p>
 */
public class LeaderboardService {

    private final StatsFormatter statsFormatter;
    private final Function<UUID, OfflinePlayer> offlinePlayerProvider;

    /**
     * 建立使用 Bukkit 預設 `OfflinePlayer` provider 的排行榜服務。
     */
    public LeaderboardService(StatsFormatter statsFormatter) {
        this(statsFormatter, Bukkit::getOfflinePlayer);
    }

    /**
     * 供測試替換 `OfflinePlayer` 來源的建構子。
     */
    LeaderboardService(StatsFormatter statsFormatter, Function<UUID, OfflinePlayer> offlinePlayerProvider) {
        this.statsFormatter = statsFormatter;
        this.offlinePlayerProvider = offlinePlayerProvider;
    }

    /**
     * 產生指定排行榜類別的前 N 名結果。
     *
     * <p>同分時會再依玩家名稱做升冪排序，確保輸出穩定。</p>
     */
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

    /**
     * 將單一玩家轉成排行榜顯示條目。
     */
    private LeaderboardEntry toEntry(LeaderboardCategory category, OfflinePlayer player) {
        long rawValue = category.extractRawValue(player);
        return new LeaderboardEntry(
                player.getUniqueId(),
                resolvePlayerName(player),
                rawValue,
                category.formatDisplayValue(statsFormatter, rawValue)
        );
    }

    /**
     * 判斷玩家是否符合進榜資格。
     */
    private boolean isEligiblePlayer(OfflinePlayer player) {
        return player.hasPlayedBefore() || player.isOnline();
    }

    /**
     * 取得排行榜顯示名稱；若 Bukkit 沒有名稱則退回 UUID。
     */
    private String resolvePlayerName(OfflinePlayer player) {
        String playerName = player.getName();
        return playerName != null ? playerName : player.getUniqueId().toString();
    }
}
