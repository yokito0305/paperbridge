package com.yokito.paperbridge.bootstrap;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * Main Bukkit plugin entrypoint.
 */
public class PaperBridgePlugin extends JavaPlugin {

    private static final String DISCORD_CONFIG_FILE_NAME = "discord.yml";

    private PaperBridgeRuntime runtime;
    private FileConfiguration discordConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (!new File(getDataFolder(), DISCORD_CONFIG_FILE_NAME).exists()) {
            saveResource(DISCORD_CONFIG_FILE_NAME, false);
        }
        // 讀取 discord.yml 配置文件
        discordConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), DISCORD_CONFIG_FILE_NAME));
        runtime = new PaperBridgeBootstrap(this).build();
        runtime.start();
    }

    @Override
    public void onDisable() {
        if (runtime == null) {
            return;
        }
        runtime.stop();
    }

    public FileConfiguration getDiscordConfig() {
        if (discordConfig == null) {
            discordConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), DISCORD_CONFIG_FILE_NAME));
        }
        return discordConfig;
    }
}
