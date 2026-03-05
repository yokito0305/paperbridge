package com.yokito.paperbridge.listeners.discordsrv;

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

        // 維度名稱
        String dimension = getDimensionName(loc.getWorld());

        // 死亡座標
        String coords = String.format("%s X: %d, Y: %d, Z: %d",
                dimension, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

        // Discord 格式的死亡時間
        long unixTimestamp = Instant.now().getEpochSecond();
        String discordTime = String.format("<t:%d:F> (<t:%d:R>)", unixTimestamp, unixTimestamp);

        // 取得原始 DiscordSRV 產生的訊息
        Message originalMsg = event.getDiscordMessage();
        
        // 使用原始訊息進行初始化，這樣可以保留原本的所有屬性
        MessageBuilder builder = new MessageBuilder(originalMsg);

        // 檢查原始訊息是否包含 Embed (DiscordSRV 預設死亡訊息通常是 Embed)
        if (!originalMsg.getEmbeds().isEmpty()) {
            MessageEmbed originalEmbed = originalMsg.getEmbeds().get(0);
            EmbedBuilder embedBuilder = new EmbedBuilder(originalEmbed);

            // 將座標與時間附加到 Embed 的 Description 欄位中
            String oldDesc = originalEmbed.getDescription() != null ? originalEmbed.getDescription() : "";
            String newDesc = oldDesc + "\n📍 死亡座標: " + coords + "\n🕐 死亡時間: " + discordTime;
            embedBuilder.setDescription(newDesc.trim());

            // 將修改後的 Embed 覆蓋回 Builder 中
            // 備註：若您的 DiscordSRV 使用的 JDA 版本較新，此處可能需改用 setEmbeds(embedBuilder.build())
            builder.setEmbeds(embedBuilder.build());
        } else {
            // 如果伺服器端將死亡訊息改為純文字格式，則做為備用方案直接附加文字
            builder.append("\n📍 死亡座標: ").append(coords)
                   .append("\n🕐 死亡時間: ").append(discordTime);
        }

        event.setDiscordMessage(builder.build());
    }

    private String getDimensionName(World world) {
        if (world == null) return "Unknown";
        return switch (world.getEnvironment()) {
            case NORMAL -> "Overworld";
            case NETHER -> "Nether";
            case THE_END -> "The End";
            default -> world.getName();
        };
    }
}