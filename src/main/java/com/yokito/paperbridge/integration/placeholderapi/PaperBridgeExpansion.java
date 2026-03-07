package com.yokito.paperbridge.integration.placeholderapi;

import com.yokito.paperbridge.service.nickname.NicknameService;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

/**
 * PaperBridge 提供給 PlaceholderAPI 的擴充入口。
 *
 * <p>此類別是 PlaceholderAPI 與暱稱服務之間的邊界，只負責把 placeholder 參數轉成
 * {@link NicknameService} 可處理的查詢。</p>
 */
public class PaperBridgeExpansion extends PlaceholderExpansion {

    private final NicknameService nicknameService;

    /**
     * 建立使用暱稱服務的 placeholder expansion。
     */
    public PaperBridgeExpansion(NicknameService nicknameService) {
        this.nicknameService = nicknameService;
    }

    /**
     * 回傳此 expansion 的 placeholder 前綴。
     */
    @Override
    public @NotNull String getIdentifier() {
        return "paperbridge";
    }

    /**
     * 回傳 expansion 作者名稱。
     */
    @Override
    public @NotNull String getAuthor() {
        return "yokito";
    }

    /**
     * 回傳 expansion 版本號。
     */
    @Override
    public @NotNull String getVersion() {
        return "1.1-BETA";
    }

    /**
     * 告知 PlaceholderAPI 重新載入時應保留此 expansion。
     */
    @Override
    public boolean persist() {
        return true;
    }

    /**
     * 告知 PlaceholderAPI 此 expansion 可直接註冊。
     */
    @Override
    public boolean canRegister() {
        return true;
    }

    /**
     * 解析 PaperBridge 提供的 placeholder。
     *
     * <p>目前只支援 `discord_nick`，若玩家不存在則回傳空字串。</p>
     */
    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        if (params.equalsIgnoreCase("discord_nick")) {
            return nicknameService.getDisplayNickname(player.getUniqueId(), player.getName());
        }

        return null;
    }
}
