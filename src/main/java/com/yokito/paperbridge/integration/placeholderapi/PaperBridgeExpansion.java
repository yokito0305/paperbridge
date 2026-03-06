package com.yokito.paperbridge.integration.placeholderapi;

import com.yokito.paperbridge.service.nickname.NicknameService;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PaperBridgeExpansion extends PlaceholderExpansion {

    private final NicknameService nicknameService;

    public PaperBridgeExpansion(NicknameService nicknameService) {
        this.nicknameService = nicknameService;
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

    @Override
    public boolean persist() {
        return true;
    }

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
            return nicknameService.getDisplayNickname(player.getUniqueId(), player.getName());
        }

        return null;
    }
}
