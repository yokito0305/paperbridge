package com.yokito.paperbridge.model.stats;

import java.util.UUID;

public record LeaderboardEntry(UUID playerUuid, String playerName, long rawValue, String displayValue) {
}
