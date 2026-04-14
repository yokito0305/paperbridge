package com.yokito.paperbridge.bootstrap;

import com.yokito.paperbridge.integration.discordsrv.DeathMessageProcessor;
import com.yokito.paperbridge.integration.discordsrv.DiscordGateway;
import com.yokito.paperbridge.integration.discordsrv.DiscordInteractionListener;
import com.yokito.paperbridge.service.discord.DiscordText;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Registers DiscordSRV-backed listeners for this plugin.
 */
public class DiscordIntegrationRegistrar implements RuntimeComponent {

    private final JavaPlugin plugin;
    private final DiscordGateway discordGateway;
    private final DeathMessageProcessor deathMessageProcessor;
    private final DiscordInteractionListener discordInteractionListener;

    public DiscordIntegrationRegistrar(
            JavaPlugin plugin,
            DiscordGateway discordGateway,
            DeathMessageProcessor deathMessageProcessor,
            DiscordInteractionListener discordInteractionListener
    ) {
        this.plugin = plugin;
        this.discordGateway = discordGateway;
        this.deathMessageProcessor = deathMessageProcessor;
        this.discordInteractionListener = discordInteractionListener;
    }

    @Override
    public void start() {
        discordGateway.subscribe(deathMessageProcessor);
        plugin.getLogger().info(DiscordText.DISCORD_DEATH_PROCESSOR_ENABLED_LOG);

        discordGateway.subscribe(discordInteractionListener);
        plugin.getLogger().info(DiscordText.DISCORD_INTERACTION_ENABLED_LOG);
    }

    @Override
    public void stop() {
        discordGateway.unsubscribe(deathMessageProcessor);
        discordInteractionListener.shutdown();
        discordGateway.unsubscribe(discordInteractionListener);
    }
}
