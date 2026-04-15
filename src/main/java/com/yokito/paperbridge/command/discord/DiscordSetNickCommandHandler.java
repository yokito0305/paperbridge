package com.yokito.paperbridge.command.discord;

import com.yokito.paperbridge.integration.discordsrv.DiscordGateway;
import com.yokito.paperbridge.service.discord.DiscordLinkedPlayerResolver;
import com.yokito.paperbridge.service.discord.DiscordText;
import com.yokito.paperbridge.service.nickname.NicknameService;
import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.SlashCommandEvent;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.OptionMapping;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.OptionType;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.build.CommandData;
import org.bukkit.OfflinePlayer;

import javax.annotation.Nonnull;

/**
 * Discord `/setnick` slash command — sets the caller's in-game nickname
 * and immediately syncs the Discord server nickname.
 *
 * <p>The command resolves the Discord user to their linked Minecraft account via
 * DiscordSRV, validates the requested nickname, persists it through
 * {@link NicknameService}, then triggers an async Discord server nickname update
 * via {@link DiscordGateway#syncMemberNickname}. All replies are ephemeral.</p>
 */
public class DiscordSetNickCommandHandler implements DiscordSlashCommand {

    static final String OPTION_NICKNAME = "nickname";

    private final DiscordLinkedPlayerResolver linkedPlayerResolver;
    private final NicknameService nicknameService;
    private final DiscordGateway discordGateway;

    public DiscordSetNickCommandHandler(
            DiscordLinkedPlayerResolver linkedPlayerResolver,
            NicknameService nicknameService,
            DiscordGateway discordGateway) {
        this.linkedPlayerResolver = linkedPlayerResolver;
        this.nicknameService = nicknameService;
        this.discordGateway = discordGateway;
    }

    @Override
    @Nonnull
    public String name() {
        return "setnick";
    }

    @Override
    public CommandData definition() {
        return new CommandData(name(), DiscordText.SET_NICK_COMMAND_DESCRIPTION)
                .addOption(OptionType.STRING, OPTION_NICKNAME, DiscordText.SET_NICK_OPTION_DESCRIPTION, true);
    }

    @Override
    public void handle(SlashCommandEvent event) {
        OfflinePlayer player = linkedPlayerResolver.resolveLinkedPlayer(event.getUser().getId());
        if (player == null) {
            event.reply(DiscordText.PLAYER_NOT_JOINED_MESSAGE)
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String nickname = resolveNickname(event);
        if (!nicknameService.isValidNickname(nickname)) {
            event.reply(DiscordText.SET_NICK_INVALID_MESSAGE)
                    .setEphemeral(true)
                    .queue();
            return;
        }

        nicknameService.setNickname(player.getUniqueId(), nickname);

        String playerName = player.getName() != null ? player.getName() : "";
        String displayNickname = nicknameService.getDisplayNickname(player.getUniqueId(), playerName);
        discordGateway.syncMemberNickname(event.getUser().getId(), displayNickname);

        event.reply(DiscordText.SET_NICK_SUCCESS_MESSAGE_PREFIX + nickname
                        + DiscordText.SET_NICK_DISCORD_SYNC_SUFFIX)
                .setEphemeral(true)
                .queue();
    }

    private String resolveNickname(SlashCommandEvent event) {
        OptionMapping option = event.getOption(OPTION_NICKNAME);
        return option != null ? option.getAsString() : "";
    }
}
