package com.yokito.paperbridge.bootstrap;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 負責將 Bukkit 指令綁定到執行器。
 */
public class MinecraftCommandRegistrar {

    private final JavaPlugin plugin;
    private final CommandExecutor discordNickCommand;

    public MinecraftCommandRegistrar(JavaPlugin plugin, CommandExecutor discordNickCommand) {
        this.plugin = plugin;
        this.discordNickCommand = discordNickCommand;
    }

    public void register() {
        bindCommand("setDiscordNick", discordNickCommand);
        bindCommand("getDiscordNick", discordNickCommand);
    }

    private void bindCommand(String commandName, CommandExecutor executor) {
        PluginCommand command = plugin.getCommand(commandName);
        if (command == null) {
            plugin.getLogger().severe(PluginText.COMMAND_NOT_DECLARED_LOG + commandName);
            throw new IllegalStateException(PluginText.COMMAND_NOT_DECLARED_LOG + commandName);
        }

        command.setExecutor(executor);
        command.setTabCompleter((executor instanceof TabCompleter) ? (TabCompleter) executor : null);
    }
}
