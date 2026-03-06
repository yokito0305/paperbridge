package com.yokito.paperbridge.integration.discordsrv;

import com.yokito.paperbridge.bootstrap.PaperBridgePlugin;
import com.yokito.paperbridge.command.discord.DiscordCommandRegistrar;
import com.yokito.paperbridge.command.discord.DiscordLeaderboardCommandHandler;
import com.yokito.paperbridge.command.discord.DiscordOnlineCommandHandler;
import com.yokito.paperbridge.command.discord.DiscordStatsCommandHandler;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordReadyEvent;
import github.scarsz.discordsrv.dependencies.jda.api.JDA;
import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.SlashCommandEvent;
import github.scarsz.discordsrv.dependencies.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class DiscordInteractionListener extends ListenerAdapter {

    private final PaperBridgePlugin plugin;
    private final DiscordCommandRegistrar commandRegistrar;
    private final DiscordStatsCommandHandler statsCommandHandler;
    private final DiscordLeaderboardCommandHandler leaderboardCommandHandler;
    private final DiscordOnlineCommandHandler onlineCommandHandler;

    public DiscordInteractionListener(
            PaperBridgePlugin plugin,
            DiscordCommandRegistrar commandRegistrar,
            DiscordStatsCommandHandler statsCommandHandler,
            DiscordLeaderboardCommandHandler leaderboardCommandHandler,
            DiscordOnlineCommandHandler onlineCommandHandler
    ) {
        this.plugin = plugin;
        this.commandRegistrar = commandRegistrar;
        this.statsCommandHandler = statsCommandHandler;
        this.leaderboardCommandHandler = leaderboardCommandHandler;
        this.onlineCommandHandler = onlineCommandHandler;
    }

    @Subscribe
    public void onDiscordReady(DiscordReadyEvent event) {
        JDA jda = DiscordSRV.getPlugin().getJda();
        if (jda == null) {
            return;
        }

        jda.addEventListener(this);
        plugin.getLogger().info("已註冊 Discord Interaction Listener");

        plugin.getServer().getScheduler().runTaskLater(
                plugin,
                () -> commandRegistrar.registerCommands(jda),
                DiscordCommandRegistrar.COMMAND_REGISTRATION_DELAY_TICKS
        );
    }

    @Override
    public void onSlashCommand(@Nonnull SlashCommandEvent event) {
        switch (event.getName()) {
            case "stats" -> statsCommandHandler.handle(event);
            case "leaderboard" -> leaderboardCommandHandler.handle(event);
            case "online" -> onlineCommandHandler.handle(event);
            default -> {
            }
        }
    }

    public void shutdown() {
        JDA jda = DiscordSRV.getPlugin().getJda();
        if (jda != null) {
            jda.removeEventListener(this);
        }
    }
}
