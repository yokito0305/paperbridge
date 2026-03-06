package com.yokito.paperbridge.model.stats;

public record PlayerStatsView(
        String deaths,
        String playerKills,
        String mobKills,
        String playTime,
        String damageTaken,
        String damageDealt,
        String distanceTraveled
) {
}
