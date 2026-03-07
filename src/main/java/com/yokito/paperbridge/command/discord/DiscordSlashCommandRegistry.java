package com.yokito.paperbridge.command.discord;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DiscordSlashCommandRegistry {

    private final List<DiscordSlashCommand> commands;
    private final Map<String, DiscordSlashCommand> commandsByName;

    public DiscordSlashCommandRegistry(Collection<? extends DiscordSlashCommand> commands) {
        this.commands = List.copyOf(commands);
        this.commandsByName = this.commands.stream()
                .collect(Collectors.toUnmodifiableMap(DiscordSlashCommand::name, Function.identity()));
    }

    public List<DiscordSlashCommand> commands() {
        return commands;
    }

    public Optional<DiscordSlashCommand> find(String commandName) {
        return Optional.ofNullable(commandsByName.get(commandName));
    }
}
