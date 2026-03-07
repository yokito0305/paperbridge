package com.yokito.paperbridge.command.discord;

import com.yokito.paperbridge.service.discord.DiscordEmbedFactory;
import com.yokito.paperbridge.service.discord.DiscordText;
import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.SlashCommandEvent;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.build.CommandData;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;

/**
 * Discord `/online` slash command 的命令物件。
 *
 * <p>此命令直接讀取 Bukkit 目前線上人數，並交由 embed factory 組成 Discord 回覆。</p>
 */
public class DiscordOnlineCommandHandler implements DiscordSlashCommand {

    private final DiscordEmbedFactory embedFactory;

    /**
     * 建立線上人數查詢命令。
     */
    public DiscordOnlineCommandHandler(DiscordEmbedFactory embedFactory) {
        this.embedFactory = embedFactory;
    }

    /**
     * 回傳 slash command 的註冊名稱。
     */
    @Override
    @Nonnull
    public String name() {
        return "online";
    }

    /**
     * 建立 Discord 註冊用的 command definition。
     */
    @Override
    public CommandData definition() {
        return new CommandData(name(), DiscordText.ONLINE_COMMAND_DESCRIPTION);
    }

    /**
     * 在收到指令時回傳目前線上玩家數。
     */
    @Override
    public void handle(SlashCommandEvent event) {
        int onlineCount = Bukkit.getOnlinePlayers().size();
        event.replyEmbeds(embedFactory.createOnlineEmbed(onlineCount).build())
                .setEphemeral(false)
                .queue();
    }
}
