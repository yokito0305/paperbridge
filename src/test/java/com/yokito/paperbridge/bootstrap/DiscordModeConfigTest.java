package com.yokito.paperbridge.bootstrap;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiscordModeConfigTest {

    @Test
    void shouldDefaultCustomBotToFalseWhenMissing() {
        DiscordModeConfig config = new DiscordModeConfig(new YamlConfiguration());

        assertFalse(config.isCustomBotEnabled());
    }

    @Test
    void shouldReadConfiguredCustomBotFlag() {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.set(DiscordModeConfig.ENABLE_CUSTOM_BOT_PATH, true);

        DiscordModeConfig config = new DiscordModeConfig(configuration);

        assertTrue(config.isCustomBotEnabled());
    }
}
