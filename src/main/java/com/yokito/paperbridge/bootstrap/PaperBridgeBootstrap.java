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

import java.util.List;

/**
 * 核心組裝器，負責建立 PaperBridge 執行所需的所有主要元件。
 *
 * <p>這裡是專案的 composition root：服務、Discord gateway、command registry、
 * registrar 與 listener 都在這裡串接完成，再回傳可直接啟停的 {@link PaperBridgeRuntime}。</p>
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
     * 依照依賴順序建立核心服務與所有註冊協調器。
     *
     * <p>這裡只建立物件圖，不直接觸發任何副作用；副作用由回傳的 runtime 控制。</p>
     */
    public PaperBridgeRuntime build() {

        // ── 1. 核心服務層 ──────────────────────────────────────────
        StatsFormatter statsFormatter = new StatsFormatter();
        NicknameService nicknameService = new NicknameService(
                new NicknameRepository(plugin.getConfig(), plugin::saveConfig));
        PlayerStatsService playerStatsService = new PlayerStatsService(statsFormatter);
        LeaderboardService leaderboardService = new LeaderboardService(statsFormatter);

        // ── 2. Discord 基礎設施（Gateway / Resolver / Embed） ─────
        DiscordGateway discordGateway = new DiscordSrvGateway();
        DiscordLinkedPlayerResolver linkedPlayerResolver =
                new DiscordLinkedPlayerResolver(discordGateway);
        DiscordEmbedFactory embedFactory = new DiscordEmbedFactory();

        // ── 3. Discord Slash Command 處理器 ───────────────────────
        DiscordStatsCommandHandler statsCommand =
                new DiscordStatsCommandHandler(linkedPlayerResolver, playerStatsService, embedFactory);
        DiscordLeaderboardCommandHandler leaderboardCommand =
                new DiscordLeaderboardCommandHandler(linkedPlayerResolver, leaderboardService, embedFactory);
        DiscordOnlineCommandHandler onlineCommand =
                new DiscordOnlineCommandHandler(embedFactory);

        // ── 4. Command Registry 與 Interaction Listener ──────────
        DiscordSlashCommandRegistry commandRegistry = new DiscordSlashCommandRegistry(
                List.of(statsCommand, leaderboardCommand, onlineCommand));
        DiscordCommandRegistrar commandRegistrar =
                new DiscordCommandRegistrar(plugin.getLogger(), commandRegistry);
        DiscordInteractionListener discordInteractionListener = new DiscordInteractionListener(
                plugin, discordGateway, commandRegistrar, commandRegistry);

        // ── 5. 其餘獨立元件 ───────────────────────────────────────
        DeathMessageProcessor deathMessageProcessor = new DeathMessageProcessor();
        DiscordNickCommand discordNickCommand = new DiscordNickCommand(nicknameService);

        // ── 6. 組裝三個 Registrar，交由 Runtime 統一管理啟停 ──────
        MinecraftCommandRegistrar minecraftCommandRegistrar =
                new MinecraftCommandRegistrar(plugin, discordNickCommand);
        PlaceholderRegistrar placeholderRegistrar =
                new PlaceholderRegistrar(plugin, nicknameService);
        DiscordIntegrationRegistrar discordIntegrationRegistrar = new DiscordIntegrationRegistrar(
                plugin, discordGateway, deathMessageProcessor, discordInteractionListener);

        return new PaperBridgeRuntime(
                minecraftCommandRegistrar,
                placeholderRegistrar,
                discordIntegrationRegistrar
        );
    }
}
