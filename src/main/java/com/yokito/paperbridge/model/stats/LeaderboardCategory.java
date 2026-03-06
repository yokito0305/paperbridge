package com.yokito.paperbridge.model.stats;

import javax.annotation.Nonnull;

public enum LeaderboardCategory {
    DEATHS("deaths", "死亡排名", "死亡次數"),
    PLAYTIME("playtime", "遊戲時數排名", "遊戲時數"),
    KILLS("kills", "擊殺排名", "總擊殺"),
    MINED("mined", "挖掘數量排名", "挖掘方塊數");

    private final String optionValue;
    private final String displayName;
    private final String metricLabel;

    LeaderboardCategory(String optionValue, String displayName, String metricLabel) {
        this.optionValue = optionValue;
        this.displayName = displayName;
        this.metricLabel = metricLabel;
    }

    @Nonnull
    public String optionValue() {
        return optionValue;
    }

    @Nonnull
    public String displayName() {
        return displayName;
    }

    @Nonnull
    public String metricLabel() {
        return metricLabel;
    }

    @Nonnull
    public static LeaderboardCategory fromOption(String optionValue) {
        for (LeaderboardCategory category : values()) {
            if (category.optionValue.equalsIgnoreCase(optionValue)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown leaderboard category: " + optionValue);
    }
}
