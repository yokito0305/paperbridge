package com.yokito.paperbridge.integration.discordsrv;

import com.yokito.paperbridge.bootstrap.PaperBridgePlugin;
import com.yokito.paperbridge.command.discord.DiscordSlashCommand;
import com.yokito.paperbridge.command.discord.DiscordSlashCommandRegistry;
import com.yokito.paperbridge.service.discord.DiscordText;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordReadyEvent;
import github.scarsz.discordsrv.api.events.GuildSlashCommandUpdateEvent;
import github.scarsz.discordsrv.dependencies.jda.api.JDA;
import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.SlashCommandEvent;
import github.scarsz.discordsrv.dependencies.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

/**
 * 連接 DiscordSRV readiness 事件與 JDA slash command 事件的橋接 listener。
 *
 * <p>此類別同時監聽 DiscordSRV 的 {@code DiscordReadyEvent} 與 JDA 的 slash command callback，
 * 讓命令路由都能集中在同一個整合邊界。</p>
 *
 * <p>Slash command 的<strong>定義注入</strong>改透過 {@code GuildSlashCommandUpdateEvent} 完成：
 * DiscordSRV 在每個 Guild 進行指令同步前會廣播該事件，我們直接將指令塞入
 * 其 command set 中，由 DiscordSRV 統一上傳，避免後續被其覆寫。</p>
 */
public class DiscordInteractionListener extends ListenerAdapter {

    private final PaperBridgePlugin plugin;
    private final DiscordGateway discordGateway;
    private final DiscordSlashCommandRegistry commandRegistry;

    /**
     * 建立 Discord 互動 listener。
     */
    public DiscordInteractionListener(
            PaperBridgePlugin plugin,
            DiscordGateway discordGateway,
            DiscordSlashCommandRegistry commandRegistry
    ) {
        this.plugin = plugin;
        this.discordGateway = discordGateway;
        this.commandRegistry = commandRegistry;
    }

    /**
     * 在 DiscordSRV 宣告 Discord 已就緒後掛上 JDA listener 以接收互動事件。
     */
    @Subscribe
    public void onDiscordReady(DiscordReadyEvent event) {
        JDA jda = discordGateway.getJda();
        if (jda == null) {
            return;
        }

        jda.addEventListener(this);
        plugin.getLogger().info(DiscordText.DISCORD_JDA_LISTENER_ATTACHED_LOG);
    }

    /**
     * 在 DiscordSRV 向 Discord 同步每個 Guild 的 Slash Commands 前，
     * 將本專案的指令定義注入至同步清單。
     *
     * <p>DiscordSRV 會在所有已連結的 Guild 各觸發一次此事件，因此不需額外過濾。</p>
     */
    @Subscribe
    public void onCommandSync(GuildSlashCommandUpdateEvent event) {
        for (DiscordSlashCommand command : commandRegistry.commands()) {
            event.getCommands().add(command.definition());
        }
        plugin.getLogger().info(
                DiscordText.DISCORD_COMMAND_SYNC_LOG
                        + commandRegistry.commands().size()
                        + DiscordText.DISCORD_COMMAND_SYNC_LOG_SUFFIX
                        + event.getGuild().getName());
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
