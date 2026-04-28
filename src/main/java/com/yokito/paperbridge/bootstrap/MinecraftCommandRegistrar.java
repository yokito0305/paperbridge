package com.yokito.paperbridge.bootstrap;

import javax.annotation.Nonnull;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Registers Bukkit commands owned by this plugin.
 */
public class MinecraftCommandRegistrar implements RuntimeComponent {

    private final JavaPlugin plugin;
    private final CommandExecutor discordNickCommand;

    public MinecraftCommandRegistrar(JavaPlugin plugin, CommandExecutor discordNickCommand) {
        this.plugin = plugin;
        this.discordNickCommand = discordNickCommand;
    }

    @Override
    public void start() {
        bindCommand("setDiscordNick", discordNickCommand);
        bindCommand("getDiscordNick", discordNickCommand);
    }

    @Override
    public void stop() {
    }

    private void bindCommand(@Nonnull String commandName, CommandExecutor executor) {
        PluginCommand command = plugin.getCommand(commandName);
        if (command == null) {
            plugin.getLogger().severe(PluginText.COMMAND_NOT_DECLARED_LOG + commandName);
            throw new IllegalStateException(PluginText.COMMAND_NOT_DECLARED_LOG + commandName);
        }

        command.setExecutor(executor);
        command.setTabCompleter((executor instanceof TabCompleter) ? (TabCompleter) executor : null);
    }
}
