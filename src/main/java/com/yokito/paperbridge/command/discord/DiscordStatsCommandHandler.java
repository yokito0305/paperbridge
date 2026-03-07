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

public class DiscordStatsCommandHandler implements DiscordSlashCommand {

    private final DiscordLinkedPlayerResolver linkedPlayerResolver;
    private final PlayerStatsService playerStatsService;
    private final DiscordEmbedFactory embedFactory;

    public DiscordStatsCommandHandler(
            DiscordLinkedPlayerResolver linkedPlayerResolver,
            PlayerStatsService playerStatsService,
            DiscordEmbedFactory embedFactory
    ) {
        this.linkedPlayerResolver = linkedPlayerResolver;
        this.playerStatsService = playerStatsService;
        this.embedFactory = embedFactory;
    }

    @Override
    @Nonnull
    public String name() {
        return "stats";
    }

    @Override
    public CommandData definition() {
        return new CommandData(name(), DiscordText.STATS_COMMAND_DESCRIPTION)
                .addOption(OptionType.USER, "member", DiscordText.STATS_MEMBER_OPTION_DESCRIPTION, false);
    }

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

    private User resolveTargetUser(SlashCommandEvent event) {
        OptionMapping memberOption = event.getOption("member");
        if (memberOption == null) {
            return event.getUser();
        }

        User mentionedUser = memberOption.getAsUser();
        return mentionedUser != null ? mentionedUser : event.getUser();
    }
}
