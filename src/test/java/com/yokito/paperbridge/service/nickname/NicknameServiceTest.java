package com.yokito.paperbridge.service.nickname;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NicknameServiceTest {

    @Test
    void shouldPersistAndReadNickname() {
        YamlConfiguration configuration = new YamlConfiguration();
        NicknameRepository repository = new NicknameRepository(configuration, () -> { });
        NicknameService nicknameService = new NicknameService(repository);
        UUID playerId = UUID.randomUUID();

        nicknameService.setNickname(playerId, "Yokito");

        assertEquals("Yokito", nicknameService.getNickname(playerId));
    }

    @Test
    void shouldFormatDisplayNickname() {
        YamlConfiguration configuration = new YamlConfiguration();
        NicknameRepository repository = new NicknameRepository(configuration, () -> { });
        NicknameService nicknameService = new NicknameService(repository);
        UUID playerId = UUID.randomUUID();

        nicknameService.setNickname(playerId, "Yokito");

        assertEquals("Yokito | Steve", nicknameService.getDisplayNickname(playerId, "Steve"));
    }

    @Test
    void shouldValidateNicknameLength() {
        YamlConfiguration configuration = new YamlConfiguration();
        NicknameRepository repository = new NicknameRepository(configuration, () -> { });
        NicknameService nicknameService = new NicknameService(repository);

        assertTrue(nicknameService.isValidNickname("abc"));
    }
}
