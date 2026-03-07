package com.yokito.paperbridge.command.discord;

import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.SlashCommandEvent;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.build.CommandData;

public interface DiscordSlashCommand {

    String name();

    CommandData definition();

    void handle(SlashCommandEvent event);
}
