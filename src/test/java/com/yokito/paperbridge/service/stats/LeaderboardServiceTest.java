package com.yokito.paperbridge.service.stats;

import com.yokito.paperbridge.model.stats.LeaderboardCategory;
import com.yokito.paperbridge.model.stats.LeaderboardEntry;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LeaderboardServiceTest {

    @Test
    void shouldSortByScoreThenNameAndLimitResults() {
        UUID alexId = UUID.randomUUID();
        UUID steveId = UUID.randomUUID();
        UUID samId = UUID.randomUUID();

        OfflinePlayer alex = fakePlayer(alexId, "Alex", true, false, Map.of(Statistic.DEATHS, 5), Map.of());
        OfflinePlayer steve = fakePlayer(steveId, "Steve", true, false, Map.of(Statistic.DEATHS, 9), Map.of());
        OfflinePlayer sam = fakePlayer(samId, "Sam", true, false, Map.of(Statistic.DEATHS, 9), Map.of());

        Function<UUID, OfflinePlayer> provider = playerId -> Map.of(
                alexId, alex,
                steveId, steve,
                samId, sam
        ).get(playerId);

        LeaderboardService service = new LeaderboardService(new StatsFormatter(), provider);

        List<LeaderboardEntry> leaderboard = service.getLeaderboard(
                LeaderboardCategory.DEATHS,
                List.of(alexId, steveId, samId),
                2
        );

        assertEquals(List.of("Sam", "Steve"), leaderboard.stream().map(LeaderboardEntry::playerName).toList());
        assertEquals(List.of(9L, 9L), leaderboard.stream().map(LeaderboardEntry::rawValue).toList());
    }

    @Test
    void shouldSkipPlayersWithoutHistoryAndFormatPlaytime() {
        UUID activeId = UUID.randomUUID();
        UUID inactiveId = UUID.randomUUID();

        OfflinePlayer active = fakePlayer(
                activeId,
                "Builder",
                true,
                false,
                Map.of(Statistic.PLAY_ONE_MINUTE, 144_000),
                Map.of(Material.STONE, 1)
        );
        OfflinePlayer inactive = fakePlayer(inactiveId, "Ghost", false, false, Map.of(), Map.of());

        LeaderboardService service = new LeaderboardService(
                new StatsFormatter(),
                playerId -> Map.of(activeId, active, inactiveId, inactive).get(playerId)
        );

        List<LeaderboardEntry> leaderboard = service.getLeaderboard(
                LeaderboardCategory.PLAYTIME,
                List.of(activeId, inactiveId),
                5
        );

        assertEquals(1, leaderboard.size());
        assertEquals("Builder", leaderboard.getFirst().playerName());
        assertEquals("2 小時 0 分", leaderboard.getFirst().displayValue());
    }

    private OfflinePlayer fakePlayer(
            UUID playerId,
            String playerName,
            boolean hasPlayedBefore,
            boolean isOnline,
            Map<Statistic, Integer> statistics,
            Map<Material, Integer> minedBlocks
    ) {
        return (OfflinePlayer) Proxy.newProxyInstance(
                OfflinePlayer.class.getClassLoader(),
                new Class<?>[]{OfflinePlayer.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getUniqueId" -> playerId;
                    case "getName" -> playerName;
                    case "hasPlayedBefore" -> hasPlayedBefore;
                    case "isOnline" -> isOnline;
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
