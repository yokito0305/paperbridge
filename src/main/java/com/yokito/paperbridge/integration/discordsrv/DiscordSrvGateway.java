package com.yokito.paperbridge.integration.discordsrv;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.JDA;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Guild;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * 以 DiscordSRV 靜態 API 實作 {@link DiscordGateway}。
 *
 * <p>
 * 這裡集中所有對 DiscordSRV 的直接呼叫，讓其他模組不需要知道外部框架細節。
 * </p>
 */
public class DiscordSrvGateway implements DiscordGateway {

    /**
     * 從 DiscordSRV 取得目前 JDA 實例。
     */
    @Override
    public @Nullable JDA getJda() {
        return DiscordSRV.getPlugin().getJda();
    }

    /**
     * 透過 DiscordSRV API 訂閱事件 listener。
     */
    @Override
    public void subscribe(Object listener) {
        DiscordSRV.api.subscribe(listener);
    }

    /**
     * 透過 DiscordSRV API 取消訂閱事件 listener。
     */
    @Override
    public void unsubscribe(Object listener) {
        DiscordSRV.api.unsubscribe(listener);
    }

    /**
     * 查詢指定 Discord 使用者綁定到哪個 Minecraft 玩家 UUID。
     */
    @Override
    public @Nullable UUID getLinkedPlayerId(String discordUserId) {
        return DiscordSRV.getPlugin().getAccountLinkManager().getUuid(discordUserId);
    }

    /**
     * 取得所有已綁定帳號的玩家 UUID 集合。
     */
    @Override
    public Set<UUID> getLinkedPlayerIds() {
        return Set.copyOf(DiscordSRV.getPlugin().getAccountLinkManager().getLinkedAccounts().values());
    }

    /**
     * 非同步更新指定 Discord 使用者在所有 Guild 的伺服器暱稱。
     *
     * <p>
     * 先從 JDA cache 拉取 Member；若不在 cache 中則透過 REST retrieveMemberById。
     * 任何 API 失敗僅記錄警告，不拋出例外。
     * </p>
     */
    @Override
    public void syncMemberNickname(String discordUserId, String displayNickname) {
        JDA jda = getJda();
        String m_discordUserId = (discordUserId != null) ? discordUserId : "04000";
        if (jda == null) {
            return;
        }

        for (Guild guild : jda.getGuilds()) {
            guild.retrieveMemberById(m_discordUserId).queue(
                    member -> guild.modifyNickname(Objects.requireNonNull(member), displayNickname).queue(
                            null,
                            err -> DiscordSRV.getPlugin().getLogger()
                                    .warning("Discord 暱稱同步失敗 (" + discordUserId + "): " + err.getMessage())),
                    err -> DiscordSRV.getPlugin().getLogger()
                            .warning("找不到 Discord 成員 (" + discordUserId + "): " + err.getMessage()));
        }
    }
}
