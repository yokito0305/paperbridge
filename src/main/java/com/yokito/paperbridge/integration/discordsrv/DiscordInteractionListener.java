package com.yokito.paperbridge.integration.discordsrv;

import com.yokito.paperbridge.bootstrap.PaperBridgePlugin;
import com.yokito.paperbridge.command.discord.DiscordCommandRegistrar;
import com.yokito.paperbridge.command.discord.DiscordSlashCommandRegistry;
import com.yokito.paperbridge.service.discord.DiscordText;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordReadyEvent;
import github.scarsz.discordsrv.dependencies.jda.api.JDA;
import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.SlashCommandEvent;
import github.scarsz.discordsrv.dependencies.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class DiscordInteractionListener extends ListenerAdapter {

    private final PaperBridgePlugin plugin;
    private final DiscordGateway discordGateway;
    private final DiscordCommandRegistrar commandRegistrar;
    private final DiscordSlashCommandRegistry commandRegistry;

    public DiscordInteractionListener(
            PaperBridgePlugin plugin,
            DiscordGateway discordGateway,
            DiscordCommandRegistrar commandRegistrar,
            DiscordSlashCommandRegistry commandRegistry
    ) {
        this.plugin = plugin;
        this.discordGateway = discordGateway;
        this.commandRegistrar = commandRegistrar;
        this.commandRegistry = commandRegistry;
    }

    @Subscribe
    public void onDiscordReady(DiscordReadyEvent event) {
        JDA jda = discordGateway.getJda();
        if (jda == null) {
            return;
        }

        jda.addEventListener(this);
        plugin.getLogger().info(DiscordText.DISCORD_JDA_LISTENER_ATTACHED_LOG);

        plugin.getServer().getScheduler().runTaskLater(
                plugin,
                () -> commandRegistrar.registerCommands(jda),
                DiscordCommandRegistrar.COMMAND_REGISTRATION_DELAY_TICKS
        );
    }

    @Override
    public void onSlashCommand(@Nonnull SlashCommandEvent event) {
        commandRegistry.find(event.getName()).ifPresent(command -> command.handle(event));
    }

    public void shutdown() {
        JDA jda = discordGateway.getJda();
        if (jda != null) {
            jda.removeEventListener(this);
        }
    }
}
