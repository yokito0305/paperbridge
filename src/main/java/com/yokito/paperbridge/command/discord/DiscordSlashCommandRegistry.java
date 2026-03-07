package com.yokito.paperbridge.command.discord;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 保存本專案可用的 Discord slash command 集合與查找表。
 *
 * <p>此類別屬於內部命令模型，不直接做任何 Discord API 呼叫；listener 與 registrar
 * 會共用這個 registry 當作命令來源。</p>
 */
public class DiscordSlashCommandRegistry {

    private final List<DiscordSlashCommand> commands;
    private final Map<String, DiscordSlashCommand> commandsByName;

    /**
     * 以不可變集合建立命令清單與名稱索引。
     */
    public DiscordSlashCommandRegistry(Collection<? extends DiscordSlashCommand> commands) {
        this.commands = List.copyOf(commands);
        this.commandsByName = this.commands.stream()
                .collect(Collectors.toUnmodifiableMap(DiscordSlashCommand::name, Function.identity()));
    }

    /**
     * 回傳目前管理的所有命令，供註冊流程遍歷使用。
     */
    public List<DiscordSlashCommand> commands() {
        return commands;
    }

    /**
     * 依 slash command 名稱查找對應命令物件。
     */
    public Optional<DiscordSlashCommand> find(String commandName) {
        return Optional.ofNullable(commandsByName.get(commandName));
    }
}
