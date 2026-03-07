package com.yokito.paperbridge.service.discord;

import com.yokito.paperbridge.integration.discordsrv.DiscordGateway;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

/**
 * 將 Discord 使用者 ID 解析成可用的 Minecraft 玩家。
 *
 * <p>此服務隔離 Discord 綁定查詢與 Bukkit `OfflinePlayer` 取得邏輯，供 Discord commands
 * 與排行榜流程重用。</p>
 */
public class DiscordLinkedPlayerResolver {

    private final DiscordGateway discordGateway;
    private final Function<UUID, OfflinePlayer> offlinePlayerProvider;

    /**
     * 建立使用 Bukkit 預設 `OfflinePlayer` provider 的解析器。
     */
    public DiscordLinkedPlayerResolver(DiscordGateway discordGateway) {
        this(discordGateway, Bukkit::getOfflinePlayer);
    }

    /**
     * 供測試替換 `OfflinePlayer` 來源的建構子。
     */
    DiscordLinkedPlayerResolver(DiscordGateway discordGateway, Function<UUID, OfflinePlayer> offlinePlayerProvider) {
        this.discordGateway = discordGateway;
        this.offlinePlayerProvider = offlinePlayerProvider;
    }

    /**
     * 將 Discord 使用者解析成已加入過伺服器的 Minecraft 玩家。
     *
     * <p>若沒有綁定資料，或玩家從未加入且目前不在線上，會回傳 `null`。</p>
     */
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

    /**
     * 取得所有已綁定玩家的 UUID，供排行榜等批次查詢使用。
     */
    public Set<UUID> getLinkedPlayerIds() {
        return discordGateway.getLinkedPlayerIds();
    }
}
