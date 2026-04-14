# PaperBridge 維護筆記

> 這份文件是給維護者與後續開發者的內部筆記，不是使用者手冊。  
> 對外說明請看根目錄的 `README.md`。

## 版本與環境

- 目前版本：`1.1.3`
- 平台：Paper `1.21.11+`
- Java：`21`
- 可選整合：DiscordSRV、PlaceholderAPI

## 目前目錄結構

```text
com.yokito.paperbridge
├── bootstrap/
│   ├── DiscordIntegrationRegistrar.java
│   ├── MinecraftCommandRegistrar.java
│   ├── PaperBridgeBootstrap.java
│   ├── PaperBridgePlugin.java
│   ├── PaperBridgeRuntime.java
│   ├── PlaceholderRegistrar.java
│   └── PluginText.java
├── command/
│   ├── discord/
│   │   ├── DiscordCommandRegistrar.java
│   │   ├── DiscordLeaderboardCommandHandler.java
│   │   ├── DiscordOnlineCommandHandler.java
│   │   ├── DiscordSlashCommand.java
│   │   ├── DiscordSlashCommandRegistry.java
│   │   └── DiscordStatsCommandHandler.java
│   └── minecraft/
│       └── DiscordNickCommand.java
├── integration/
│   ├── discordsrv/
│   │   ├── DeathMessageProcessor.java
│   │   ├── DiscordGateway.java
│   │   ├── DiscordInteractionListener.java
│   │   └── DiscordSrvGateway.java
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
│   │   ├── DiscordLinkedPlayerResolver.java
│   │   └── DiscordText.java
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

## Composition Root 與啟動流程

目前專案已收斂成「純 composition root」形式：

1. `PaperBridgePlugin`
   - 只負責 Bukkit lifecycle
   - `onEnable()` 做 `saveDefaultConfig()`、`build()`、`runtime.start()`
   - `onDisable()` 只呼叫 `runtime.stop()`

2. `PaperBridgeBootstrap`
   - 專案唯一 object graph 建立入口
   - 所有 `new` 都集中在這裡
   - 負責組裝 service、gateway、command handler、registrar

3. `PaperBridgeRuntime`
   - 統一副作用入口
   - `start()` 依序：
     - 註冊 Minecraft commands
     - 註冊 Placeholder expansion
     - 訂閱 DiscordSRV listeners
   - `stop()`：
     - 解除 Discord 相關訂閱
     - 關閉 JDA listener

## Discord 指令架構

- `DiscordInteractionListener`
  - 只負責事件入口與命令分派
- `DiscordSlashCommandRegistry`
  - 管理 slash command 名稱與 handler 對應
- `DiscordCommandRegistrar`
  - 發布 guild slash commands
  - 清除舊的 global `/stats`、`/leaderboard`、`/online`
- `DiscordStatsCommandHandler`
  - 查詢單一玩家統計
- `DiscordLeaderboardCommandHandler`
  - 查詢排行榜
  - `leaderboard` 會先 `deferReply(false)`，再回填結果，避免 `10062 Unknown interaction`
- `DiscordOnlineCommandHandler`
  - 查詢目前在線玩家數

## Discord 註冊策略

- slash commands 使用 guild command，而非 global command
- 在 `DiscordReadyEvent` 後延遲註冊
  - 目前延遲值：`40 ticks`
  - 目的是避開 DiscordSRV 啟動後的 guild command 清理流程
- 註冊前會清除舊版 global commands，避免同名重複顯示

## 目前對外功能

### Minecraft 指令

- `/setDiscordNick <nickname>`
- `/getDiscordNick [player]`

### Discord Slash Commands

- `/stats member:<optional>`
- `/leaderboard category:<deaths|playtime|kills|mined>`
- `/online`

### PlaceholderAPI

- `%paperbridge_discord_nick%`

## Stats 與排行榜說明

- `PlayerStatsService`
  - 單一玩家統計查詢
- `LeaderboardService`
  - 排行榜排序與 top N 擷取
- `StatsFormatter`
  - 統一格式化時數、傷害、距離與排行榜顯示值
- `LeaderboardCategory`
  - 目前支援：
    - `deaths`
    - `playtime`
    - `kills`
    - `mined`

### mined 為何較慢

`mined` 目前是所有可用 `Statistic.MINE_BLOCK` 的方塊統計總和，因此會遍歷大量 `Material`。  
這個分類比讀單一統計值的 `deaths` / `playtime` / `kills` 慢很多，是目前已知的效能熱點。

## 暱稱資料流

- `NicknameRepository`
  - 負責 `config.yml` 中 `nicks.<uuid>` 的讀寫
- `NicknameService`
  - 統一提供驗證、儲存、查詢、顯示格式
- Minecraft command 與 Placeholder expansion 共用同一套 nickname service

## 目前測試

目前已有 7 個測試類別：

- `EncodingSmokeTest`
- `DiscordSlashCommandRegistryTest`
- `LeaderboardCategoryTest`
- `DiscordLinkedPlayerResolverTest`
- `NicknameServiceTest`
- `LeaderboardServiceTest`
- `StatsFormatterTest`

目前 Maven 測試命令：

```bash
mvn test
```

## 文件同步注意事項

每次修改以下內容時，應同步更新 `README.md` 或本文件：

- 指令名稱或參數
- `plugin.yml` 版本與主類別
- package 結構
- Discord command 註冊策略
- Placeholder 名稱
