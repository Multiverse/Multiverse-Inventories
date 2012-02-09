package com.onarandombox.multiverseinventories.listener;

import com.onarandombox.MultiverseCore.event.MVConfigReloadEvent;
import com.onarandombox.MultiverseCore.event.MVVersionEvent;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Listener for custom events from Multiverse-Core.
 */
public class MVICoreListener implements Listener {

    private MultiverseInventories plugin;

    public MVICoreListener(MultiverseInventories plugin) {
        this.plugin = plugin;
    }

    /**
     * Adds Multiverse-Inventories version info to /mv version.
     *
     * @param event The MVVersionEvent that this plugin will listen for.
     */
    @EventHandler
    public void versionRequest(MVVersionEvent event) {
        event.appendVersionInfo(this.plugin.getVersionInfo());
    }

    /**
     * Hooks Multiverse-Inventories into the Multiverse reload command.
     *
     * @param event The MVConfigReloadEvent that this plugin will listen for.
     */
    @EventHandler
    public void configReload(MVConfigReloadEvent event) {
        this.plugin.reloadConfig();
        event.addConfig("Multiverse-Inventories - config.yml");
    }
}
