package com.yokito.paperbridge.commands;

import com.yokito.paperbridge.PaperBridge;
import com.yokito.paperbridge.manager.StatsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Minecraft 遊戲內 /stats 指令，讓玩家查看自己或他人的遊戲統計數據。
 */
public class PlayerStatsCommand implements CommandExecutor {

    private final PaperBridge plugin;
    private final StatsManager statsManager;

    public PlayerStatsCommand(PaperBridge plugin, StatsManager statsManager) {
        this.plugin = plugin;
        this.statsManager = statsManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("此指令只能由遊戲內玩家執行。"));
            return true;
        }

        OfflinePlayer target;
        if (args.length > 0) {
            // 查看指定玩家的統計
            target = Bukkit.getOfflinePlayer(args[0]);
            if (!target.hasPlayedBefore() && !target.isOnline()) {
                player.sendMessage(Component.text("找不到玩家: " + args[0]).color(NamedTextColor.RED));
                return true;
            }
        } else {
            target = player;
        }

        Map<String, String> stats = statsManager.getFormattedStats(target);

        String targetName = target.getName() != null ? target.getName() : "未知";

        // 組裝統計訊息
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("══════ ")
                .color(NamedTextColor.GOLD)
                .append(Component.text(targetName + " 的遊戲統計")
                        .color(NamedTextColor.YELLOW)
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" ══════").color(NamedTextColor.GOLD)));

        player.sendMessage(Component.text(" 💀 死亡次數: ").color(NamedTextColor.WHITE)
                .append(Component.text(stats.get("deaths")).color(NamedTextColor.RED)));
        player.sendMessage(Component.text(" ⚔ 玩家擊殺: ").color(NamedTextColor.WHITE)
                .append(Component.text(stats.get("playerKills")).color(NamedTextColor.GREEN)));
        player.sendMessage(Component.text(" 🗡 怪物擊殺: ").color(NamedTextColor.WHITE)
                .append(Component.text(stats.get("mobKills")).color(NamedTextColor.GREEN)));
        player.sendMessage(Component.text(" 🕐 遊戲時數: ").color(NamedTextColor.WHITE)
                .append(Component.text(stats.get("playTime")).color(NamedTextColor.AQUA)));
        player.sendMessage(Component.text(" ❤ 承受傷害: ").color(NamedTextColor.WHITE)
                .append(Component.text(stats.get("damageTaken")).color(NamedTextColor.GOLD)));
        player.sendMessage(Component.text(" 💥 造成傷害: ").color(NamedTextColor.WHITE)
                .append(Component.text(stats.get("damageDealt")).color(NamedTextColor.GOLD)));
        player.sendMessage(Component.text(" 🚶 移動距離: ").color(NamedTextColor.WHITE)
                .append(Component.text(stats.get("distanceTraveled")).color(NamedTextColor.LIGHT_PURPLE)));

        player.sendMessage(Component.text("══════════════════════════").color(NamedTextColor.GOLD));
        player.sendMessage(Component.empty());

        return true;
    }
}
