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

/**
 * 核心組裝器，負責建立 PaperBridge 執行所需的所有主要元件。
 *
 * <p>這裡是專案的 composition root：服務、Discord gateway、command registry、
 * registrar 與 listener 都在這裡串接完成，再打包成 {@link PaperBridgeComponents} 回傳。</p>
 */
public class PaperBridgeBootstrap {

        private final PaperBridgePlugin plugin;

        /**
         * 建立綁定到特定 plugin 實例的組裝器。
         */
        public PaperBridgeBootstrap(PaperBridgePlugin plugin) {
                this.plugin = plugin;
        }

        /**
         * 依照依賴順序建立核心服務與 Discord 整合元件。
         *
         * <p>這裡只做組裝，不觸發任何註冊動作；實際註冊時機由 {@link PaperBridgePlugin}
         * 在生命周期入口控制。</p>
         */
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
