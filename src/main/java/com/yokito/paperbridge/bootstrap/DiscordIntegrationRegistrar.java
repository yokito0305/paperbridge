package com.yokito.paperbridge.bootstrap;

import com.yokito.paperbridge.integration.discordsrv.DeathMessageProcessor;
import com.yokito.paperbridge.integration.discordsrv.DiscordGateway;
import com.yokito.paperbridge.integration.discordsrv.DiscordInteractionListener;
import com.yokito.paperbridge.service.discord.DiscordText;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 負責 DiscordSRV listener 的掛載與解除掛載。
 */
public class DiscordIntegrationRegistrar {

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

    public void register() {
        if (plugin.getServer().getPluginManager().getPlugin("DiscordSRV") == null) {
            return;
        }

        discordGateway.subscribe(deathMessageProcessor);
        plugin.getLogger().info(DiscordText.DISCORD_DEATH_PROCESSOR_ENABLED_LOG);

        discordGateway.subscribe(discordInteractionListener);
        plugin.getLogger().info(DiscordText.DISCORD_INTERACTION_ENABLED_LOG);
    }

    public void unregister() {
        if (plugin.getServer().getPluginManager().getPlugin("DiscordSRV") == null) {
            return;
        }

        discordGateway.unsubscribe(deathMessageProcessor);
        discordInteractionListener.shutdown();
        discordGateway.unsubscribe(discordInteractionListener);
    }
}
