package com.yokito.paperbridge.model.stats;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LeaderboardCategoryTest {

    @Test
    void shouldResolveOptionValue() {
        assertEquals(LeaderboardCategory.DEATHS, LeaderboardCategory.fromOption("deaths"));
        assertEquals(LeaderboardCategory.PLAYTIME, LeaderboardCategory.fromOption("playtime"));
        assertEquals(LeaderboardCategory.KILLS, LeaderboardCategory.fromOption("kills"));
        assertEquals(LeaderboardCategory.MINED, LeaderboardCategory.fromOption("mined"));
    }
}
