package com.yokito.paperbridge.bootstrap;

import com.yokito.paperbridge.command.minecraft.DiscordNickCommand;
import com.yokito.paperbridge.integration.discordsrv.DeathMessageProcessor;
import com.yokito.paperbridge.integration.placeholderapi.PaperBridgeExpansion;
import com.yokito.paperbridge.service.discord.DiscordText;
import com.yokito.paperbridge.service.nickname.NicknameService;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class PaperBridgePlugin extends JavaPlugin {

    private PaperBridgeComponents components;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        components = new PaperBridgeBootstrap(this).build();

        registerMinecraftCommands(components.nicknameService());
        registerPlaceholderExpansion(components.nicknameService());
        registerDiscordIntegrations(components.deathMessageProcessor());
    }

    @Override
    public void onDisable() {
        if (components == null) {
            return;
        }

        DeathMessageProcessor deathMessageProcessor = components.deathMessageProcessor();
        components.discordGateway().unsubscribe(deathMessageProcessor);
        components.discordInteractionListener().shutdown();
        components.discordGateway().unsubscribe(components.discordInteractionListener());
    }

    private void registerMinecraftCommands(NicknameService nicknameService) {
        DiscordNickCommand nickCommand = new DiscordNickCommand(nicknameService);
        bindCommand("setDiscordNick", nickCommand);
        bindCommand("getDiscordNick", nickCommand);
    }

    private void registerPlaceholderExpansion(NicknameService nicknameService) {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            return;
        }

        new PaperBridgeExpansion(nicknameService).register();
        getLogger().info(DiscordText.PLACEHOLDER_ENABLED_LOG);
    }

    private void registerDiscordIntegrations(DeathMessageProcessor deathMessageProcessor) {
        if (getServer().getPluginManager().getPlugin("DiscordSRV") == null) {
            return;
        }

        components.discordGateway().subscribe(deathMessageProcessor);
        getLogger().info(DiscordText.DISCORD_DEATH_PROCESSOR_ENABLED_LOG);

        components.discordGateway().subscribe(components.discordInteractionListener());
        getLogger().info(DiscordText.DISCORD_INTERACTION_ENABLED_LOG);
    }

    private void bindCommand(String commandName, CommandExecutor executor) {
        PluginCommand command = getCommand(commandName);
        if (command == null) {
            getLogger().severe(PluginText.COMMAND_NOT_DECLARED_LOG + commandName);
            throw new IllegalStateException(PluginText.COMMAND_NOT_DECLARED_LOG + commandName);
        }

        command.setExecutor(executor);
    }
}
