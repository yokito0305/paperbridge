package com.yokito.paperbridge.bootstrap;

import com.yokito.paperbridge.command.minecraft.DiscordNickCommand;
import com.yokito.paperbridge.integration.discordsrv.DeathMessageProcessor;
import com.yokito.paperbridge.integration.placeholderapi.PaperBridgeExpansion;
import com.yokito.paperbridge.service.discord.DiscordText;
import com.yokito.paperbridge.service.nickname.NicknameService;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 外掛主入口，負責 Paper 生命周期與整體功能啟停。
 *
 * <p>此類別位於 composition root 的最外層，會在啟用時委派 {@link PaperBridgeBootstrap}
 * 組裝核心元件，並依伺服器上已安裝的插件決定要不要啟用 PlaceholderAPI 與 DiscordSRV 整合。</p>
 */
public class PaperBridgePlugin extends JavaPlugin {

    private PaperBridgeComponents components;

    /**
     * 在 Paper 啟用外掛時建立核心元件並註冊可用整合。
     *
     * <p>副作用包含讀取預設設定檔、綁定 Minecraft 指令、註冊 Placeholder expansion，
     * 以及掛載 Discord 相關 listener。</p>
     */
    @Override
    public void onEnable() {
        saveDefaultConfig();
        components = new PaperBridgeBootstrap(this).build();

        registerMinecraftCommands(components.nicknameService());
        registerPlaceholderExpansion(components.nicknameService());
        registerDiscordIntegrations(components.deathMessageProcessor());
    }

    /**
     * 在外掛停用時解除 Discord 相關 listener，避免 JDA 與 DiscordSRV 持續保留參考。
     */
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

    /**
     * 將 `plugin.yml` 中宣告的 Minecraft 指令綁到同一個執行器。
     *
     * <p>`setDiscordNick` 與 `getDiscordNick` 共用相同流程物件，只在內部再依名稱分流。</p>
     */
    private void registerMinecraftCommands(NicknameService nicknameService) {
        DiscordNickCommand nickCommand = new DiscordNickCommand(nicknameService);
        bindCommand("setDiscordNick", nickCommand);
        bindCommand("getDiscordNick", nickCommand);
    }

    /**
     * 在 PlaceholderAPI 存在時註冊 PaperBridge 的 placeholder 擴充。
     */
    private void registerPlaceholderExpansion(NicknameService nicknameService) {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            return;
        }

        new PaperBridgeExpansion(nicknameService).register();
        getLogger().info(DiscordText.PLACEHOLDER_ENABLED_LOG);
    }

    /**
     * 在 DiscordSRV 存在時掛上死亡訊息處理器與 Discord slash command listener。
     */
    private void registerDiscordIntegrations(DeathMessageProcessor deathMessageProcessor) {
        if (getServer().getPluginManager().getPlugin("DiscordSRV") == null) {
            return;
        }

        components.discordGateway().subscribe(deathMessageProcessor);
        getLogger().info(DiscordText.DISCORD_DEATH_PROCESSOR_ENABLED_LOG);

        components.discordGateway().subscribe(components.discordInteractionListener());
        getLogger().info(DiscordText.DISCORD_INTERACTION_ENABLED_LOG);
    }

    /**
     * 綁定單一 Bukkit 指令到執行器。
     *
     * <p>若 `plugin.yml` 沒有宣告對應指令，這裡會直接 fail fast，避免執行時才出現
     * `NullPointerException`。</p>
     */
    private void bindCommand(String commandName, CommandExecutor executor) {
        PluginCommand command = getCommand(commandName);
        if (command == null) {
            getLogger().severe(PluginText.COMMAND_NOT_DECLARED_LOG + commandName);
            throw new IllegalStateException(PluginText.COMMAND_NOT_DECLARED_LOG + commandName);
        }

        command.setExecutor(executor);
        command.setTabCompleter((executor instanceof TabCompleter) ? (TabCompleter) executor : null);
    }
}
