package com.yokito.paperbridge.integration.discordsrv;

import com.yokito.paperbridge.bootstrap.PaperBridgePlugin;
import com.yokito.paperbridge.command.discord.DiscordCommandRegistrar;
import com.yokito.paperbridge.command.discord.DiscordSlashCommandRegistry;
import com.yokito.paperbridge.service.discord.DiscordText;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordReadyEvent;
import github.scarsz.discordsrv.dependencies.jda.api.JDA;
import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.SlashCommandEvent;
import github.scarsz.discordsrv.dependencies.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

/**
 * 連接 DiscordSRV readiness 事件與 JDA slash command 事件的橋接 listener。
 *
 * <p>此類別同時監聽 DiscordSRV 的 `DiscordReadyEvent` 與 JDA 的 slash command callback，
 * 讓 command 註冊與命令路由都能集中在同一個整合邊界。</p>
 */
public class DiscordInteractionListener extends ListenerAdapter {

    private final PaperBridgePlugin plugin;
    private final DiscordGateway discordGateway;
    private final DiscordCommandRegistrar commandRegistrar;
    private final DiscordSlashCommandRegistry commandRegistry;

    /**
     * 建立 Discord 互動 listener。
     */
    public DiscordInteractionListener(
            PaperBridgePlugin plugin,
            DiscordGateway discordGateway,
            DiscordCommandRegistrar commandRegistrar,
            DiscordSlashCommandRegistry commandRegistry
    ) {
        this.plugin = plugin;
        this.discordGateway = discordGateway;
        this.commandRegistrar = commandRegistrar;
        this.commandRegistry = commandRegistry;
    }

    /**
     * 在 DiscordSRV 宣告 Discord 已就緒後掛上 JDA listener，並延後註冊 slash commands。
     */
    @Subscribe
    public void onDiscordReady(DiscordReadyEvent event) {
        JDA jda = discordGateway.getJda();
        if (jda == null) {
            return;
        }

        jda.addEventListener(this);
        plugin.getLogger().info(DiscordText.DISCORD_JDA_LISTENER_ATTACHED_LOG);

        plugin.getServer().getScheduler().runTaskLater(
                plugin,
                () -> commandRegistrar.registerCommands(jda),
                DiscordCommandRegistrar.COMMAND_REGISTRATION_DELAY_TICKS
        );
    }

    /**
     * JDA slash command 入口，依命令名稱轉發到 registry 中對應的命令物件。
     */
    @Override
    public void onSlashCommand(@Nonnull SlashCommandEvent event) {
        commandRegistry.find(event.getName()).ifPresent(command -> command.handle(event));
    }

    /**
     * 在 plugin 停用時將自己從 JDA 移除，避免重載後 listener 重複註冊。
     */
    public void shutdown() {
        JDA jda = discordGateway.getJda();
        if (jda != null) {
            jda.removeEventListener(this);
        }
    }
}
