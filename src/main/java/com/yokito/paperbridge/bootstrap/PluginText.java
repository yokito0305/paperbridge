package com.yokito.paperbridge.bootstrap;

/**
 * Shared plugin log messages.
 */
public final class PluginText {

    public static final String COMMAND_NOT_DECLARED_LOG = "plugin.yml command not declared: ";
    public static final String CUSTOM_BOT_MODE_NOT_IMPLEMENTED_LOG =
            "discord.yml enables custom bot mode, but this version does not implement it yet. Discord features are disabled.";
    public static final String DISCORD_SRV_MODE_ENABLED_LOG =
            "DiscordSRV detected. DiscordSRV integration mode enabled.";
    public static final String DISCORD_INTEGRATION_SKIPPED_LOG =
            "Custom bot mode is disabled and DiscordSRV was not detected. Discord integration is skipped.";

    private PluginText() {
    }
}
