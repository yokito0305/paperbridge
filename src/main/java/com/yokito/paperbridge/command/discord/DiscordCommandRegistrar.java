package com.yokito.paperbridge.command.discord;

import com.yokito.paperbridge.service.discord.DiscordText;
import github.scarsz.discordsrv.dependencies.jda.api.JDA;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Guild;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.Command;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.build.CommandData;

import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * 將本專案管理的 slash commands 註冊到 Discord guild，目前保留實作用於 custom bot 註冊。
 *
 * <p>
 * 此類別只處理外部平台註冊，不負責命令查找；命令集合的來源由
 * {@link DiscordSlashCommandRegistry} 提供。
 * </p>
 */
public class DiscordCommandRegistrar {

    public static final long COMMAND_REGISTRATION_DELAY_TICKS = 40L;

    private final Logger logger;
    private final DiscordSlashCommandRegistry commandRegistry;

    /**
     * 建立 Discord command registrar。
     */
    public DiscordCommandRegistrar(Logger logger, DiscordSlashCommandRegistry commandRegistry) {
        this.logger = logger;
        this.commandRegistry = commandRegistry;
    }

    /**
     * 重新發布目前由本專案管理的 slash commands。
     *
     * <p>
     * 流程會先清除同名舊全域指令，再逐個 guild upsert 最新 definition。
     * </p>
     */
    public void registerCommands(JDA jda) {
        deleteLegacyGlobalCommands(jda);

        for (Guild guild : jda.getGuilds()) {
            for (DiscordSlashCommand command : commandRegistry.commands()) {
                registerGuildCommand(guild, command.definition());
            }
        }
    }

    /**
     * 刪除同名舊全域指令，避免與 guild 指令版本並存。
     */
    private void deleteLegacyGlobalCommands(JDA jda) {
        Set<String> managedCommandNames = commandRegistry.commands().stream()
                .map(DiscordSlashCommand::name)
                .collect(Collectors.toUnmodifiableSet());

        jda.retrieveCommands().queue(
                commands -> commands.stream()
                        .filter(command -> managedCommandNames.contains(command.getName()))
                        .forEach(this::deleteGlobalCommand),
                err -> logger.warning(DiscordText.LEGACY_COMMAND_DELETE_FAILURE_LOG + err.getMessage()));
    }

    /**
     * 刪除單一舊版全域指令並記錄結果。
     */
    private void deleteGlobalCommand(Command command) {
        command.delete().queue(
                ignored -> logger.info(DiscordText.GLOBAL_COMMAND_DELETE_SUCCESS_LOG + command.getName()),
                err -> logger.warning(
                        DiscordText.GLOBAL_COMMAND_DELETE_FAILURE_LOG + command.getName() + "): " + err.getMessage()));
    }

    /**
     * 在指定 guild 中註冊或更新單一 slash command。
     */
    @SuppressWarnings("null")
    private void registerGuildCommand(Guild guild, CommandData commandData) {
        guild.upsertCommand(commandData).queue(
                cmd -> logger.info(DiscordText.GUILD_COMMAND_REGISTER_SUCCESS_LOG + cmd.getName()
                        + " (Guild: " + guild.getName() + ", ID: " + cmd.getId() + ")"),
                err -> logger.warning(DiscordText.GUILD_COMMAND_REGISTER_FAILURE_LOG
                        + guild.getName() + ", Command: /" + commandData.getName() + "): " + err.getMessage()));
    }
}
