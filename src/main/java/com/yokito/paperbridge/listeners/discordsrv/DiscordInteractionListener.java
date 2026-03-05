package com.yokito.paperbridge.listeners.discordsrv;

import com.yokito.paperbridge.PaperBridge;
import com.yokito.paperbridge.manager.StatsManager;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordReadyEvent;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.JDA;
import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.SlashCommandEvent;
import github.scarsz.discordsrv.dependencies.jda.api.hooks.ListenerAdapter;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.build.CommandData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.awt.Color;
import java.util.Map;
import java.util.UUID;

/**
 * 處理來自 Discord 的 Slash Command 互動。
 * <p>
 * 透過 DiscordSRV 取得 JDA 實例，註冊 /stats 指令，
 * 並在收到指令時查詢連結玩家的遊戲統計數據，以私密 (Ephemeral) 訊息回覆。
 * </p>
 */
public class DiscordInteractionListener extends ListenerAdapter {

    private final PaperBridge plugin;
    private final StatsManager statsManager;

    public DiscordInteractionListener(PaperBridge plugin, StatsManager statsManager) {
        this.plugin = plugin;
        this.statsManager = statsManager;
    }

    /**
     * 當 DiscordSRV 完成與 Discord 的連線時觸發。
     * 此時我們可以取得 JDA 實例來註冊 Slash Command。
     */
    @Subscribe
    public void onDiscordReady(DiscordReadyEvent event) {
        JDA jda = DiscordSRV.getPlugin().getJda();
        if (jda == null) return;

        // 註冊 Discord Slash Command: /stats
        jda.upsertCommand(
                new CommandData("stats", "查詢你在 Minecraft 伺服器中的遊戲統計數據")
        ).queue(
                cmd -> plugin.getLogger().info("已成功註冊 Discord Slash Command: /stats (ID: " + cmd.getId() + ")"),
                err -> plugin.getLogger().warning("註冊 Discord Slash Command 失敗: " + err.getMessage())
        );

        // 將自身註冊為 JDA 事件監聽器，以接收 SlashCommandEvent
        jda.addEventListener(this);
        plugin.getLogger().info("已註冊 Discord Interaction Listener");
    }

    /**
     * 處理 Discord Slash Command 事件。
     * 當使用者在 Discord 輸入 /stats 時觸發。
     */
    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        if (!event.getName().equals("stats")) return;

        String discordUserId = event.getUser().getId();

        // 透過 DiscordSRV 的帳號連結管理器查詢 Minecraft UUID
        UUID mcUuid = DiscordSRV.getPlugin().getAccountLinkManager().getUuid(discordUserId);

        if (mcUuid == null) {
            event.reply("❌ **你的 Discord 帳號尚未連結 Minecraft 帳號。**\n"
                            + "請使用 DiscordSRV 的連結功能進行綁定後再試一次。")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(mcUuid);

        if (!player.hasPlayedBefore() && !player.isOnline()) {
            event.reply("❌ 找不到此玩家的遊戲紀錄，可能尚未進入過伺服器。")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // 取得格式化的統計數據
        Map<String, String> stats = statsManager.getFormattedStats(player);
        String playerName = player.getName() != null ? player.getName() : "未知玩家";

        // 建構 Discord Embed
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("📊 " + playerName + " 的遊戲統計")
                .setColor(Color.decode("#55FF55"))
                .addField("💀 死亡次數", stats.get("deaths"), true)
                .addField("⚔ 玩家擊殺", stats.get("playerKills"), true)
                .addField("🗡 怪物擊殺", stats.get("mobKills"), true)
                .addField("🕐 遊戲時數", stats.get("playTime"), true)
                .addField("❤ 承受傷害", stats.get("damageTaken"), true)
                .addField("💥 造成傷害", stats.get("damageDealt"), true)
                .addField("🚶 移動距離", stats.get("distanceTraveled"), true)
                .setFooter("PaperBridge Stats · 僅你可見", null)
                .setTimestamp(java.time.Instant.now());

        // 以 Ephemeral 訊息回覆 (僅輸入指令的使用者可見)
        event.replyEmbeds(embed.build())
                .setEphemeral(true)
                .queue();
    }

    /**
     * 清理：移除 JDA 事件監聽器。
     * 於插件停用時由 PaperBridge.onDisable() 呼叫。
     */
    public void shutdown() {
        JDA jda = DiscordSRV.getPlugin().getJda();
        if (jda != null) {
            jda.removeEventListener(this);
        }
    }
}
