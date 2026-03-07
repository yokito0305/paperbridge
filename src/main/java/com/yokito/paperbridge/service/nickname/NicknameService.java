package com.yokito.paperbridge.service.nickname;

import com.yokito.paperbridge.util.NicknameValidator;

import java.util.UUID;

/**
 * 提供 Discord 暱稱的驗證、儲存與顯示組合規則。
 *
 * <p>此服務是 nickname 功能的業務入口，負責決定資料如何對外呈現，而實際持久化由
 * {@link NicknameRepository} 處理。</p>
 */
public class NicknameService {

    private final NicknameRepository nicknameRepository;

    /**
     * 建立使用指定 repository 的暱稱服務。
     */
    public NicknameService(NicknameRepository nicknameRepository) {
        this.nicknameRepository = nicknameRepository;
    }

    /**
     * 驗證暱稱是否符合專案允許的格式。
     */
    public boolean isValidNickname(String nickname) {
        return NicknameValidator.isValid(nickname);
    }

    /**
     * 儲存玩家暱稱。
     */
    public void setNickname(UUID playerId, String nickname) {
        nicknameRepository.save(playerId, nickname);
    }

    /**
     * 讀取玩家暱稱；若沒有設定則回傳 repository 定義的預設值。
     */
    public String getNickname(UUID playerId) {
        return nicknameRepository.find(playerId);
    }

    /**
     * 產生適合顯示在 Placeholder 或 UI 上的暱稱文字。
     *
     * <p>若玩家沒有自訂暱稱，或暱稱與原名相同，則直接回傳玩家名稱。</p>
     */
    public String getDisplayNickname(UUID playerId, String playerName) {
        String nick = nicknameRepository.findRaw(playerId);
        if (nick == null || nick.equals(playerName)) {
            return playerName;
        }
        return nick + " | " + playerName;
    }
}
