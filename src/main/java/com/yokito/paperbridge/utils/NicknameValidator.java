package com.yokito.paperbridge.utils;

/**
 * Discord 暱稱驗證器。
 * 提供暱稱格式與長度的驗證邏輯。
 */
public class NicknameValidator {

    private NicknameValidator() {
        // 工具類不允許實例化
    }

    /**
     * 驗證 Discord 暱稱是否符合長度規範 (1~10 字元)。
     *
     * @param nickname 要驗證的暱稱
     * @return true 表示合法，false 表示不合法
     */
    public static boolean isValid(String nickname) {
        if (nickname == null) {
            return false;
        }
        return nickname.length() >= 1 && nickname.length() <= 10;
    }
}
