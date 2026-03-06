package com.yokito.paperbridge.service.stats;

import com.yokito.paperbridge.model.stats.PlayerStatsView;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;

public class PlayerStatsService {

    private final StatsFormatter statsFormatter;

    public PlayerStatsService(StatsFormatter statsFormatter) {
        this.statsFormatter = statsFormatter;
    }

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
