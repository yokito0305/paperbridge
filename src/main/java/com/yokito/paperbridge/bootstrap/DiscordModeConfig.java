package com.yokito.paperbridge.bootstrap;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Reads Discord mode flags from discord.yml.
 */
public class DiscordModeConfig {

    static final String ENABLE_CUSTOM_BOT_PATH = "enable-custom-bot";

    private final boolean customBotEnabled;

    public DiscordModeConfig(FileConfiguration configuration) {
        this.customBotEnabled = configuration.getBoolean(ENABLE_CUSTOM_BOT_PATH, false);
    }

    public boolean isCustomBotEnabled() {
        return customBotEnabled;
    }
}
