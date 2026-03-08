package com.yokito.paperbridge.command.minecraft;

import com.yokito.paperbridge.service.nickname.NicknameService;
import net.kyori.adventure.text.Component;

import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

/**
 * 處理 Minecraft 端的 Discord 暱稱查詢與設定指令。
 *
 * <p>
 * 此執行器同時掛在 `setDiscordNick` 與 `getDiscordNick`，再依實際指令名稱轉發到
 * 對應處理流程。
 * </p>
 */
public class DiscordNickCommand implements CommandExecutor, TabCompleter {

    private final NicknameService nicknameService;

    /**
     * 建立使用暱稱服務的 Bukkit 指令執行器。
     */
    public DiscordNickCommand(NicknameService nicknameService) {
        this.nicknameService = nicknameService;
    }

    /**
     * Bukkit 指令入口。
     *
     * <p>
     * 僅允許遊戲內玩家執行，並將兩個指令名稱分流到各自的私有處理方法。
     * </p>
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("此指令只能由遊戲內玩家執行。"));
            return true;
        }

        if (command.getName().equalsIgnoreCase("setDiscordNick")) {
            return handleSetNickname(player, args);
        }
        if (command.getName().equalsIgnoreCase("getDiscordNick")) {
            return handleGetNickname(player, args);
        }
        return false;
    }

    /**
     * Bukkit 指令補全入口。
     *
     * <p>
     * 當玩家輸入 `/getDiscordNick <tab>` 時，提供線上玩家名稱的補全建議。
     * </p>
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("setDiscordNick") && args.length == 1) {
            return Collections.singletonList("<請輸入你的自訂暱稱>");
        }
        if (command.getName().equalsIgnoreCase("getDiscordNick") && args.length == 1) {
            return null; // 讓 Bukkit 使用線上玩家名稱作為補全建議
        }
        return Collections.emptyList();
    }

    /**
     * 處理 `/setDiscordNick`，負責驗證輸入並儲存暱稱。
     */
    private boolean handleSetNickname(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(Component.text("用法: /setDiscordNick <暱稱>"));
            return false;
        }

        String nickname = args[0];
        if (!nicknameService.isValidNickname(nickname)) {
            player.sendMessage(Component.text("暱稱長度必須在 1 到 10 個字元之間。"));
            return true;
        }

        nicknameService.setNickname(player.getUniqueId(), nickname);
        player.sendMessage(Component.text("您的 Discord 暱稱已設定為: " + nickname));
        return true;
    }

    /**
     * 處理 `/getDiscordNick`，可查自己或線上玩家的暱稱。
     */
    private boolean handleGetNickname(Player player, String[] args) {
        Player target = player;
        if (args.length > 0) {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(Component.text("找不到玩家: " + args[0]));
                return true;
            }
        }

        String currentNick = nicknameService.getNickname(target.getUniqueId());
        player.sendMessage(Component.text("目前的 Discord 暱稱為: " + currentNick));
        return true;
    }
}
