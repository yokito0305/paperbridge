package com.yokito.paperbridge.integration.discordsrv;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.JDA;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public class DiscordSrvGateway implements DiscordGateway {

    @Override
    public @Nullable JDA getJda() {
        return DiscordSRV.getPlugin().getJda();
    }

    @Override
    public void subscribe(Object listener) {
        DiscordSRV.api.subscribe(listener);
    }

    @Override
    public void unsubscribe(Object listener) {
        DiscordSRV.api.unsubscribe(listener);
    }

    @Override
    public @Nullable UUID getLinkedPlayerId(String discordUserId) {
        return DiscordSRV.getPlugin().getAccountLinkManager().getUuid(discordUserId);
    }

    @Override
    public Set<UUID> getLinkedPlayerIds() {
        return Set.copyOf(DiscordSRV.getPlugin().getAccountLinkManager().getLinkedAccounts().values());
    }
}
