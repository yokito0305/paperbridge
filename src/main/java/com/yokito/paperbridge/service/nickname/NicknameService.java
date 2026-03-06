package com.yokito.paperbridge.service.nickname;

import com.yokito.paperbridge.util.NicknameValidator;

import java.util.UUID;

public class NicknameService {

    private final NicknameRepository nicknameRepository;

    public NicknameService(NicknameRepository nicknameRepository) {
        this.nicknameRepository = nicknameRepository;
    }

    public boolean isValidNickname(String nickname) {
        return NicknameValidator.isValid(nickname);
    }

    public void setNickname(UUID playerId, String nickname) {
        nicknameRepository.save(playerId, nickname);
    }

    public String getNickname(UUID playerId) {
        return nicknameRepository.find(playerId);
    }

    public String getDisplayNickname(UUID playerId, String playerName) {
        String nick = nicknameRepository.findRaw(playerId);
        if (nick == null || nick.equals(playerName)) {
            return playerName;
        }
        return nick + " | " + playerName;
    }
}
