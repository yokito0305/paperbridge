# DiscordSRV API 使用整理 (PaperBridge)

更新日期: 2026-04-16

## 版本與範圍

- DiscordSRV 依賴版本: 1.28.0
- 依賴來源: [pom.xml](pom.xml)
- Bukkit 載入宣告: [src/main/resources/plugin.yml](src/main/resources/plugin.yml)
- 本文件涵蓋:
  - 專案在正式程式碼中直接使用的 DiscordSRV API
  - 透過 DiscordSRV 重新封裝之 JDA API 用法
  - 啟動/關閉生命週期與事件流
  - 測試中對 DiscordSRV/JDA 型別的使用概況

## 整合入口與啟用條件

- 啟動入口: [src/main/java/com/yokito/paperbridge/bootstrap/PaperBridgePlugin.java](src/main/java/com/yokito/paperbridge/bootstrap/PaperBridgePlugin.java)
- 組裝入口: [src/main/java/com/yokito/paperbridge/bootstrap/PaperBridgeBootstrap.java](src/main/java/com/yokito/paperbridge/bootstrap/PaperBridgeBootstrap.java)
- DiscordSRV 模式條件:
  - discord.yml 的 enable-custom-bot 為 false
  - 伺服器中存在且啟用 DiscordSRV 外掛
- 真正的 DiscordSRV 綁定集中在:
  - [src/main/java/com/yokito/paperbridge/integration/discordsrv/DiscordSrvGateway.java](src/main/java/com/yokito/paperbridge/integration/discordsrv/DiscordSrvGateway.java)
  - [src/main/java/com/yokito/paperbridge/integration/discordsrv/DiscordInteractionListener.java](src/main/java/com/yokito/paperbridge/integration/discordsrv/DiscordInteractionListener.java)
  - [src/main/java/com/yokito/paperbridge/integration/discordsrv/DeathMessageProcessor.java](src/main/java/com/yokito/paperbridge/integration/discordsrv/DeathMessageProcessor.java)

## DiscordSRV 直接 API 對照

| API | 專案用途 | 實作位置 |
|---|---|---|
| DiscordSRV.getPlugin().getJda() | 取得目前 JDA 實例，作為 slash 事件監聽與 guild/member 操作入口 | [DiscordSrvGateway.java](src/main/java/com/yokito/paperbridge/integration/discordsrv/DiscordSrvGateway.java) |
| DiscordSRV.api.subscribe(listener) | 訂閱 DiscordSRV 事件 listener | [DiscordSrvGateway.java](src/main/java/com/yokito/paperbridge/integration/discordsrv/DiscordSrvGateway.java) |
| DiscordSRV.api.unsubscribe(listener) | 取消訂閱 DiscordSRV 事件 listener | [DiscordSrvGateway.java](src/main/java/com/yokito/paperbridge/integration/discordsrv/DiscordSrvGateway.java) |
| DiscordSRV.getPlugin().getAccountLinkManager().getUuid(discordUserId) | 由 Discord 使用者 ID 解析綁定的 Minecraft UUID | [DiscordSrvGateway.java](src/main/java/com/yokito/paperbridge/integration/discordsrv/DiscordSrvGateway.java) |
| DiscordSRV.getPlugin().getAccountLinkManager().getLinkedAccounts().values() | 取得全部已綁定玩家 UUID，供排行榜查詢 | [DiscordSrvGateway.java](src/main/java/com/yokito/paperbridge/integration/discordsrv/DiscordSrvGateway.java) |
| DiscordSRV.getPlugin().getLogger() | 在 Discord API 非同步失敗時輸出警告 | [DiscordSrvGateway.java](src/main/java/com/yokito/paperbridge/integration/discordsrv/DiscordSrvGateway.java) |

## DiscordSRV 事件模型

事件訂閱採用 DiscordSRV 的 @Subscribe 機制。

### 1) DiscordReadyEvent

- 位置: [DiscordInteractionListener.java](src/main/java/com/yokito/paperbridge/integration/discordsrv/DiscordInteractionListener.java)
- 用途:
  - 在 DiscordSRV 宣告 Discord 就緒後，將本插件 listener 掛到 JDA
  - 呼叫 jda.addEventListener(this)
- 影響:
  - 後續才能收到 SlashCommandEvent

### 2) GuildSlashCommandUpdateEvent

- 位置: [DiscordInteractionListener.java](src/main/java/com/yokito/paperbridge/integration/discordsrv/DiscordInteractionListener.java)
- 用途:
  - 在 DiscordSRV 同步 guild commands 前，將本專案命令 definition 注入 event.getCommands()
- 影響:
  - 讓 DiscordSRV 負責最終同步，避免命令被後續覆蓋

### 3) DeathMessagePostProcessEvent

- 位置: [DeathMessageProcessor.java](src/main/java/com/yokito/paperbridge/integration/discordsrv/DeathMessageProcessor.java)
- 用途:
  - 攔截 DiscordSRV 死亡訊息
  - 在 embed 或純文字訊息補上死亡座標與時間
  - 透過 event.setDiscordMessage(...) 回寫

## 透過 DiscordSRV 重新封裝的 JDA API 用法

本專案採用 DiscordSRV 內建的 shaded JDA 套件路徑:

- github.scarsz.discordsrv.dependencies.jda.api.*

### A. Slash Command 事件與回覆

主要型別與操作:

- SlashCommandEvent
  - event.getName()
  - event.getUser()
  - event.getOption(...)
  - event.reply(...).setEphemeral(...).queue()
  - event.replyEmbeds(...).setEphemeral(...).queue()
  - event.deferReply(...).queue(...)
- OptionMapping
  - getAsString()
  - getAsUser()

使用位置:

- [DiscordInteractionListener.java](src/main/java/com/yokito/paperbridge/integration/discordsrv/DiscordInteractionListener.java)
- [DiscordSetNickCommandHandler.java](src/main/java/com/yokito/paperbridge/command/discord/DiscordSetNickCommandHandler.java)
- [DiscordStatsCommandHandler.java](src/main/java/com/yokito/paperbridge/command/discord/DiscordStatsCommandHandler.java)
- [DiscordLeaderboardCommandHandler.java](src/main/java/com/yokito/paperbridge/command/discord/DiscordLeaderboardCommandHandler.java)
- [DiscordOnlineCommandHandler.java](src/main/java/com/yokito/paperbridge/command/discord/DiscordOnlineCommandHandler.java)

### B. Slash Command 定義建構

主要型別與操作:

- CommandData
  - new CommandData(name, description)
  - addOption(...)
  - addOptions(...)
- OptionData
  - new OptionData(OptionType, name, description, required)
  - addChoice(displayName, value)
- OptionType
  - STRING
  - USER

使用位置:

- [DiscordSlashCommand.java](src/main/java/com/yokito/paperbridge/command/discord/DiscordSlashCommand.java)
- [DiscordSetNickCommandHandler.java](src/main/java/com/yokito/paperbridge/command/discord/DiscordSetNickCommandHandler.java)
- [DiscordStatsCommandHandler.java](src/main/java/com/yokito/paperbridge/command/discord/DiscordStatsCommandHandler.java)
- [DiscordLeaderboardCommandHandler.java](src/main/java/com/yokito/paperbridge/command/discord/DiscordLeaderboardCommandHandler.java)
- [DiscordOnlineCommandHandler.java](src/main/java/com/yokito/paperbridge/command/discord/DiscordOnlineCommandHandler.java)

### C. Guild/Member 與非同步 REST 動作

主要型別與操作:

- JDA
  - getGuilds()
  - addEventListener(listener)
  - removeEventListener(listener)
- Guild
  - retrieveMemberById(discordUserId).queue(success, failure)
  - modifyNickname(member, displayNickname).queue(success, failure)
- queue(success, failure) callback 模式

使用位置:

- [DiscordSrvGateway.java](src/main/java/com/yokito/paperbridge/integration/discordsrv/DiscordSrvGateway.java)
- [DiscordInteractionListener.java](src/main/java/com/yokito/paperbridge/integration/discordsrv/DiscordInteractionListener.java)

### D. 訊息與 Embed 操作

主要型別與操作:

- Message / MessageBuilder
  - 讀取原始 Discord 訊息
  - 複製並重建訊息內容
- MessageEmbed / EmbedBuilder
  - 複製原 embed
  - 修改 description
  - build 後回寫

使用位置:

- [DeathMessageProcessor.java](src/main/java/com/yokito/paperbridge/integration/discordsrv/DeathMessageProcessor.java)
- [DiscordEmbedFactory.java](src/main/java/com/yokito/paperbridge/service/discord/DiscordEmbedFactory.java)

## 指令層用法對照

| Slash Command | 參數 | 主要 API | 回覆模式 |
|---|---|---|---|
| /setnick | nickname: STRING (required) | event.getUser, event.getOption, event.reply, DiscordGateway.syncMemberNickname | 文字回覆, ephemeral=true |
| /stats | member: USER (optional) | event.getOption, OptionMapping.getAsUser, event.replyEmbeds | embed 回覆, ephemeral=false |
| /leaderboard | category: STRING (required, choices) | event.getOption, event.deferReply(false), hook.editOriginalEmbeds | 延遲回覆後編輯原訊息 |
| /online | 無 | event.replyEmbeds | embed 回覆, ephemeral=false |

命令定義集中於各 handler 的 definition()，命令路由由 [DiscordInteractionListener.java](src/main/java/com/yokito/paperbridge/integration/discordsrv/DiscordInteractionListener.java) 的 onSlashCommand 透過名稱分派。

## 帳號綁定與暱稱同步流程

1. 由 Discord user id 呼叫 DiscordGateway.getLinkedPlayerId
2. 透過 Bukkit 取得 OfflinePlayer，若未曾進服且不在線上則視為無效
3. 暱稱變更後呼叫 DiscordGateway.syncMemberNickname
4. syncMemberNickname 會遍歷所有 guild，逐一 retrieveMemberById 後 modifyNickname
5. 任一 API 失敗只記錄 warning，不中斷主流程

關鍵實作:

- [DiscordLinkedPlayerResolver.java](src/main/java/com/yokito/paperbridge/service/discord/DiscordLinkedPlayerResolver.java)
- [DiscordSetNickCommandHandler.java](src/main/java/com/yokito/paperbridge/command/discord/DiscordSetNickCommandHandler.java)
- [DiscordSrvGateway.java](src/main/java/com/yokito/paperbridge/integration/discordsrv/DiscordSrvGateway.java)

## 啟停生命週期

- 啟動註冊:
  - [DiscordIntegrationRegistrar.java](src/main/java/com/yokito/paperbridge/bootstrap/DiscordIntegrationRegistrar.java)
  - subscribe DeathMessageProcessor
  - subscribe DiscordInteractionListener
- 停用清理:
  - unsubscribe DeathMessageProcessor
  - DiscordInteractionListener.shutdown() 內 removeEventListener
  - unsubscribe DiscordInteractionListener

## 目前保留但未接線的註冊流程

[DiscordCommandRegistrar.java](src/main/java/com/yokito/paperbridge/command/discord/DiscordCommandRegistrar.java) 內含:

- jda.retrieveCommands() 刪除舊全域命令
- guild.upsertCommand(...) 註冊 guild 命令

現況:

- 這個類別目前未被組裝到 runtime 中
- 實際 DiscordSRV 模式改採 GuildSlashCommandUpdateEvent 注入策略

## 測試中對 DiscordSRV/JDA 的使用概況

主要測試檔案:

- [src/test/java/com/yokito/paperbridge/command/discord/DiscordSetNickCommandHandlerTest.java](src/test/java/com/yokito/paperbridge/command/discord/DiscordSetNickCommandHandlerTest.java)
- [src/test/java/com/yokito/paperbridge/command/discord/DiscordSlashCommandRegistryTest.java](src/test/java/com/yokito/paperbridge/command/discord/DiscordSlashCommandRegistryTest.java)
- [src/test/java/com/yokito/paperbridge/service/discord/DiscordLinkedPlayerResolverTest.java](src/test/java/com/yokito/paperbridge/service/discord/DiscordLinkedPlayerResolverTest.java)

特性:

- 以 mock 或 fake gateway 取代真實 DiscordSRV 連線
- 驗證 SlashCommandEvent 回覆流程、帳號綁定解析、暱稱同步呼叫

## 維護注意事項

- 若升級 DiscordSRV 或 JDA，優先檢查:
  - 事件型別名稱是否異動
  - SlashCommandEvent 與 CommandData API 是否有破壞性變更
  - AccountLinkManager 相關方法簽名是否改變
- syncMemberNickname 為非同步 fire-and-forget，若要提高可觀測性，建議補充:
  - 成功/失敗計數
  - guild 維度的追蹤 log
- DiscordSRV 模式與 custom bot 模式目前互斥，且 custom bot 尚未實作。
