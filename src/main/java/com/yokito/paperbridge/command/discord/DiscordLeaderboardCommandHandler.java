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

public class DiscordLeaderboardCommandHandler implements DiscordSlashCommand {

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

    @Override
    @Nonnull
    public String name() {
        return "leaderboard";
    }

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
