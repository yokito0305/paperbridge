package com.yokito.paperbridge.bootstrap;

/**
 * 集中管理 plugin 啟動流程使用的固定文字。
 *
 * <p>目前只保留 plugin 層級的 log 訊息，避免 {@link PaperBridgePlugin} 內散落字串。</p>
 */
public final class PluginText {

    public static final String COMMAND_NOT_DECLARED_LOG = "plugin.yml 缺少命令定義: ";

    /**
     * 工具類別不允許被實例化。
     */
    private PluginText() {
    }
}
