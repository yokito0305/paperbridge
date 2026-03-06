package com.yokito.paperbridge.service.stats;

import com.yokito.paperbridge.model.stats.LeaderboardCategory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StatsFormatterTest {

    private final StatsFormatter statsFormatter = new StatsFormatter();

    @Test
    void shouldFormatPlayTimeForLeaderboard() {
        assertEquals("1 小時 0 分鐘", statsFormatter.formatLeaderboardValue(LeaderboardCategory.PLAYTIME, 72_000));
    }

    @Test
    void shouldFormatDamageInHearts() {
        assertEquals("12.0 ❤", statsFormatter.formatDamage(120));
    }

    @Test
    void shouldFormatDistanceInKilometersWhenNeeded() {
        assertEquals("1.50 km", statsFormatter.formatDistance(150_000));
    }
}
