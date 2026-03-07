package com.yokito.paperbridge.command.discord;

import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.SlashCommandEvent;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.build.CommandData;

/**
 * 表示一個可被註冊與執行的 Discord slash command。
 *
 * <p>同一個命令物件需要同時回答三件事：名稱、註冊 definition、以及收到互動事件後的處理方式。</p>
 */
public interface DiscordSlashCommand {

    /**
     * 回傳 slash command 的唯一名稱。
     */
    String name();

    /**
     * 建立要送給 Discord 註冊的 command definition。
     */
    CommandData definition();

    /**
     * 處理實際收到的 slash command 互動事件。
     */
    void handle(SlashCommandEvent event);
}
