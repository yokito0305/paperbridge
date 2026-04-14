package com.yokito.paperbridge.service.discord;

import com.yokito.paperbridge.model.stats.LeaderboardCategory;
import com.yokito.paperbridge.model.stats.LeaderboardEntry;
import com.yokito.paperbridge.model.stats.PlayerStatsView;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;

import java.awt.Color;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 集中建立 Discord embed，避免命令物件自行拼接輸出格式。
 *
 * <p>此類別只關注呈現層，不負責查資料；資料的取得與整理應先由 service 完成。</p>
 */
public class DiscordEmbedFactory {

    /**
     * 依玩家名稱與統計資料建立個人統計 embed。
     */
    public EmbedBuilder createStatsEmbed(String playerName, PlayerStatsView stats) {
        return new EmbedBuilder()
                .setTitle(playerName + DiscordText.STATS_TITLE_SUFFIX)
                .setColor(Color.decode("#55FF55"))
                .addField(DiscordText.DEATHS_FIELD, stats.deaths(), true)
                .addField(DiscordText.PLAYER_KILLS_FIELD, stats.playerKills(), true)
                .addField(DiscordText.MOB_KILLS_FIELD, stats.mobKills(), true)
                .addField(DiscordText.PLAY_TIME_FIELD, stats.playTime(), true)
                .addField(DiscordText.DAMAGE_TAKEN_FIELD, stats.damageTaken(), true)
                .addField(DiscordText.DAMAGE_DEALT_FIELD, stats.damageDealt(), true)
                .addField(DiscordText.DISTANCE_TRAVELED_FIELD, stats.distanceTraveled(), true)
                .setFooter(DiscordText.STATS_FOOTER, null)
                .setTimestamp(Instant.now());
    }

    /**
     * 建立排行榜 embed。
     */
    public EmbedBuilder createLeaderboardEmbed(LeaderboardCategory category, List<LeaderboardEntry> leaderboard, int limit) {
        return new EmbedBuilder()
                .setTitle(":trophy:" + category.displayName() + " TOP " + limit)
                .setColor(Color.decode("#F1C40F"))
                .setFooter(DiscordText.LEADERBOARD_FOOTER, null)
                .setDescription(formatLeaderboardLines(category, leaderboard))
                .setTimestamp(Instant.now());
    }

    /**
     * 建立線上人數 embed。
     */
    public EmbedBuilder createOnlineEmbed(int onlineCount) {
        return new EmbedBuilder()
                .setTitle(DiscordText.ONLINE_TITLE)
                .setColor(Color.decode("#2ECC71"))
                .setDescription(DiscordText.ONLINE_DESCRIPTION_PREFIX + onlineCount + DiscordText.ONLINE_DESCRIPTION_SUFFIX)
                .setFooter(DiscordText.ONLINE_FOOTER, null)
                .setTimestamp(Instant.now());
    }

    /**
     * 將排行榜條目序列化為 Discord description 文字。
     */
    private String formatLeaderboardLines(LeaderboardCategory category, List<LeaderboardEntry> leaderboard) {
        return IntStream.range(0, leaderboard.size())
                .mapToObj(index -> formatLeaderboardLine(index + 1, category, leaderboard.get(index)))
                .collect(Collectors.joining("\n"));
    }

    /**
     * 格式化單一排行榜列。
     */
    private String formatLeaderboardLine(int rank, LeaderboardCategory category, LeaderboardEntry entry) {
        return getRankBadge(rank)
                + " **" + entry.playerName() + "**"
                + "  `"
                + category.metricLabel() + ": " + entry.displayValue()
                + "`";
    }

    /**
     * 依名次回傳要顯示的徽章文字。
     */
    private String getRankBadge(int rank) {
        return switch (rank) {
            case 1 -> ":first_place:";
            case 2 -> ":second_place:";
            case 3 -> ":third_place:";
            default -> "#" + rank;
        };
    }
}
