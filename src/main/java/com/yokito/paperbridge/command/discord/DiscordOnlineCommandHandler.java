package com.yokito.paperbridge.command.discord;

import com.yokito.paperbridge.service.discord.DiscordEmbedFactory;
import com.yokito.paperbridge.service.discord.DiscordText;
import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.SlashCommandEvent;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.build.CommandData;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;

public class DiscordOnlineCommandHandler implements DiscordSlashCommand {

    private final DiscordEmbedFactory embedFactory;

    public DiscordOnlineCommandHandler(DiscordEmbedFactory embedFactory) {
        this.embedFactory = embedFactory;
    }

    @Override
    @Nonnull
    public String name() {
        return "online";
    }

    @Override
    public CommandData definition() {
        return new CommandData(name(), DiscordText.ONLINE_COMMAND_DESCRIPTION);
    }

    @Override
    public void handle(SlashCommandEvent event) {
        int onlineCount = Bukkit.getOnlinePlayers().size();
        event.replyEmbeds(embedFactory.createOnlineEmbed(onlineCount).build())
                .setEphemeral(false)
                .queue();
    }
}
