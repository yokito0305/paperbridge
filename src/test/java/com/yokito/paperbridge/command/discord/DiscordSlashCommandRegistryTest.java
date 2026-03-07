package com.yokito.paperbridge.command.discord;

import com.yokito.paperbridge.integration.discordsrv.DiscordGateway;
import com.yokito.paperbridge.service.discord.DiscordEmbedFactory;
import com.yokito.paperbridge.service.discord.DiscordLinkedPlayerResolver;
import com.yokito.paperbridge.service.stats.LeaderboardService;
import com.yokito.paperbridge.service.stats.PlayerStatsService;
import com.yokito.paperbridge.service.stats.StatsFormatter;
import github.scarsz.discordsrv.dependencies.jda.api.JDA;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiscordSlashCommandRegistryTest {

    @Test
    void shouldExposeAllConfiguredCommandsByName() {
        StatsFormatter statsFormatter = new StatsFormatter();
        DiscordEmbedFactory embedFactory = new DiscordEmbedFactory();
        DiscordGateway discordGateway = new TestDiscordGateway();
        DiscordLinkedPlayerResolver linkedPlayerResolver = new DiscordLinkedPlayerResolver(discordGateway);

        DiscordSlashCommandRegistry registry = new DiscordSlashCommandRegistry(List.of(
                new DiscordStatsCommandHandler(linkedPlayerResolver, new PlayerStatsService(statsFormatter), embedFactory),
                new DiscordLeaderboardCommandHandler(linkedPlayerResolver, new LeaderboardService(statsFormatter), embedFactory),
                new DiscordOnlineCommandHandler(embedFactory)
        ));

        assertEquals(List.of("stats", "leaderboard", "online"),
                registry.commands().stream().map(DiscordSlashCommand::name).toList());
        assertTrue(registry.find("stats").isPresent());
        assertTrue(registry.find("leaderboard").isPresent());
        assertTrue(registry.find("online").isPresent());
        assertTrue(registry.find("missing").isEmpty());

        assertEquals("stats", registry.find("stats").orElseThrow().definition().getName());
        assertEquals(1, registry.find("stats").orElseThrow().definition().getOptions().size());
        assertEquals("leaderboard", registry.find("leaderboard").orElseThrow().definition().getName());
        assertEquals(1, registry.find("leaderboard").orElseThrow().definition().getOptions().size());
        assertEquals("online", registry.find("online").orElseThrow().definition().getName());
    }

    private static final class TestDiscordGateway implements DiscordGateway {

        @Override
        public JDA getJda() {
            return null;
        }

        @Override
        public void subscribe(Object listener) {
        }

        @Override
        public void unsubscribe(Object listener) {
        }

        @Override
        public UUID getLinkedPlayerId(String discordUserId) {
            return null;
        }

        @Override
        public Set<UUID> getLinkedPlayerIds() {
            return Set.of();
        }
    }
}
