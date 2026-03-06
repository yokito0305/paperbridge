package com.yokito.paperbridge.integration.discordsrv;

import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DeathMessagePostProcessEvent;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.MessageBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.time.Instant;

public class DeathMessageProcessor {

    @Subscribe
    public void onDeathMessagePostProcess(DeathMessagePostProcessEvent event) {
        Player player = event.getPlayer();
        Location loc = player.getLocation();

        String dimension = getDimensionName(loc.getWorld());
        String coords = String.format("%s X: %d, Y: %d, Z: %d",
                dimension, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

        long unixTimestamp = Instant.now().getEpochSecond();
        String discordTime = String.format("<t:%d:F> (<t:%d:R>)", unixTimestamp, unixTimestamp);

        Message originalMessage = event.getDiscordMessage();
        MessageBuilder builder = new MessageBuilder(originalMessage);

        if (!originalMessage.getEmbeds().isEmpty()) {
            MessageEmbed originalEmbed = originalMessage.getEmbeds().get(0);
            EmbedBuilder embedBuilder = new EmbedBuilder(originalEmbed);

            String oldDescription = originalEmbed.getDescription() != null ? originalEmbed.getDescription() : "";
            String newDescription = oldDescription
                    + "\n📍 死亡座標: " + coords
                    + "\n🕐 死亡時間: " + discordTime;
            embedBuilder.setDescription(newDescription.trim());
            builder.setEmbeds(embedBuilder.build());
        } else {
            builder.append("\n📍 死亡座標: ").append(coords)
                    .append("\n🕐 死亡時間: ").append(discordTime);
        }

        event.setDiscordMessage(builder.build());
    }

    private String getDimensionName(World world) {
        if (world == null) {
            return "Unknown";
        }
        return switch (world.getEnvironment()) {
            case NORMAL -> "Overworld";
            case NETHER -> "Nether";
            case THE_END -> "The End";
            default -> world.getName();
        };
    }
}
