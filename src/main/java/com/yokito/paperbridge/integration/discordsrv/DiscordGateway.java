package com.yokito.paperbridge.integration.discordsrv;

import github.scarsz.discordsrv.dependencies.jda.api.JDA;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

/**
 * 封裝本專案所需的 DiscordSRV / JDA 存取點。
 *
 * <p>service 與 listener 只依賴這個 facade，而不直接觸碰 DiscordSRV 靜態 API，
 * 方便測試與未來替換整合實作。</p>
 */
public interface DiscordGateway {

    /**
     * 取得目前可用的 JDA 實例；若 Discord 尚未就緒則可能為 `null`。
     */
    @Nullable
    JDA getJda();

    /**
     * 訂閱 DiscordSRV API 事件。
     */
    void subscribe(Object listener);

    /**
     * 取消訂閱 DiscordSRV API 事件。
     */
    void unsubscribe(Object listener);

    /**
     * 依 Discord 使用者 ID 查詢已綁定的 Minecraft 玩家 UUID。
     */
    @Nullable
    UUID getLinkedPlayerId(String discordUserId);

    /**
     * 更新指定 Discord 使用者在所有 Guild 中的伺服器暱稱。
     *
     * <p>操作為非同步（fire-and-forget）；失敗時由實作層記錄警告，
     * 不影響呼叫端的主流程。</p>
     */
    void syncMemberNickname(String discordUserId, String displayNickname);

    /**
     * 取得目前所有已綁定的 Minecraft 玩家 UUID。
     */
    Set<UUID> getLinkedPlayerIds();
}
