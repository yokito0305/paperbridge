package com.yokito.paperbridge.service.nickname;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.UUID;

/**
 * 封裝 Discord 暱稱在 Bukkit 設定檔中的讀寫細節。
 *
 * <p>此 repository 只處理資料儲存位置與存檔副作用，不承擔暱稱驗證或顯示規則。</p>
 */
public class NicknameRepository {

    private static final String DEFAULT_NICKNAME = "尚未設定";
    private final FileConfiguration config;
    private final Runnable saveAction;

    /**
     * 建立以 `FileConfiguration` 為後端的 repository。
     */
    public NicknameRepository(FileConfiguration config, Runnable saveAction) {
        this.config = config;
        this.saveAction = saveAction;
    }

    /**
     * 將指定玩家的暱稱寫入設定檔並立即保存。
     */
    public void save(UUID playerId, String nickname) {
        config.set(getPath(playerId), nickname);
        saveAction.run();
    }

    /**
     * 讀取玩家暱稱，若不存在則回傳預設顯示值。
     */
    public String find(UUID playerId) {
        return config.getString(getPath(playerId), DEFAULT_NICKNAME);
    }

    /**
     * 直接讀取原始暱稱值，不提供預設回退。
     */
    public String findRaw(UUID playerId) {
        return config.getString(getPath(playerId));
    }

    /**
     * 計算玩家暱稱在設定檔中的儲存路徑。
     */
    private String getPath(UUID playerId) {
        return "nicks." + playerId;
    }
}
