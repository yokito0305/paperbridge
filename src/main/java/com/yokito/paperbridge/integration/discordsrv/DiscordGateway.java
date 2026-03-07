package com.yokito.paperbridge.integration.discordsrv;

import github.scarsz.discordsrv.dependencies.jda.api.JDA;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public interface DiscordGateway {

    @Nullable
    JDA getJda();

    void subscribe(Object listener);

    void unsubscribe(Object listener);

    @Nullable
    UUID getLinkedPlayerId(String discordUserId);

    Set<UUID> getLinkedPlayerIds();
}
