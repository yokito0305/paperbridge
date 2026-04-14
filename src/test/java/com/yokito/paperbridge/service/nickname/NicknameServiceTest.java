package com.yokito.paperbridge.service.nickname;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class NicknameServiceTest {

    // ── getNickname ────────────────────────────────────────────────────

    @Test
    void shouldPersistAndReadNickname() {
        NicknameService nicknameService = buildService();
        UUID playerId = UUID.randomUUID();

        nicknameService.setNickname(playerId, "Yokito");

        assertEquals("Yokito", nicknameService.getNickname(playerId));
    }

    @Test
    void shouldReturnDefaultWhenNicknameNotSet() {
        NicknameService nicknameService = buildService();
        UUID playerId = UUID.randomUUID();

        assertEquals("尚未設定", nicknameService.getNickname(playerId));
    }

    // ── getDisplayNickname ─────────────────────────────────────────────

    @Test
    void shouldFormatDisplayNickname() {
        NicknameService nicknameService = buildService();
        UUID playerId = UUID.randomUUID();

        nicknameService.setNickname(playerId, "Yokito");

        assertEquals("Yokito | Steve", nicknameService.getDisplayNickname(playerId, "Steve"));
    }

    @Test
    void shouldReturnPlayerNameWhenNicknameNotSet() {
        NicknameService nicknameService = buildService();
        UUID playerId = UUID.randomUUID();

        assertEquals("Steve", nicknameService.getDisplayNickname(playerId, "Steve"));
    }

    @Test
    void shouldReturnPlayerNameWhenNicknameEqualsPlayerName() {
        NicknameService nicknameService = buildService();
        UUID playerId = UUID.randomUUID();

        nicknameService.setNickname(playerId, "Steve");

        assertEquals("Steve", nicknameService.getDisplayNickname(playerId, "Steve"));
    }

    // ── isValidNickname ────────────────────────────────────────────────

    @Test
    void shouldAcceptNicknameWithinLengthBounds() {
        NicknameService nicknameService = buildService();

        assertTrue(nicknameService.isValidNickname("a"), "length 1 should be valid");
        assertTrue(nicknameService.isValidNickname("abc"), "length 3 should be valid");
        assertTrue(nicknameService.isValidNickname("1234567890"), "length 10 should be valid");
    }

    @Test
    void shouldRejectEmptyNickname() {
        NicknameService nicknameService = buildService();

        assertFalse(nicknameService.isValidNickname(""));
    }

    @Test
    void shouldRejectNullNickname() {
        NicknameService nicknameService = buildService();

        assertFalse(nicknameService.isValidNickname(null));
    }

    @Test
    void shouldRejectNicknameThatExceedsMaxLength() {
        NicknameService nicknameService = buildService();

        assertFalse(nicknameService.isValidNickname("12345678901")); // length 11
    }

    // ── saveAction ─────────────────────────────────────────────────────

    @Test
    void shouldInvokeSaveActionOnSetNickname() {
        AtomicInteger saveCount = new AtomicInteger();
        NicknameRepository repository = new NicknameRepository(
                new YamlConfiguration(), saveCount::incrementAndGet);
        NicknameService nicknameService = new NicknameService(repository);

        nicknameService.setNickname(UUID.randomUUID(), "Yokito");

        assertEquals(1, saveCount.get());
    }

    // ── helpers ────────────────────────────────────────────────────────

    private static NicknameService buildService() {
        NicknameRepository repository = new NicknameRepository(
                new YamlConfiguration(), () -> {
                });
        return new NicknameService(repository);
    }
}
