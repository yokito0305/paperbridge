package com.yokito.paperbridge.listeners.discordsrv;

import com.yokito.paperbridge.PaperBridge;
import com.yokito.paperbridge.manager.StatsManager;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordReadyEvent;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.JDA;
import github.scarsz.discordsrv.dependencies.jda.api.entities.User;
import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.SlashCommandEvent;
import github.scarsz.discordsrv.dependencies.jda.api.hooks.ListenerAdapter;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.OptionMapping;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.OptionType;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.build.CommandData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * 處理來自 Discord 的 Slash Command 互動。
 * <p>
 * 透過 DiscordSRV 取得 JDA 實例，註冊 /stats 指令，
 * 並在收到指令時查詢指定 Discord 成員綁定的遊戲統計數據，以私密 (Ephemeral) 訊息回覆。
 * </p>
 */
public class DiscordInteractionListener extends ListenerAdapter {

    private static final String PLAYER_NOT_JOINED_MESSAGE = "此玩家尚未加入伺服器";

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
        if (jda == null) {
            return;
        }

        jda.upsertCommand(
                new CommandData("stats", "查詢你或其他成員在 Minecraft 伺服器中的遊戲統計數據")
                        .addOption(OptionType.USER, "member", "要查詢的 Discord 成員", false)
        ).queue(
                cmd -> plugin.getLogger().info("已成功註冊 Discord Slash Command: /stats (ID: " + cmd.getId() + ")"),
                err -> plugin.getLogger().warning("註冊 Discord Slash Command 失敗: " + err.getMessage())
        );

        jda.addEventListener(this);
        plugin.getLogger().info("已註冊 Discord Interaction Listener");
    }

    /**
     * 處理 Discord Slash Command 事件。
     * 當使用者在 Discord 輸入 /stats 時觸發。
     */
    @Override
    public void onSlashCommand(@Nonnull SlashCommandEvent event) {
        if (!"stats".equals(event.getName())) {
            return;
        }

        User targetUser = resolveTargetUser(event);
        OfflinePlayer player = resolveLinkedPlayer(targetUser.getId());
        if (player == null) {
            replyPlayerNotJoined(event);
            return;
        }

        Map<String, String> stats = statsManager.getFormattedStats(player);
        String playerName = player.getName() != null ? player.getName() : targetUser.getName();

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
                .setFooter("PaperBridge Stats", null)
                .setTimestamp(Instant.now());

        event.replyEmbeds(embed.build())
                .queue();
    }

    private User resolveTargetUser(SlashCommandEvent event) {
        OptionMapping memberOption = event.getOption("member");
        if (memberOption == null) {
            return event.getUser();
        }

        User mentionedUser = memberOption.getAsUser();
        return mentionedUser != null ? mentionedUser : event.getUser();
    }

    private OfflinePlayer resolveLinkedPlayer(String discordUserId) {
        UUID mcUuid = DiscordSRV.getPlugin().getAccountLinkManager().getUuid(discordUserId);
        if (mcUuid == null) {
            return null;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(mcUuid);
        if (!player.hasPlayedBefore() && !player.isOnline()) {
            return null;
        }

        return player;
    }

    private void replyPlayerNotJoined(SlashCommandEvent event) {
        event.reply("❌ " + PLAYER_NOT_JOINED_MESSAGE)
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
