package com.yokito.paperbridge.bootstrap;

import com.yokito.paperbridge.command.discord.DiscordCommandRegistrar;
import com.yokito.paperbridge.command.discord.DiscordLeaderboardCommandHandler;
import com.yokito.paperbridge.command.discord.DiscordOnlineCommandHandler;
import com.yokito.paperbridge.command.discord.DiscordSlashCommandRegistry;
import com.yokito.paperbridge.command.discord.DiscordStatsCommandHandler;
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

import java.util.List;

public class PaperBridgeBootstrap {

        private final PaperBridgePlugin plugin;

        public PaperBridgeBootstrap(PaperBridgePlugin plugin) {
                this.plugin = plugin;
        }

        public PaperBridgeComponents build() {
                StatsFormatter statsFormatter = new StatsFormatter();
                NicknameService nicknameService = new NicknameService(
                                new NicknameRepository(plugin.getConfig(), plugin::saveConfig));
                PlayerStatsService playerStatsService = new PlayerStatsService(statsFormatter);
                LeaderboardService leaderboardService = new LeaderboardService(statsFormatter);

                DiscordGateway discordGateway = new DiscordSrvGateway();
                DiscordLinkedPlayerResolver linkedPlayerResolver = new DiscordLinkedPlayerResolver(discordGateway);
                DiscordEmbedFactory embedFactory = new DiscordEmbedFactory();

                DiscordStatsCommandHandler statsCommand = new DiscordStatsCommandHandler(
                                linkedPlayerResolver,
                                playerStatsService,
                                embedFactory);
                DiscordLeaderboardCommandHandler leaderboardCommand = new DiscordLeaderboardCommandHandler(
                                linkedPlayerResolver,
                                leaderboardService,
                                embedFactory);
                DiscordOnlineCommandHandler onlineCommand = new DiscordOnlineCommandHandler(embedFactory);

                DiscordSlashCommandRegistry commandRegistry = new DiscordSlashCommandRegistry(
                                List.of(statsCommand, leaderboardCommand, onlineCommand));
                DiscordCommandRegistrar commandRegistrar = new DiscordCommandRegistrar(plugin.getLogger(),
                                commandRegistry);

                return new PaperBridgeComponents(
                                nicknameService,
                                playerStatsService,
                                leaderboardService,
                                discordGateway,
                                commandRegistry,
                                commandRegistrar,
                                new DiscordInteractionListener(plugin, discordGateway, commandRegistrar,
                                                commandRegistry),
                                new DeathMessageProcessor());
        }

}