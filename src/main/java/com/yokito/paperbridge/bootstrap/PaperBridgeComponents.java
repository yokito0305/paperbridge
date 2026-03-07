package com.yokito.paperbridge.bootstrap;

import com.yokito.paperbridge.command.discord.DiscordCommandRegistrar;
import com.yokito.paperbridge.command.discord.DiscordSlashCommandRegistry;
import com.yokito.paperbridge.integration.discordsrv.DeathMessageProcessor;
import com.yokito.paperbridge.integration.discordsrv.DiscordGateway;
import com.yokito.paperbridge.integration.discordsrv.DiscordInteractionListener;
import com.yokito.paperbridge.service.nickname.NicknameService;
import com.yokito.paperbridge.service.stats.LeaderboardService;
import com.yokito.paperbridge.service.stats.PlayerStatsService;

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
