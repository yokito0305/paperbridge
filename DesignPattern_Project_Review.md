# PaperBridge 設計模式檢視筆記

## 1) 參考 PDF 核心脈絡（Design Patterns CD）

依照講義目錄與內文摘要，核心分類是：

- Creational Patterns：Abstract Factory, Builder, Factory Method, Prototype, Singleton
- Structural Patterns：Adapter, Bridge, Composite, Decorator, Facade, Flyweight, Proxy
- Behavioral Patterns：Chain of Responsibility, Command, Interpreter, Iterator, Mediator, Memento, Observer, State, Strategy, Template Method, Visitor

講義重點是：先建立「介面導向 + 責任分配清楚」的核心，再用模式降低耦合、提升可擴充性與替換性。

## 2) 專案現況對照（目前已經有的模式）

### 2.1 Core 結構（可視為你的基底樣式）

- `bootstrap` 做組裝（Composition Root）：
  - [PaperBridgePlugin.java](/G:/我的雲端硬碟/NTUT/other/DiscordNick/paperbridge/src/main/java/com/yokito/paperbridge/bootstrap/PaperBridgePlugin.java)
- `service` 承接業務邏輯，`model` 承接資料視圖，`integration/command` 做 I/O 與框架邊界。
- 已具備基本分層，方向正確。

### 2.2 已明確落地的模式

- `Repository`
  - [NicknameRepository.java](/G:/我的雲端硬碟/NTUT/other/DiscordNick/paperbridge/src/main/java/com/yokito/paperbridge/service/nickname/NicknameRepository.java)
  - 把資料存取（config）從服務層分離。
- `Factory`（簡化版）
  - [DiscordEmbedFactory.java](/G:/我的雲端硬碟/NTUT/other/DiscordNick/paperbridge/src/main/java/com/yokito/paperbridge/service/discord/DiscordEmbedFactory.java)
  - 將 Embed 建構邏輯集中。
- `Observer`（框架事件）
  - [DiscordInteractionListener.java](/G:/我的雲端硬碟/NTUT/other/DiscordNick/paperbridge/src/main/java/com/yokito/paperbridge/integration/discordsrv/DiscordInteractionListener.java)
  - [DeathMessageProcessor.java](/G:/我的雲端硬碟/NTUT/other/DiscordNick/paperbridge/src/main/java/com/yokito/paperbridge/integration/discordsrv/DeathMessageProcessor.java)
  - 使用 DiscordSRV `@Subscribe` 事件機制。
- `Adapter`（邊界轉接）
  - [PaperBridgeExpansion.java](/G:/我的雲端硬碟/NTUT/other/DiscordNick/paperbridge/src/main/java/com/yokito/paperbridge/integration/placeholderapi/PaperBridgeExpansion.java)
  - [DiscordLinkedPlayerResolver.java](/G:/我的雲端硬碟/NTUT/other/DiscordNick/paperbridge/src/main/java/com/yokito/paperbridge/service/discord/DiscordLinkedPlayerResolver.java)
  - 把外部框架資料轉成內部可用模型。
- `Command`（部分）
  - `Discord*CommandHandler` 類別分拆了命令行為，但分派仍用 `switch`。

### 2.3 目前「像模式但還沒完全落地」的地方

- `Strategy`：`LeaderboardService#getRawValue` 用 `switch` 選算法（可再抽策略物件）
  - [LeaderboardService.java:60](/G:/我的雲端硬碟/NTUT/other/DiscordNick/paperbridge/src/main/java/com/yokito/paperbridge/service/stats/LeaderboardService.java:60)
- `Command`：Slash command 分派仍硬編碼 `switch`
  - [DiscordInteractionListener.java:57](/G:/我的雲端硬碟/NTUT/other/DiscordNick/paperbridge/src/main/java/com/yokito/paperbridge/integration/discordsrv/DiscordInteractionListener.java:57)

## 3) 從核心樣式逐步擴張（建議路徑）

### 第 0 層：維持現有分層（你已做到）

- `bootstrap -> command/integration -> service -> model/repository`
- 先保持單一責任與可測試性。

### 第 1 層：Behavioral 先補齊（最有感）

- 把 `switch` 改成 `Command Registry`（`Map<String, SlashCommandHandler>`）
  - `DiscordInteractionListener` 只做路由，不知道具體 handler。
  - 新增命令時不需改 listener（符合 OCP）。
- 把排行榜統計抽成 `LeaderboardMetricStrategy`
  - 每個 category 一個策略類別或 enum 內含函式物件。
  - 消除 `switch` 累積。

### 第 2 層：Structural 強化

- 新增 `DiscordFacade`（或 `DiscordGateway`）封裝 DiscordSRV + JDA 操作
  - 目前多處直接呼叫 `DiscordSRV.getPlugin()` / `DiscordSRV.api`，耦合偏高。
- 對訊息組裝可加 `Decorator`
  - 例如把 `DeathMessageProcessor` 的附加欄位改成可組合的 enrichers（座標、時間、世界資訊）。

### 第 3 層：Creational 收斂

- 引入 `ServiceFactory` 或 `BootstrapModule`
  - 目前 `PaperBridgePlugin` 直接 `new` 多個物件，規模再大會難維護。
- 將共用物件（如 `StatsFormatter`）統一建一次再注入，避免分散建構。

## 4) 是否需要修改？結論與優先順序

結論：目前專案**已正確使用部分模式**，但在可擴充性與解耦上，已到「建議開始重構」的門檻。

### 高優先（建議先改）

1. Command 分派改為 Registry + Handler 介面（取代 `switch`）。
2. `LeaderboardService` 統計計算改 Strategy 化，避免 category 增加時持續改核心服務。

### 中優先

1. 抽 Discord Facade/Gateway，隔離框架依賴與靜態存取。
2. Bootstrap 集中工廠化，明確生命週期與建構責任。

### 低優先

1. 訊息文案與本地化集中（目前字串分散，且有編碼顯示異常跡象）。
2. 補測試：command dispatch、leaderboard strategy、integration boundary mock 測試。

## 5) 目前可見風險（非 pattern 純度，但影響穩定性）

- `getCommand(...)` 直接 `.setExecutor(...)`，缺少 null 防護，可能 NPE。
  - [PaperBridgePlugin.java:64](/G:/我的雲端硬碟/NTUT/other/DiscordNick/paperbridge/src/main/java/com/yokito/paperbridge/bootstrap/PaperBridgePlugin.java:64)
  - [PaperBridgePlugin.java:65](/G:/我的雲端硬碟/NTUT/other/DiscordNick/paperbridge/src/main/java/com/yokito/paperbridge/bootstrap/PaperBridgePlugin.java:65)
- Discord 命令定義文字與部分訊息字串有亂碼現象，需檢查檔案編碼是否一致（建議 UTF-8）。

## 6) 官方 API 抽查（可用性驗證）

- Paper `JavaPlugin#getCommand(String)` 為 nullable，需防護 null：
  - https://jd.papermc.io/paper/1.21.11/org/bukkit/plugin/java/JavaPlugin.html
- PlaceholderExpansion 內建擴充建議與 `persist()` / `onRequest(...)` 行為：
  - https://wiki.placeholderapi.com/developers/creating-a-placeholderexpansion/
- JDA `Guild#upsertCommand(CommandData)` 可建立/更新指令（idempotent）：
  - https://docs.jda.wiki/net/dv8tion/jda/api/entities/Guild.html
- JDA `CommandData` 為介面，官方建議用 `Commands` 工廠建立：
  - https://docs.jda.wiki/net/dv8tion/jda/api/interactions/commands/build/CommandData.html

## 7) 驗證狀態

- 已執行：`mvn -q test`
- 結果：通過（現況功能未破壞）
