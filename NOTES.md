# PaperBridge 專案筆記

> **版本**: 1.0-BETA  
> **作者**: yokito  
> **平台**: Paper 1.21.11+ (Java 21)  
> **相依插件**: DiscordSRV (選用)、PlaceholderAPI (選用)

---

## 目錄結構

```
com.yokito.paperbridge
│
├── PaperBridge.java                          // 插件主類，繼承 JavaPlugin
│
├── commands/                                 // 指令邏輯 (Minecraft & Discord)
│   ├── DiscordNickCommand.java               //   /setDiscordNick, /getDiscordNick
│   └── PlayerStatsCommand.java               //   /stats [player]
│
├── listeners/                                // 事件監聽器
│   ├── minecraft/                            //   (預留) 遊戲內事件
│   └── discordsrv/                           //   DiscordSRV 相關事件
│       ├── DeathMessageProcessor.java        //     死亡訊息加工
│       └── DiscordInteractionListener.java   //     Discord Slash Command 處理
│
├── integrations/                             // 第三方插件橋接
│   └── placeholderapi/
│       └── PaperBridgeExpansion.java         //   %paperbridge_discord_nick%
│
├── manager/                                  // 核心邏輯處理器 (Service Layer)
│   └── StatsManager.java                     //   統計數據抓取與格式化
│
└── utils/                                    // 工具類
    ├── ColorUtil.java                        //   HEX/RGB → TextColor
    ├── TimeUtil.java                         //   Tick → 時間字串
    └── NicknameValidator.java                //   暱稱長度驗證
```

---

## 設定檔

### `plugin.yml`

| 項目 | 值 |
|---|---|
| `name` | PaperBridge |
| `main` | `com.yokito.paperbridge.PaperBridge` |
| `api-version` | 1.21.11 |
| 軟依賴 | PlaceholderAPI, DiscordSRV |

**Minecraft 指令**

| 指令 | 用法 | 權限 | 說明 |
|---|---|---|---|
| `/setDiscordNick` | `/setDiscordNick <暱稱>` | `paperbridge.setnick` | 設定 Discord 暱稱 |
| `/getDiscordNick` | `/getDiscordNick [player]` | `paperbridge.getnick` | 查看 Discord 暱稱 |
| `/stats` | `/stats [player]` | `paperbridge.stats` | 查看遊戲統計數據 |

**權限**

| 權限節點 | 預設 | 說明 |
|---|---|---|
| `paperbridge.setnick` | `true` | 設定自己的暱稱 |
| `paperbridge.getnick.self` | `true` | 查看自己的暱稱 |
| `paperbridge.getnick.others` | `true` | 查看他人的暱稱 |
| `paperbridge.stats` | `true` | 查看遊戲統計 |

### `config.yml`

```yaml
# 儲存玩家的 Discord 暱稱對應 (UUID → 暱稱)
nicks: {}
```

---

## 各檔案詳細說明

---

### `PaperBridge.java`

> **套件**: `com.yokito.paperbridge`  
> **繼承**: `JavaPlugin`  
> **角色**: 插件主入口

#### 欄位

| 名稱 | 類型 | 說明 |
|---|---|---|
| `deathMessageProcessor` | `DeathMessageProcessor` | 死亡訊息處理器實例 |
| `discordInteractionListener` | `DiscordInteractionListener` | Discord Slash Command 監聽器 |
| `statsManager` | `StatsManager` | 統計數據管理器 |

#### 方法

| 方法 | 回傳 | 說明 |
|---|---|---|
| `onEnable()` | `void` | 插件啟動時執行。依序：儲存預設 config → 初始化 `StatsManager` → 註冊 Minecraft 指令 (`setDiscordNick`, `getDiscordNick`, `stats`) → 若有 PlaceholderAPI 則註冊擴展 → 若有 DiscordSRV 則訂閱 `DeathMessageProcessor` 與 `DiscordInteractionListener` |
| `onDisable()` | `void` | 插件停用時執行。取消訂閱 DiscordSRV 事件、呼叫 `discordInteractionListener.shutdown()` 移除 JDA 監聽器 |
| `getStatsManager()` | `StatsManager` | 取得統計數據管理器實例 |

#### 生命週期流程

```
onEnable()
  ├─ saveDefaultConfig()
  ├─ new StatsManager(this)
  ├─ 註冊 DiscordNickCommand → setDiscordNick, getDiscordNick
  ├─ 註冊 PlayerStatsCommand → stats
  ├─ [PlaceholderAPI?] → PaperBridgeExpansion.register()
  └─ [DiscordSRV?] → subscribe(DeathMessageProcessor)
                    → subscribe(DiscordInteractionListener)

onDisable()
  ├─ unsubscribe(DeathMessageProcessor)
  └─ DiscordInteractionListener.shutdown()
     └─ unsubscribe(DiscordInteractionListener)
```

---

### `commands/DiscordNickCommand.java`

> **套件**: `com.yokito.paperbridge.commands`  
> **實作**: `CommandExecutor`  
> **角色**: 處理 `/setDiscordNick` 與 `/getDiscordNick` 指令

#### 建構子

| 參數 | 類型 | 說明 |
|---|---|---|
| `plugin` | `PaperBridge` | 插件主實例，用於存取 config |

#### 方法

| 方法 | 回傳 | 說明 |
|---|---|---|
| `onCommand(sender, command, label, args)` | `boolean` | 指令處理入口，根據 command name 分派至 set 或 get 邏輯 |

#### 指令邏輯

**`/setDiscordNick <暱稱>`**
1. 僅限遊戲內玩家執行
2. 驗證參數數量
3. 呼叫 `NicknameValidator.isValid()` 驗證暱稱長度 (1~10 字元)
4. 寫入 `config.yml` 的 `nicks.<UUID>` 路徑

**`/getDiscordNick [player]`**
1. 僅限遊戲內玩家執行
2. 若有參數則查詢指定玩家，否則查詢自己
3. 從 `config.yml` 讀取暱稱，預設值為 `"尚未設定"`

---

### `commands/PlayerStatsCommand.java`

> **套件**: `com.yokito.paperbridge.commands`  
> **實作**: `CommandExecutor`  
> **角色**: 處理 Minecraft 遊戲內 `/stats` 指令

#### 建構子

| 參數 | 類型 | 說明 |
|---|---|---|
| `plugin` | `PaperBridge` | 插件主實例 |
| `statsManager` | `StatsManager` | 統計數據管理器 |

#### 方法

| 方法 | 回傳 | 說明 |
|---|---|---|
| `onCommand(sender, command, label, args)` | `boolean` | 指令處理入口 |

#### 指令邏輯

**`/stats [player]`**
1. 僅限遊戲內玩家執行
2. 若有參數則查詢指定玩家 (含離線)；否則查詢自己
3. 呼叫 `statsManager.getFormattedStats(target)` 取得統計 Map
4. 使用 Adventure API (`Component`) 組裝美化的聊天訊息，包含：
   - 💀 死亡次數 (紅色)
   - ⚔ 玩家擊殺 (綠色)
   - 🗡 怪物擊殺 (綠色)
   - 🕐 遊戲時數 (青色)
   - ❤ 承受傷害 (金色)
   - 💥 造成傷害 (金色)
   - 🚶 移動距離 (淺紫色)

---

### `listeners/discordsrv/DeathMessageProcessor.java`

> **套件**: `com.yokito.paperbridge.listeners.discordsrv`  
> **角色**: 攔截 DiscordSRV 的死亡訊息，附加座標與時間資訊

#### 方法

| 方法 | 註解 | 說明 |
|---|---|---|
| `onDeathMessagePostProcess(event)` | `@Subscribe` | 監聽 `DeathMessagePostProcessEvent`，在 DiscordSRV 產生死亡訊息後進行後處理 |
| `getDimensionName(world)` | — | 將 `World.Environment` 轉換為維度名稱字串 (`Overworld` / `Nether` / `The End`) |

#### 處理流程

```
DeathMessagePostProcessEvent
  ├─ 取得玩家死亡座標 (維度 + XYZ)
  ├─ 產生 Discord 時間戳記 (<t:unix:F> 格式)
  ├─ 判斷原始訊息是否為 Embed
  │   ├─ [是] → 在 Embed Description 附加座標與時間
  │   └─ [否] → 以純文字方式附加
  └─ event.setDiscordMessage(修改後的訊息)
```

#### 附加到 Discord 的資訊

- 📍 死亡座標: `{維度} X: {x}, Y: {y}, Z: {z}`
- 🕐 死亡時間: `<t:timestamp:F> (<t:timestamp:R>)` (Discord 原生時間格式)

---

### `listeners/discordsrv/DiscordInteractionListener.java`

> **套件**: `com.yokito.paperbridge.listeners.discordsrv`  
> **繼承**: `ListenerAdapter` (JDA)  
> **角色**: 在 Discord 端提供 `/stats` Slash Command，讓使用者查詢連結帳號的遊戲統計

#### 建構子

| 參數 | 類型 | 說明 |
|---|---|---|
| `plugin` | `PaperBridge` | 插件主實例 |
| `statsManager` | `StatsManager` | 統計數據管理器 |

#### 方法

| 方法 | 註解 | 說明 |
|---|---|---|
| `onDiscordReady(event)` | `@Subscribe` | 監聽 `DiscordReadyEvent`。當 DiscordSRV 與 Discord 連線就緒後：(1) 呼叫 `jda.upsertCommand()` 註冊 `/stats` Slash Command (2) 呼叫 `jda.addEventListener(this)` 註冊自身為 JDA 事件監聽器 |
| `onSlashCommand(event)` | `@Override` | 處理 Discord 使用者輸入 `/stats` 時的邏輯 |
| `shutdown()` | — | 從 JDA 移除自身事件監聽器，於插件停用時呼叫 |

#### `/stats` Slash Command 流程

```
使用者在 Discord 輸入 /stats
  ├─ 取得 Discord User ID
  ├─ 透過 DiscordSRV AccountLinkManager 查詢對應的 MC UUID
  │   └─ [null] → 回覆錯誤「尚未連結帳號」(Ephemeral)
  ├─ 透過 UUID 取得 OfflinePlayer
  │   └─ [未曾上線] → 回覆錯誤「找不到遊戲紀錄」(Ephemeral)
  ├─ 呼叫 statsManager.getFormattedStats(player)
  ├─ 建構 EmbedBuilder (綠色主題)
  │   ├─ Title: 📊 {玩家名} 的遊戲統計
  │   ├─ Fields: 死亡/擊殺/時數/傷害/距離
  │   └─ Footer: "PaperBridge Stats · 僅你可見"
  └─ event.replyEmbeds(embed).setEphemeral(true).queue()
         ↑ Ephemeral = 僅輸入指令的使用者可見
```

---

### `integrations/placeholderapi/PaperBridgeExpansion.java`

> **套件**: `com.yokito.paperbridge.integrations.placeholderapi`  
> **繼承**: `PlaceholderExpansion`  
> **角色**: 提供 PlaceholderAPI 變數 `%paperbridge_discord_nick%`

#### 方法

| 方法 | 回傳 | 說明 |
|---|---|---|
| `getIdentifier()` | `"paperbridge"` | PlaceholderAPI 辨識名稱 |
| `getAuthor()` | `"yokito"` | 作者名稱 |
| `getVersion()` | `"1.0-BETA"` | 擴展版本 |
| `persist()` | `true` | 確保 `/papi reload` 時不會被清除 |
| `canRegister()` | `true` | 允許註冊 |
| `onRequest(player, params)` | `String` | 處理變數請求 |

#### 支援的變數

| Placeholder | 說明 |
|---|---|
| `%paperbridge_discord_nick%` | 若玩家有設定暱稱且與 MC 名不同，回傳 `暱稱 \| MC名稱`；否則回傳 MC 名稱 |

---

### `manager/StatsManager.java`

> **套件**: `com.yokito.paperbridge.manager`  
> **角色**: 核心統計數據管理器，負責從 Bukkit Statistic API 抓取數據並格式化

#### 建構子

| 參數 | 類型 | 說明 |
|---|---|---|
| `plugin` | `PaperBridge` | 插件主實例 |

#### 方法

| 方法 | 回傳 | 可見性 | 說明 |
|---|---|---|---|
| `getFormattedStats(player)` | `Map<String, String>` | `public` | 回傳格式化的統計資料 Map |
| `formatDamage(rawDamage)` | `String` | `private` | 將原始傷害值 (1/10 ❤) 格式化，≥1000 顯示為 `Xk ❤` |
| `formatDistance(totalCm)` | `String` | `private` | 將公分距離格式化，≥1000m 顯示為 `X.XX km`，否則 `X.X m` |

#### `getFormattedStats()` 回傳的 Map Keys

| Key | 資料來源 (Bukkit Statistic) | 格式化 |
|---|---|---|
| `deaths` | `DEATHS` | 整數 |
| `playerKills` | `PLAYER_KILLS` | 整數 |
| `mobKills` | `MOB_KILLS` | 整數 |
| `playTime` | `PLAY_ONE_MINUTE` (tick) | `TimeUtil.ticksToTimeString()` → "X 天 Y 小時 Z 分鐘" |
| `damageTaken` | `DAMAGE_TAKEN` | `formatDamage()` → "X.X ❤" 或 "X.Xk ❤" |
| `damageDealt` | `DAMAGE_DEALT` | `formatDamage()` → "X.X ❤" 或 "X.Xk ❤" |
| `distanceTraveled` | `WALK + SPRINT + SWIM + FLY + BOAT + HORSE + MINECART + AVIATE` (cm) | `formatDistance()` → "X.X m" 或 "X.XX km" |

---

### `utils/TimeUtil.java`

> **套件**: `com.yokito.paperbridge.utils`  
> **角色**: 時間轉換工具類 (不可實例化)

#### 方法

| 方法 | 回傳 | 說明 |
|---|---|---|
| `ticksToTimeString(ticks)` | `String` | 將 tick 數轉為 `"X 天 Y 小時 Z 分鐘"` 格式。1 tick = 1/20 秒 |
| `secondsToTimeString(totalSeconds)` | `String` | 將秒數轉為時間字串 (內部呼叫 `ticksToTimeString(seconds * 20)`) |

#### 轉換公式

```
ticks → totalSeconds = ticks / 20
totalSeconds → days    = totalSeconds / 86400
             → hours   = (totalSeconds % 86400) / 3600
             → minutes = (totalSeconds % 3600) / 60
```

---

### `utils/ColorUtil.java`

> **套件**: `com.yokito.paperbridge.utils`  
> **角色**: 顏色轉換工具類 (不可實例化)，封裝 Adventure API 的 `TextColor`

#### 方法

| 方法 | 回傳 | 說明 |
|---|---|---|
| `fromHex(hex)` | `TextColor` | 從 HEX 字串 (如 `"#FF5555"`) 建立 TextColor |
| `fromRGB(r, g, b)` | `TextColor` | 從 RGB 數值 (0-255) 建立 TextColor |

---

### `utils/NicknameValidator.java`

> **套件**: `com.yokito.paperbridge.utils`  
> **角色**: Discord 暱稱驗證工具類 (不可實例化)

#### 方法

| 方法 | 回傳 | 說明 |
|---|---|---|
| `isValid(nickname)` | `boolean` | 驗證暱稱是否不為 null 且長度介於 1~10 字元之間 |

#### 驗證規則

```
nickname != null && nickname.length() >= 1 && nickname.length() <= 10
```

---

## 外部依賴

| 依賴 | 版本 | Scope | 用途 |
|---|---|---|---|
| `paper-api` | 1.21.11-R0.1-SNAPSHOT | provided | Minecraft Paper 伺服器 API |
| `placeholderapi` | 2.12.2 | provided | PlaceholderAPI 變數擴展 |
| `discordsrv` | 1.28.0 | provided | Discord ↔ Minecraft 橋接 (含 JDA) |
| `junit-jupiter` | 6.0.3 (BOM) | test | 單元測試 |
