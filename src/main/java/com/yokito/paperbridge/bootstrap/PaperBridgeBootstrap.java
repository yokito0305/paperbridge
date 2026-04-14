package com.yokito.paperbridge.bootstrap;

import com.yokito.paperbridge.command.discord.DiscordCommandRegistrar;
import com.yokito.paperbridge.command.discord.DiscordLeaderboardCommandHandler;
import com.yokito.paperbridge.command.discord.DiscordOnlineCommandHandler;
import com.yokito.paperbridge.command.discord.DiscordSlashCommandRegistry;
import com.yokito.paperbridge.command.discord.DiscordStatsCommandHandler;
import com.yokito.paperbridge.command.minecraft.DiscordNickCommand;
import com.yokito.paperbridge.integration.discordsrv.DeathMessageProcessor;
import com.yokito.paperbridge.integration.discordsrv.DiscordGateway;
import com.yokito.paperbridge.integration.discordsrv.DiscordInteractionListener;
import com.yokito.paperbridge.integration.discordsrv.DiscordSrvGateway;
import com.yokito.paperbridge.service.discord.DiscordEmbedFactory;
import com.yokito.paperbridge.service.discord.DiscordLinkedPlayerResolver;
import com.yokito.paperbridge.service.nickname.NicknameRepository;
import com.yokito.paperbridge.service.nickname.NicknameService;
import com.yokito.paperbridge.service.stats.LeaderboardService;
import com.yokito.paperbridge.service.stats.PlayerStatsService;
import com.yokito.paperbridge.service.stats.StatsFormatter;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * Composition root for plugin services and runtime-managed integrations.
 */
public class PaperBridgeBootstrap {

    private final PaperBridgePlugin plugin;

    public PaperBridgeBootstrap(PaperBridgePlugin plugin) {
        this.plugin = plugin;
    }

    public PaperBridgeRuntime build() {
        // Initialize services
        StatsFormatter statsFormatter = new StatsFormatter();
        NicknameService nicknameService = new NicknameService(
                new NicknameRepository(plugin.getConfig(), plugin::saveConfig));
        PlayerStatsService playerStatsService = new PlayerStatsService(statsFormatter);
        LeaderboardService leaderboardService = new LeaderboardService(statsFormatter);

        // Initialize command handlers
        DiscordNickCommand discordNickCommand = new DiscordNickCommand(nicknameService);

        // Initialize runtime components
        MinecraftCommandRegistrar minecraftCommandRegistrar =
                new MinecraftCommandRegistrar(plugin, discordNickCommand);
        PlaceholderRegistrar placeholderRegistrar =
                new PlaceholderRegistrar(plugin, nicknameService);

        return new PaperBridgeRuntime(
                minecraftCommandRegistrar,
                placeholderRegistrar,
                createDiscordRuntimeComponent(playerStatsService, leaderboardService)
        );
    }

    // 這個方法負責根據配置決定是否啟用 DiscordSRV 插件整合，並創建相應的 RuntimeComponent
    private RuntimeComponent createDiscordRuntimeComponent(
            PlayerStatsService playerStatsService,
            LeaderboardService leaderboardService
    ) {
        DiscordModeConfig discordModeConfig = new DiscordModeConfig(plugin.getDiscordConfig());
        if (discordModeConfig.isCustomBotEnabled()) {
            plugin.getLogger().warning(PluginText.CUSTOM_BOT_MODE_NOT_IMPLEMENTED_LOG);
            return NoOpRuntimeComponent.INSTANCE;
        }

        Plugin discordSrvPlugin = plugin.getServer().getPluginManager().getPlugin("DiscordSRV");
        if (discordSrvPlugin == null || !discordSrvPlugin.isEnabled()) {
            plugin.getLogger().info(PluginText.DISCORD_INTEGRATION_SKIPPED_LOG);
            return NoOpRuntimeComponent.INSTANCE;
        }

        plugin.getLogger().info(PluginText.DISCORD_SRV_MODE_ENABLED_LOG);

        DiscordGateway discordGateway = new DiscordSrvGateway();
        DiscordLinkedPlayerResolver linkedPlayerResolver = new DiscordLinkedPlayerResolver(discordGateway);
        DiscordEmbedFactory embedFactory = new DiscordEmbedFactory();

        DiscordStatsCommandHandler statsCommand =
                new DiscordStatsCommandHandler(linkedPlayerResolver, playerStatsService, embedFactory);
        DiscordLeaderboardCommandHandler leaderboardCommand =
                new DiscordLeaderboardCommandHandler(linkedPlayerResolver, leaderboardService, embedFactory);
        DiscordOnlineCommandHandler onlineCommand = new DiscordOnlineCommandHandler(embedFactory);

        DiscordSlashCommandRegistry commandRegistry = new DiscordSlashCommandRegistry(
                List.of(statsCommand, leaderboardCommand, onlineCommand));
        DiscordCommandRegistrar commandRegistrar =
                new DiscordCommandRegistrar(plugin.getLogger(), commandRegistry);
        DiscordInteractionListener discordInteractionListener = new DiscordInteractionListener(
                plugin, discordGateway, commandRegistrar, commandRegistry);
        DeathMessageProcessor deathMessageProcessor = new DeathMessageProcessor();

        return new DiscordIntegrationRegistrar(
                plugin, discordGateway, deathMessageProcessor, discordInteractionListener
        );
    }
}
