package com.yokito.paperbridge.bootstrap;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * 外掛主入口，只負責 Bukkit 生命周期與 runtime 委派。
 */
public class PaperBridgePlugin extends JavaPlugin {

    private PaperBridgeRuntime runtime;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        runtime = new PaperBridgeBootstrap(this).build();
        runtime.start();
    }

    @Override
    public void onDisable() {
        if (runtime == null) {
            return;
        }
        runtime.stop();
    }
}
