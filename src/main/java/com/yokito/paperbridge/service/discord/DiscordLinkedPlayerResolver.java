package com.yokito.paperbridge.service.discord;

import com.yokito.paperbridge.integration.discordsrv.DiscordGateway;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public class DiscordLinkedPlayerResolver {

    private final DiscordGateway discordGateway;
    private final Function<UUID, OfflinePlayer> offlinePlayerProvider;

    public DiscordLinkedPlayerResolver(DiscordGateway discordGateway) {
        this(discordGateway, Bukkit::getOfflinePlayer);
    }

    DiscordLinkedPlayerResolver(DiscordGateway discordGateway, Function<UUID, OfflinePlayer> offlinePlayerProvider) {
        this.discordGateway = discordGateway;
        this.offlinePlayerProvider = offlinePlayerProvider;
    }

    public @Nullable OfflinePlayer resolveLinkedPlayer(String discordUserId) {
        UUID playerId = discordGateway.getLinkedPlayerId(discordUserId);
        if (playerId == null) {
            return null;
        }

        OfflinePlayer player = offlinePlayerProvider.apply(playerId);
        if (!player.hasPlayedBefore() && !player.isOnline()) {
            return null;
        }

        return player;
    }

    public Set<UUID> getLinkedPlayerIds() {
        return discordGateway.getLinkedPlayerIds();
    }
}
