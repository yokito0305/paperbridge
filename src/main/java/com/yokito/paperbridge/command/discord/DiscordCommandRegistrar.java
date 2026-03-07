package com.yokito.paperbridge.command.discord;

import com.yokito.paperbridge.service.discord.DiscordText;
import github.scarsz.discordsrv.dependencies.jda.api.JDA;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Guild;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.Command;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.build.CommandData;

import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DiscordCommandRegistrar {

    public static final long COMMAND_REGISTRATION_DELAY_TICKS = 40L;

    private final Logger logger;
    private final DiscordSlashCommandRegistry commandRegistry;

    public DiscordCommandRegistrar(Logger logger, DiscordSlashCommandRegistry commandRegistry) {
        this.logger = logger;
        this.commandRegistry = commandRegistry;
    }

    public void registerCommands(JDA jda) {
        deleteLegacyGlobalCommands(jda);

        for (Guild guild : jda.getGuilds()) {
            for (DiscordSlashCommand command : commandRegistry.commands()) {
                registerGuildCommand(guild, command.definition());
            }
        }
    }

    private void deleteLegacyGlobalCommands(JDA jda) {
        Set<String> managedCommandNames = commandRegistry.commands().stream()
                .map(DiscordSlashCommand::name)
                .collect(Collectors.toUnmodifiableSet());

        jda.retrieveCommands().queue(
                commands -> commands.stream()
                        .filter(command -> managedCommandNames.contains(command.getName()))
                        .forEach(this::deleteGlobalCommand),
                err -> logger.warning(DiscordText.LEGACY_COMMAND_DELETE_FAILURE_LOG + err.getMessage())
        );
    }

    private void deleteGlobalCommand(Command command) {
        command.delete().queue(
                ignored -> logger.info(DiscordText.GLOBAL_COMMAND_DELETE_SUCCESS_LOG + command.getName()),
                err -> logger.warning(
                        DiscordText.GLOBAL_COMMAND_DELETE_FAILURE_LOG + command.getName() + "): " + err.getMessage()
                )
        );
    }

    @SuppressWarnings("null")
    private void registerGuildCommand(Guild guild, CommandData commandData) {
        guild.upsertCommand(commandData).queue(
                cmd -> logger.info(DiscordText.GUILD_COMMAND_REGISTER_SUCCESS_LOG + cmd.getName()
                        + " (Guild: " + guild.getName() + ", ID: " + cmd.getId() + ")"),
                err -> logger.warning(DiscordText.GUILD_COMMAND_REGISTER_FAILURE_LOG
                        + guild.getName() + ", Command: /" + commandData.getName() + "): " + err.getMessage())
        );
    }
}
