package com.yokito.paperbridge;

import com.yokito.paperbridge.commands.DiscordNickCommand;
import com.yokito.paperbridge.commands.PlayerStatsCommand;
import com.yokito.paperbridge.integrations.placeholderapi.PaperBridgeExpansion;
import com.yokito.paperbridge.listeners.discordsrv.DeathMessageProcessor;
import com.yokito.paperbridge.listeners.discordsrv.DiscordInteractionListener;
import com.yokito.paperbridge.manager.StatsManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PaperBridge extends JavaPlugin {

    private DeathMessageProcessor deathMessageProcessor;
    private DiscordInteractionListener discordInteractionListener;
    private StatsManager statsManager;

    @Override
    public void onEnable() {
        // 產生預設的 config.yml
        saveDefaultConfig();

        // 初始化核心管理器
        statsManager = new StatsManager(this);

        // 註冊 Minecraft 指令
        DiscordNickCommand nickCommand = new DiscordNickCommand(this);
        getCommand("setDiscordNick").setExecutor(nickCommand);
        getCommand("getDiscordNick").setExecutor(nickCommand);

        PlayerStatsCommand statsCommand = new PlayerStatsCommand(this, statsManager);
        getCommand("stats").setExecutor(statsCommand);

        // 註冊 PlaceholderAPI 擴展
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PaperBridgeExpansion(this).register();
            getLogger().info("已成功掛載 PlaceholderAPI 擴展");
        }

        // 註冊 DiscordSRV 相關監聽器
        if (getServer().getPluginManager().getPlugin("DiscordSRV") != null) {
            deathMessageProcessor = new DeathMessageProcessor();
            github.scarsz.discordsrv.DiscordSRV.api.subscribe(deathMessageProcessor);
            getLogger().info("已成功掛載 DiscordSRV Death Message Processor");

            discordInteractionListener = new DiscordInteractionListener(this, statsManager);
            github.scarsz.discordsrv.DiscordSRV.api.subscribe(discordInteractionListener);
            getLogger().info("已成功掛載 DiscordSRV Interaction Listener");
        }
    }

    @Override
    public void onDisable() {
        // 取消訂閱 DiscordSRV 事件
        if (deathMessageProcessor != null) {
            github.scarsz.discordsrv.DiscordSRV.api.unsubscribe(deathMessageProcessor);
        }
        if (discordInteractionListener != null) {
            discordInteractionListener.shutdown();
            github.scarsz.discordsrv.DiscordSRV.api.unsubscribe(discordInteractionListener);
        }
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }
}
