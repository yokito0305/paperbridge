package com.yokito.paperbridge.command.discord;

import com.yokito.paperbridge.model.stats.LeaderboardCategory;
import com.yokito.paperbridge.model.stats.LeaderboardEntry;
import com.yokito.paperbridge.service.discord.DiscordEmbedFactory;
import com.yokito.paperbridge.service.discord.DiscordLinkedPlayerResolver;
import com.yokito.paperbridge.service.discord.DiscordText;
import com.yokito.paperbridge.service.stats.LeaderboardService;
import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.SlashCommandEvent;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.OptionMapping;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.OptionType;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.build.CommandData;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.build.OptionData;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Discord `/leaderboard` slash command 的命令物件。
 *
 * <p>此類別同時提供指令 definition 與執行流程，讓註冊與 runtime dispatch 共用同一份命令描述。</p>
 */
public class DiscordLeaderboardCommandHandler implements DiscordSlashCommand {

    private static final int LEADERBOARD_LIMIT = 5;

    private final DiscordLinkedPlayerResolver linkedPlayerResolver;
    private final LeaderboardService leaderboardService;
    private final DiscordEmbedFactory embedFactory;

    /**
     * 建立排行榜指令處理器。
     */
    public DiscordLeaderboardCommandHandler(
            DiscordLinkedPlayerResolver linkedPlayerResolver,
            LeaderboardService leaderboardService,
            DiscordEmbedFactory embedFactory
    ) {
        this.linkedPlayerResolver = linkedPlayerResolver;
        this.leaderboardService = leaderboardService;
        this.embedFactory = embedFactory;
    }

    /**
     * 回傳 slash command 的註冊名稱。
     */
    @Override
    @Nonnull
    public String name() {
        return "leaderboard";
    }

    /**
     * 建立 Discord 註冊用的 command definition，包含排行榜類別選項。
     */
    @Override
    public CommandData definition() {
        OptionData categoryOption = new OptionData(
                OptionType.STRING,
                "category",
                DiscordText.LEADERBOARD_CATEGORY_OPTION_DESCRIPTION,
                true
        );
        for (LeaderboardCategory category : LeaderboardCategory.values()) {
            categoryOption.addChoice(category.displayName(), category.optionValue());
        }

        return new CommandData(name(), DiscordText.LEADERBOARD_COMMAND_DESCRIPTION)
                .addOptions(categoryOption);
    }

    /**
     * 處理 Discord slash command 互動事件。
     *
     * <p>流程包含參數驗證、類別解析、讀取已綁定玩家的排行榜資料，最後再用 embed 輸出。</p>
     */
    @Override
    public void handle(SlashCommandEvent event) {
        OptionMapping categoryOption = event.getOption("category");
        if (categoryOption == null) {
            event.reply(DiscordText.CATEGORY_REQUIRED_MESSAGE)
                    .setEphemeral(true)
                    .queue();
            return;
        }

        LeaderboardCategory category;
        try {
            category = LeaderboardCategory.fromOption(categoryOption.getAsString());
        } catch (IllegalArgumentException exception) {
            event.reply(DiscordText.INVALID_CATEGORY_MESSAGE)
                    .setEphemeral(true)
                    .queue();
            return;
        }

        event.deferReply(false).queue(hook -> {
            List<LeaderboardEntry> leaderboard = leaderboardService.getLeaderboard(
                    category,
                    linkedPlayerResolver.getLinkedPlayerIds(),
                    LEADERBOARD_LIMIT
            );
            if (leaderboard.isEmpty()) {
                hook.editOriginal(DiscordText.NO_LEADERBOARD_DATA_MESSAGE).queue();
                return;
            }

            hook.editOriginalEmbeds(embedFactory.createLeaderboardEmbed(category, leaderboard, LEADERBOARD_LIMIT).build())
                    .queue();
        });
    }
}
