package com.yokito.paperbridge.service.discord;

import github.scarsz.discordsrv.DiscordSRV;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Set;
import java.util.UUID;

public class DiscordLinkedPlayerResolver {

    public OfflinePlayer resolveLinkedPlayer(String discordUserId) {
        UUID playerId = DiscordSRV.getPlugin().getAccountLinkManager().getUuid(discordUserId);
        if (playerId == null) {
            return null;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
        if (!player.hasPlayedBefore() && !player.isOnline()) {
            return null;
        }

        return player;
    }

    public Set<UUID> getLinkedPlayerIds() {
        return Set.copyOf(DiscordSRV.getPlugin().getAccountLinkManager().getLinkedAccounts().values());
    }
}
