package com.yokito.paperbridge.command.discord;

import com.yokito.paperbridge.bootstrap.PaperBridgePlugin;
import com.yokito.paperbridge.model.stats.LeaderboardCategory;
import github.scarsz.discordsrv.dependencies.jda.api.JDA;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Guild;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.Command;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.OptionType;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.build.CommandData;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.build.OptionData;

import java.util.Set;

public class DiscordCommandRegistrar {

    public static final long COMMAND_REGISTRATION_DELAY_TICKS = 40L;
    private static final Set<String> MANAGED_COMMAND_NAMES = Set.of("stats", "leaderboard", "online");

    private final PaperBridgePlugin plugin;

    public DiscordCommandRegistrar(PaperBridgePlugin plugin) {
        this.plugin = plugin;
    }

    public void registerCommands(JDA jda) {
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

    private CommandData buildStatsCommand() {
        return new CommandData("stats", "查詢你或其他成員在 Minecraft 伺服器中的遊戲統計數據")
                .addOption(OptionType.USER, "member", "要查詢的 Discord 成員", false);
    }

    private CommandData buildLeaderboardCommand() {
        return new CommandData("leaderboard", "查詢 Minecraft 伺服器排行榜")
                .addOptions(new OptionData(OptionType.STRING, "category", "要查詢的排行榜類別", true)
                        .addChoice("死亡排名", LeaderboardCategory.DEATHS.optionValue())
                        .addChoice("遊戲時數排名", LeaderboardCategory.PLAYTIME.optionValue())
                        .addChoice("擊殺排名", LeaderboardCategory.KILLS.optionValue())
                        .addChoice("挖掘數量排名", LeaderboardCategory.MINED.optionValue()));
    }

    private CommandData buildOnlineCommand() {
        return new CommandData("online", "查詢目前 Minecraft 在線玩家數量");
    }

    private void registerGuildCommand(Guild guild, CommandData commandData) {
        guild.upsertCommand(commandData).queue(
                cmd -> plugin.getLogger().info("已成功註冊 Guild Slash Command: /" + cmd.getName()
                        + " (Guild: " + guild.getName() + ", ID: " + cmd.getId() + ")"),
                err -> plugin.getLogger().warning("註冊 Guild Slash Command 失敗 (Guild: "
                        + guild.getName() + ", Command: /" + commandData.getName() + "): " + err.getMessage())
        );
    }
}
