package com.yokito.paperbridge.service.stats;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StatsFormatterTest {

    private final StatsFormatter statsFormatter = new StatsFormatter();

    @Test
    void shouldFormatPlayTime() {
        assertEquals("1 小時 0 分", statsFormatter.formatPlayTime(72_000));
    }

    @Test
    void shouldFormatDamageInHearts() {
        assertEquals("12.0 顆心", statsFormatter.formatDamage(120));
    }

    @Test
    void shouldFormatDistanceInKilometersWhenNeeded() {
        assertEquals("1.50 km", statsFormatter.formatDistance(150_000));
    }
}
