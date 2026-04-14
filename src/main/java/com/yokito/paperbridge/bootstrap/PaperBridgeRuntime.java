package com.yokito.paperbridge.bootstrap;

/**
 * Owns plugin lifecycle components.
 */
public class PaperBridgeRuntime {

    private final RuntimeComponent minecraftCommandRegistrar;
    private final RuntimeComponent placeholderRegistrar;
    private final RuntimeComponent discordIntegrationRegistrar;

    public PaperBridgeRuntime(
            RuntimeComponent minecraftCommandRegistrar,
            RuntimeComponent placeholderRegistrar,
            RuntimeComponent discordIntegrationRegistrar
    ) {
        this.minecraftCommandRegistrar = minecraftCommandRegistrar;
        this.placeholderRegistrar = placeholderRegistrar;
        this.discordIntegrationRegistrar = discordIntegrationRegistrar;
    }

    public void start() {
        minecraftCommandRegistrar.start();
        placeholderRegistrar.start();
        discordIntegrationRegistrar.start();
    }

    public void stop() {
        discordIntegrationRegistrar.stop();
        placeholderRegistrar.stop();
        minecraftCommandRegistrar.stop();
    }
}
