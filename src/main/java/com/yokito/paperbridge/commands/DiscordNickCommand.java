package com.yokito.paperbridge.commands;

import com.yokito.paperbridge.PaperBridge;
import com.yokito.paperbridge.utils.NicknameValidator;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;

public class DiscordNickCommand implements CommandExecutor {

    private final PaperBridge plugin;

    public DiscordNickCommand(PaperBridge plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("此指令只能由遊戲內玩家執行。"));
            return true;
        }

        String path = "nicks." + player.getUniqueId();

        if (command.getName().equalsIgnoreCase("setDiscordNick")) {
            if (args.length == 0) {
                player.sendMessage(Component.text("用法: /setDiscordNick <暱稱>"));
                return false;
            }

            String nickname = args[0];

            // 基礎的字串長度驗證 (將供後續 Unit Test 使用)
            if (!NicknameValidator.isValid(nickname)) {
                player.sendMessage(Component.text("暱稱長度必須在 1 到 10 個字元之間。"));
                return true;
            }

            // 儲存至 config.yml
            plugin.getConfig().set(path, nickname);
            plugin.saveConfig();
            player.sendMessage(Component.text("您的 Discord 暱稱已設定為: " + nickname));
            return true;
        }

        if (command.getName().equalsIgnoreCase("getDiscordNick")) {
            Player target = player;
            if (args.length > 0) {
                target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    player.sendMessage(Component.text("找不到玩家: " + args[0]));
                    return true;
                }
            }

            path = "nicks." + target.getUniqueId();
            String currentNick = plugin.getConfig().getString(path, "尚未設定");
            player.sendMessage(Component.text("目前的 Discord 暱稱為: " + currentNick));
            return true;
        }

        return false;
    }
}
