package com.yokito.paperbridge.command.minecraft;

import com.yokito.paperbridge.service.nickname.NicknameService;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DiscordNickCommand implements CommandExecutor {

    private final NicknameService nicknameService;

    public DiscordNickCommand(NicknameService nicknameService) {
        this.nicknameService = nicknameService;
    }

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
