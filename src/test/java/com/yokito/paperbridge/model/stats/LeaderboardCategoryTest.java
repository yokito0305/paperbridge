package com.yokito.paperbridge.model.stats;

import com.yokito.paperbridge.service.stats.StatsFormatter;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LeaderboardCategoryTest {

    @Test
    void shouldResolveOptionValue() {
        assertEquals(LeaderboardCategory.DEATHS, LeaderboardCategory.fromOption("deaths"));
        assertEquals(LeaderboardCategory.PLAYTIME, LeaderboardCategory.fromOption("playtime"));
        assertEquals(LeaderboardCategory.KILLS, LeaderboardCategory.fromOption("kills"));
        assertEquals(LeaderboardCategory.MINED, LeaderboardCategory.fromOption("mined"));
    }

    @Test
    void shouldExtractAndFormatEachCategory() {
        OfflinePlayer player = fakePlayer(
                UUID.randomUUID(),
                "Steve",
                Map.of(
                        Statistic.DEATHS, 7,
                        Statistic.PLAY_ONE_MINUTE, 72_000,
                        Statistic.PLAYER_KILLS, 5,
                        Statistic.MOB_KILLS, 9
                ),
                Map.of(Material.STONE, 12, Material.DIRT, 8)
        );
        StatsFormatter formatter = new StatsFormatter();

        assertEquals(7L, LeaderboardCategory.DEATHS.extractRawValue(player));
        assertEquals("7", LeaderboardCategory.DEATHS.formatDisplayValue(formatter, 7));

        assertEquals(72_000L, LeaderboardCategory.PLAYTIME.extractRawValue(player));
        assertEquals("1 小時 0 分", LeaderboardCategory.PLAYTIME.formatDisplayValue(formatter, 72_000));

        assertEquals(14L, LeaderboardCategory.KILLS.extractRawValue(player));
        assertEquals("14", LeaderboardCategory.KILLS.formatDisplayValue(formatter, 14));

        assertEquals(20L, LeaderboardCategory.MINED.extractRawValue(player));
        assertEquals("20", LeaderboardCategory.MINED.formatDisplayValue(formatter, 20));
    }

    private OfflinePlayer fakePlayer(
            UUID playerId,
            String playerName,
            Map<Statistic, Integer> statistics,
            Map<Material, Integer> minedBlocks
    ) {
        return (OfflinePlayer) Proxy.newProxyInstance(
                OfflinePlayer.class.getClassLoader(),
                new Class<?>[]{OfflinePlayer.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getUniqueId" -> playerId;
                    case "getName" -> playerName;
                    case "hasPlayedBefore" -> true;
                    case "isOnline" -> false;
                    case "getStatistic" -> {
                        if (args.length == 1) {
                            yield statistics.getOrDefault(args[0], 0);
                        }
                        if (args.length == 2 && args[0] == Statistic.MINE_BLOCK) {
                            yield minedBlocks.getOrDefault(args[1], 0);
                        }
                        yield 0;
                    }
                    case "hashCode" -> playerId.hashCode();
                    case "equals" -> proxy == args[0];
                    case "toString" -> "FakeOfflinePlayer[" + playerName + "]";
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }
}
