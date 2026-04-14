package com.yokito.paperbridge.bootstrap;

import com.yokito.paperbridge.integration.placeholderapi.PaperBridgeExpansion;
import com.yokito.paperbridge.service.discord.DiscordText;
import com.yokito.paperbridge.service.nickname.NicknameService;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Registers PlaceholderAPI integration when it is available.
 */
public class PlaceholderRegistrar implements RuntimeComponent {

    private final JavaPlugin plugin;
    private final NicknameService nicknameService;

    public PlaceholderRegistrar(JavaPlugin plugin, NicknameService nicknameService) {
        this.plugin = plugin;
        this.nicknameService = nicknameService;
    }

    @Override
    public void start() {
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            return;
        }

        new PaperBridgeExpansion(nicknameService).register();
        plugin.getLogger().info(DiscordText.PLACEHOLDER_ENABLED_LOG);
    }

    @Override
    public void stop() {
    }
}
