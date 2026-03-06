package com.yokito.paperbridge.service.nickname;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.UUID;

public class NicknameRepository {

    private static final String DEFAULT_NICKNAME = "尚未設定";
    private final FileConfiguration config;
    private final Runnable saveAction;

    public NicknameRepository(FileConfiguration config, Runnable saveAction) {
        this.config = config;
        this.saveAction = saveAction;
    }

    public void save(UUID playerId, String nickname) {
        config.set(getPath(playerId), nickname);
        saveAction.run();
    }

    public String find(UUID playerId) {
        return config.getString(getPath(playerId), DEFAULT_NICKNAME);
    }

    public String findRaw(UUID playerId) {
        return config.getString(getPath(playerId));
    }

    private String getPath(UUID playerId) {
        return "nicks." + playerId;
    }
}
