# PaperBridge 專案筆記

> **版本**: 1.1-BETA  
> **平台**: Paper 1.21.11+ / Java 21  
> **整合**: DiscordSRV、PlaceholderAPI

## 目錄結構

```text
com.yokito.paperbridge
├── bootstrap/
│   └── PaperBridgePlugin.java
├── command/
│   ├── discord/
│   │   ├── DiscordCommandRegistrar.java
│   │   ├── DiscordStatsCommandHandler.java
│   │   ├── DiscordLeaderboardCommandHandler.java
│   │   └── DiscordOnlineCommandHandler.java
│   └── minecraft/
│       └── DiscordNickCommand.java
├── integration/
│   ├── discordsrv/
│   │   ├── DeathMessageProcessor.java
│   │   └── DiscordInteractionListener.java
│   └── placeholderapi/
│       └── PaperBridgeExpansion.java
├── model/
│   └── stats/
│       ├── LeaderboardCategory.java
│       ├── LeaderboardEntry.java
│       └── PlayerStatsView.java
├── service/
│   ├── discord/
│   │   ├── DiscordEmbedFactory.java
│   │   └── DiscordLinkedPlayerResolver.java
│   ├── nickname/
│   │   ├── NicknameRepository.java
│   │   └── NicknameService.java
│   └── stats/
│       ├── LeaderboardService.java
│       ├── PlayerStatsService.java
│       └── StatsFormatter.java
└── util/
    ├── NicknameValidator.java
    └── TimeUtil.java
```

## 功能概覽

### Minecraft 指令

- `/setDiscordNick <nickname>`: 設定玩家的 Discord 暱稱
- `/getDiscordNick [player]`: 查看自己或在線玩家的 Discord 暱稱

### Discord Slash Commands

- `/stats member:<optional>`: 查詢自己或指定 Discord 成員的 Minecraft 統計
- `/leaderboard category:<deaths|playtime|kills|mined>`: 查詢前五名排行榜
- `/online`: 查詢目前 Minecraft 在線玩家數量

## 啟動流程

1. `PaperBridgePlugin` 建立 `NicknameService`、`PlayerStatsService`、`LeaderboardService`
2. 註冊 Minecraft 指令
3. 若有 PlaceholderAPI，註冊 `%paperbridge_discord_nick%`
4. 若有 DiscordSRV：
   - 訂閱 `DeathMessageProcessor`
   - 訂閱 `DiscordInteractionListener`
   - Discord Ready 後延遲註冊 guild slash commands
   - 同時清除舊的全域 `/stats`、`/leaderboard`、`/online`

## 結構說明

- `DiscordInteractionListener` 只負責事件入口與分派
- `DiscordCommandRegistrar` 負責 guild command 註冊與舊 global command 清理
- `Discord*CommandHandler` 各自負責單一 Discord 指令
- `DiscordLinkedPlayerResolver` 專責 Discord 帳號連結查詢
- `PlayerStatsService` / `LeaderboardService` 負責統計資料計算
- `StatsFormatter` 統一格式化時數、傷害、距離與排行榜數值
- `NicknameService` / `NicknameRepository` 統一處理 `config.yml` 的 nick 存取

## 目前行為

- Discord 指令目前回覆為公開訊息；錯誤訊息仍為 ephemeral
- 排行榜只統計已連結 Discord 且曾進過伺服器的玩家
- `mined` 代表所有可用 `MINE_BLOCK` 統計的方塊總和
