package com.yokito.paperbridge.bootstrap;

import com.yokito.paperbridge.command.discord.DiscordCommandRegistrar;
import com.yokito.paperbridge.command.discord.DiscordSlashCommandRegistry;
import com.yokito.paperbridge.integration.discordsrv.DeathMessageProcessor;
import com.yokito.paperbridge.integration.discordsrv.DiscordGateway;
import com.yokito.paperbridge.integration.discordsrv.DiscordInteractionListener;
import com.yokito.paperbridge.service.nickname.NicknameService;
import com.yokito.paperbridge.service.stats.LeaderboardService;
import com.yokito.paperbridge.service.stats.PlayerStatsService;

/**
 * 封裝由 {@link PaperBridgeBootstrap} 組裝完成的核心元件。
 *
 * <p>此 record 讓 {@link PaperBridgePlugin} 可以用明確欄位取得需要註冊或關閉的物件，
 * 同時避免 plugin 主類別自己持有過多細節依賴。</p>
 */
public record PaperBridgeComponents(
        NicknameService nicknameService,
        PlayerStatsService playerStatsService,
        LeaderboardService leaderboardService,
        DiscordGateway discordGateway,
        DiscordSlashCommandRegistry discordCommandRegistry,
        DiscordCommandRegistrar discordCommandRegistrar,
        DiscordInteractionListener discordInteractionListener,
        DeathMessageProcessor deathMessageProcessor
) {
}
