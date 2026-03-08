package com.yokito.paperbridge.bootstrap;

/**
 * 封裝 PaperBridge 啟停所需的副作用流程。
 *
 * <p>所有註冊與解除註冊都集中在這個 runtime 中，讓 plugin 主類別只需要委派 {@code start()} 和 {@code stop()} 方法。</p>
 */
public class PaperBridgeRuntime {

    private final MinecraftCommandRegistrar minecraftCommandRegistrar;
    private final PlaceholderRegistrar placeholderRegistrar;
    private final DiscordIntegrationRegistrar discordIntegrationRegistrar;

    public PaperBridgeRuntime(
            MinecraftCommandRegistrar minecraftCommandRegistrar,
            PlaceholderRegistrar placeholderRegistrar,
            DiscordIntegrationRegistrar discordIntegrationRegistrar
    ) {
        this.minecraftCommandRegistrar = minecraftCommandRegistrar;
        this.placeholderRegistrar = placeholderRegistrar;
        this.discordIntegrationRegistrar = discordIntegrationRegistrar;
    }

    public void start() {
        minecraftCommandRegistrar.register();
        placeholderRegistrar.register();
        discordIntegrationRegistrar.register();
    }

    public void stop() {
        discordIntegrationRegistrar.unregister();
    }
}
