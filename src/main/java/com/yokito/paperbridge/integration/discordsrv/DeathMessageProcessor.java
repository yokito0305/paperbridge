package com.yokito.paperbridge.integration.discordsrv;

import com.yokito.paperbridge.service.discord.DiscordText;
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
                    + "\n" + DiscordText.DEATH_LOCATION_LABEL + ": " + coords
                    + "\n" + DiscordText.DEATH_TIME_LABEL + ": " + discordTime;
            embedBuilder.setDescription(newDescription.trim());
            builder.setEmbeds(embedBuilder.build());
        } else {
            builder.append("\n").append(DiscordText.DEATH_LOCATION_LABEL).append(": ").append(coords)
                    .append("\n").append(DiscordText.DEATH_TIME_LABEL).append(": ").append(discordTime);
        }

        event.setDiscordMessage(builder.build());
    }

    private String getDimensionName(World world) {
        if (world == null) {
            return "未知維度";
        }
        return switch (world.getEnvironment()) {
            case NORMAL -> "主世界";
            case NETHER -> "地獄";
            case THE_END -> "終界";
            default -> world.getName();
        };
    }
}
