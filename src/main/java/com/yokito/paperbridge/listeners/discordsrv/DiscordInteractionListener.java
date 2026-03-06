package com.yokito.paperbridge.listeners.discordsrv;

import com.yokito.paperbridge.PaperBridge;
import com.yokito.paperbridge.manager.StatsManager;
import com.yokito.paperbridge.manager.StatsManager.LeaderboardCategory;
import com.yokito.paperbridge.manager.StatsManager.LeaderboardEntry;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordReadyEvent;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.JDA;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.Command;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Guild;
import github.scarsz.discordsrv.dependencies.jda.api.entities.User;
import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.SlashCommandEvent;
import github.scarsz.discordsrv.dependencies.jda.api.hooks.ListenerAdapter;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.OptionMapping;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.OptionType;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.build.CommandData;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.build.OptionData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 處理來自 Discord 的 Slash Command 互動。
 */
public class DiscordInteractionListener extends ListenerAdapter {

    private static final String PLAYER_NOT_JOINED_MESSAGE = "此玩家尚未加入伺服器";
    private static final String NO_LEADERBOARD_DATA_MESSAGE = "目前沒有可顯示的排行榜資料";
    private static final int LEADERBOARD_LIMIT = 5;
    private static final long COMMAND_REGISTRATION_DELAY_TICKS = 40L;
    private static final Set<String> MANAGED_COMMAND_NAMES = Set.of("stats", "leaderboard", "online");

    private final PaperBridge plugin;
    private final StatsManager statsManager;

    public DiscordInteractionListener(PaperBridge plugin, StatsManager statsManager) {
        this.plugin = plugin;
        this.statsManager = statsManager;
    }

    @Subscribe
    public void onDiscordReady(DiscordReadyEvent event) {
        JDA jda = DiscordSRV.getPlugin().getJda();
        if (jda == null) {
            return;
        }

        jda.addEventListener(this);
        plugin.getLogger().info("已註冊 Discord Interaction Listener");

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> registerDiscordCommands(jda), COMMAND_REGISTRATION_DELAY_TICKS);
    }

    private void registerDiscordCommands(JDA jda) {
        deleteLegacyGlobalCommands(jda);

        for (Guild guild : jda.getGuilds()) {
            registerGuildCommand(guild, buildStatsCommand());
            registerGuildCommand(guild, buildLeaderboardCommand());
            registerGuildCommand(guild, buildOnlineCommand());
        }
    }

    private void deleteLegacyGlobalCommands(JDA jda) {
        jda.retrieveCommands().queue(
                commands -> commands.stream()
                        .filter(command -> MANAGED_COMMAND_NAMES.contains(command.getName()))
                        .forEach(this::deleteGlobalCommand),
                err -> plugin.getLogger().warning("讀取全域 Slash Commands 失敗: " + err.getMessage())
        );
    }

    private void deleteGlobalCommand(Command command) {
        command.delete().queue(
                ignored -> plugin.getLogger().info("已刪除舊的全域 Slash Command: /" + command.getName()),
                err -> plugin.getLogger().warning("刪除全域 Slash Command 失敗 (/" + command.getName() + "): " + err.getMessage())
        );
    }

    @Nonnull
    private CommandData buildStatsCommand() {
        return new CommandData("stats", "查詢你或其他成員在 Minecraft 伺服器中的遊戲統計數據")
                .addOption(OptionType.USER, "member", "要查詢的 Discord 成員", false);
    }

    @Nonnull
    private CommandData buildLeaderboardCommand() {
        return new CommandData("leaderboard", "查詢 Minecraft 伺服器排行榜")
                .addOptions(new OptionData(OptionType.STRING, "category", "要查詢的排行榜類別", true)
                        .addChoice("死亡排名", LeaderboardCategory.DEATHS.optionValue())
                        .addChoice("遊戲時數排名", LeaderboardCategory.PLAYTIME.optionValue())
                        .addChoice("擊殺排名", LeaderboardCategory.KILLS.optionValue())
                        .addChoice("挖掘數量排名", LeaderboardCategory.MINED.optionValue()));
    }

    @Nonnull
    private CommandData buildOnlineCommand() {
        return new CommandData("online", "查詢目前 Minecraft 在線玩家數量");
    }

    private void registerGuildCommand(Guild guild,@Nonnull CommandData commandData) {
        guild.upsertCommand(commandData).queue(
                cmd -> plugin.getLogger().info("已成功註冊 Guild Slash Command: /" + cmd.getName()
                        + " (Guild: " + guild.getName() + ", ID: " + cmd.getId() + ")"),
                err -> plugin.getLogger().warning("註冊 Guild Slash Command 失敗 (Guild: "
                        + guild.getName() + ", Command: /" + commandData.getName() + "): " + err.getMessage())
        );
    }

    @Override
    public void onSlashCommand(@Nonnull SlashCommandEvent event) {
        switch (event.getName()) {
            case "stats" -> handleStatsCommand(event);
            case "leaderboard" -> handleLeaderboardCommand(event);
            case "online" -> handleOnlineCommand(event);
            default -> {
            }
        }
    }

    private void handleStatsCommand(SlashCommandEvent event) {
        User targetUser = resolveTargetUser(event);
        OfflinePlayer player = resolveLinkedPlayer(targetUser.getId());
        if (player == null) {
            replyPlayerNotJoined(event);
            return;
        }

        Map<String, String> stats = statsManager.getFormattedStats(player);
        String playerName = player.getName() != null ? player.getName() : targetUser.getName();

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("📊 " + playerName + " 的遊戲統計")
                .setColor(Color.decode("#55FF55"))
                .addField("💀 死亡次數", stats.get("deaths"), true)
                .addField("⚔ 玩家擊殺", stats.get("playerKills"), true)
                .addField("🗡 怪物擊殺", stats.get("mobKills"), true)
                .addField("🕐 遊戲時數", stats.get("playTime"), true)
                .addField("❤ 承受傷害", stats.get("damageTaken"), true)
                .addField("💥 造成傷害", stats.get("damageDealt"), true)
                .addField("🚶 移動距離", stats.get("distanceTraveled"), true)
                .setFooter("PaperBridge Stats · 僅你可見", null)
                .setTimestamp(Instant.now());

        event.replyEmbeds(embed.build())
                .setEphemeral(false) // 讓所有人都能看到玩家的統計數據
                .queue();
    }

    private void handleLeaderboardCommand(SlashCommandEvent event) {
        OptionMapping categoryOption = event.getOption("category");
        if (categoryOption == null) {
            event.reply("❌ 缺少排行榜類別參數")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        LeaderboardCategory category;
        try {
            category = LeaderboardCategory.fromOption(categoryOption.getAsString());
        } catch (IllegalArgumentException exception) {
            event.reply("❌ 不支援的排行榜類別")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        List<LeaderboardEntry> leaderboard = statsManager.getLeaderboard(category, LEADERBOARD_LIMIT);
        if (leaderboard.isEmpty()) {
            event.reply("❌ " + NO_LEADERBOARD_DATA_MESSAGE)
                    .setEphemeral(true)
                    .queue();
            return;
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🏆 " + category.displayName() + " TOP " + LEADERBOARD_LIMIT)
                .setColor(Color.decode("#F1C40F"))
                .setFooter("PaperBridge Leaderboard", null)
                .setDescription(formatLeaderboardLines(category, leaderboard))
                .addBlankField(false)
                .setTimestamp(Instant.now());

        event.replyEmbeds(embed.build())
                .setEphemeral(false)
                .queue();
    }

    private void handleOnlineCommand(SlashCommandEvent event) {
        int onlineCount = Bukkit.getOnlinePlayers().size();

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🟢 目前在線玩家")
                .setColor(Color.decode("#2ECC71"))
                .setDescription("目前共有 **" + onlineCount + "** 位玩家在線")
                .setFooter("PaperBridge Online", null)
                .setTimestamp(Instant.now());

        event.replyEmbeds(embed.build())
                .setEphemeral(false)
                .queue();
    }

    private String formatLeaderboardLines(LeaderboardCategory category, List<LeaderboardEntry> leaderboard) {
        return java.util.stream.IntStream.range(0, leaderboard.size())
                .mapToObj(index -> formatLeaderboardLine(index + 1, category, leaderboard.get(index)))
                .collect(Collectors.joining("\n"));
    }

    private String formatLeaderboardLine(int rank, LeaderboardCategory category, LeaderboardEntry entry) {
        return getRankBadge(rank)
                + " **" + entry.playerName() + "**"
                + "  `"
                + category.metricLabel() + ": " + entry.displayValue()
                + "`";
    }

    private String getRankBadge(int rank) {
        return switch (rank) {
            case 1 -> "🥇";
            case 2 -> "🥈";
            case 3 -> "🥉";
            default -> "▫";
        };
    }

    private User resolveTargetUser(SlashCommandEvent event) {
        OptionMapping memberOption = event.getOption("member");
        if (memberOption == null) {
            return event.getUser();
        }

        User mentionedUser = memberOption.getAsUser();
        return mentionedUser != null ? mentionedUser : event.getUser();
    }

    private OfflinePlayer resolveLinkedPlayer(String discordUserId) {
        UUID mcUuid = DiscordSRV.getPlugin().getAccountLinkManager().getUuid(discordUserId);
        if (mcUuid == null) {
            return null;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(mcUuid);
        if (!player.hasPlayedBefore() && !player.isOnline()) {
            return null;
        }

        return player;
    }

    private void replyPlayerNotJoined(SlashCommandEvent event) {
        event.reply("❌ " + PLAYER_NOT_JOINED_MESSAGE)
                .setEphemeral(true)
                .queue();
    }

    public void shutdown() {
        JDA jda = DiscordSRV.getPlugin().getJda();
        if (jda != null) {
            jda.removeEventListener(this);
        }
    }
}
