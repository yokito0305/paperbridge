package com.yokito.paperbridge.service.discord;

import com.yokito.paperbridge.integration.discordsrv.DiscordGateway;
import github.scarsz.discordsrv.dependencies.jda.api.JDA;
import org.bukkit.OfflinePlayer;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DiscordLinkedPlayerResolverTest {

    @Test
    void shouldResolveEligibleLinkedPlayer() {
        UUID playerId = UUID.randomUUID();
        OfflinePlayer player = fakePlayer(playerId, "Alex", true, false);
        DiscordLinkedPlayerResolver resolver = new DiscordLinkedPlayerResolver(
                new FakeDiscordGateway(Map.of("123", playerId), Set.of(playerId)),
                id -> player
        );

        assertEquals(player, resolver.resolveLinkedPlayer("123"));
        assertEquals(Set.of(playerId), resolver.getLinkedPlayerIds());
    }

    @Test
    void shouldReturnNullWhenAccountIsNotLinkedOrPlayerNeverJoined() {
        UUID playerId = UUID.randomUUID();
        DiscordLinkedPlayerResolver resolver = new DiscordLinkedPlayerResolver(
                new FakeDiscordGateway(Map.of("123", playerId), Set.of(playerId)),
                id -> fakePlayer(playerId, "Ghost", false, false)
        );

        assertNull(resolver.resolveLinkedPlayer("missing"));
        assertNull(resolver.resolveLinkedPlayer("123"));
    }

    private OfflinePlayer fakePlayer(UUID playerId, String playerName, boolean hasPlayedBefore, boolean isOnline) {
        return (OfflinePlayer) Proxy.newProxyInstance(
                OfflinePlayer.class.getClassLoader(),
                new Class<?>[]{OfflinePlayer.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getUniqueId" -> playerId;
                    case "getName" -> playerName;
                    case "hasPlayedBefore" -> hasPlayedBefore;
                    case "isOnline" -> isOnline;
                    case "hashCode" -> playerId.hashCode();
                    case "equals" -> proxy == args[0];
                    case "toString" -> "FakeOfflinePlayer[" + playerName + "]";
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private record FakeDiscordGateway(Map<String, UUID> linkedAccounts, Set<UUID> linkedPlayerIds) implements DiscordGateway {

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
            return linkedAccounts.get(discordUserId);
        }

        @Override
        public Set<UUID> getLinkedPlayerIds() {
            return linkedPlayerIds;
        }

        @Override
        public void syncMemberNickname(String discordUserId, String displayNickname) {
        }
    }
}
