package com.yokito.paperbridge.command.discord;

import com.yokito.paperbridge.model.stats.LeaderboardCategory;
import com.yokito.paperbridge.model.stats.LeaderboardEntry;
import com.yokito.paperbridge.service.discord.DiscordEmbedFactory;
import com.yokito.paperbridge.service.discord.DiscordLinkedPlayerResolver;
import com.yokito.paperbridge.service.stats.LeaderboardService;
import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.SlashCommandEvent;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.OptionMapping;

import java.util.List;

public class DiscordLeaderboardCommandHandler {

    private static final String NO_LEADERBOARD_DATA_MESSAGE = "目前沒有可顯示的排行榜資料";
    private static final int LEADERBOARD_LIMIT = 5;

    private final DiscordLinkedPlayerResolver linkedPlayerResolver;
    private final LeaderboardService leaderboardService;
    private final DiscordEmbedFactory embedFactory;

    public DiscordLeaderboardCommandHandler(
            DiscordLinkedPlayerResolver linkedPlayerResolver,
            LeaderboardService leaderboardService,
            DiscordEmbedFactory embedFactory
    ) {
        this.linkedPlayerResolver = linkedPlayerResolver;
        this.leaderboardService = leaderboardService;
        this.embedFactory = embedFactory;
    }

    public void handle(SlashCommandEvent event) {
        OptionMapping categoryOption = event.getOption("category");
        if (categoryOption == null) {
            event.reply("❌ 缺少排行榜類別參數")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        LeaderboardCategory category;
        try {
            category = LeaderboardCategory.fromOption(categoryOption.getAsString());
        } catch (IllegalArgumentException exception) {
            event.reply("❌ 不支援的排行榜類別")
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
                hook.editOriginal("❌ " + NO_LEADERBOARD_DATA_MESSAGE)
                        .queue();
                return;
            }

            hook.editOriginalEmbeds(embedFactory.createLeaderboardEmbed(category, leaderboard, LEADERBOARD_LIMIT).build())
                    .queue();
        });
    }
}
