package com.yokito.paperbridge.bootstrap;

import com.yokito.paperbridge.command.discord.DiscordCommandRegistrar;
import com.yokito.paperbridge.command.discord.DiscordLeaderboardCommandHandler;
import com.yokito.paperbridge.command.discord.DiscordOnlineCommandHandler;
import com.yokito.paperbridge.command.discord.DiscordStatsCommandHandler;
import com.yokito.paperbridge.command.minecraft.DiscordNickCommand;
import com.yokito.paperbridge.integration.discordsrv.DeathMessageProcessor;
import com.yokito.paperbridge.integration.discordsrv.DiscordInteractionListener;
import com.yokito.paperbridge.integration.placeholderapi.PaperBridgeExpansion;
import com.yokito.paperbridge.service.discord.DiscordEmbedFactory;
import com.yokito.paperbridge.service.discord.DiscordLinkedPlayerResolver;
import com.yokito.paperbridge.service.nickname.NicknameRepository;
import com.yokito.paperbridge.service.nickname.NicknameService;
import com.yokito.paperbridge.service.stats.LeaderboardService;
import com.yokito.paperbridge.service.stats.PlayerStatsService;
import com.yokito.paperbridge.service.stats.StatsFormatter;
import org.bukkit.plugin.java.JavaPlugin;

public class PaperBridgePlugin extends JavaPlugin {

    private DeathMessageProcessor deathMessageProcessor;
    private DiscordInteractionListener discordInteractionListener;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        NicknameService nicknameService = buildNicknameService();
        PlayerStatsService playerStatsService = buildPlayerStatsService();
        LeaderboardService leaderboardService = buildLeaderboardService();

        registerMinecraftCommands(nicknameService);
        registerPlaceholderExpansion(nicknameService);
        registerDiscordIntegrations(playerStatsService, leaderboardService);
    }

    @Override
    public void onDisable() {
        if (deathMessageProcessor != null) {
            github.scarsz.discordsrv.DiscordSRV.api.unsubscribe(deathMessageProcessor);
        }
        if (discordInteractionListener != null) {
            discordInteractionListener.shutdown();
            github.scarsz.discordsrv.DiscordSRV.api.unsubscribe(discordInteractionListener);
        }
    }

    private NicknameService buildNicknameService() {
        NicknameRepository nicknameRepository = new NicknameRepository(getConfig(), this::saveConfig);
        return new NicknameService(nicknameRepository);
    }

    private PlayerStatsService buildPlayerStatsService() {
        return new PlayerStatsService(new StatsFormatter());
    }

    private LeaderboardService buildLeaderboardService() {
        return new LeaderboardService(new StatsFormatter());
    }

    private void registerMinecraftCommands(NicknameService nicknameService) {
        DiscordNickCommand nickCommand = new DiscordNickCommand(nicknameService);
        getCommand("setDiscordNick").setExecutor(nickCommand);
        getCommand("getDiscordNick").setExecutor(nickCommand);
    }

    private void registerPlaceholderExpansion(NicknameService nicknameService) {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            return;
        }

        new PaperBridgeExpansion(nicknameService).register();
        getLogger().info("已成功掛載 PlaceholderAPI 擴展");
    }

    private void registerDiscordIntegrations(
            PlayerStatsService playerStatsService,
            LeaderboardService leaderboardService
    ) {
        if (getServer().getPluginManager().getPlugin("DiscordSRV") == null) {
            return;
        }

        DiscordLinkedPlayerResolver linkedPlayerResolver = new DiscordLinkedPlayerResolver();
        DiscordEmbedFactory embedFactory = new DiscordEmbedFactory();
        DiscordCommandRegistrar commandRegistrar = new DiscordCommandRegistrar(this);
        DiscordStatsCommandHandler statsCommandHandler = new DiscordStatsCommandHandler(
                linkedPlayerResolver,
                playerStatsService,
                embedFactory
        );
        DiscordLeaderboardCommandHandler leaderboardCommandHandler = new DiscordLeaderboardCommandHandler(
                linkedPlayerResolver,
                leaderboardService,
                embedFactory
        );
        DiscordOnlineCommandHandler onlineCommandHandler = new DiscordOnlineCommandHandler(embedFactory);

        deathMessageProcessor = new DeathMessageProcessor();
        github.scarsz.discordsrv.DiscordSRV.api.subscribe(deathMessageProcessor);
        getLogger().info("已成功掛載 DiscordSRV Death Message Processor");

        discordInteractionListener = new DiscordInteractionListener(
                this,
                commandRegistrar,
                statsCommandHandler,
                leaderboardCommandHandler,
                onlineCommandHandler
        );
        github.scarsz.discordsrv.DiscordSRV.api.subscribe(discordInteractionListener);
        getLogger().info("已成功掛載 DiscordSRV Interaction Listener");
    }
}
