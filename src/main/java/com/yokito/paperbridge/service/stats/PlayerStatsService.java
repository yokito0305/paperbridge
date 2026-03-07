package com.yokito.paperbridge.service.stats;

import com.yokito.paperbridge.model.stats.PlayerStatsView;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;

/**
 * 收集單一玩家的 Minecraft 統計資料並轉成顯示模型。
 *
 * <p>此服務負責從 Bukkit `Statistic` API 取值，再交由 {@link StatsFormatter} 做格式化。</p>
 */
public class PlayerStatsService {

    private final StatsFormatter statsFormatter;

    /**
     * 建立玩家統計服務。
     */
    public PlayerStatsService(StatsFormatter statsFormatter) {
        this.statsFormatter = statsFormatter;
    }

    /**
     * 讀取指定玩家的統計資料並組成 `PlayerStatsView`。
     *
     * <p>距離資料會累加多種移動方式，確保 Discord 與 Placeholder 顯示一致。</p>
     */
    public PlayerStatsView getPlayerStats(OfflinePlayer player) {
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

        return new PlayerStatsView(
                String.valueOf(deaths),
                String.valueOf(playerKills),
                String.valueOf(mobKills),
                statsFormatter.formatPlayTime(playTimeTicks),
                statsFormatter.formatDamage(damageTaken),
                statsFormatter.formatDamage(damageDealt),
                statsFormatter.formatDistance(totalDistanceCm)
        );
    }
}
