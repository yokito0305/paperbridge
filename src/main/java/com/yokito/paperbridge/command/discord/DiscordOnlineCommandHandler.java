package com.yokito.paperbridge.command.discord;

import com.yokito.paperbridge.service.discord.DiscordEmbedFactory;
import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.SlashCommandEvent;
import org.bukkit.Bukkit;

public class DiscordOnlineCommandHandler {

    private final DiscordEmbedFactory embedFactory;

    public DiscordOnlineCommandHandler(DiscordEmbedFactory embedFactory) {
        this.embedFactory = embedFactory;
    }

    public void handle(SlashCommandEvent event) {
        int onlineCount = Bukkit.getOnlinePlayers().size();
        event.replyEmbeds(embedFactory.createOnlineEmbed(onlineCount).build())
                .setEphemeral(false)
                .queue();
    }
}
