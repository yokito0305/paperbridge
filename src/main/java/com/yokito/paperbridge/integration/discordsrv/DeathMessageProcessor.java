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

/**
 * 攔截 DiscordSRV 發出的死亡訊息，補上座標與時間資訊。
 *
 * <p>此類別以 DiscordSRV 的事件訂閱機制運作，屬於 DiscordSRV observer callback，
 * 不直接參與 plugin 啟停流程判斷。</p>
 */
public class DeathMessageProcessor {

    /**
     * 在 DiscordSRV 完成死亡訊息後再補充額外欄位。
     *
     * <p>若原本訊息已有 embed，會更新 embed description；否則會直接附加文字內容。</p>
     */
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

    /**
     * 將 Bukkit world 環境轉成適合顯示在 Discord 的維度名稱。
     */
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
