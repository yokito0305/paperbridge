package com.yokito.paperbridge.command.discord;

import com.yokito.paperbridge.model.stats.PlayerStatsView;
import com.yokito.paperbridge.service.discord.DiscordEmbedFactory;
import com.yokito.paperbridge.service.discord.DiscordLinkedPlayerResolver;
import com.yokito.paperbridge.service.discord.DiscordText;
import com.yokito.paperbridge.service.stats.PlayerStatsService;
import github.scarsz.discordsrv.dependencies.jda.api.entities.User;
import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.SlashCommandEvent;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.OptionMapping;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.OptionType;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.build.CommandData;

import javax.annotation.Nonnull;

import org.bukkit.OfflinePlayer;

/**
 * Discord `/stats` slash command 的命令物件。
 *
 * <p>此命令會先把 Discord 使用者解析成綁定的 Minecraft 玩家，再交由統計服務與 embed factory
 * 產生回覆。</p>
 */
public class DiscordStatsCommandHandler implements DiscordSlashCommand {

    private final DiscordLinkedPlayerResolver linkedPlayerResolver;
    private final PlayerStatsService playerStatsService;
    private final DiscordEmbedFactory embedFactory;

    /**
     * 建立統計指令處理器。
     */
    public DiscordStatsCommandHandler(
            DiscordLinkedPlayerResolver linkedPlayerResolver,
            PlayerStatsService playerStatsService,
            DiscordEmbedFactory embedFactory
    ) {
        this.linkedPlayerResolver = linkedPlayerResolver;
        this.playerStatsService = playerStatsService;
        this.embedFactory = embedFactory;
    }

    /**
     * 回傳 slash command 的註冊名稱。
     */
    @Override
    @Nonnull
    public String name() {
        return "stats";
    }

    /**
     * 建立 Discord 註冊用的 command definition。
     *
     * <p>`member` 是可選參數；若省略則查詢發送指令的使用者。</p>
     */
    @Override
    public CommandData definition() {
        return new CommandData(name(), DiscordText.STATS_COMMAND_DESCRIPTION)
                .addOption(OptionType.USER, "member", DiscordText.STATS_MEMBER_OPTION_DESCRIPTION, false);
    }

    /**
     * 處理 Discord slash command 互動事件並回傳玩家統計 embed。
     */
    @Override
    public void handle(SlashCommandEvent event) {
        User targetUser = resolveTargetUser(event);
        OfflinePlayer player = linkedPlayerResolver.resolveLinkedPlayer(targetUser.getId());
        if (player == null) {
            event.reply(DiscordText.PLAYER_NOT_JOINED_MESSAGE)
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String playerName = player.getName() != null ? player.getName() : targetUser.getName();
        PlayerStatsView stats = playerStatsService.getPlayerStats(player);

        event.replyEmbeds(embedFactory.createStatsEmbed(playerName, stats).build())
                .setEphemeral(false)
                .queue();
    }

    /**
     * 決定本次要查詢哪個 Discord 使用者。
     *
     * <p>若沒有指定 `member`，預設使用發送指令的本人。</p>
     */
    private User resolveTargetUser(SlashCommandEvent event) {
        OptionMapping memberOption = event.getOption("member");
        if (memberOption == null) {
            return event.getUser();
        }

        User mentionedUser = memberOption.getAsUser();
        return mentionedUser != null ? mentionedUser : event.getUser();
    }
}
