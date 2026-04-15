package com.yokito.paperbridge.service.discord;

/**
 * 集中管理 Discord 指令、embed 與 log 相關固定文字。
 *
 * <p>此類別屬於呈現層資源庫，讓 command、listener 與 factory 不需要各自散落重複字串。</p>
 */
public final class DiscordText {

    public static final String STATS_COMMAND_DESCRIPTION = "查詢 Discord 綁定玩家的 Minecraft 統計資料";
    public static final String STATS_MEMBER_OPTION_DESCRIPTION = "指定要查詢的 Discord 成員";
    public static final String LEADERBOARD_COMMAND_DESCRIPTION = "查詢 Minecraft 玩家排行榜";
    public static final String LEADERBOARD_CATEGORY_OPTION_DESCRIPTION = "選擇要查詢的排行榜類別";
    public static final String SET_NICK_COMMAND_DESCRIPTION = "設定你的 Minecraft 暱稱";
    public static final String SET_NICK_OPTION_DESCRIPTION = "要設定的暱稱 (1 - 10 個字元)";
    public static final String SET_NICK_INVALID_MESSAGE = "暱稱長度必須在 1 到 10 個字元之間。";
    public static final String SET_NICK_SUCCESS_MESSAGE_PREFIX = "你的 Minecraft 暱稱已設定為：";
    public static final String SET_NICK_DISCORD_SYNC_SUFFIX = "，Discord 暱稱將同步更新。";
    public static final String ONLINE_COMMAND_DESCRIPTION = "查詢目前 Minecraft 在線玩家數量";

    public static final String PLAYER_NOT_JOINED_MESSAGE = "此 Discord 使用者尚未綁定，或從未加入過伺服器。";
    public static final String CATEGORY_REQUIRED_MESSAGE = "請提供要查詢的排行榜類別。";
    public static final String INVALID_CATEGORY_MESSAGE = "無效的排行榜類別。";
    public static final String NO_LEADERBOARD_DATA_MESSAGE = "目前沒有可顯示的排行榜資料。";

    public static final String STATS_TITLE_SUFFIX = " 的 Minecraft 統計";
    public static final String DEATHS_FIELD = "死亡次數";
    public static final String PLAYER_KILLS_FIELD = "玩家擊殺";
    public static final String MOB_KILLS_FIELD = "生物擊殺";
    public static final String PLAY_TIME_FIELD = "遊玩時間";
    public static final String DAMAGE_TAKEN_FIELD = "承受傷害";
    public static final String DAMAGE_DEALT_FIELD = "造成傷害";
    public static final String DISTANCE_TRAVELED_FIELD = "移動距離";
    public static final String STATS_FOOTER = "PaperBridge Stats";
    public static final String LEADERBOARD_FOOTER = "PaperBridge Leaderboard";
    public static final String ONLINE_TITLE = ":green_circle: Minecraft 在線玩家";
    public static final String ONLINE_DESCRIPTION_PREFIX = "目前共有 **";
    public static final String ONLINE_DESCRIPTION_SUFFIX = "** 位玩家在線。";
    public static final String ONLINE_FOOTER = "PaperBridge Online";

    public static final String DEATH_LOCATION_LABEL = "死亡座標";
    public static final String DEATH_TIME_LABEL = "死亡時間";

    public static final String PLACEHOLDER_ENABLED_LOG = "已啟用 PlaceholderAPI 擴充。";
    public static final String DISCORD_DEATH_PROCESSOR_ENABLED_LOG = "已啟用 DiscordSRV 死亡訊息處理器。";
    public static final String DISCORD_INTERACTION_ENABLED_LOG = "已啟用 DiscordSRV 互動監聽器。";
    public static final String DISCORD_JDA_LISTENER_ATTACHED_LOG = "已掛載 Discord Slash Command 監聽器。";
    public static final String LEGACY_COMMAND_DELETE_FAILURE_LOG = "刪除舊版 Slash Command 失敗: ";
    public static final String GLOBAL_COMMAND_DELETE_SUCCESS_LOG = "已刪除全域 Slash Command: /";
    public static final String GLOBAL_COMMAND_DELETE_FAILURE_LOG = "刪除 Slash Command 失敗 (/";
    public static final String GUILD_COMMAND_REGISTER_SUCCESS_LOG = "已成功註冊 Guild Slash Command: /";
    public static final String GUILD_COMMAND_REGISTER_FAILURE_LOG = "註冊 Guild Slash Command 失敗 (Guild: ";
    public static final String DISCORD_COMMAND_SYNC_LOG = "已將 ";
    public static final String DISCORD_COMMAND_SYNC_LOG_SUFFIX = " 個 Slash Commands 注入至 Guild: ";

    /**
     * 工具類別不允許被實例化。
     */
    private DiscordText() {
    }
}
