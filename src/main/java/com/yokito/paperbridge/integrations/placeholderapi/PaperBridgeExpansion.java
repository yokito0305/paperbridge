package com.yokito.paperbridge.integrations.placeholderapi;

import com.yokito.paperbridge.PaperBridge;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PaperBridgeExpansion extends PlaceholderExpansion {

    private final PaperBridge plugin;

    public PaperBridgeExpansion(PaperBridge plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "paperbridge";
    }

    @Override
    public @NotNull String getAuthor() {
        return "yokito";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.1-BETA";
    }

    // 確保 /papi reload 時不會清除這個變數
    @Override
    public boolean persist() {
        return true;
    }

    // 允許註冊
    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        if (params.equalsIgnoreCase("discord_nick")) {
            // 若玩家未設定 Discord 暱稱，則回傳玩家的 Minecraft 名稱
            String nick = plugin.getConfig().getString("nicks." + player.getUniqueId(), null);
            return (nick != null) && (!nick.equals(player.getName())) ? nick + " | " + player.getName() : player.getName();
        }

        return null; // 未知的變數回傳 null
    }
}
