package com.yokito.paperbridge.bootstrap;

import com.yokito.paperbridge.integration.placeholderapi.PaperBridgeExpansion;
import com.yokito.paperbridge.service.discord.DiscordText;
import com.yokito.paperbridge.service.nickname.NicknameService;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 負責 PlaceholderAPI 擴展註冊。
 */
public class PlaceholderRegistrar {

    private final JavaPlugin plugin;
    private final NicknameService nicknameService;

    public PlaceholderRegistrar(JavaPlugin plugin, NicknameService nicknameService) {
        this.plugin = plugin;
        this.nicknameService = nicknameService;
    }

    public void register() {
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            return;
        }

        new PaperBridgeExpansion(nicknameService).register();
        plugin.getLogger().info(DiscordText.PLACEHOLDER_ENABLED_LOG);
    }
}
