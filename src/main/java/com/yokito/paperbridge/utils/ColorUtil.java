package com.yokito.paperbridge.utils;

import net.kyori.adventure.text.format.TextColor;

/**
 * 顏色工具類。
 * 提供便捷的方法來建立 Adventure API 的 TextColor。
 */
public class ColorUtil {

    private ColorUtil() {
        // 工具類不允許實例化
    }

    /**
     * 從 HEX 字串建立 TextColor。
     *
     * @param hex HEX 色碼，例如 "#FF5555"
     * @return TextColor 物件，若格式錯誤則回傳 null
     */
    public static TextColor fromHex(String hex) {
        return TextColor.fromHexString(hex);
    }

    /**
     * 從 RGB 數值建立 TextColor。
     *
     * @param r 紅 (0-255)
     * @param g 綠 (0-255)
     * @param b 藍 (0-255)
     * @return TextColor 物件
     */
    public static TextColor fromRGB(int r, int g, int b) {
        return TextColor.color(r, g, b);
    }
}
