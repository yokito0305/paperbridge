# PaperBridge

PaperBridge 是一個 Paper 插件，負責把 Minecraft 伺服器資料橋接到 Discord。  
目前主要整合 DiscordSRV 與 PlaceholderAPI，提供 Discord slash commands、玩家暱稱綁定與統計查詢功能。

## 功能

### Minecraft 指令

- `/setDiscordNick <nickname>`
  - 設定玩家的 Discord 暱稱
- `/getDiscordNick [player]`
  - 查看自己或線上玩家的 Discord 暱稱

### Discord Slash Commands

- `/stats member:<optional>`
  - 查詢自己或指定 Discord 成員綁定的 Minecraft 統計
- `/leaderboard category:<deaths|playtime|kills|mined>`
  - 查詢前五名排行榜
- `/online`
  - 查詢目前 Minecraft 在線玩家數量

### PlaceholderAPI

- `%paperbridge_discord_nick%`

## Discord 功能說明

- slash commands 採 guild command 註冊
- 啟動後會延遲註冊 guild commands，避免 DiscordSRV 啟動流程覆蓋
- 啟動時會清除舊的全域 `/stats`、`/leaderboard`、`/online`
- `/leaderboard` 目前支援：
  - `deaths`
  - `playtime`
  - `kills`
  - `mined`

### 關於 `mined`

`mined` 目前代表所有可用 `Statistic.MINE_BLOCK` 統計的方塊總和。  
這個排行榜需要遍歷較多統計資料，所以會比其他排行榜慢。

## 快速開始

### 環境需求

- Paper `1.21.11`
- Java `21`

### 可選依賴

- PlaceholderAPI

### 必要依賴

- DiscordSRV

### 建置

```bash
mvn clean package
```

產出 jar 位於：

```text
target/PaperBridge-1.1.2.jar
```

## 專案架構

目前結構採分層式設計：

- `bootstrap`
  - plugin shell、composition root、runtime、registrar
- `command`
  - Minecraft command 與 Discord command handler
- `integration`
  - DiscordSRV / PlaceholderAPI 橋接
- `service`
  - 統計、暱稱、Discord 呈現邏輯
- `model`
  - 統計與排行榜資料模型
- `util`
  - 純工具類

## 開發提示

- `NOTES.md`
  - 維護者內部筆記，記錄目前結構與啟動策略
- `DesignPattern_Diagrams.md`
  - 架構與設計圖參考

## 版本資訊

- 插件版本：`1.1.2`
- 主類別：`com.yokito.paperbridge.bootstrap.PaperBridgePlugin`
